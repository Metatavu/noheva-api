package fi.metatavu.muisti.api.test.functional

import fi.metatavu.muisti.api.client.models.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
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

            val visitor1Id = UUID.randomUUID()
            val visitor1Tag = UUID.randomUUID().toString()
            val visitor2Id = UUID.randomUUID()
            val visitor2Tag = UUID.randomUUID().toString()
            val createUsers = arrayOf(VisitorSessionUser(visitor1Id, visitor1Tag), VisitorSessionUser(visitor2Id, visitor2Tag))
            val createVariables = arrayOf(VisitorSessionVariable("key1", "val1"), VisitorSessionVariable("key2", "val2"))
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val createdVisitorSession = it.admin().visitorSessions().create(exhibitionId, VisitorSessionState.cOMPLETE, createUsers, createVariables)
            assertNotNull(createdVisitorSession)
            it.admin().exhibitions().assertCreateFail(400, "")

            assertEquals(createdVisitorSession.state, VisitorSessionState.cOMPLETE)
            assertEquals(createdVisitorSession.users.size, 2)
            assertEquals(createdVisitorSession.variables!!.size, 2)
            assertEquals(visitor1Tag, createdVisitorSession.users.find { session -> session.userId == visitor1Id }!!.tagId)
            assertEquals(visitor2Tag, createdVisitorSession.users.find { session -> session.userId == visitor2Id }!!.tagId)
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

            it.admin().visitorSessions().assertListFail(404, nonExistingExhibitionId)
            assertEquals(0, it.admin().visitorSessions().listVisitorSessions(exhibitionId).size)

            val createdVisitorSession = it.admin().visitorSessions().create(exhibitionId)
            val createdVisitorSessionId = createdVisitorSession.id!!
            val visitorSessions = it.admin().visitorSessions().listVisitorSessions(exhibitionId)
            assertEquals(1, visitorSessions.size)
            assertEquals(createdVisitorSessionId, visitorSessions[0].id)
            it.admin().visitorSessions().delete(exhibitionId, createdVisitorSessionId)
            assertEquals(0, it.admin().visitorSessions().listVisitorSessions(exhibitionId).size)
        }
    }

    @Test
    fun testUpdateExhibition() {
        ApiTestBuilder().use {
            val mqttSubscription = it.mqtt().subscribe(MqttExhibitionVisitorSessionUpdate::class.java,"visitorsessions/update")

            val visitor1Id = UUID.randomUUID()
            val visitor1Tag = UUID.randomUUID().toString()
            val visitor2Id = UUID.randomUUID()
            val visitor2Tag = UUID.randomUUID().toString()
            val visitor3Id = UUID.randomUUID()
            val visitor3Tag = UUID.randomUUID().toString()

            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()

            val createUsers = arrayOf(VisitorSessionUser(visitor1Id, visitor1Tag), VisitorSessionUser(visitor2Id, visitor2Tag))
            val createVariables = arrayOf(VisitorSessionVariable("key1", "val1"), VisitorSessionVariable("key2", "val2"))
            val createdVisitorSession = it.admin().visitorSessions().create(exhibitionId, VisitorSessionState.pENDING, createUsers, createVariables)
            val createdVisitorSessionId = createdVisitorSession.id!!

            val foundCreatedVisitorSession = it.admin().visitorSessions().findVisitorSession(exhibitionId, createdVisitorSessionId)
            assertEquals(createdVisitorSession.id, foundCreatedVisitorSession?.id)
            assertEquals(createdVisitorSession.state, foundCreatedVisitorSession?.state)
            assertEquals(createdVisitorSession.users.size, 2)
            assertEquals(createdVisitorSession.variables!!.size, 2)
            assertEquals(visitor1Tag, createdVisitorSession.users.find { session -> session.userId == visitor1Id }!!.tagId)
            assertEquals(visitor2Tag, createdVisitorSession.users.find { session -> session.userId == visitor2Id }!!.tagId)
            assertEquals("val1", createdVisitorSession.variables!!.find { session -> session.name == "key1" }!!.value)
            assertEquals("val2", createdVisitorSession.variables!!.find { session -> session.name == "key2" }!!.value)

            val updateUsers = arrayOf(VisitorSessionUser(visitor3Id, visitor3Tag), VisitorSessionUser(visitor2Id, visitor2Tag))
            val updateVariables = arrayOf(VisitorSessionVariable("key3", "val3"), VisitorSessionVariable("key2", "val2"))
            val updatedVisitorSession = it.admin().visitorSessions().updateVisitorSession(exhibitionId, VisitorSession(VisitorSessionState.cOMPLETE, updateUsers, createdVisitorSessionId, exhibitionId, updateVariables, exhibitionId))
            val foundUpdatedVisitorSession = it.admin().visitorSessions().findVisitorSession(exhibitionId, createdVisitorSessionId)

            assertEquals(updatedVisitorSession!!.id, foundUpdatedVisitorSession?.id)
            assertEquals(updatedVisitorSession.state, foundUpdatedVisitorSession?.state)
            assertEquals(updatedVisitorSession.users.size, 2)
            assertEquals(updatedVisitorSession.variables!!.size, 2)
            assertEquals(visitor3Tag, updatedVisitorSession.users.find { session -> session.userId == visitor3Id }!!.tagId)
            assertEquals(visitor2Tag, updatedVisitorSession.users.find { session -> session.userId == visitor2Id }!!.tagId)
            assertEquals("val3", updatedVisitorSession.variables!!.find { session -> session.name == "key3" }!!.value)
            assertEquals("val2", updatedVisitorSession.variables!!.find { session -> session.name == "key2" }!!.value)

            it.admin().visitorSessions().assertUpdateFail(404, nonExistingExhibitionId, VisitorSession(VisitorSessionState.cOMPLETE, createUsers, nonExistingExhibitionId, exhibitionId, createVariables, exhibitionId))

            assertJsonsEqual(listOf(MqttExhibitionVisitorSessionUpdate(exhibitionId = exhibitionId, id = createdVisitorSession.id!!, usersChanged = true, variablesChanged = true)), mqttSubscription.getMessages(1))
        }
    }

    @Test
    fun testDeleteExhibition() {
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