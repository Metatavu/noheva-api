package fi.metatavu.muisti.api.test.functional

import org.apache.commons.codec.digest.DigestUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.util.*

/**
 * File functional tests
 *
 * @author Antti Leppä
 */
class FileTestsIT : AbstractFunctionalTest() {

    @Test
    @Throws(Exception::class)
    fun testUploadFile() {
        ApiTestBuilder().use { builder ->
            val folder = UUID.randomUUID().toString()
            val storedFile = builder.admin().files().upload(folder, "test-image.jpg", "image/jpeg", filename = null)
            assertNotNull(storedFile)

            val files = builder.admin().files().listStoredFiles(folder)
            assertEquals(1, files.size)

            download(files[0].uri).use { stream -> assertEquals(getResourceMd5("test-image.jpg"), DigestUtils.md5Hex(stream))  }
        }
    }

    @Test
    @Throws(Exception::class)
    fun testFindFile() {
        ApiTestBuilder().use { builder ->
            val folder = UUID.randomUUID().toString()
            val storedFile = builder.admin().files().upload(folder = folder, resourceName = "test-image.jpg", contentType = "image/jpeg", filename = null)
            assertNotNull(storedFile)
            assertNotNull(storedFile.id)

            val storedFileId = storedFile.id!!

            assertNotNull(builder.admin().files().findStoredFile(storedFileId = storedFileId))
            builder.admin().files().assertFindFailStatus(404, storedFileId = UUID.randomUUID().toString())
        }
    }

    @Test
    @Throws(Exception::class)
    fun testListFiles() {
        ApiTestBuilder().use { builder ->
            val folder = UUID.randomUUID().toString()
            val storedFile1 = builder.admin().files().upload(folder, "test-image.jpg", "image/jpeg", filename = null)
            assertNotNull(storedFile1)

            val storedFile2 = builder.admin().files().upload(folder, "test-image-2.jpg", "image/jpeg", filename = null)
            assertNotNull(storedFile2)

            val folderFiles = builder.admin().files().listStoredFiles(folder)
            assertEquals(2, folderFiles.size)

            assertEquals("test-image.jpg", folderFiles.firstOrNull{ it.id == storedFile1.id }?.fileName)
            assertEquals("image/jpeg", folderFiles.firstOrNull{ it.id == storedFile1.id }?.contentType)
            assertEquals("test-image-2.jpg", folderFiles.firstOrNull{ it.id == storedFile2.id }?.fileName)
            assertEquals("image/jpeg", folderFiles.firstOrNull{ it.id == storedFile2.id }?.contentType)

            val rootFiles = builder.admin().files().listStoredFiles("/")
            assertEquals(2, rootFiles.size)
            assertEquals(folder, rootFiles.first().fileName)
            assertEquals("inode/directory", rootFiles.first().contentType)
        }
    }

    @Test
    @Throws(Exception::class)
    fun testUpdateFile() {
        ApiTestBuilder().use { builder ->
            val folder = UUID.randomUUID().toString()
            val createdFile= builder.admin().files().upload(folder, "test-image.jpg", "image/jpeg", filename = null)
            assertEquals("test-image.jpg", createdFile.fileName)

            val updatedFile = builder.admin().files().updateStoredFile(createdFile.copy(fileName = "changedfile.jpg"))
            assertEquals("changedfile.jpg", updatedFile.fileName)

            val foundUpdatedFile = builder.admin().files().findStoredFile(createdFile.id!!)
            assertEquals("changedfile.jpg", foundUpdatedFile.fileName)
        }
    }

}
