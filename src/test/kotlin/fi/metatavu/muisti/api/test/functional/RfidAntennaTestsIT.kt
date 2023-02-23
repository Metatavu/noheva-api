package fi.metatavu.muisti.api.test.functional

import fi.metatavu.muisti.api.client.models.*
import fi.metatavu.muisti.api.test.functional.resources.KeycloakResource
import fi.metatavu.muisti.api.test.functional.resources.MqttResource
import fi.metatavu.muisti.api.test.functional.resources.MysqlResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.util.*

/**
 * Test class for testing RFID antennas API
 *
 * @author Antti Leppä
 */
@QuarkusTest
@QuarkusTestResource.List(
    QuarkusTestResource(MysqlResource::class),
    QuarkusTestResource(KeycloakResource::class),
    QuarkusTestResource(MqttResource::class)
)
class RfidAntennaTestsIT: AbstractFunctionalTest() {

    @Test
    fun testCreateRfidAntenna() {
      createTestBuilder().use {
        val mqttSubscription = it.mqtt.subscribe(MqttRfidAntennaCreate::class.java,"rfidantennas/create")

        val exhibition = it.admin.exhibitions.create()
        val exhibitionId = exhibition.id!!

        val floor = it.admin.exhibitionFloors.create(exhibitionId = exhibitionId)
        val floorId = floor.id!!
        val room = it.admin.exhibitionRooms.create(exhibitionId = exhibitionId, floorId = floorId)
        val roomId = room.id!!
        val group = it.admin.exhibitionDeviceGroups.create(exhibitionId = exhibitionId, roomId = roomId, name = "Group 1")
        val createdRfidAntenna = it.admin.rfidAntennas.create(
          exhibitionId,
          RfidAntenna(
            groupId = group.id!!,
            roomId = roomId,
            name = "name",
            antennaNumber = 5,
            readerId = "readid",
            location = Point(x = 123.0, y = 234.0),
            visitorSessionStartThreshold = 80,
            visitorSessionEndThreshold = 10
          )
        )

        assertNotNull(createdRfidAntenna)
        assertJsonsEqual(listOf(MqttRfidAntennaCreate(exhibitionId = exhibitionId, id = createdRfidAntenna.id!!)), mqttSubscription.getMessages(1))
        assertEquals(80, createdRfidAntenna.visitorSessionStartThreshold)
        assertEquals(10, createdRfidAntenna.visitorSessionEndThreshold)

        it.admin.rfidAntennas.assertCreateFail(
          400,
          exhibitionId,
          RfidAntenna(
            groupId = UUID.randomUUID(),
            roomId = roomId,
            name = "name",
            antennaNumber = 5,
            readerId = "readid",
            location = Point(x = 123.0, y = 234.0),
            visitorSessionStartThreshold = 80,
            visitorSessionEndThreshold = 10
          )
        )

        it.admin.rfidAntennas.assertCreateFail(
          400,
          exhibitionId,
          RfidAntenna(
            groupId = group.id,
            roomId = roomId,
            name = "",
            antennaNumber = 5,
            readerId = "readid",
            location = Point(x = 123.0, y = 234.0),
            visitorSessionStartThreshold = 80,
            visitorSessionEndThreshold = 10
          )
        )
      }
   }

    @Test
    fun testFindRfidAntenna() {
        createTestBuilder().use {
            val exhibition = it.admin.exhibitions.create()
            val exhibitionId = exhibition.id!!
            val floor = it.admin.exhibitionFloors.create(exhibitionId = exhibitionId)
            val floorId = floor.id!!
            val room = it.admin.exhibitionRooms.create(exhibitionId = exhibitionId, floorId = floorId)
            val roomId = room.id!!

            val nonExistingExhibitionId = UUID.randomUUID()
            val nonExistingRfidAntennaId = UUID.randomUUID()
            val createdRfidAntenna = it.admin.rfidAntennas.create(exhibitionId = exhibitionId, roomId = roomId)
            val createdRfidAntennaId = createdRfidAntenna.id!!

            it.admin.rfidAntennas.assertFindFail(404, exhibitionId, nonExistingRfidAntennaId)
            it.admin.rfidAntennas.assertFindFail(404, nonExistingExhibitionId, nonExistingRfidAntennaId)
            it.admin.rfidAntennas.assertFindFail(404, nonExistingExhibitionId, createdRfidAntennaId)
            assertNotNull(it.admin.rfidAntennas.findRfidAntenna(exhibitionId, createdRfidAntennaId))
        }
    }

    @Test
    fun testListRfidAntennas() {
        createTestBuilder().use {
            val exhibition = it.admin.exhibitions.create()
            val exhibitionId = exhibition.id!!
            val floor = it.admin.exhibitionFloors.create(exhibitionId = exhibitionId)
            val floorId = floor.id!!
            val room1 = it.admin.exhibitionRooms.create(exhibitionId = exhibitionId, floorId = floorId)
            val roomId1 = room1.id!!

            val room2 = it.admin.exhibitionRooms.create(exhibitionId = exhibitionId, floorId = floorId)
            val roomId2 = room2.id!!

            val group1 = it.admin.exhibitionDeviceGroups.create(exhibitionId = exhibitionId, roomId = roomId1, name = "Group 1")
            val group2 = it.admin.exhibitionDeviceGroups.create(exhibitionId = exhibitionId, roomId = roomId2, name = "Group 2")
            val nonExistingExhibitionId = UUID.randomUUID()

            it.admin.rfidAntennas.assertListFail(expectedStatus = 404, exhibitionId = nonExistingExhibitionId, deviceGroupId = null, roomId = null)
            it.admin.rfidAntennas.assertListFail(expectedStatus = 400, exhibitionId = exhibitionId, deviceGroupId = UUID.randomUUID(), roomId = null)
            it.admin.rfidAntennas.assertListFail(expectedStatus = 400, exhibitionId = exhibitionId, deviceGroupId = null, roomId = UUID.randomUUID())

            assertEquals(0, it.admin.rfidAntennas.listRfidAntennas(exhibitionId = exhibitionId, deviceGroupId = null, roomId = null).size)

            val createdRfidAntenna = it.admin.rfidAntennas.create(exhibitionId = exhibitionId, payload = RfidAntenna(
                name = "Default",
                roomId = roomId1,
                readerId = "readerid1234",
                antennaNumber = 1,
                location = Point(x = 1.0, y = 2.0),
                groupId = group1.id,
                visitorSessionStartThreshold = 80,
                visitorSessionEndThreshold = 10
            ))

            val createdRfidAntennaId = createdRfidAntenna.id!!

            it.admin.rfidAntennas.assertCount(expected = 1, exhibitionId = exhibitionId, deviceGroupId = null, roomId = null)
            it.admin.rfidAntennas.assertCount(expected = 1, exhibitionId = exhibitionId, deviceGroupId = group1.id!!, roomId = null)
            it.admin.rfidAntennas.assertCount(expected = 0, exhibitionId = exhibitionId, deviceGroupId = group2.id!!, roomId = null)

            it.admin.rfidAntennas.assertCount(expected = 1, exhibitionId = exhibitionId, deviceGroupId = null, roomId = roomId1)
            it.admin.rfidAntennas.assertCount(expected = 1, exhibitionId = exhibitionId, deviceGroupId = group1.id, roomId = roomId1)
            it.admin.rfidAntennas.assertCount(expected = 0, exhibitionId = exhibitionId, deviceGroupId = group2.id, roomId = roomId1)

            val rfidAntennas = it.admin.rfidAntennas.listRfidAntennas(exhibitionId = exhibitionId, deviceGroupId = null, roomId = null)
            assertEquals(1, rfidAntennas.size)
            assertEquals(createdRfidAntennaId, rfidAntennas[0].id)
            it.admin.rfidAntennas.delete(exhibitionId, createdRfidAntennaId)
            assertEquals(0, it.admin.rfidAntennas.listRfidAntennas(exhibitionId = exhibitionId, deviceGroupId = null, roomId = null).size)
        }
    }

    @Test
    fun testUpdateExhibition() {
      createTestBuilder().use {
        val mqttSubscription= it.mqtt.subscribe(MqttRfidAntennaUpdate::class.java,"rfidantennas/update")
        val exhibition = it.admin.exhibitions.create()
        val exhibitionId = exhibition.id!!
        val nonExistingExhibitionId = UUID.randomUUID()
        val floor = it.admin.exhibitionFloors.create(exhibitionId = exhibitionId)
        val floorId = floor.id!!
        val room1 = it.admin.exhibitionRooms.create(exhibitionId = exhibitionId, floorId = floorId)
        val roomId1 = room1.id!!
        val room2 = it.admin.exhibitionRooms.create(exhibitionId = exhibitionId, floorId = floorId)
        val roomId2 = room2.id!!

        val group1 = it.admin.exhibitionDeviceGroups.create(exhibitionId = exhibitionId, roomId = roomId1, name = "Group 1")
        val group2 = it.admin.exhibitionDeviceGroups.create(exhibitionId = exhibitionId, roomId = roomId2, name = "Group 2")

        val createdRfidAntenna = it.admin.rfidAntennas.create(exhibitionId, RfidAntenna(
          groupId = group1.id!!,
          roomId = roomId1,
          name = "created name",
          location = Point(-123.0, 234.0),
          readerId = "createid",
          antennaNumber = 15,
          visitorSessionStartThreshold = 80,
          visitorSessionEndThreshold = 10
        ))

        val createdRfidAntennaId = createdRfidAntenna.id!!

        val foundCreatedRfidAntenna = it.admin.rfidAntennas.findRfidAntenna(exhibitionId, createdRfidAntennaId)
        assertEquals(createdRfidAntenna.id, foundCreatedRfidAntenna.id)
        assertEquals("created name", createdRfidAntenna.name)
        assertEquals(-123.0, createdRfidAntenna.location.x)
        assertEquals(234.0, createdRfidAntenna.location.y)
        assertEquals("createid", createdRfidAntenna.readerId)
        assertEquals(15, createdRfidAntenna.antennaNumber)
        assertEquals(80, createdRfidAntenna.visitorSessionStartThreshold)
        assertEquals(10, createdRfidAntenna.visitorSessionEndThreshold)

        val updatedRfidAntenna = it.admin.rfidAntennas.updateRfidAntenna(exhibitionId, RfidAntenna(
          id = createdRfidAntennaId,
          groupId = group2.id!!,
          roomId = roomId2,
          name = "update name",
          location = Point(-654.0, 765.0),
          readerId = "updateid",
          antennaNumber = 1,
          visitorSessionStartThreshold = 50,
          visitorSessionEndThreshold = 20
        ))

        assertEquals(updatedRfidAntenna.id, foundCreatedRfidAntenna.id)
        assertEquals("update name", updatedRfidAntenna.name)
        assertEquals(-654.0, updatedRfidAntenna.location.x)
        assertEquals(765.0, updatedRfidAntenna.location.y)
        assertEquals("updateid", updatedRfidAntenna.readerId)
        assertEquals(1, updatedRfidAntenna.antennaNumber)
        assertEquals(50, updatedRfidAntenna.visitorSessionStartThreshold)
        assertEquals(20, updatedRfidAntenna.visitorSessionEndThreshold)

        val foundUpdatedRfidAntenna = it.admin.rfidAntennas.findRfidAntenna(exhibitionId, createdRfidAntennaId)
        assertEquals(foundUpdatedRfidAntenna.id, foundCreatedRfidAntenna.id)
        assertEquals("update name", foundUpdatedRfidAntenna.name)
        assertEquals(-654.0, foundUpdatedRfidAntenna.location.x)
        assertEquals(765.0, foundUpdatedRfidAntenna.location.y)
        assertEquals("updateid", foundUpdatedRfidAntenna.readerId)
        assertEquals(1, foundUpdatedRfidAntenna.antennaNumber)
        assertEquals(50, foundUpdatedRfidAntenna.visitorSessionStartThreshold)
        assertEquals(20, foundUpdatedRfidAntenna.visitorSessionEndThreshold)

        it.admin.rfidAntennas.updateRfidAntenna(exhibitionId, updatedRfidAntenna)

        assertJsonsEqual(
          listOf(
            MqttRfidAntennaUpdate(exhibitionId = exhibitionId, id = createdRfidAntenna.id, groupChanged = true),
            MqttRfidAntennaUpdate(exhibitionId = exhibitionId, id = createdRfidAntenna.id, groupChanged = false)
          ),
          mqttSubscription.getMessages(2)
        )

        it.admin.rfidAntennas.assertUpdateFail(404, nonExistingExhibitionId, RfidAntenna(
          id = createdRfidAntennaId,
          groupId = UUID.randomUUID(),
          roomId = roomId2,
          name = "update name",
          location = Point(-654.0, 765.0),
          readerId = "updateid",
          antennaNumber = 1,
          visitorSessionStartThreshold = 80,
          visitorSessionEndThreshold = 10
        ))

        it.admin.rfidAntennas.assertUpdateFail(400, exhibitionId, RfidAntenna(
          id = createdRfidAntennaId,
          groupId = group2.id,
          roomId = UUID.randomUUID(),
          name = "update name",
          location = Point(-654.0, 765.0),
          readerId = "updateid",
          antennaNumber = 1,
          visitorSessionStartThreshold = 80,
          visitorSessionEndThreshold = 10
        ))

        it.admin.rfidAntennas.assertUpdateFail(400, exhibitionId, RfidAntenna(
          id = createdRfidAntennaId,
          groupId = group2.id,
          roomId = roomId2,
          name = "",
          location = Point(-654.0, 765.0),
          readerId = "updateid",
          antennaNumber = 1,
          visitorSessionStartThreshold = 80,
          visitorSessionEndThreshold = 10
        ))

        it.admin.rfidAntennas.assertUpdateFail(400, exhibitionId, RfidAntenna(
          id = createdRfidAntennaId,
          groupId = group2.id,
          roomId = roomId2,
          name = "update name",
          location = Point(-654.0, 765.0),
          readerId = "",
          antennaNumber = 1,
          visitorSessionStartThreshold = 80,
          visitorSessionEndThreshold = 10
        ))
      }
    }

    @Test
    fun testDeleteExhibition() {
        createTestBuilder().use {
            val mqttSubscription = it.mqtt.subscribe(MqttRfidAntennaDelete::class.java,"rfidantennas/delete")
            val exhibition = it.admin.exhibitions.create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()
            val nonExistingSessionVariableId = UUID.randomUUID()
            val floor = it.admin.exhibitionFloors.create(exhibitionId = exhibitionId)
            val floorId = floor.id!!
            val room = it.admin.exhibitionRooms.create(exhibitionId = exhibitionId, floorId = floorId)
            val roomId = room.id!!

            val group = it.admin.exhibitionDeviceGroups.create(exhibitionId = exhibitionId, roomId = roomId, name = "Group 1")
            val createdRfidAntenna = it.admin.rfidAntennas.create(exhibitionId, RfidAntenna(
                groupId = group.id!!,
                roomId = roomId,
                name = "created name",
                location = Point(-123.0, 234.0),
                readerId = "createid",
                antennaNumber = 15,
                visitorSessionStartThreshold = 80,
                visitorSessionEndThreshold = 10
            ))

            val createdRfidAntennaId = createdRfidAntenna.id!!

            assertNotNull(it.admin.rfidAntennas.findRfidAntenna(exhibitionId, createdRfidAntennaId))
            it.admin.rfidAntennas.assertDeleteFail(404, exhibitionId, nonExistingSessionVariableId)
            it.admin.rfidAntennas.assertDeleteFail(404, nonExistingExhibitionId, createdRfidAntennaId)
            it.admin.rfidAntennas.assertDeleteFail(404, nonExistingExhibitionId, nonExistingSessionVariableId)

            it.admin.rfidAntennas.delete(exhibitionId, createdRfidAntenna)
            assertJsonsEqual(listOf(MqttRfidAntennaDelete(exhibitionId = exhibitionId, id = createdRfidAntenna.id)), mqttSubscription.getMessages(1))

            it.admin.rfidAntennas.assertDeleteFail(404, exhibitionId, createdRfidAntennaId)
        }
    }

}
