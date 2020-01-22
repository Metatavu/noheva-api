package fi.metatavu.muisti.api.test.functional

import org.apache.commons.lang3.math.NumberUtils

import org.junit.Test
import org.junit.Assert.assertNotNull

import fi.metatavu.muisti.api.test.functional.TestBuilder

/**
 * Test class for testing exhibitions API
 *
 * @author Antti Leppä
 */
class ExhibitionTestsIT {

    @Test
    fun testCreateExhibition() {
        Thread.sleep(60 * 1000 * 10)

        TestBuilder().use {
            val admin = it.admin()!!
            assertNotNull(admin)
            val exhibitions = admin.exhibitions()!!
            val result = exhibitions.create()

            assertNotNull(result)
        }
   }

}