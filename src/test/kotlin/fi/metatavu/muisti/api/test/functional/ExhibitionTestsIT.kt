package fi.metatavu.muisti.api.test.functional

import fi.metatavu.muisti.api.client.models.Exhibition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.util.*

/**
 * Test class for testing exhibitions API
 *
 * @author Antti Leppä
 */
class ExhibitionTestsIT: AbstractFunctionalTest() {

    @Test
    fun testCreateExhibition() {
        ApiTestBuilder().use {
            assertNotNull(it.admin().exhibitions().create())
            it.admin().exhibitions().assertCreateFail(400, "")
        }
   }

    @Test
    fun testFindExhibition() {
        ApiTestBuilder().use {
            val nonExistingExhibitionId = UUID.randomUUID()
            it.admin().exhibitions().assertFindFail(404, nonExistingExhibitionId)
            val createdExhibition = it.admin().exhibitions().create()
            val createdExhibitionId = createdExhibition.id!!
            it.admin().exhibitions().assertFindFail(404, nonExistingExhibitionId)
            assertNotNull(it.admin().exhibitions().findExhibition(createdExhibitionId))
        }
    }

    @Test
    fun testListExhibitions() {
        ApiTestBuilder().use {
            assertEquals(0, it.admin().exhibitions().listExhibitions().size)
            val createdExhibition = it.admin().exhibitions().create()
            val createdExhibitionId = createdExhibition.id!!
            val exhibitions = it.admin().exhibitions().listExhibitions()
            assertEquals(1, exhibitions.size)
            assertEquals(createdExhibitionId, exhibitions[0].id)
            it.admin().exhibitions().delete(createdExhibition)
            assertEquals(0, it.admin().exhibitions().listExhibitions().size)
        }
    }

    @Test
    fun testUpdateExhibition() {
        ApiTestBuilder().use {
            val createdExhibition = it.admin().exhibitions().create()
            val createdExhibitionId = createdExhibition.id!!

            val foundCreatedExhibition = it.admin().exhibitions().findExhibition(createdExhibitionId)
            assertEquals(createdExhibition.id, foundCreatedExhibition?.id)
            assertEquals(createdExhibition.name, foundCreatedExhibition?.name)

            val updateBody = Exhibition("new name", createdExhibition.id, createdExhibition.creatorId, createdExhibition.lastModifierId, createdExhibition.createdAt, createdExhibition.modifiedAt)

            val updatedExhibition = it.admin().exhibitions().updateExhibition(updateBody)
            assertEquals(updateBody.id, updatedExhibition?.id)
            assertEquals(updateBody.name, updatedExhibition?.name)

            val foundUpdatedExhibition = it.admin().exhibitions().findExhibition(createdExhibitionId)
            assertEquals(updateBody.id, foundUpdatedExhibition?.id)
            assertEquals(updateBody.name, foundUpdatedExhibition?.name)

            it.admin().exhibitions().assertUpdateFail(404, Exhibition("fail name", UUID.randomUUID(), createdExhibition.creatorId, createdExhibition.lastModifierId, createdExhibition.createdAt, createdExhibition.modifiedAt))
            it.admin().exhibitions().assertUpdateFail(400, Exhibition("", UUID.randomUUID(), createdExhibition.creatorId, createdExhibition.lastModifierId, createdExhibition.createdAt, createdExhibition.modifiedAt))
        }
    }

    @Test
    fun testDeleteExhibition() {
        ApiTestBuilder().use {
            val createdExhibition = it.admin().exhibitions().create()
            val createdExhibitionId = createdExhibition.id!!
            assertNotNull(it.admin().exhibitions().findExhibition(createdExhibitionId))
            it.admin().exhibitions().delete(createdExhibition)
            it.admin().exhibitions().assertFindFail(404, createdExhibitionId)
            it.admin().exhibitions().assertDeleteFail(404, UUID.randomUUID())
        }
    }

}