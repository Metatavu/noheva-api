package fi.metatavu.muisti.api.test.functional

import org.apache.commons.codec.digest.DigestUtils

import java.io.IOException

/**
 * Abstract base class for functional tests
 *
 * @author Antti Leppä
 */
abstract class AbstractFunctionalTest {

    /**
     * Calculates contents md5 from a resource
     *
     * @param resourceName resource name
     * @return resource contents md5
     * @throws IOException thrown when file reading fails
     */
    @Throws(IOException::class)
    protected open fun getResourceMd5(resourceName: String?): String? {
        val classLoader = javaClass.classLoader
        classLoader.getResourceAsStream(resourceName).use { fileStream -> return DigestUtils.md5Hex(fileStream) }
    }

}