package fi.metatavu.muisti.api.test.functional

import org.junit.Test
import org.junit.Assert.assertNotNull

/**
 * Test class for testing exhibitions API
 *
 * @author Antti Leppä
 */
class ExhibitionTestsIT: AbstractFunctionalTest() {

    @Test
    fun testCreateExhibition() {
        TestBuilder().use {
            assertNotNull(it.admin().exhibitions().create())
        }
   }

}