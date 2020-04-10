package fi.metatavu.muisti.api.test.functional

import fi.metatavu.muisti.api.client.models.ExhibitionFloor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.util.*

/**
 * Test class for testing exhibition floors API
 *
 * @author Antti Leppä
 */
class ExhibitionFloorTestsIT: AbstractFunctionalTest() {

    @Test
    fun testCreateExhibitionFloor() {
        TestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val createdExhibitionFloor = it.admin().exhibitionFloors().create(exhibition.id!!, ExhibitionFloor(name = "name"))
            assertNotNull(createdExhibitionFloor)
            it.admin().exhibitions().assertCreateFail(400, "")
        }
   }

    @Test
    fun testFindExhibitionFloor() {
        TestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()
            val nonExistingExhibitionFloorId = UUID.randomUUID()
            val createdExhibitionFloor = it.admin().exhibitionFloors().create(exhibitionId)
            val createdExhibitionFloorId = createdExhibitionFloor.id!!

            it.admin().exhibitionFloors().assertFindFail(404, exhibitionId, nonExistingExhibitionFloorId)
            it.admin().exhibitionFloors().assertFindFail(404, nonExistingExhibitionId, nonExistingExhibitionFloorId)
            it.admin().exhibitionFloors().assertFindFail(404, nonExistingExhibitionId, createdExhibitionFloorId)
            assertNotNull(it.admin().exhibitionFloors().findExhibitionFloor(exhibitionId, createdExhibitionFloorId))
        }
    }

    @Test
    fun testListExhibitionFloors() {
        TestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()

            it.admin().exhibitionFloors().assertListFail(404, nonExistingExhibitionId)
            assertEquals(0, it.admin().exhibitionFloors().listExhibitionFloors(exhibitionId).size)

            val createdExhibitionFloor = it.admin().exhibitionFloors().create(exhibitionId)
            val createdExhibitionFloorId = createdExhibitionFloor.id!!
            val exhibitionFloors = it.admin().exhibitionFloors().listExhibitionFloors(exhibitionId)
            assertEquals(1, exhibitionFloors.size)
            assertEquals(createdExhibitionFloorId, exhibitionFloors[0].id)
            it.admin().exhibitionFloors().delete(exhibitionId, createdExhibitionFloorId)
            assertEquals(0, it.admin().exhibitionFloors().listExhibitionFloors(exhibitionId).size)
        }
    }

    @Test
    fun testUpdateExhibition() {
        TestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()

            val createdExhibitionFloor = it.admin().exhibitionFloors().create(exhibitionId, ExhibitionFloor(name = "created name"))
            val createdExhibitionFloorId = createdExhibitionFloor.id!!

            val foundCreatedExhibitionFloor = it.admin().exhibitionFloors().findExhibitionFloor(exhibitionId, createdExhibitionFloorId)
            assertEquals(createdExhibitionFloor.id, foundCreatedExhibitionFloor?.id)
            assertEquals("created name", createdExhibitionFloor.name)

            val updatedExhibitionFloor = it.admin().exhibitionFloors().updateExhibitionFloor(exhibitionId, ExhibitionFloor("updated name", createdExhibitionFloorId))
            val foundUpdatedExhibitionFloor = it.admin().exhibitionFloors().findExhibitionFloor(exhibitionId, createdExhibitionFloorId)

            assertEquals(updatedExhibitionFloor!!.id, foundUpdatedExhibitionFloor?.id)
            assertEquals("updated name", updatedExhibitionFloor.name)

            it.admin().exhibitionFloors().assertUpdateFail(404, nonExistingExhibitionId, ExhibitionFloor("name", createdExhibitionFloorId))
        }
    }

    @Test
    fun testDeleteExhibition() {
        TestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()
            val nonExistingSessionVariableId = UUID.randomUUID()
            val createdExhibitionFloor = it.admin().exhibitionFloors().create(exhibitionId)
            val createdExhibitionFloorId = createdExhibitionFloor.id!!

            assertNotNull(it.admin().exhibitionFloors().findExhibitionFloor(exhibitionId, createdExhibitionFloorId))
            it.admin().exhibitionFloors().assertDeleteFail(404, exhibitionId, nonExistingSessionVariableId)
            it.admin().exhibitionFloors().assertDeleteFail(404, nonExistingExhibitionId, createdExhibitionFloorId)
            it.admin().exhibitionFloors().assertDeleteFail(404, nonExistingExhibitionId, nonExistingSessionVariableId)

            it.admin().exhibitionFloors().delete(exhibitionId, createdExhibitionFloor)

            it.admin().exhibitionFloors().assertDeleteFail(404, exhibitionId, createdExhibitionFloorId)
        }
    }

}