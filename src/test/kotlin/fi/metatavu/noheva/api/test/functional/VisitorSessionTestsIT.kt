package fi.metatavu.noheva.api.test.functional

import fi.metatavu.noheva.api.client.models.*
import fi.metatavu.noheva.api.test.functional.builder.TestBuilder
import fi.metatavu.noheva.api.test.functional.resources.KeycloakResource
import fi.metatavu.noheva.api.test.functional.resources.MqttResource
import fi.metatavu.noheva.api.test.functional.resources.MysqlResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import org.awaitility.Awaitility
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Test class for testing visitor sessions API
 * @author Antti Leppä
 */
@QuarkusTest
@QuarkusTestResource.List(
    QuarkusTestResource(MysqlResource::class),
    QuarkusTestResource(KeycloakResource::class),
    QuarkusTestResource(MqttResource::class)
)
@TestProfile(DefaultTestProfile::class)
class VisitorSessionTestsIT : AbstractFunctionalTest() {

    @Test
    fun testCreateVisitorSession() {
        createTestBuilder().use {
            val mqttSubscription =
                it.mqtt.subscribe(MqttExhibitionVisitorSessionCreate::class.java, "visitorsessions/create")
            it.admin.exhibitions.listExhibitions()
            val exhibition = it.admin.exhibitions.create()
            val exhibitionId = exhibition.id!!

            val visitor1 = it.admin.visitors.create(
                exhibitionId, Visitor(
                    email = "visitor1@example.com",
                    tagId = "tag1",
                    language = "fi"
                )
            )

            val visitor2 = it.admin.visitors.create(
                exhibitionId, Visitor(
                    email = "visitor2@example.com",
                    tagId = "tag2",
                    language = "fi"
                )
            )

            for (name in arrayOf("key1", "key2", "key3", "key4")) {
                it.admin.visitorVariables.create(
                    exhibitionId = exhibitionId, payload = VisitorVariable(
                        name = name,
                        type = VisitorVariableType.TEXT,
                        editableFromUI = false
                    )
                )
            }

            val createVariables =
                arrayOf(VisitorSessionVariable("key1", "val1"), VisitorSessionVariable("key2", "val2"))

            val createdVisitorSession = it.admin.visitorSessions.create(
                exhibitionId, VisitorSession(
                    visitorIds = arrayOf(visitor1.id!!, visitor2.id!!),
                    variables = createVariables,
                    visitedDeviceGroups = arrayOf(),
                    language = "FI",
                    state = VisitorSessionState.COMPLETE
                )
            )

            assertNotNull(createdVisitorSession)

            assertEquals(createdVisitorSession.state, VisitorSessionState.COMPLETE)
            assertEquals(createdVisitorSession.visitorIds.size, 2)
            assertEquals(createdVisitorSession.variables!!.size, 2)
            assertTrue(createdVisitorSession.visitorIds.contains(visitor1.id))
            assertTrue(createdVisitorSession.visitorIds.contains(visitor2.id))
            assertEquals("val1", createdVisitorSession.variables.find { session -> session.name == "key1" }!!.value)
            assertEquals("val2", createdVisitorSession.variables.find { session -> session.name == "key2" }!!.value)

            assertJsonsEqual(
                listOf(
                    MqttExhibitionVisitorSessionCreate(
                        exhibitionId = exhibitionId,
                        id = createdVisitorSession.id!!
                    )
                ), mqttSubscription.getMessages(1)
            )
        }
    }

    @Test
    fun testValidateVisitorSessionVariable() {
        createTestBuilder().use {
            val exhibition = it.admin.exhibitions.create()
            val exhibitionId = exhibition.id!!

            it.admin.visitorVariables.create(
                exhibitionId = exhibitionId, payload = VisitorVariable(
                    name = "text",
                    type = VisitorVariableType.TEXT,
                    editableFromUI = false
                )
            )

            it.admin.visitorVariables.create(
                exhibitionId = exhibitionId, payload = VisitorVariable(
                    name = "number",
                    type = VisitorVariableType.NUMBER,
                    editableFromUI = false
                )
            )

            it.admin.visitorVariables.create(
                exhibitionId = exhibitionId, payload = VisitorVariable(
                    name = "boolean",
                    type = VisitorVariableType.BOOLEAN,
                    editableFromUI = false
                )
            )

            it.admin.visitorVariables.create(
                exhibitionId = exhibitionId, payload = VisitorVariable(
                    name = "enum",
                    type = VisitorVariableType.ENUMERATED,
                    enum = arrayOf("valid"),
                    editableFromUI = false
                )
            )

            val visitor = it.admin.visitors.create(
                exhibitionId, Visitor(
                    email = "visitor1@example.com",
                    tagId = "tag1",
                    language = "fi"
                )
            )

            it.admin.visitorSessions.assertCreateFail(
                expectedStatus = 400, exhibitionId = exhibitionId, payload = VisitorSession(
                    visitorIds = arrayOf(visitor.id!!),
                    variables = arrayOf(VisitorSessionVariable("key1", "val1")),
                    visitedDeviceGroups = arrayOf(),
                    language = "FI",
                    state = VisitorSessionState.COMPLETE
                )
            )

            it.admin.visitorSessions.assertCreateFail(
                expectedStatus = 400, exhibitionId = exhibitionId, payload = VisitorSession(
                    visitorIds = arrayOf(visitor.id),
                    variables = arrayOf(VisitorSessionVariable("number", "val1")),
                    visitedDeviceGroups = arrayOf(),
                    language = "FI",
                    state = VisitorSessionState.COMPLETE
                )
            )

            it.admin.visitorSessions.assertCreateFail(
                expectedStatus = 400, exhibitionId = exhibitionId, payload = VisitorSession(
                    visitorIds = arrayOf(visitor.id),
                    variables = arrayOf(VisitorSessionVariable("boolean", "val1")),
                    visitedDeviceGroups = arrayOf(),
                    language = "FI",
                    state = VisitorSessionState.COMPLETE
                )
            )

            it.admin.visitorSessions.assertCreateFail(
                expectedStatus = 400, exhibitionId = exhibitionId, payload = VisitorSession(
                    visitorIds = arrayOf(visitor.id),
                    variables = arrayOf(VisitorSessionVariable("enum", "val1")),
                    visitedDeviceGroups = arrayOf(),
                    language = "FI",
                    state = VisitorSessionState.COMPLETE
                )
            )

            it.admin.visitorSessions.create(
                exhibitionId = exhibitionId, payload = VisitorSession(
                    visitorIds = arrayOf(visitor.id),
                    variables = arrayOf(VisitorSessionVariable("number", "12")),
                    visitedDeviceGroups = arrayOf(),
                    language = "FI",
                    state = VisitorSessionState.COMPLETE
                )
            )

            it.admin.visitorSessions.create(
                exhibitionId = exhibitionId, payload = VisitorSession(
                    visitorIds = arrayOf(visitor.id),
                    variables = arrayOf(VisitorSessionVariable("boolean", "true")),
                    visitedDeviceGroups = arrayOf(),
                    language = "FI",
                    state = VisitorSessionState.COMPLETE
                )
            )

            it.admin.visitorSessions.create(
                exhibitionId = exhibitionId, payload = VisitorSession(
                    visitorIds = arrayOf(visitor.id),
                    variables = arrayOf(VisitorSessionVariable("enum", "valid")),
                    visitedDeviceGroups = arrayOf(),
                    language = "FI",
                    state = VisitorSessionState.COMPLETE
                )
            )

        }
    }

    @Test
    fun testFindVisitorSession() {
        createTestBuilder().use {
            val exhibition = it.admin.exhibitions.create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()
            val nonExistingVisitorSessionId = UUID.randomUUID()
            val createdVisitorSession = it.admin.visitorSessions.create(exhibitionId)
            val createdVisitorSessionId = createdVisitorSession.id!!

            it.admin.visitorSessions.assertFindFail(404, exhibitionId, nonExistingVisitorSessionId)
            it.admin.visitorSessions.assertFindFail(404, nonExistingExhibitionId, nonExistingVisitorSessionId)
            it.admin.visitorSessions.assertFindFail(404, nonExistingExhibitionId, createdVisitorSessionId)
            assertNotNull(it.admin.visitorSessions.findVisitorSession(exhibitionId, createdVisitorSessionId))
        }
    }

    @Test
    fun testListVisitorSessions() {
        createTestBuilder().use {
            val exhibition = it.admin.exhibitions.create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()

            it.admin.visitorSessions.assertListFail(404, exhibitionId = nonExistingExhibitionId, tagId = null)
            assertEquals(
                0,
                it.admin.visitorSessions.listVisitorSessions(exhibitionId = exhibitionId, tagId = null).size
            )

            val createdVisitorSession = it.admin.visitorSessions.create(exhibitionId)
            val createdVisitorSessionId = createdVisitorSession.id!!
            val visitorSessions =
                it.admin.visitorSessions.listVisitorSessions(exhibitionId = exhibitionId, tagId = null)
            assertEquals(1, visitorSessions.size)
            assertEquals(createdVisitorSessionId, visitorSessions[0].id)
            it.admin.visitorSessions.delete(exhibitionId, createdVisitorSessionId)
            assertEquals(
                0,
                it.admin.visitorSessions.listVisitorSessions(exhibitionId = exhibitionId, tagId = null).size
            )
        }
    }

    @Test
    fun testListVisitorSessionTimeout() {
        createTestBuilder().use {
            val exhibition = it.admin.exhibitions.create()
            val exhibitionId = exhibition.id!!
            val createdVisitorSession = it.admin.visitorSessions.create(exhibitionId)
            val createdVisitorSessionId = createdVisitorSession.id!!
            it.admin.visitorSessions.assertCount(expected = 1, exhibitionId = exhibitionId, tagId = null)
            waitForVisitorSessionNotFound(
                apiTestBuilder = it,
                exhibitionId = exhibitionId,
                visitorSessionId = createdVisitorSessionId
            )
            it.admin.visitorSessions.assertCount(expected = 0, exhibitionId = exhibitionId, tagId = null)
        }
    }

    @Test
    fun testUpdateVisitorSession() {
        createTestBuilder().use {
            val mqttSubscription =
                it.mqtt.subscribe(MqttExhibitionVisitorSessionUpdate::class.java, "visitorsessions/update")

            val exhibition = it.admin.exhibitions.create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()

            val floor = it.admin.exhibitionFloors.create(exhibitionId = exhibitionId)
            val floorId = floor.id!!
            val room = it.admin.exhibitionRooms.create(exhibitionId = exhibitionId, floorId = floorId)
            val roomId = room.id!!

            val deviceGroup1 =
                it.admin.exhibitionDeviceGroups.create(exhibitionId = exhibitionId, roomId = roomId, name = "Group 1")
            val deviceGroupId1 = deviceGroup1.id!!
            val deviceGroup2 =
                it.admin.exhibitionDeviceGroups.create(exhibitionId = exhibitionId, roomId = roomId, name = "Group 2")
            val deviceGroupId2 = deviceGroup2.id!!
            val deviceGroup3 =
                it.admin.exhibitionDeviceGroups.create(exhibitionId = exhibitionId, roomId = roomId, name = "Group 3")
            val deviceGroupId3 = deviceGroup3.id!!

            val visitor1 = it.admin.visitors.create(
                exhibitionId, Visitor(
                    email = "visitor1@example.com",
                    tagId = "tag1",
                    language = "fi"
                )
            )

            val visitor2 = it.admin.visitors.create(
                exhibitionId, Visitor(
                    email = "visitor2@example.com",
                    tagId = "tag2",
                    language = "fi"
                )
            )

            val visitor3 = it.admin.visitors.create(
                exhibitionId, Visitor(
                    email = "visitor3@example.com",
                    tagId = "tag3",
                    language = "fi"
                )
            )

            for (name in arrayOf("key1", "key2", "key3", "key4")) {
                it.admin.visitorVariables.create(
                    exhibitionId = exhibitionId, payload = VisitorVariable(
                        name = name,
                        type = VisitorVariableType.TEXT,
                        editableFromUI = false
                    )
                )
            }

            val createVariables = arrayOf(
                VisitorSessionVariable("key1", "val1"),
                VisitorSessionVariable("key2", "val2"),
                VisitorSessionVariable("key3", "val3")
            )

            val createVisitorIds = arrayOf(visitor1.id!!, visitor2.id!!)

            val createdVisitorSession = it.admin.visitorSessions.create(
                exhibitionId, VisitorSession(
                    state = VisitorSessionState.PENDING,
                    visitorIds = createVisitorIds,
                    variables = createVariables,
                    language = "FI",
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
                )
            )

            val createdVisitorSessionId = createdVisitorSession.id!!
            val foundCreatedVisitorSession =
                it.admin.visitorSessions.findVisitorSession(exhibitionId, createdVisitorSessionId)

            assertEquals(createdVisitorSession.id, foundCreatedVisitorSession.id)
            assertEquals(createdVisitorSession.state, foundCreatedVisitorSession.state)
            assertEquals("FI", foundCreatedVisitorSession.language)
            assertEquals(createdVisitorSession.language, foundCreatedVisitorSession.language)
            assertEquals(createdVisitorSession.visitorIds.size, 2)
            assertEquals(createdVisitorSession.variables!!.size, 3)
            assertTrue(createdVisitorSession.visitorIds.contains(visitor1.id))
            assertTrue(createdVisitorSession.visitorIds.contains(visitor2.id))
            assertEquals("val1", createdVisitorSession.variables.find { session -> session.name == "key1" }!!.value)
            assertEquals("val2", createdVisitorSession.variables.find { session -> session.name == "key2" }!!.value)
            assertEquals("val3", createdVisitorSession.variables.find { session -> session.name == "key3" }!!.value)

            assertEquals(foundCreatedVisitorSession.visitedDeviceGroups?.size, 2)
            assertNotNull(foundCreatedVisitorSession.visitedDeviceGroups?.firstOrNull { item -> item.deviceGroupId == deviceGroupId1 })
            assertNotNull(foundCreatedVisitorSession.visitedDeviceGroups?.firstOrNull { item -> item.deviceGroupId == deviceGroupId2 })

            val updateVariables = arrayOf(
                VisitorSessionVariable("key4", "val4"),
                VisitorSessionVariable("key3", "upd3"),
                VisitorSessionVariable("key2", "val2")
            )

            val updateVisitorIds = arrayOf(visitor3.id!!, visitor2.id)
            val visitedDeviceGroups = arrayOf<VisitorSessionVisitedDeviceGroup>()

            val updatedVisitorSession = it.admin.visitorSessions.updateVisitorSession(
                exhibitionId, VisitorSession(
                    id = createdVisitorSession.id,
                    state = VisitorSessionState.COMPLETE,
                    variables = updateVariables,
                    visitorIds = updateVisitorIds,
                    language = "EN",
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
                )
            )

            val foundUpdatedVisitorSession =
                it.admin.visitorSessions.findVisitorSession(exhibitionId, createdVisitorSessionId)

            assertEquals(updatedVisitorSession.id, foundUpdatedVisitorSession.id)
            assertEquals(updatedVisitorSession.state, foundUpdatedVisitorSession.state)
            assertEquals(updatedVisitorSession.language, "EN")
            assertEquals(updatedVisitorSession.visitorIds.size, 2)
            assertEquals(updatedVisitorSession.variables!!.size, 3)
            assertTrue(updatedVisitorSession.visitorIds.contains(visitor3.id))
            assertTrue(updatedVisitorSession.visitorIds.contains(visitor2.id))
            assertEquals("val4", updatedVisitorSession.variables.find { session -> session.name == "key4" }!!.value)
            assertEquals("upd3", updatedVisitorSession.variables.find { session -> session.name == "key3" }!!.value)
            assertEquals("val2", updatedVisitorSession.variables.find { session -> session.name == "key2" }!!.value)

            assertEquals(updatedVisitorSession.visitedDeviceGroups?.size, 2)
            assertNotNull(updatedVisitorSession.visitedDeviceGroups?.firstOrNull { item -> item.deviceGroupId == deviceGroupId3 })
            assertNotNull(updatedVisitorSession.visitedDeviceGroups?.firstOrNull { item -> item.deviceGroupId == deviceGroupId2 })

            it.admin.visitorSessions.assertUpdateFail(
                404, nonExistingExhibitionId, VisitorSession(
                    id = createdVisitorSession.id,
                    state = VisitorSessionState.COMPLETE,
                    variables = updateVariables,
                    visitorIds = updateVisitorIds,
                    language = "FI",
                    visitedDeviceGroups = visitedDeviceGroups
                )
            )

            assertJsonsEqual(
                listOf(
                    MqttExhibitionVisitorSessionUpdate(
                        exhibitionId = exhibitionId,
                        id = createdVisitorSession.id,
                        visitorsChanged = true,
                        variablesChanged = true
                    )
                ), mqttSubscription.getMessages(1)
            )
        }
    }

    @Test
    fun testDeleteVisitorSession() {
        createTestBuilder().use {
            val mqttSubscription =
                it.mqtt.subscribe(MqttExhibitionVisitorSessionDelete::class.java, "visitorsessions/delete")

            val exhibition = it.admin.exhibitions.create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()
            val nonExistingSessionVariableId = UUID.randomUUID()
            val createdVisitorSession = it.admin.visitorSessions.create(exhibitionId)
            val createdVisitorSessionId = createdVisitorSession.id!!

            assertNotNull(it.admin.visitorSessions.findVisitorSession(exhibitionId, createdVisitorSessionId))
            it.admin.visitorSessions.assertDeleteFail(404, exhibitionId, nonExistingSessionVariableId)
            it.admin.visitorSessions.assertDeleteFail(404, nonExistingExhibitionId, createdVisitorSessionId)
            it.admin.visitorSessions.assertDeleteFail(404, nonExistingExhibitionId, nonExistingSessionVariableId)

            it.admin.visitorSessions.delete(exhibitionId, createdVisitorSession)

            it.admin.visitorSessions.assertDeleteFail(404, exhibitionId, createdVisitorSessionId)

            assertJsonsEqual(
                listOf(
                    MqttExhibitionVisitorSessionDelete(
                        exhibitionId = exhibitionId,
                        id = createdVisitorSession.id
                    )
                ), mqttSubscription.getMessages(1)
            )
        }
    }

    /**
     * Waits for visitor session for to be timed out
     *
     * @param apiTestBuilder API test builder instance
     * @param exhibitionId exhibition id
     * @param visitorSessionId visitor session id
     */
    private fun waitForVisitorSessionNotFound(apiTestBuilder: TestBuilder, exhibitionId: UUID, visitorSessionId: UUID) {
        Awaitility
            .await().atMost(Duration.ofMinutes(5))
            .pollInterval(Duration.ofSeconds(10))
            .until {
                !isVisitorSessionFound(
                    apiTestBuilder = apiTestBuilder,
                    exhibitionId = exhibitionId,
                    visitorSessionId = visitorSessionId
                )
            }
    }

    /**
     * Checks whether visitor session can be found from the API
     *
     * @param apiTestBuilder API test builder instance
     * @param exhibitionId exhibition id
     * @param visitorSessionId visitor session id
     */
    private fun isVisitorSessionFound(
        apiTestBuilder: TestBuilder,
        exhibitionId: UUID,
        visitorSessionId: UUID
    ): Boolean {
        return apiTestBuilder.admin.visitorSessions
            .listVisitorSessions(exhibitionId = exhibitionId, tagId = null)
            .firstOrNull { it.id == visitorSessionId } != null
    }

}