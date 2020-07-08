package fi.metatavu.muisti.api.test.functional

import fi.metatavu.muisti.api.client.models.*
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
        ApiTestBuilder().use {
            val mqttSubscription = it.mqtt().subscribe(MqttDeviceGroupCreate::class.java,"devicegroups/create")

            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val floor = it.admin().exhibitionFloors().create(exhibitionId = exhibitionId)
            val floorId = floor.id!!
            val room = it.admin().exhibitionRooms().create(exhibitionId = exhibitionId, floorId = floorId)
            val roomId = room.id!!

            val createdExhibitionDeviceGroup = it.admin().exhibitionDeviceGroups().create(exhibition.id!!,
              ExhibitionDeviceGroup(
                name = "name",
                roomId = roomId,
                allowVisitorSessionCreation = false
              )
            )

            assertJsonsEqual(listOf(MqttDeviceGroupCreate(exhibitionId = exhibitionId, id = createdExhibitionDeviceGroup.id!!)), mqttSubscription.getMessages(1))

            assertNotNull(createdExhibitionDeviceGroup)
            it.admin().exhibitions().assertCreateFail(400, "")
        }
   }

    @Test
    fun testFindExhibitionDeviceGroup() {
        ApiTestBuilder().use {
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
        ApiTestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()
            val floor = it.admin().exhibitionFloors().create(exhibitionId = exhibitionId)
            val floorId = floor.id!!
            val room1 = it.admin().exhibitionRooms().create(exhibitionId = exhibitionId, floorId = floorId)
            val roomId1 = room1.id!!
            val room2 = it.admin().exhibitionRooms().create(exhibitionId = exhibitionId, floorId = floorId)
            val roomId2 = room2.id!!

            it.admin().exhibitionDeviceGroups().assertListFail(expectedStatus = 404, exhibitionId = nonExistingExhibitionId, roomId = roomId1)
            assertEquals(0, it.admin().exhibitionDeviceGroups().listExhibitionDeviceGroups(exhibitionId = exhibitionId, roomId = roomId1).size)

            val createdExhibitionDeviceGroup = it.admin().exhibitionDeviceGroups().create(
                exhibitionId = exhibitionId,
                roomId = roomId1
            )

            it.admin().exhibitionDeviceGroups().create(exhibitionId = exhibitionId, roomId = roomId2)
            it.admin().exhibitionDeviceGroups().create(exhibitionId = exhibitionId, roomId = roomId2)

            it.admin().exhibitionDeviceGroups().assertCount(1, exhibitionId = exhibitionId, roomId = roomId1)
            it.admin().exhibitionDeviceGroups().assertCount(2, exhibitionId = exhibitionId, roomId = roomId2)
            it.admin().exhibitionDeviceGroups().assertCount(3, exhibitionId = exhibitionId, roomId = null)

            val createdExhibitionDeviceGroupId = createdExhibitionDeviceGroup.id!!
            val exhibitionDeviceGroups = it.admin().exhibitionDeviceGroups().listExhibitionDeviceGroups( exhibitionId = exhibitionId, roomId = roomId1)
            assertEquals(1, exhibitionDeviceGroups.size)
            assertEquals(createdExhibitionDeviceGroupId, exhibitionDeviceGroups[0].id)
            it.admin().exhibitionDeviceGroups().delete(exhibitionId, createdExhibitionDeviceGroupId)
            assertEquals(0, it.admin().exhibitionDeviceGroups().listExhibitionDeviceGroups( exhibitionId = exhibitionId, roomId = roomId1).size)
        }
    }

    @Test
    fun testUpdateExhibitionDeviceGroup() {
        ApiTestBuilder().use {
            val mqttSubscription= it.mqtt().subscribe(MqttDeviceGroupUpdate::class.java,"devicegroups/update")
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
                    roomId = roomId,
                    allowVisitorSessionCreation = false
                )
            )

            val createdExhibitionDeviceGroupId = createdExhibitionDeviceGroup.id!!

            val foundCreatedExhibitionDeviceGroup = it.admin().exhibitionDeviceGroups().findExhibitionDeviceGroup(exhibitionId, createdExhibitionDeviceGroupId)
            assertEquals(createdExhibitionDeviceGroup.id, foundCreatedExhibitionDeviceGroup?.id)
            assertEquals("created name", createdExhibitionDeviceGroup.name)
            assertEquals(false, createdExhibitionDeviceGroup.allowVisitorSessionCreation)

            val updatedExhibitionDeviceGroup = it.admin().exhibitionDeviceGroups().updateExhibitionDeviceGroup(exhibitionId, ExhibitionDeviceGroup(
              name = "updated name",
              roomId = roomId,
              id = createdExhibitionDeviceGroupId,
              allowVisitorSessionCreation = true
            ))

            assertJsonsEqual(listOf(MqttDeviceGroupUpdate(exhibitionId = exhibitionId, id = createdExhibitionDeviceGroup.id!!)), mqttSubscription.getMessages(1))

            val foundUpdatedExhibitionDeviceGroup = it.admin().exhibitionDeviceGroups().findExhibitionDeviceGroup(exhibitionId, createdExhibitionDeviceGroupId)

            assertEquals(updatedExhibitionDeviceGroup!!.id, foundUpdatedExhibitionDeviceGroup?.id)
            assertEquals("updated name", updatedExhibitionDeviceGroup.name)
            assertEquals(true, updatedExhibitionDeviceGroup.allowVisitorSessionCreation)

            it.admin().exhibitionDeviceGroups().assertUpdateFail(404, nonExistingExhibitionId, ExhibitionDeviceGroup(
                name = "name",
                id = createdExhibitionDeviceGroupId,
                allowVisitorSessionCreation = false
            ))
        }
    }

    @Test
    fun testDeleteExhibitionDeviceGroup() {
        ApiTestBuilder().use {
            val mqttSubscription= it.mqtt().subscribe(MqttDeviceGroupDelete::class.java,"devicegroups/delete")

            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()
            val nonExistingSessionVariableId = UUID.randomUUID()
            val floor = it.admin().exhibitionFloors().create(exhibitionId = exhibitionId)
            val floorId = floor.id!!
            val room = it.admin().exhibitionRooms().create(exhibitionId = exhibitionId, floorId = floorId)
            val roomId = room.id!!

            val createdContentVersion = it.admin().contentVersions().create(exhibitionId)
            val createdExhibitionDeviceGroup = it.admin().exhibitionDeviceGroups().create(exhibitionId = exhibitionId, roomId = roomId)

            val groupContentVersionToCreate = GroupContentVersion(
                name = "Group name",
                status = GroupContentVersionStatus.notstarted,
                deviceGroupId = createdExhibitionDeviceGroup.id!!,
                contentVersionId = createdContentVersion.id!!
            )

            val createdGroupContentVersion = it.admin().groupContentVersions().create(exhibitionId = exhibitionId, payload = groupContentVersionToCreate)
            val createdExhibitionDeviceGroupId = createdExhibitionDeviceGroup.id!!

            assertNotNull(it.admin().exhibitionDeviceGroups().findExhibitionDeviceGroup(exhibitionId, createdExhibitionDeviceGroupId))
            it.admin().exhibitionDeviceGroups().assertDeleteFail(404, exhibitionId, nonExistingSessionVariableId)
            it.admin().exhibitionDeviceGroups().assertDeleteFail(404, nonExistingExhibitionId, createdExhibitionDeviceGroupId)
            it.admin().exhibitionDeviceGroups().assertDeleteFail(404, nonExistingExhibitionId, nonExistingSessionVariableId)
            it.admin().exhibitionDeviceGroups().assertDeleteFail(400, exhibitionId = exhibitionId, id = createdExhibitionDeviceGroupId)

            it.admin().groupContentVersions().delete(exhibitionId = exhibitionId, groupContentVersion = createdGroupContentVersion)

            it.admin().exhibitionDeviceGroups().delete(exhibitionId, createdExhibitionDeviceGroup)
            assertJsonsEqual(listOf(MqttDeviceGroupDelete(exhibitionId = exhibitionId, id = createdExhibitionDeviceGroup.id!!)), mqttSubscription.getMessages(1))

            it.admin().exhibitionDeviceGroups().assertDeleteFail(404, exhibitionId, createdExhibitionDeviceGroupId)
        }
    }

}