package fi.metatavu.noheva.files.storage

import fi.metatavu.noheva.api.spec.model.StoredFile
import fi.metatavu.noheva.files.FileMeta
import fi.metatavu.noheva.files.InputFile
import fi.metatavu.noheva.media.ImageReader
import fi.metatavu.noheva.media.ImageScaler
import fi.metatavu.noheva.media.ImageWriter
import io.quarkus.runtime.configuration.ConfigUtils
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils
import org.eclipse.microprofile.config.inject.ConfigProperty
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.exception.SdkClientException
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.http.apache.ApacheHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.endpoints.S3EndpointProvider
import software.amazon.awssdk.services.s3.model.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject


/**
 * File storage provider for storing files in S3.
 *
 * @author Antti Leppä
 */
@ApplicationScoped
@Suppress("unused")
class S3FileStorageProvider : FileStorageProvider {

    @Inject
    lateinit var imageReader: ImageReader

    @Inject
    lateinit var imageWriter: ImageWriter

    @Inject
    lateinit var imageScaler: ImageScaler

    @ConfigProperty(name = "s3.file.storage.region")
    lateinit var region: String

    @ConfigProperty(name = "s3.file.storage.endpoint")
    lateinit var endpoint: Optional<String>

    @ConfigProperty(name = "s3.file.storage.bucket")
    lateinit var bucket: String

    @ConfigProperty(name = "s3.file.storage.prefix")
    lateinit var prefix: String

    @ConfigProperty(name = "s3.file.storage.keyid")
    lateinit var keyId: String

    @ConfigProperty(name = "s3.file.storage.secret")
    lateinit var secret: String

    /**
     * Returns whether system is running in test mode
     *
     * @return whether system is running in test mode
     */
    fun isInTestMode(): Boolean {
        return ConfigUtils.getProfiles().contains("test")
    }

    @Throws(FileStorageException::class)
    override fun init() {
        try {
            client.headBucket(HeadBucketRequest.builder().bucket(bucket).build())
        } catch (ex: S3Exception) {
            throw FileStorageException(String.format("bucket '%s' does not exist", bucket))
        }
    }

    @Throws(FileStorageException::class)
    override fun store(inputFile: InputFile): StoredFile {
        val meta: FileMeta = inputFile.meta
        val folder: String = inputFile.folder
        val fileKey = "$folder/${inputFile.meta.fileName}".trimStart('/')
        val data = inputFile.data
        data ?: throw FileStorageException("Input file does not contain data")

        val tempFilePath = Files.createTempFile("upload", "s3")
        val tempFile = tempFilePath.toFile()
        FileOutputStream(tempFile).use { fileOutputStream -> IOUtils.copy(data, fileOutputStream) }
        try {
            val thumbnailKey = uploadThumbnail(fileKey = fileKey, contentType = meta.contentType, tempFile = tempFile)
            val objectMeta = uploadObject(
                key = fileKey,
                thumbnailKey = thumbnailKey,
                contentType = meta.contentType,
                filename = meta.fileName,
                tempFile = tempFile
            )

            return translateObject(fileKey = fileKey, objectMeta = objectMeta)
        } catch (e: SdkClientException) {
            throw FileStorageException(e)
        }
    }

    override fun find(storedFileId: String): StoredFile? {
        try {
            val key = getKey(storedFileId)
            val s3Object = client.getObject(
                GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(getKey(storedFileId))
                    .build()
            ).response()
            return translateObject(key, s3Object.metadata())
        } catch (e: S3Exception) {
            if (e.statusCode() == 404) {
                return null
            }

            throw FileStorageException(e)
        } catch (e: Exception) {
            throw FileStorageException(e)
        }
    }

    override fun list(folder: String): List<StoredFile> {
        try {
            val prefix = StringUtils.stripEnd(StringUtils.stripStart(folder, "/"), "/") + "/"

            val request = ListObjectsRequest.builder()
                .bucket(bucket)
                .delimiter("/")

            if (prefix.isNotEmpty() && prefix != "/") {
                request.prefix(prefix)
            }

            val awsResult = client.listObjects(request.build())

            val result = awsResult.commonPrefixes().map { it.prefix() }
                .filter { !it.startsWith("__") }
                .map(this::translateFolder)

            return result.plus(awsResult.contents()
                .filter { !it.key().startsWith("__") }
                .map {
                    val key = it.key()
                    val metadata = client.getObject(
                        GetObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .build()
                    ).response().metadata()
                    translateObject(key, metadata)
                }
            )
        } catch (e: Exception) {
            throw FileStorageException(e)
        }
    }

    override fun update(storedFile: StoredFile): StoredFile {
        try {
            val key = getKey(storedFile.id!!)
            val s3Object = client.getObject(
                GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build()
            )
            val objectMeta = s3Object.response().metadata().toMutableMap()
            objectMeta[X_FILE_NAME] = storedFile.fileName

            val request = CopyObjectRequest.builder()
                .sourceBucket(this.bucket)
                .sourceKey(key)
                .destinationBucket(this.bucket)
                .destinationKey(key)
                .metadata(objectMeta)
                .metadataDirective(MetadataDirective.REPLACE)
                .build()

            client.copyObject(request)

            return translateObject(key, objectMeta)
        } catch (e: Exception) {
            throw FileStorageException(e)
        }
    }

    override fun delete(storedFileId: String) {
        try {
            val fileKey = this.getKey(storedFileId)
            val s3Object = client.getObject(
                GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileKey)
                    .build()
            )

            if (s3Object != null) {
                val objectMeta = s3Object.response().metadata()
                val thumbnailKey = objectMeta[X_THUMBNAIL_KEY]
                if (thumbnailKey != null) {
                    client.deleteObject(
                        DeleteObjectRequest.builder()
                            .bucket(bucket)
                            .key(thumbnailKey)
                            .build()
                        )
                    }
                }
                client.deleteObject(
                    DeleteObjectRequest.builder()
                        .bucket(bucket)
                        .key(fileKey)
                        .build()
                )
        } catch (e: Exception) {
            throw FileStorageException(e)
        }
    }

    override val id: String
        get() = "S3"

    /**
     * Returns initialized S3 client based on the profile (localstack test container requires additional
     * endpoint configuration)
     *
     * @return initialized S3 client
     */
    private val client
        get() = if (isInTestMode())
            S3Client.builder()
                .endpointOverride(
                    URI.create(endpoint.get())
                )
                .region(Region.of(region))
                .httpClient(ApacheHttpClient.create())
                .credentialsProvider(
                    StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(
                            keyId,
                            secret
                        )
                    )
                ).endpointProvider(
                    S3EndpointProvider.defaultProvider()
                ).build()
        else
            S3Client.builder()
                .region(Region.of(region))
                .httpClient(ApacheHttpClient.create())
                .credentialsProvider(
                    StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(
                            keyId,
                            secret
                        )
                    )
                ).endpointProvider(
                    S3EndpointProvider.defaultProvider()
                ).build()

    /**
     * Uploads a thumbnail into bucket
     *
     * @param fileKey file key
     * @param contentType content type of original file
     * @param tempFile file data in temp file
     * @return uploaded thumbnail key
     */
    private fun uploadThumbnail(fileKey: String, contentType: String, tempFile: File): String? {
        val thumbnailData = createThumbnail(contentType = contentType, tempFile = tempFile)
        thumbnailData ?: return null

        val key = "__thumbnails/$fileKey-512x512.jpg"

        val objectMeta = mutableMapOf<String, String>()
        objectMeta[CONTENT_TYPE] = "image/jpeg"
        try {
            try {
                objectMeta[CONTENT_LENGTH] = thumbnailData.size.toLong().toString()

                client.putObject(
                    PutObjectRequest.builder()
                        .bucket(bucket)
                        .metadata(objectMeta)
                        .key(key)
                        .acl(ObjectCannedACL.PUBLIC_READ)
                        .build(),
                    RequestBody.fromBytes(thumbnailData)
                )

                return key
            } catch (e: SdkClientException) {
                throw FileStorageException(e)
            }
        } catch (e: IOException) {
            throw FileStorageException(e)
        }
    }

    /**
     * Uploads object into the storage
     *
     * @param key object key
     * @param thumbnailKey key of thumbnail object
     * @param contentType content type of object to be uploaded
     * @param filename object filename
     * @param tempFile object data in temp file
     * @return uploaded object
     */
    private fun uploadObject(
        key: String,
        thumbnailKey: String?,
        contentType: String,
        filename: String,
        tempFile: File
    ): MutableMap<String, String> {
        val objectMeta = mutableMapOf<String, String>()
        objectMeta[CONTENT_TYPE] = contentType
        objectMeta[X_FILE_NAME] = filename

        if (thumbnailKey != null) {
            objectMeta[X_THUMBNAIL_KEY] = thumbnailKey
        }

        try {
            try {
                objectMeta[CONTENT_LENGTH] = tempFile.length().toString()

                client.putObject(
                    PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .metadata(objectMeta)
                        .acl(ObjectCannedACL.PUBLIC_READ)
                        .build(),
                    RequestBody.fromFile(tempFile)
                )

                return objectMeta
            } catch (e: SdkClientException) {
                throw FileStorageException(e)
            }
        } catch (e: IOException) {
            throw FileStorageException(e)
        }
    }

    /**
     * Creates thumbnail from given uploaded file
     *
     * @param contentType content type of uploaded image
     * @param tempFile temporary file containing uploaded file data*
     * @return created thumbnail image or null if thumbnail could not be created
     */
    private fun createThumbnail(contentType: String, tempFile: File): ByteArray? {
        if (contentType.startsWith("image/")) {
            return createImageThumbnail(tempFile)
        }

        return null
    }

    /**
     * Creates thumbnail from given image file
     *
     * @param tempFile temporary file containing the image
     * @return created thumbnail image or null if image could not be created
     */
    private fun createImageThumbnail(tempFile: File): ByteArray? {
        val image = FileInputStream(tempFile).use(imageReader::readBufferedImage)
        image ?: return null

        val scaledImage = imageScaler.scaleToCover(originalImage = image, size = THUMBNAIL_SIZE, downScaleOnly = false)
        val croppedImage = scaledImage?.getSubimage(0, 0, THUMBNAIL_SIZE, THUMBNAIL_SIZE)
        croppedImage ?: return null

        return imageWriter.writeBufferedImage(croppedImage, "jpg")
    }

    /**
     * Converts stored file id into S3 key
     *
     * @param storedFileId stored file id
     * @return S3 key
     */
    private fun getKey(storedFileId: String): String {
        return URLDecoder.decode(storedFileId, StandardCharsets.UTF_8)
    }

    /**
     * Converts S3 key into stored file id
     *
     * @param key S3 key
     * @return stored file id
     */
    private fun getStoredFileId(key: String): String {
        return URLEncoder.encode(key, StandardCharsets.UTF_8)
    }

    /**
     * Translates object details into stored file
     *
     * @param fileKey S3 key
     * @param objectMeta object metadata
     * @return stored file
     */
    private fun translateObject(fileKey: String, objectMeta: MutableMap<String, String>): StoredFile {
        val fileName = objectMeta[X_FILE_NAME] ?: fileKey
        val thumbnailKey = objectMeta[X_THUMBNAIL_KEY]

        val thumbnailUri = if (thumbnailKey != null) "$prefix/$thumbnailKey" else null

        return StoredFile(
            id = getStoredFileId(fileKey),
            contentType = objectMeta[CONTENT_TYPE] ?: "",
            fileName = fileName,
            uri = "$prefix/$fileKey",
            thumbnailUri = thumbnailUri
        )
    }

    /**
     * Translates folder into stored file
     *
     * @param key S3 key
     * @return stored file
     */
    private fun translateFolder(key: String): StoredFile {
        return StoredFile(
            id = getStoredFileId(key),
            contentType = "inode/directory",
            fileName = StringUtils.stripEnd(StringUtils.stripStart(key, "/"), "/"),
            uri = "$prefix/$key"
        )
    }

    companion object {
        const val X_FILE_NAME = "x-file-name"
        const val X_THUMBNAIL_KEY = "x-thumbnail-key"
        const val CONTENT_LENGTH = "content-length"
        const val CONTENT_TYPE = "content-type"

        const val THUMBNAIL_SIZE = 512
    }


}