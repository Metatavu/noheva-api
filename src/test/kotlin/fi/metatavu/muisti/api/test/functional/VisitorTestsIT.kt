package fi.metatavu.muisti.api.test.functional

import fi.metatavu.muisti.api.client.models.*
import fi.metatavu.muisti.api.test.functional.resources.KeycloakResource
import fi.metatavu.muisti.api.test.functional.resources.MqttResource
import fi.metatavu.muisti.api.test.functional.resources.MysqlResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.util.*

/**
 * Test class for testing visitor API
 *
 * @author Antti Leppä
 */
@QuarkusTest
@QuarkusTestResource.List(
    QuarkusTestResource(MysqlResource::class),
    QuarkusTestResource(KeycloakResource::class),
    QuarkusTestResource(MqttResource::class)
)
@TestProfile(DefaultTestProfile::class)
class VisitorTestsIT: AbstractFunctionalTest() {

    @Test
    fun testCreateVisitor() {
        createTestBuilder().use {
            val mqttSubscription = it.mqtt.subscribe(MqttExhibitionVisitorSessionCreate::class.java,"visitors/create")
            val exhibition = it.admin.exhibitions.create()
            val exhibitionId = exhibition.id!!
            val createdVisitor = it.admin.visitors.create(exhibitionId, Visitor(
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
        createTestBuilder().use {
            val exhibition = it.admin.exhibitions.create()
            val exhibitionId = exhibition.id!!
            val createdVisitor = it.admin.visitors.create(exhibitionId)
            val visitorId = createdVisitor.id!!
            val foundVisitor = it.admin.visitors.findVisitor(exhibitionId, visitorId)
            assertJsonsEqual(createdVisitor, foundVisitor)

            it.admin.visitors.assertFindFail(404, exhibitionId, UUID.randomUUID())
            it.admin.visitors.assertFindFail(404, UUID.randomUUID(), UUID.randomUUID())
            it.admin.visitors.assertFindFail(404, UUID.randomUUID(), visitorId)
        }
    }

    @Test
    fun testFindVisitorTag() {
        createTestBuilder().use {
            val exhibition = it.admin.exhibitions.create()
            val exhibitionId = exhibition.id!!

            it.admin.visitors.create(exhibitionId, Visitor(
                    email = "test@example.com",
                    language = "fi",
                    tagId = "testtag"
            ))

            assertEquals("testtag", it.admin.visitors.findVisitorTag(exhibitionId, "testtag")?.tagId)
            it.admin.visitors.assertFindVisitorFail(expectedStatus = 404, exhibitionId = exhibitionId, tagId = "nottag")
        }
    }

    @Test
    fun testListVisitors() {
        createTestBuilder().use {
            val exhibition = it.admin.exhibitions.create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()

            it.admin.visitors.create(exhibitionId, Visitor(
                    email = "visitor1@example.com",
                    language = "fi",
                    tagId = "faketag1"
            ))

            it.admin.visitors.create(exhibitionId, Visitor(
                    email = "visitor2@example.com",
                    language = "fi",
                    tagId = "faketag2"
            ))

            val visitors1 = it.admin.visitors.listVisitors(
                exhibitionId = exhibitionId,
                tagId = null,
                email = null
            )
            assertEquals(2, visitors1.size)
            assertNotNull(visitors1.firstOrNull { visitor ->  visitor.tagId == "faketag1" })
            assertNotNull(visitors1.firstOrNull { visitor ->  visitor.tagId == "faketag2" })

            val visitors2 = it.admin.visitors.listVisitors(
                exhibitionId = exhibitionId,
                tagId = "faketag2",
                email = null
            )
            assertEquals(1, visitors2.size)
            assertNotNull(visitors2.firstOrNull { visitor ->  visitor.tagId == "faketag2" })

            val visitors3 = it.admin.visitors.listVisitors(
                exhibitionId = exhibitionId,
                tagId = null,
                email = "visitor1@example.com"
            )
            assertEquals(1, visitors3.size)
            assertEquals("visitor1@example.com", visitors3.first().email)

            it.admin.visitors.assertCount(
                expected = 0,
                exhibitionId = exhibitionId,
                tagId = "noexistingtag",
                email = null
            )

            it.admin.visitors.assertListFail(
                expectedStatus = 404,
                exhibitionId = nonExistingExhibitionId,
                tagId = null,
                email = null
            )
        }
    }

    @Test
    fun testUpdateVisitor() {
        createTestBuilder().use {
            val mqttSubscription = it.mqtt.subscribe(MqttExhibitionVisitorSessionCreate::class.java,"visitors/update")
            val exhibition = it.admin.exhibitions.create()
            val exhibitionId = exhibition.id!!

            val createdVisitor = it.admin.visitors.create(exhibitionId, Visitor(
                email = "visitor@example.com",
                language = "fi",
                firstName = "First",
                lastName = "Last",
                phone = "+358 01 234 5678",
                birthYear = 1980,
                tagId = "faketag"
            ))

            assertEquals("faketag", createdVisitor.tagId)
            assertEquals("visitor@example.com", createdVisitor.email)
            assertEquals("First", createdVisitor.firstName)
            assertEquals("Last", createdVisitor.lastName)
            assertEquals("+358 01 234 5678", createdVisitor.phone)
            assertEquals(1980, createdVisitor.birthYear)

            val updatedVisitor = it.admin.visitors.updateVisitor(exhibitionId, Visitor(
                id = createdVisitor.id,
                language = "fi",
                email = "visitor@example.com",
                tagId = "updatetag",
                firstName = "First name",
                lastName = "Last name",
                phone = "+358 12 345 6789",
                birthYear = 1985
            ))

            assertEquals("updatetag", updatedVisitor?.tagId)
            assertEquals("First name", updatedVisitor?.firstName)
            assertEquals("Last name", updatedVisitor?.lastName)
            assertEquals("+358 12 345 6789", updatedVisitor?.phone)
            assertEquals(1985, updatedVisitor?.birthYear)

            assertJsonsEqual(listOf(MqttVisitorUpdate(exhibitionId = exhibitionId, id = createdVisitor.id!!)), mqttSubscription.getMessages(1))
        }
    }

    @Test
    fun testDeleteVisitor() {
        createTestBuilder().use {
            val mqttSubscription = it.mqtt.subscribe(MqttExhibitionVisitorSessionCreate::class.java,"visitors/delete")
            val exhibition = it.admin.exhibitions.create()
            val exhibitionId = exhibition.id!!
            val createdVisitor = it.admin.visitors.create(exhibitionId)
            val createdVisitorId = createdVisitor.id!!

            assertNotNull(it.admin.visitors.findVisitor(exhibitionId = exhibitionId, visitorId = createdVisitorId))
            it.admin.visitors.delete(exhibitionId = exhibitionId, visitorId = createdVisitorId)
            it.admin.visitors.assertFindFail(404, exhibitionId = exhibitionId, visitorId = createdVisitorId)
            assertJsonsEqual(listOf(MqttVisitorDelete(exhibitionId = exhibitionId, id = createdVisitor.id)), mqttSubscription.getMessages(1))
        }
    }

}