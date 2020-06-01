package fi.metatavu.muisti.api.test.functional

import fi.metatavu.muisti.api.client.models.Visitor
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
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val createdVisitor = it.admin().visitors().create(exhibitionId, Visitor(
                email = "visitor@example.com",
                tagId = "faketag"
            ))

            assertNotNull(createdVisitor)
            assertNotNull(createdVisitor.id)
            assertEquals("faketag", createdVisitor.tagId)
            assertEquals("visitor@example.com", createdVisitor.email)
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
    fun testListVisitors() {
        ApiTestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()

            it.admin().visitors().create(exhibitionId, Visitor(
                    email = "visitor1@example.com",
                    tagId = "faketag1"
            ))

            it.admin().visitors().create(exhibitionId, Visitor(
                    email = "visitor2@example.com",
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
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!

            val createdVisitor = it.admin().visitors().create(exhibitionId, Visitor(
                email = "visitor@example.com",
                tagId = "faketag"
            ))

            assertEquals("faketag", createdVisitor.tagId)
            assertEquals("visitor@example.com", createdVisitor.email)

            val updatedVisitor = it.admin().visitors().updateVisitor(exhibitionId, Visitor(
                    id = createdVisitor.id,
                    email = "visitor@example.com",
                    tagId = "updatetag"
            ))

            assertEquals("updatetag", updatedVisitor?.tagId)
        }
    }

    @Test
    fun testDeleteVisitor() {
        ApiTestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val createdVisitor = it.admin().visitors().create(exhibitionId)
            val createdVisitorId = createdVisitor.id!!

            assertNotNull(it.admin().visitors().findVisitor(exhibitionId = exhibitionId, visitorId = createdVisitorId))
            it.admin().visitors().delete(exhibitionId = exhibitionId, visitorId = createdVisitorId)
            it.admin().visitors().assertFindFail(404, exhibitionId = exhibitionId, visitorId = createdVisitorId)
        }
    }

}