package fi.metatavu.muisti.api.test.functional

import fi.metatavu.muisti.api.client.models.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.util.*

/**
 * Test class for testing visitor API
 *
 * @author Antti Leppä
 */
class VisitorTestsIT: AbstractFunctionalTest() {

    @Test
    fun testCreateVisitor() {
        ApiTestBuilder().use {
            val mqttSubscription = it.mqtt().subscribe(MqttExhibitionVisitorSessionCreate::class.java,"visitors/create")
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val createdVisitor = it.admin().visitors().create(exhibitionId, Visitor(
                email = "visitor@example.com",
                language = "fi",
                tagId = "faketag"
            ))

            assertNotNull(createdVisitor)
            assertNotNull(createdVisitor.id)
            assertEquals("faketag", createdVisitor.tagId)
            assertEquals("visitor@example.com", createdVisitor.email)

            assertJsonsEqual(listOf(MqttVisitorCreate(exhibitionId = exhibitionId, id = createdVisitor.id!!)), mqttSubscription.getMessages(1))
        }
    }

    @Test
    fun testFindVisitor() {
        ApiTestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val createdVisitor = it.admin().visitors().create(exhibitionId)
            val visitorId = createdVisitor.id!!
            val foundVisitor = it.admin().visitors().findVisitor(exhibitionId, visitorId)
            assertJsonsEqual(createdVisitor, foundVisitor)

            it.admin().visitors().assertFindFail(404, exhibitionId, UUID.randomUUID())
            it.admin().visitors().assertFindFail(404, UUID.randomUUID(), UUID.randomUUID())
            it.admin().visitors().assertFindFail(404, UUID.randomUUID(), visitorId)
        }
    }

    @Test
    fun testFindVisitorTag() {
        ApiTestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!

            it.admin().visitors().create(exhibitionId, Visitor(
                    email = "test@example.com",
                    language = "fi",
                    tagId = "testtag"
            ))

            assertEquals("testtag", it.admin().visitors().findVisitorTag(exhibitionId, "testtag")?.tagId)
            it.admin().visitors().assertFindVisitorFail(expectedStatus = 404, exhibitionId = exhibitionId, tagId = "nottag")
        }
    }

    @Test
    fun testListVisitors() {
        ApiTestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()

            it.admin().visitors().create(exhibitionId, Visitor(
                    email = "visitor1@example.com",
                    language = "fi",
                    tagId = "faketag1"
            ))

            it.admin().visitors().create(exhibitionId, Visitor(
                    email = "visitor2@example.com",
                    language = "fi",
                    tagId = "faketag2"
            ))

            val visitors1 = it.admin().visitors().listVisitors(exhibitionId = exhibitionId, tagId = null)
            assertEquals(2, visitors1.size)
            assertNotNull(visitors1.firstOrNull { visitor ->  visitor.tagId == "faketag1" })
            assertNotNull(visitors1.firstOrNull { visitor ->  visitor.tagId == "faketag2" })

            val visitors2 = it.admin().visitors().listVisitors(exhibitionId = exhibitionId, tagId = "faketag2")
            assertEquals(1, visitors2.size)
            assertNotNull(visitors2.firstOrNull { visitor ->  visitor.tagId == "faketag2" })

            val visitors3 = it.admin().visitors().listVisitors(exhibitionId = exhibitionId, tagId = "noexistingtag")
            assertEquals(0, visitors3.size)

            it.admin().visitors().assertListFail(404, exhibitionId = nonExistingExhibitionId, tagId = null)
        }
    }

    @Test
    fun testUpdateVisitor() {
        ApiTestBuilder().use {
            val mqttSubscription = it.mqtt().subscribe(MqttExhibitionVisitorSessionCreate::class.java,"visitors/update")
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!

            val createdVisitor = it.admin().visitors().create(exhibitionId, Visitor(
                email = "visitor@example.com",
                language = "fi",
                firstName = "First",
                lastName = "Last",
                phone = "+358 01 234 5678",
                birthYear = 1980,
                tagId = "faketag"
            ))

            val createdVisitorId = createdVisitor.id!!

            assertEquals("faketag", createdVisitor.tagId)
            assertEquals("visitor@example.com", createdVisitor.email)
            assertEquals("First", createdVisitor.firstName)
            assertEquals("Last", createdVisitor.lastName)
            assertEquals("+358 01 234 5678", createdVisitor.phone)
            assertEquals(1980, createdVisitor.birthYear)

            val updatedVisitor = it.admin().visitors().updateVisitor(exhibitionId, Visitor(
                    id = createdVisitor.id,
                    language = "fi",
                    email = "visitor@example.com",
                    tagId = "updatetag"
            ))

            assertEquals("updatetag", updatedVisitor?.tagId)
            assertEquals("First name", updatedVisitor?.firstName)
            assertEquals("Last name", updatedVisitor?.lastName)
            assertEquals("+358 12 345 6789", updatedVisitor?.phone)
            assertEquals(1980, updatedVisitor?.birthYear)

            val foundVisitor = it.admin().visitors().findVisitor(
                exhibitionId = exhibitionId,
                visitorId = createdVisitorId
            )

            assertJsonsEqual(updatedVisitor, foundVisitor)
            assertJsonsEqual(listOf(MqttVisitorUpdate(exhibitionId = exhibitionId, id = createdVisitor.id!!)), mqttSubscription.getMessages(1))
        }
    }

    @Test
    fun testDeleteVisitor() {
        ApiTestBuilder().use {
            val mqttSubscription = it.mqtt().subscribe(MqttExhibitionVisitorSessionCreate::class.java,"visitors/delete")
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val createdVisitor = it.admin().visitors().create(exhibitionId)
            val createdVisitorId = createdVisitor.id!!

            assertNotNull(it.admin().visitors().findVisitor(exhibitionId = exhibitionId, visitorId = createdVisitorId))
            it.admin().visitors().delete(exhibitionId = exhibitionId, visitorId = createdVisitorId)
            it.admin().visitors().assertFindFail(404, exhibitionId = exhibitionId, visitorId = createdVisitorId)
            assertJsonsEqual(listOf(MqttVisitorDelete(exhibitionId = exhibitionId, id = createdVisitor.id!!)), mqttSubscription.getMessages(1))
        }
    }

}