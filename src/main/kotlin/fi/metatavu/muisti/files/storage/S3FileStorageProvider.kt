package fi.metatavu.muisti.files.storage

import com.amazonaws.SdkClientException
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.*
import fi.metatavu.muisti.api.spec.model.StoredFile
import fi.metatavu.muisti.files.FileMeta
import fi.metatavu.muisti.files.InputFile
import fi.metatavu.muisti.media.ImageReader
import fi.metatavu.muisti.media.ImageScaler
import fi.metatavu.muisti.media.ImageWriter
import io.quarkus.runtime.configuration.ProfileManager
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
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
class S3FileStorageProvider : FileStorageProvider {

    @Inject
    lateinit var imageReader: ImageReader

    @Inject
    lateinit var imageWriter: ImageWriter

    @Inject
    lateinit var imageScaler: ImageScaler

    @ConfigProperty(name = "s3.file.storage.region")
    var region: String? = null

    @ConfigProperty(name = "s3.file.storage.endpoint")
    var endpoint: String? = null

    @ConfigProperty(name = "s3.file.storage.bucket")
    var bucket: String? = null

    @ConfigProperty(name = "s3.file.storage.prefix")
    var prefix: String? = null

    @ConfigProperty(name = "s3.file.storage.keyid")
    var keyId: String? = null

    @ConfigProperty(name = "s3.file.storage.secret")
    var secret: String? = null

    /**
     * Returns whether system is running in test mode
     *
     * @return whether system is running in test mode
     */
    fun isInTestMode(): Boolean {
        return StringUtils.equals("test", ProfileManager.getActiveProfile())
    }

    @Throws(FileStorageException::class)
    override fun init() {
        val client: AmazonS3 = client
        if (!client.doesBucketExistV2(bucket)) {
            throw FileStorageException(String.format("bucket '%s' does not exist", bucket))
        }
    }

    @Throws(FileStorageException::class)
    override fun store(inputFile: InputFile): StoredFile {
        val meta: FileMeta = inputFile.meta
        val folder: String = inputFile.folder
        val fileKey = "$folder/${UUID.randomUUID()}"
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
            val s3Object = client.getObject(bucket, getKey(storedFileId))
            return translateObject(s3Object.key, s3Object.objectMetadata)
        } catch (e: AmazonS3Exception) {
            if (e.statusCode == 404) {
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

            val request = ListObjectsV2Request()
                .withBucketName(bucket)
                .withDelimiter("/")

            if (prefix.isNotEmpty() && prefix != "/") {
                request.prefix = prefix
            }

            val awsResult = client.listObjectsV2(request)

            val result = awsResult.commonPrefixes
                .filter { !it.startsWith("__") }
                .map(this::translateFolder)

            return result.plus(awsResult.objectSummaries
                .filter { !it.key.startsWith("__") }
                .map {
                    val key = it.key
                    val metadata: ObjectMetadata = client.getObjectMetadata(bucket, key)
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
            val s3Object = client.getObject(bucket, key)

            /*
            todo: localstack does not support old request with cloned metadata
            val objectMeta = s3Object.objectMetadata.clone()
            objectMeta.addUserMetadata(X_FILE_NAME, storedFile.fileName)
             */
            val objectMeta = ObjectMetadata()
            objectMeta.userMetadata = s3Object.objectMetadata.userMetadata
            objectMeta.addUserMetadata(X_FILE_NAME, storedFile.fileName)
            objectMeta.contentType = s3Object.objectMetadata.contentType
            val request = CopyObjectRequest(this.bucket, key, this.bucket, key)
                .withNewObjectMetadata(objectMeta)

            client.copyObject(request)

            return translateObject(key, objectMeta)
        } catch (e: Exception) {
            throw FileStorageException(e)
        }
    }

    override fun delete(storedFileId: String) {
        try {
            val fileKey = this.getKey(storedFileId)
            val s3Object = client.getObject(bucket, fileKey)
            if (s3Object != null) {
                val objectMeta = s3Object.objectMetadata
                val thumbnailKey = objectMeta.userMetadata[X_THUMBNAIL_KEY]
                thumbnailKey ?: client.deleteObject(this.bucket, thumbnailKey)
                client.deleteObject(this.bucket, fileKey)
            }
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
    private val client: AmazonS3
        get() = if (isInTestMode())
            AmazonS3ClientBuilder
                .standard()
                .withEndpointConfiguration(
                    AwsClientBuilder.EndpointConfiguration(
                        endpoint,
                        region
                    )
                )
                .withCredentials(
                    AWSStaticCredentialsProvider(
                        BasicAWSCredentials(keyId, secret)
                    )
                )
                .build()
        else
            AmazonS3ClientBuilder
                .standard()
                .withRegion(region)
                .withCredentials(
                    AWSStaticCredentialsProvider(
                        BasicAWSCredentials(keyId, secret)
                    )
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

        val objectMeta = ObjectMetadata()
        objectMeta.contentType = "image/jpeg"
        try {
            try {
                objectMeta.contentLength = thumbnailData.size.toLong()
                thumbnailData.inputStream().use { thumbnailInputStream ->
                    client.putObject(
                        PutObjectRequest(bucket, key, thumbnailInputStream, objectMeta).withCannedAcl(
                            CannedAccessControlList.PublicRead
                        )
                    )
                }

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
    ): ObjectMetadata {
        val objectMeta = ObjectMetadata()
        objectMeta.contentType = contentType
        objectMeta.addUserMetadata(X_FILE_NAME, filename)

        if (thumbnailKey != null) {
            objectMeta.addUserMetadata(X_THUMBNAIL_KEY, thumbnailKey)
        }

        try {
            try {
                objectMeta.contentLength = tempFile.length()

                FileInputStream(tempFile).use { fileInputStream ->
                    client.putObject(
                        PutObjectRequest(bucket, key, fileInputStream, objectMeta).withCannedAcl(
                            CannedAccessControlList.PublicRead
                        )
                    )
                }

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
    private fun translateObject(fileKey: String, objectMeta: ObjectMetadata): StoredFile {

        val fileName = objectMeta.userMetadata[X_FILE_NAME] ?: fileKey
        val thumbnailKey = objectMeta.userMetadata[X_THUMBNAIL_KEY]

        val thumbnailUri = if (thumbnailKey != null) "$prefix/$thumbnailKey" else null

        return StoredFile(
            id = getStoredFileId(fileKey),
            contentType = objectMeta.contentType,
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
        const val THUMBNAIL_SIZE = 512
    }


}