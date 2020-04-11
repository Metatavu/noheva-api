package fi.metatavu.muisti.files.storage

import com.amazonaws.SdkClientException
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import fi.metatavu.muisti.files.FileMeta
import fi.metatavu.muisti.files.InputFile
import fi.metatavu.muisti.files.OutputFile
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.URI
import java.nio.file.Files
import java.util.*


/**
 * File storage provider for storing files in S3.
 *
 * @author Antti Leppä
 */
open class S3FileStorageProvider : FileStorageProvider {
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
    override fun store(inputFile: InputFile): OutputFile {
        val client: AmazonS3 = client
        val key = UUID.randomUUID().toString()
        val meta: FileMeta = inputFile.meta
        val folder: String = inputFile.folder
        val objectMeta = ObjectMetadata()
        objectMeta.contentType = meta.contentType
        objectMeta.addUserMetadata("x-file-name", meta.fileName)
        try {
            val tempFile = Files.createTempFile("upload", "s3")
            FileOutputStream(tempFile.toFile()).use { fileOutputStream -> IOUtils.copy(inputFile.data, fileOutputStream) }
            try {
                FileInputStream(tempFile.toFile()).use { fileInputStream ->
                    client.putObject(PutObjectRequest(bucket, String.format("%s/%s", folder, key), fileInputStream, objectMeta).withCannedAcl(CannedAccessControlList.PublicRead))
                    return OutputFile(meta, URI.create(String.format("%s/%s/%s", prefix, folder, key)))
                }
            } catch (e: SdkClientException) {
                throw FileStorageException(e)
            }
        } catch (e: IOException) {
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
}