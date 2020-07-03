package fi.metatavu.muisti.api.test.functional

import fi.metatavu.muisti.api.client.models.ContentVersion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.util.*

/**
 * Test class for testing content versions API
 *
 * @author Antti Leppä
 * @author Jari Nykänen
 */
class ContentVersionTestsIT: AbstractFunctionalTest() {

    @Test
    fun testCreateContentVersion() {
        ApiTestBuilder().use {
            val exhibitionId = it.admin().exhibitions().create().id!!
            val floorId = it.admin().exhibitionFloors().create(exhibitionId).id!!
            val roomId = it.admin().exhibitionRooms().create(exhibitionId, floorId).id!!
            val contentVersionToCreate = ContentVersion(name = "created name", language = "FI", rooms = arrayOf<UUID>(roomId))
            val createdContentVersion = it.admin().contentVersions().create(exhibitionId, contentVersionToCreate)
            assertNotNull(createdContentVersion)
            it.admin().exhibitions().assertCreateFail(400, "")
        }
    }

    @Test
    fun testUpdateContentVersion() {
        ApiTestBuilder().use {
            val exhibitionId = it.admin().exhibitions().create().id!!
            val floorId = it.admin().exhibitionFloors().create(exhibitionId).id!!
            val roomId = it.admin().exhibitionRooms().create(exhibitionId, floorId).id!!
            val createdContentVersion = it.admin().contentVersions().create(exhibitionId, ContentVersion(name = "created name", language = "FI", rooms = arrayOf<UUID>(roomId)))
            assertNotNull(createdContentVersion)

            val contentVersionToUpdate = ContentVersion(
                id = createdContentVersion.id!!,
                name = "Updated name",
                language = "EN",
                rooms = createdContentVersion.rooms
            )
            val updatedContentVersion = it.admin().contentVersions().updateContentVersion(exhibitionId, contentVersionToUpdate)

            assertNotNull(updatedContentVersion)
            assertEquals(createdContentVersion.id!!, updatedContentVersion?.id!!)
            assertEquals(updatedContentVersion.language, "EN")
            assertEquals(updatedContentVersion.name, "Updated name")
        }
    }

    @Test
    fun testFindContentVersion() {
        ApiTestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()
            val nonExistingContentVersionId = UUID.randomUUID()
            val createdContentVersion = it.admin().contentVersions().create(exhibitionId)
            val createdContentVersionId = createdContentVersion.id!!

            it.admin().contentVersions().assertFindFail(404, exhibitionId, nonExistingContentVersionId)
            it.admin().contentVersions().assertFindFail(404, nonExistingExhibitionId, nonExistingContentVersionId)
            it.admin().contentVersions().assertFindFail(404, nonExistingExhibitionId, createdContentVersionId)
            assertNotNull(it.admin().contentVersions().findContentVersion(exhibitionId, createdContentVersionId))
        }
    }

    @Test
    fun testListContentVersions() {
        ApiTestBuilder().use {
            val exhibitionId = it.admin().exhibitions().create().id!!
            val floorId = it.admin().exhibitionFloors().create(exhibitionId).id!!
            val roomId = it.admin().exhibitionRooms().create(exhibitionId, floorId).id!!
            val nonExistingExhibitionId = UUID.randomUUID()

            it.admin().contentVersions().assertListFail(404, nonExistingExhibitionId, null)
            assertEquals(0, it.admin().contentVersions().listContentVersions(exhibitionId, null).size)

            val createdContentVersionId = it.admin().contentVersions().create(exhibitionId).id!!
            val contentVersions = it.admin().contentVersions().listContentVersions(exhibitionId, null)
            assertEquals(1, contentVersions.size)
            assertEquals(createdContentVersionId, contentVersions[0].id)
            it.admin().contentVersions().delete(exhibitionId, createdContentVersionId)
            assertEquals(0, it.admin().contentVersions().listContentVersions(exhibitionId, null).size)

            it.admin().contentVersions().create(exhibitionId, ContentVersion(name = "created name", language = "FI", rooms = arrayOf<UUID>(roomId)))
            it.admin().contentVersions().create(exhibitionId, ContentVersion(name = "another name", language = "FI", rooms = arrayOf<UUID>(roomId)))
            it.admin().contentVersions().create(exhibitionId)

            val contentVersionsWithoutRoomId = it.admin().contentVersions().listContentVersions(exhibitionId, null)
            assertEquals(3, contentVersionsWithoutRoomId.size)

            val contentVersionsWithRoomId = it.admin().contentVersions().listContentVersions(exhibitionId, roomId)
            assertEquals(2, contentVersionsWithRoomId.size)
        }
    }
}