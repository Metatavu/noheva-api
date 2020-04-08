package fi.metatavu.muisti.api.test.functional

import fi.metatavu.muisti.api.client.models.ExhibitionDeviceGroup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.util.*

/**
 * Test class for testing exhibition deviceGroups API
 *
 * @author Antti Leppä
 */
class ExhibitionDeviceGroupTestsIT: AbstractFunctionalTest() {

    @Test
    fun testCreateExhibitionDeviceGroup() {
        TestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val floor = it.admin().exhibitionFloors().create(exhibitionId = exhibitionId)
            val floorId = floor.id!!
            val room = it.admin().exhibitionRooms().create(exhibitionId = exhibitionId, floorId = floorId)
            val roomId = room.id!!

            val createdExhibitionDeviceGroup = it.admin().exhibitionDeviceGroups().create(exhibition.id!!,
              ExhibitionDeviceGroup(
                name = "name",
                roomId = roomId
              )
            )

            assertNotNull(createdExhibitionDeviceGroup)
            it.admin().exhibitions().assertCreateFail(400, "")
        }
   }

    @Test
    fun testFindExhibitionDeviceGroup() {
        TestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()
            val nonExistingExhibitionDeviceGroupId = UUID.randomUUID()
            val floor = it.admin().exhibitionFloors().create(exhibitionId = exhibitionId)
            val floorId = floor.id!!
            val room = it.admin().exhibitionRooms().create(exhibitionId = exhibitionId, floorId = floorId)
            val roomId = room.id!!

            val createdExhibitionDeviceGroup = it.admin().exhibitionDeviceGroups().create(
                exhibitionId = exhibitionId,
                roomId = roomId
            )

            val createdExhibitionDeviceGroupId = createdExhibitionDeviceGroup.id!!

            it.admin().exhibitionDeviceGroups().assertFindFail(404, exhibitionId, nonExistingExhibitionDeviceGroupId)
            it.admin().exhibitionDeviceGroups().assertFindFail(404, nonExistingExhibitionId, nonExistingExhibitionDeviceGroupId)
            it.admin().exhibitionDeviceGroups().assertFindFail(404, nonExistingExhibitionId, createdExhibitionDeviceGroupId)
            assertNotNull(it.admin().exhibitionDeviceGroups().findExhibitionDeviceGroup(exhibitionId, createdExhibitionDeviceGroupId))
        }
    }

    @Test
    fun testListExhibitionDeviceGroups() {
        TestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()
            val floor = it.admin().exhibitionFloors().create(exhibitionId = exhibitionId)
            val floorId = floor.id!!
            val room = it.admin().exhibitionRooms().create(exhibitionId = exhibitionId, floorId = floorId)
            val roomId = room.id!!

            it.admin().exhibitionDeviceGroups().assertListFail(404, nonExistingExhibitionId)
            assertEquals(0, it.admin().exhibitionDeviceGroups().listExhibitionDeviceGroups(exhibitionId).size)

            val createdExhibitionDeviceGroup = it.admin().exhibitionDeviceGroups().create(
                exhibitionId = exhibitionId,
                roomId = roomId
            )

            val createdExhibitionDeviceGroupId = createdExhibitionDeviceGroup.id!!
            val exhibitionDeviceGroups = it.admin().exhibitionDeviceGroups().listExhibitionDeviceGroups(exhibitionId)
            assertEquals(1, exhibitionDeviceGroups.size)
            assertEquals(createdExhibitionDeviceGroupId, exhibitionDeviceGroups[0].id)
            it.admin().exhibitionDeviceGroups().delete(exhibitionId, createdExhibitionDeviceGroupId)
            assertEquals(0, it.admin().exhibitionDeviceGroups().listExhibitionDeviceGroups(exhibitionId).size)
        }
    }

    @Test
    fun testUpdateExhibition() {
        TestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()
            val floor = it.admin().exhibitionFloors().create(exhibitionId = exhibitionId)
            val floorId = floor.id!!
            val room = it.admin().exhibitionRooms().create(exhibitionId = exhibitionId, floorId = floorId)
            val roomId = room.id!!

            val createdExhibitionDeviceGroup = it.admin().exhibitionDeviceGroups().create(
                exhibitionId = exhibitionId,
                payload = ExhibitionDeviceGroup(
                    name = "created name",
                    roomId = roomId
                )
            )

            val createdExhibitionDeviceGroupId = createdExhibitionDeviceGroup.id!!

            val foundCreatedExhibitionDeviceGroup = it.admin().exhibitionDeviceGroups().findExhibitionDeviceGroup(exhibitionId, createdExhibitionDeviceGroupId)
            assertEquals(createdExhibitionDeviceGroup.id, foundCreatedExhibitionDeviceGroup?.id)
            assertEquals("created name", createdExhibitionDeviceGroup.name)

            val updatedExhibitionDeviceGroup = it.admin().exhibitionDeviceGroups().updateExhibitionDeviceGroup(exhibitionId, ExhibitionDeviceGroup(
              name = "updated name",
              roomId = roomId,
              id = createdExhibitionDeviceGroupId
            ))

            val foundUpdatedExhibitionDeviceGroup = it.admin().exhibitionDeviceGroups().findExhibitionDeviceGroup(exhibitionId, createdExhibitionDeviceGroupId)

            assertEquals(updatedExhibitionDeviceGroup!!.id, foundUpdatedExhibitionDeviceGroup?.id)
            assertEquals("updated name", updatedExhibitionDeviceGroup.name)

            it.admin().exhibitionDeviceGroups().assertUpdateFail(404, nonExistingExhibitionId, ExhibitionDeviceGroup("name", createdExhibitionDeviceGroupId))
        }
    }

    @Test
    fun testDeleteExhibition() {
        TestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()
            val nonExistingSessionVariableId = UUID.randomUUID()
            val floor = it.admin().exhibitionFloors().create(exhibitionId = exhibitionId)
            val floorId = floor.id!!
            val room = it.admin().exhibitionRooms().create(exhibitionId = exhibitionId, floorId = floorId)
            val roomId = room.id!!

            val createdExhibitionDeviceGroup = it.admin().exhibitionDeviceGroups().create(exhibitionId = exhibitionId, roomId = roomId)
            val createdExhibitionDeviceGroupId = createdExhibitionDeviceGroup.id!!

            assertNotNull(it.admin().exhibitionDeviceGroups().findExhibitionDeviceGroup(exhibitionId, createdExhibitionDeviceGroupId))
            it.admin().exhibitionDeviceGroups().assertDeleteFail(404, exhibitionId, nonExistingSessionVariableId)
            it.admin().exhibitionDeviceGroups().assertDeleteFail(404, nonExistingExhibitionId, createdExhibitionDeviceGroupId)
            it.admin().exhibitionDeviceGroups().assertDeleteFail(404, nonExistingExhibitionId, nonExistingSessionVariableId)

            it.admin().exhibitionDeviceGroups().delete(exhibitionId, createdExhibitionDeviceGroup)

            it.admin().exhibitionDeviceGroups().assertDeleteFail(404, exhibitionId, createdExhibitionDeviceGroupId)
        }
    }

}