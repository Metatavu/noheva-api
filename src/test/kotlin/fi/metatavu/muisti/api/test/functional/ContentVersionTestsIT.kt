package fi.metatavu.muisti.api.test.functional

import fi.metatavu.muisti.api.client.models.ExhibitionContentVersion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.util.*

/**
 * Test class for testing exhibition content versions API
 *
 * @author Antti Leppä
 */
class ContentVersionTestsIT: AbstractFunctionalTest() {

    @Test
    fun testCreateExhibitionContentVersion() {
        ApiTestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val createdExhibitionContentVersion = it.admin().exhibitionContentVersions().create(exhibition.id!!, ExhibitionContentVersion(name = "created name"))
            assertNotNull(createdExhibitionContentVersion)
            it.admin().exhibitions().assertCreateFail(400, "")
        }
   }

    @Test
    fun testFindExhibitionContentVersion() {
        ApiTestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()
            val nonExistingExhibitionContentVersionId = UUID.randomUUID()
            val createdExhibitionContentVersion = it.admin().exhibitionContentVersions().create(exhibitionId)
            val createdExhibitionContentVersionId = createdExhibitionContentVersion.id!!

            it.admin().exhibitionContentVersions().assertFindFail(404, exhibitionId, nonExistingExhibitionContentVersionId)
            it.admin().exhibitionContentVersions().assertFindFail(404, nonExistingExhibitionId, nonExistingExhibitionContentVersionId)
            it.admin().exhibitionContentVersions().assertFindFail(404, nonExistingExhibitionId, createdExhibitionContentVersionId)
            assertNotNull(it.admin().exhibitionContentVersions().findExhibitionContentVersion(exhibitionId, createdExhibitionContentVersionId))
        }
    }

    @Test
    fun testListExhibitionContentVersions() {
        ApiTestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()

            it.admin().exhibitionContentVersions().assertListFail(404, nonExistingExhibitionId)
            assertEquals(0, it.admin().exhibitionContentVersions().listExhibitionContentVersions(exhibitionId).size)

            val createdExhibitionContentVersion = it.admin().exhibitionContentVersions().create(exhibitionId)
            val createdExhibitionContentVersionId = createdExhibitionContentVersion.id!!
            val exhibitionContentVersions = it.admin().exhibitionContentVersions().listExhibitionContentVersions(exhibitionId)
            assertEquals(1, exhibitionContentVersions.size)
            assertEquals(createdExhibitionContentVersionId, exhibitionContentVersions[0].id)
            it.admin().exhibitionContentVersions().delete(exhibitionId, createdExhibitionContentVersionId)
            assertEquals(0, it.admin().exhibitionContentVersions().listExhibitionContentVersions(exhibitionId).size)
        }
    }

    @Test
    fun testUpdateExhibition() {
        ApiTestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()

            val createdExhibitionContentVersion = it.admin().exhibitionContentVersions().create(exhibitionId, ExhibitionContentVersion(name = "created name"))
            val createdExhibitionContentVersionId = createdExhibitionContentVersion.id!!

            val foundCreatedExhibitionContentVersion = it.admin().exhibitionContentVersions().findExhibitionContentVersion(exhibitionId, createdExhibitionContentVersionId)
            assertEquals(createdExhibitionContentVersion.id, foundCreatedExhibitionContentVersion?.id)
            assertEquals("created name", createdExhibitionContentVersion.name)

            val updatedExhibitionContentVersion = it.admin().exhibitionContentVersions().updateExhibitionContentVersion(exhibitionId, ExhibitionContentVersion("updated name", createdExhibitionContentVersionId))
            val foundUpdatedExhibitionContentVersion = it.admin().exhibitionContentVersions().findExhibitionContentVersion(exhibitionId, createdExhibitionContentVersionId)

            assertEquals(updatedExhibitionContentVersion!!.id, foundUpdatedExhibitionContentVersion?.id)
            assertEquals("updated name", updatedExhibitionContentVersion.name)

            it.admin().exhibitionContentVersions().assertUpdateFail(404, nonExistingExhibitionId, ExhibitionContentVersion("name", createdExhibitionContentVersionId))
        }


    }

    @Test
    fun testDeleteExhibition() {
        ApiTestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()
            val nonExistingSessionVariableId = UUID.randomUUID()
            val createdExhibitionContentVersion = it.admin().exhibitionContentVersions().create(exhibitionId)
            val createdExhibitionContentVersionId = createdExhibitionContentVersion.id!!

            assertNotNull(it.admin().exhibitionContentVersions().findExhibitionContentVersion(exhibitionId, createdExhibitionContentVersionId))
            it.admin().exhibitionContentVersions().assertDeleteFail(404, exhibitionId, nonExistingSessionVariableId)
            it.admin().exhibitionContentVersions().assertDeleteFail(404, nonExistingExhibitionId, createdExhibitionContentVersionId)
            it.admin().exhibitionContentVersions().assertDeleteFail(404, nonExistingExhibitionId, nonExistingSessionVariableId)

            it.admin().exhibitionContentVersions().delete(exhibitionId, createdExhibitionContentVersion)

            it.admin().exhibitionContentVersions().assertDeleteFail(404, exhibitionId, createdExhibitionContentVersionId)
        }
    }

}