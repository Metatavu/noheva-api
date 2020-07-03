package fi.metatavu.muisti.api.test.functional

import fi.metatavu.muisti.api.client.models.*
import org.junit.Assert.*
import org.junit.Test
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Test class for testing visitor sessions API
 *
 * @author Antti Leppä
 */
class VisitorSessionTestsIT: AbstractFunctionalTest() {

    @Test
    fun testCreateVisitorSession() {
        ApiTestBuilder().use {
            val mqttSubscription = it.mqtt().subscribe(MqttExhibitionVisitorSessionCreate::class.java,"visitorsessions/create")
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!

            val visitor1 = it.admin().visitors().create(exhibitionId, Visitor(
                email = "visitor1@example.com",
                tagId = "tag1"
            ))

            val visitor2 = it.admin().visitors().create(exhibitionId, Visitor(
                email = "visitor2@example.com",
                tagId = "tag2"
            ))

            val createVariables = arrayOf(VisitorSessionVariable("key1", "val1"), VisitorSessionVariable("key2", "val2"))

            val createdVisitorSession = it.admin().visitorSessions().create(exhibitionId, VisitorSession(
                visitorIds = arrayOf(visitor1.id!!, visitor2.id!!),
                variables = createVariables,
                visitedDeviceGroups = arrayOf(),
                state = VisitorSessionState.cOMPLETE
            ))

            assertNotNull(createdVisitorSession)
            it.admin().exhibitions().assertCreateFail(400, "")

            assertEquals(createdVisitorSession.state, VisitorSessionState.cOMPLETE)
            assertEquals(createdVisitorSession.visitorIds.size, 2)
            assertEquals(createdVisitorSession.variables!!.size, 2)
            assertTrue(createdVisitorSession.visitorIds.contains(visitor1.id))
            assertTrue(createdVisitorSession.visitorIds.contains(visitor2.id))
            assertEquals("val1", createdVisitorSession.variables!!.find { session -> session.name == "key1" }!!.value)
            assertEquals("val2", createdVisitorSession.variables!!.find { session -> session.name == "key2" }!!.value)

            assertJsonsEqual(listOf(MqttExhibitionVisitorSessionCreate(exhibitionId = exhibitionId, id = createdVisitorSession.id!!)), mqttSubscription.getMessages(1))
        }
   }

    @Test
    fun testFindVisitorSession() {
        ApiTestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()
            val nonExistingVisitorSessionId = UUID.randomUUID()
            val createdVisitorSession = it.admin().visitorSessions().create(exhibitionId)
            val createdVisitorSessionId = createdVisitorSession.id!!

            it.admin().visitorSessions().assertFindFail(404, exhibitionId, nonExistingVisitorSessionId)
            it.admin().visitorSessions().assertFindFail(404, nonExistingExhibitionId, nonExistingVisitorSessionId)
            it.admin().visitorSessions().assertFindFail(404, nonExistingExhibitionId, createdVisitorSessionId)
            assertNotNull(it.admin().visitorSessions().findVisitorSession(exhibitionId, createdVisitorSessionId))
        }
    }

    @Test
    fun testListVisitorSessions() {
        ApiTestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()

            it.admin().visitorSessions().assertListFail(404, exhibitionId = nonExistingExhibitionId, tagId = null)
            assertEquals(0, it.admin().visitorSessions().listVisitorSessions(exhibitionId = exhibitionId, tagId = null).size)

            val createdVisitorSession = it.admin().visitorSessions().create(exhibitionId)
            val createdVisitorSessionId = createdVisitorSession.id!!
            val visitorSessions = it.admin().visitorSessions().listVisitorSessions(exhibitionId = exhibitionId, tagId = null)
            assertEquals(1, visitorSessions.size)
            assertEquals(createdVisitorSessionId, visitorSessions[0].id)
            it.admin().visitorSessions().delete(exhibitionId, createdVisitorSessionId)
            assertEquals(0, it.admin().visitorSessions().listVisitorSessions(exhibitionId = exhibitionId, tagId = null).size)
        }
    }

    @Test
    fun testUpdateVisitorSession() {
        ApiTestBuilder().use {
            val mqttSubscription = it.mqtt().subscribe(MqttExhibitionVisitorSessionUpdate::class.java,"visitorsessions/update")

            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()

            val floor = it.admin().exhibitionFloors().create(exhibitionId = exhibitionId)
            val floorId = floor.id!!
            val room = it.admin().exhibitionRooms().create(exhibitionId = exhibitionId, floorId = floorId)
            val roomId = room.id!!

            val deviceGroup1 = it.admin().exhibitionDeviceGroups().create(exhibitionId = exhibitionId, roomId = roomId)
            val deviceGroupId1 = deviceGroup1.id!!
            val deviceGroup2 = it.admin().exhibitionDeviceGroups().create(exhibitionId = exhibitionId, roomId = roomId)
            val deviceGroupId2 = deviceGroup2.id!!
            val deviceGroup3 = it.admin().exhibitionDeviceGroups().create(exhibitionId = exhibitionId, roomId = roomId)
            val deviceGroupId3 = deviceGroup3.id!!

            val visitor1 = it.admin().visitors().create(exhibitionId, Visitor(
                email = "visitor1@example.com",
                tagId = "tag1"
            ))

            val visitor2 = it.admin().visitors().create(exhibitionId, Visitor(
                email = "visitor2@example.com",
                tagId = "tag2"
            ))

            val visitor3 = it.admin().visitors().create(exhibitionId, Visitor(
                email = "visitor3@example.com",
                tagId = "tag3"
            ))

            val createVariables = arrayOf(VisitorSessionVariable("key1", "val1"), VisitorSessionVariable("key2", "val2"))
            val createVisitorIds = arrayOf(visitor1.id!!, visitor2.id!!)

            val createdVisitorSession = it.admin().visitorSessions().create(exhibitionId, VisitorSession(
                state = VisitorSessionState.pENDING,
                visitorIds = createVisitorIds,
                variables = createVariables,
                visitedDeviceGroups = arrayOf(
                    VisitorSessionVisitedDeviceGroup(
                        deviceGroupId = deviceGroupId1,
                        enteredAt = OffsetDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
                        exitedAt = OffsetDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
                    ),
                    VisitorSessionVisitedDeviceGroup(
                        deviceGroupId = deviceGroupId2,
                        enteredAt = OffsetDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
                        exitedAt = OffsetDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
                    )
                )
            ))

            val createdVisitorSessionId = createdVisitorSession.id!!
            val foundCreatedVisitorSession = it.admin().visitorSessions().findVisitorSession(exhibitionId, createdVisitorSessionId)

            assertEquals(createdVisitorSession.id, foundCreatedVisitorSession?.id)
            assertEquals(createdVisitorSession.state, foundCreatedVisitorSession?.state)
            assertEquals(createdVisitorSession.visitorIds.size, 2)
            assertEquals(createdVisitorSession.variables!!.size, 2)
            assertTrue(createdVisitorSession.visitorIds.contains(visitor1.id))
            assertTrue(createdVisitorSession.visitorIds.contains(visitor2.id))
            assertEquals("val1", createdVisitorSession.variables!!.find { session -> session.name == "key1" }!!.value)
            assertEquals("val2", createdVisitorSession.variables!!.find { session -> session.name == "key2" }!!.value)

            assertEquals(foundCreatedVisitorSession?.visitedDeviceGroups?.size, 2)
            assertNotNull(foundCreatedVisitorSession?.visitedDeviceGroups?.firstOrNull { item ->  item.deviceGroupId == deviceGroupId1 })
            assertNotNull(foundCreatedVisitorSession?.visitedDeviceGroups?.firstOrNull { item ->  item.deviceGroupId == deviceGroupId2 })

            val updateVariables = arrayOf(VisitorSessionVariable("key3", "val3"), VisitorSessionVariable("key2", "val2"))
            val updateVisitorIds = arrayOf(visitor3.id!!, visitor2.id!!)
            val visitedDeviceGroups = arrayOf<VisitorSessionVisitedDeviceGroup>()

            val updatedVisitorSession = it.admin().visitorSessions().updateVisitorSession(exhibitionId, VisitorSession(
                id = createdVisitorSession.id,
                state = VisitorSessionState.cOMPLETE,
                variables = updateVariables,
                visitorIds = updateVisitorIds,
                visitedDeviceGroups = arrayOf(
                    VisitorSessionVisitedDeviceGroup(
                        deviceGroupId = deviceGroupId3,
                        enteredAt = OffsetDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
                        exitedAt = OffsetDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
                    ),
                    VisitorSessionVisitedDeviceGroup(
                        deviceGroupId = deviceGroupId2,
                        enteredAt = OffsetDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
                        exitedAt = OffsetDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
                    )
                )
            ))

            val foundUpdatedVisitorSession = it.admin().visitorSessions().findVisitorSession(exhibitionId, createdVisitorSessionId)

            assertEquals(updatedVisitorSession!!.id, foundUpdatedVisitorSession?.id)
            assertEquals(updatedVisitorSession.state, foundUpdatedVisitorSession?.state)
            assertEquals(updatedVisitorSession.visitorIds.size, 2)
            assertEquals(updatedVisitorSession.variables!!.size, 2)
            assertTrue(updatedVisitorSession.visitorIds.contains(visitor3.id))
            assertTrue(updatedVisitorSession.visitorIds.contains(visitor2.id))
            assertEquals("val3", updatedVisitorSession.variables!!.find { session -> session.name == "key3" }!!.value)
            assertEquals("val2", updatedVisitorSession.variables!!.find { session -> session.name == "key2" }!!.value)

            assertEquals(updatedVisitorSession.visitedDeviceGroups?.size, 2)
            assertNotNull(updatedVisitorSession.visitedDeviceGroups?.firstOrNull { item ->  item.deviceGroupId == deviceGroupId3 })
            assertNotNull(updatedVisitorSession.visitedDeviceGroups?.firstOrNull { item ->  item.deviceGroupId == deviceGroupId2 })

            it.admin().visitorSessions().assertUpdateFail(404, nonExistingExhibitionId, VisitorSession(
                id = createdVisitorSession.id,
                state = VisitorSessionState.cOMPLETE,
                variables = updateVariables,
                visitorIds = updateVisitorIds,
                visitedDeviceGroups = visitedDeviceGroups
            ))

            assertJsonsEqual(listOf(MqttExhibitionVisitorSessionUpdate(exhibitionId = exhibitionId, id = createdVisitorSession.id!!, visitorsChanged = true, variablesChanged = true)), mqttSubscription.getMessages(1))
        }
    }

    @Test
    fun testDeleteVisitorSession() {
        ApiTestBuilder().use {
            val mqttSubscription = it.mqtt().subscribe(MqttExhibitionVisitorSessionDelete::class.java,"visitorsessions/delete")

            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()
            val nonExistingSessionVariableId = UUID.randomUUID()
            val createdVisitorSession = it.admin().visitorSessions().create(exhibitionId)
            val createdVisitorSessionId = createdVisitorSession.id!!

            assertNotNull(it.admin().visitorSessions().findVisitorSession(exhibitionId, createdVisitorSessionId))
            it.admin().visitorSessions().assertDeleteFail(404, exhibitionId, nonExistingSessionVariableId)
            it.admin().visitorSessions().assertDeleteFail(404, nonExistingExhibitionId, createdVisitorSessionId)
            it.admin().visitorSessions().assertDeleteFail(404, nonExistingExhibitionId, nonExistingSessionVariableId)

            it.admin().visitorSessions().delete(exhibitionId, createdVisitorSession)

            it.admin().visitorSessions().assertDeleteFail(404, exhibitionId, createdVisitorSessionId)

            assertJsonsEqual(listOf(MqttExhibitionVisitorSessionDelete(exhibitionId = exhibitionId, id = createdVisitorSession.id!!)), mqttSubscription.getMessages(1))
        }
    }

}