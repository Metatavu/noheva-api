package fi.metatavu.muisti.api.test.functional

import fi.metatavu.muisti.api.test.functional.resources.AwsResource
import fi.metatavu.muisti.api.test.functional.resources.KeycloakResource
import fi.metatavu.muisti.api.test.functional.resources.MqttResource
import fi.metatavu.muisti.api.test.functional.resources.MysqlResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import org.apache.commons.codec.digest.DigestUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.util.*

/**
 * File functional tests
 *
 * @author Antti Leppä
 */
@QuarkusTest
@QuarkusTestResource.List(
    QuarkusTestResource(MysqlResource::class),
    QuarkusTestResource(KeycloakResource::class),
    QuarkusTestResource(MqttResource::class),
    QuarkusTestResource(AwsResource::class)
)
class FileTestsIT : AbstractFunctionalTest() {

    @Test
    @Throws(Exception::class)
    fun testUploadFile() {
        createTestBuilder().use { builder ->
            val folder = UUID.randomUUID().toString()
            val storedFile = builder.admin().storedFiles.upload(folder, "test-image.jpg", "image/jpeg", filename = null)
            assertNotNull(storedFile)

            val files = builder.admin().storedFiles.listStoredFiles(folder)
            assertEquals(1, files.size)

            download(files[0].uri).use { stream -> assertEquals(getResourceMd5("test-image.jpg"), DigestUtils.md5Hex(stream))  }
        }
    }

    @Test
    @Throws(Exception::class)
    fun testFindFile() {
        createTestBuilder().use { builder ->
            val folder = UUID.randomUUID().toString()
            val storedFile = builder.admin().storedFiles.upload(folder = folder, resourceName = "test-image.jpg", contentType = "image/jpeg", filename = null)
            assertNotNull(storedFile)
            assertNotNull(storedFile.id)

            val storedFileId = storedFile.id!!

            assertNotNull(builder.admin().storedFiles.findStoredFile(storedFileId = storedFileId))
            builder.admin().storedFiles.assertFindFailStatus(404, storedFileId = UUID.randomUUID().toString())
        }
    }

    @Test
    @Throws(Exception::class)
    fun testListFiles() {
        createTestBuilder().use { builder ->
            val folder = UUID.randomUUID().toString()
            val storedFile1 = builder.admin().storedFiles.upload(folder, "test-image.jpg", "image/jpeg", filename = null)
            assertNotNull(storedFile1)

            val storedFile2 = builder.admin().storedFiles.upload(folder, "test-image-2.jpg", "image/jpeg", filename = null)
            assertNotNull(storedFile2)

            val folderFiles = builder.admin().storedFiles.listStoredFiles(folder)
            assertEquals(2, folderFiles.size)

            assertEquals("test-image.jpg", folderFiles.firstOrNull{ it.id == storedFile1.id }?.fileName)
            assertEquals("image/jpeg", folderFiles.firstOrNull{ it.id == storedFile1.id }?.contentType)
            assertEquals("test-image-2.jpg", folderFiles.firstOrNull{ it.id == storedFile2.id }?.fileName)
            assertEquals("image/jpeg", folderFiles.firstOrNull{ it.id == storedFile2.id }?.contentType)

            val rootFiles = builder.admin().storedFiles.listStoredFiles("/")

            val folderFile = rootFiles.firstOrNull { rootFile -> rootFile.fileName == folder }
            assertNotNull(folderFile)
            assertEquals("inode/directory", folderFile?.contentType)
        }
    }

    @Test
    @Throws(Exception::class)
    fun testUpdateFile() {
        createTestBuilder().use { builder ->
            val folder = UUID.randomUUID().toString()
            val createdFile= builder.admin().storedFiles.upload(folder, "test-image.jpg", "image/jpeg", filename = null)
            assertEquals("test-image.jpg", createdFile.fileName)

            val updatedFile = builder.admin().storedFiles.updateStoredFile(createdFile.copy(fileName = "changedfile.jpg"))
            assertEquals("changedfile.jpg", updatedFile.fileName)

            val foundUpdatedFile = builder.admin().storedFiles.findStoredFile(createdFile.id!!)
            assertEquals("changedfile.jpg", foundUpdatedFile.fileName)
        }
    }

}
