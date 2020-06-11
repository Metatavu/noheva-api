package fi.metatavu.muisti.files.storage

import com.amazonaws.SdkClientException
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.*
import fi.metatavu.muisti.api.spec.model.StoredFile
import fi.metatavu.muisti.files.FileMeta
import fi.metatavu.muisti.files.InputFile
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.*
import javax.enterprise.context.ApplicationScoped


/**
 * File storage provider for storing files in S3.
 *
 * @author Antti Leppä
 */
@ApplicationScoped
class S3FileStorageProvider : FileStorageProvider {

    private var region: String? = null
    private var bucket: String? = null
    private var prefix: String? = null

    @Throws(FileStorageException::class)
    override fun init() {
        region = System.getenv("S3_FILE_STORAGE_REGION")
        bucket = System.getenv("S3_FILE_STORAGE_BUCKET")
        prefix = System.getenv("S3_FILE_STORAGE_PREFIX")
        if (StringUtils.isBlank(region)) {
            throw FileStorageException("S3_FILE_STORAGE_REGION is not set")
        }
        if (StringUtils.isBlank(bucket)) {
            throw FileStorageException("S3_FILE_STORAGE_BUCKET is not set")
        }
        if (StringUtils.isBlank(prefix)) {
            throw FileStorageException("S3_FILE_STORAGE_PREFIX is not set")
        }
        val client: AmazonS3 = client
        if (!client.doesBucketExistV2(bucket)) {
            throw FileStorageException(String.format("bucket '%s' does not exist", bucket))
        }
    }

    @Throws(FileStorageException::class)
    override fun store(inputFile: InputFile): StoredFile {
        val fileKey = UUID.randomUUID().toString()
        val meta: FileMeta = inputFile.meta
        val folder: String = inputFile.folder
        val objectMeta = ObjectMetadata()
        objectMeta.contentType = meta.contentType
        objectMeta.addUserMetadata("x-file-name", meta.fileName)
        try {
            val tempFilePath = Files.createTempFile("upload", "s3")
            val tempFile = tempFilePath.toFile()
            FileOutputStream(tempFile).use { fileOutputStream -> IOUtils.copy(inputFile.data, fileOutputStream) }
            try {
                objectMeta.contentLength = tempFile.length()

                FileInputStream(tempFile).use { fileInputStream ->
                    val key = "$folder/$fileKey"
                    client.putObject(PutObjectRequest(bucket, key, fileInputStream, objectMeta).withCannedAcl(CannedAccessControlList.PublicRead))
                    return translateObject(key, objectMeta)
                }
            } catch (e: SdkClientException) {
                throw FileStorageException(e)
            }
        } catch (e: IOException) {
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
            val request = ListObjectsV2Request()
                .withBucketName(bucket)
                .withPrefix(folder)

            val response = client.listObjectsV2(request)

            return response.objectSummaries.map {
                val key = it.key
                val metadata: ObjectMetadata = client.getObjectMetadata(bucket, key)
                translateObject(key, metadata)
            }
        } catch (e: Exception) {
            throw FileStorageException(e)
        }
    }

    override fun update(storedFile: StoredFile): StoredFile {
        try {
            val key = getKey(storedFile.id)
            val s3Object = client.getObject(bucket, key)

            val objectMeta = s3Object.objectMetadata.clone()
            objectMeta.addUserMetadata("x-file-name", storedFile.fileName)

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
            client.deleteObject(this.bucket, this.getKey(storedFileId))
        } catch (e: Exception) {
            throw FileStorageException(e)
        }
    }

    override val id: String
        get() = "S3"

    /**
     * Returns initialized S3 client
     *
     * @return initialized S3 client
     */
    private val client: AmazonS3 get() = AmazonS3ClientBuilder.standard().withRegion(region).build()

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
     * @param key S3 key
     * @param metadata object metadata
     * @return stored file
     */
    private fun translateObject(key: String, metadata: ObjectMetadata): StoredFile {
        val result = StoredFile()
        val fileName = metadata.userMetadata["x-file-name"] ?: key
        result.id = getStoredFileId(key)
        result.contentType = metadata.contentType
        result.fileName = fileName
        result.uri = "$prefix/$key"

        return result
    }
}