package fi.metatavu.muisti.api.test.functional

import org.apache.commons.codec.digest.DigestUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.io.FileInputStream

/**
 * File upload functional tests
 *
 * @author Antti Leppä
 */
class FileTestsIT : AbstractFunctionalTest() {

    @Test
    @Throws(Exception::class)
    fun testUploadFile() {
        TestBuilder().use { builder ->
            val (_, uri) = builder.admin().files().upload("folder", "test-image.jpg", "image/jpeg")
            val file = getAsUploadsFile(File(uri))
            assertTrue(file.exists())
            FileInputStream(file).use { fileInputStream -> assertEquals(getResourceMd5("test-image.jpg"), DigestUtils.md5Hex(fileInputStream)) }
        }
    }

    /**
     * Returns file as mapped into test-volumes uploads folder
     *
     * @return file as mapped into test-volumes uploads folder
     */
    protected fun getAsUploadsFile(serverFile: File): File {
        val uploadsFolder = File(System.getProperty("user.dir"), "test-volumes/uploads")
        return File(uploadsFolder, serverFile.absolutePath.substring("/opt/uploads/".length))
    }

}
