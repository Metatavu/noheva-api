package fi.metatavu.muisti.api.test.functional

import fi.metatavu.muisti.api.client.infrastructure.ApiClient
import org.apache.commons.lang3.math.NumberUtils

import org.junit.Test
import org.junit.Assert.assertNotNull

import fi.metatavu.muisti.api.test.functional.TestBuilder
import org.slf4j.LoggerFactory

/**
 * Test class for testing exhibitions API
 *
 * @author Antti Leppä
 */
class ExhibitionTestsIT {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Test
    fun testCreateExhibition() {

        TestBuilder().use {
            val admin = it.admin()!!
            assertNotNull(admin)
            val exhibitions = admin.exhibitions()!!
            val result = exhibitions.create()

            assertNotNull(result)
        }
   }

}