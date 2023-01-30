package fi.metatavu.muisti.api.test.functional

import fi.metatavu.muisti.api.client.models.ContentVersion
import fi.metatavu.muisti.api.client.models.ContentVersionActiveCondition
import fi.metatavu.muisti.api.test.functional.resources.KeycloakResource
import fi.metatavu.muisti.api.test.functional.resources.MqttResource
import fi.metatavu.muisti.api.test.functional.resources.MysqlResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import org.junit.Assert.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.util.*

/**
 * Test class for testing content versions API
 *
 * @author Antti Leppä
 * @author Jari Nykänen
 */
@QuarkusTest
@QuarkusTestResource.List(
    QuarkusTestResource(MysqlResource::class),
    QuarkusTestResource(KeycloakResource::class),
    QuarkusTestResource(MqttResource::class)
)
class ContentVersionTestsIT: AbstractFunctionalTest() {

    @Test
    fun testCreateContentVersion() {
        createTestBuilder().use {
            val exhibitionId = it.admin().exhibitions.create().id!!
            val floorId = it.admin().exhibitionFloors.create(exhibitionId).id!!
            val roomId = it.admin().exhibitionRooms.create(exhibitionId, floorId).id!!
            val contentVersionToCreate = ContentVersion(name = "created name", language = "FI", rooms = arrayOf(roomId))
            val createdContentVersion = it.admin().contentVersions.create(exhibitionId, contentVersionToCreate)
            assertNotNull(createdContentVersion)
        }
    }

    @Test
    fun testUpdateContentVersion() {
        createTestBuilder().use {
            val exhibitionId = it.admin().exhibitions.create().id!!
            val floorId = it.admin().exhibitionFloors.create(exhibitionId).id!!
            val roomId = it.admin().exhibitionRooms.create(exhibitionId, floorId).id!!
            val createdContentVersion = it.admin().contentVersions.create(
                exhibitionId = exhibitionId,
                payload = ContentVersion(
                    name = "created name",
                    language = "FI",
                    rooms = arrayOf(roomId)
                )
            )

            assertNotNull(createdContentVersion)
            Assertions.assertNull(createdContentVersion.activeCondition)

            val contentVersionToUpdate = ContentVersion(
                id = createdContentVersion.id!!,
                name = "Updated name",
                language = "EN",
                rooms = createdContentVersion.rooms,
                activeCondition = ContentVersionActiveCondition(
                    userVariable = "user variable",
                    equals = "value"
                )
            )
            val updatedContentVersion = it.admin().contentVersions.updateContentVersion(exhibitionId, contentVersionToUpdate)

            assertNotNull(updatedContentVersion)
            assertEquals(createdContentVersion.id, updatedContentVersion.id!!)
            assertEquals(updatedContentVersion.language, "EN")
            assertEquals(updatedContentVersion.name, "Updated name")
            assertEquals("user variable", updatedContentVersion.activeCondition?.userVariable)
            assertEquals("value", updatedContentVersion.activeCondition?.equals)
        }
    }

    @Test
    fun testFindContentVersion() {
        createTestBuilder().use {
            val exhibition = it.admin().exhibitions.create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()
            val nonExistingContentVersionId = UUID.randomUUID()
            val createdContentVersion = it.admin().contentVersions.create(exhibitionId = exhibitionId)
            val createdContentVersionId = createdContentVersion.id!!

            it.admin().contentVersions.assertFindFail(404, exhibitionId, nonExistingContentVersionId)
            it.admin().contentVersions.assertFindFail(404, nonExistingExhibitionId, nonExistingContentVersionId)
            it.admin().contentVersions.assertFindFail(404, nonExistingExhibitionId, createdContentVersionId)
            assertNotNull(it.admin().contentVersions.findContentVersion(exhibitionId, createdContentVersionId))
        }
    }

    @Test
    fun testListContentVersions() {
        createTestBuilder().use {
            val exhibitionId = it.admin().exhibitions.create().id!!
            val floorId = it.admin().exhibitionFloors.create(exhibitionId).id!!
            val roomId = it.admin().exhibitionRooms.create(exhibitionId, floorId).id!!
            val nonExistingExhibitionId = UUID.randomUUID()

            it.admin().contentVersions.assertListFail(404, nonExistingExhibitionId, null)
            assertEquals(0, it.admin().contentVersions.listContentVersions(exhibitionId, null).size)

            val createdContentVersionId = it.admin().contentVersions.create(exhibitionId = exhibitionId).id!!
            val contentVersions = it.admin().contentVersions.listContentVersions(exhibitionId, null)
            assertEquals(1, contentVersions.size)
            assertEquals(createdContentVersionId, contentVersions[0].id)
            it.admin().contentVersions.delete(exhibitionId, createdContentVersionId)
            assertEquals(0, it.admin().contentVersions.listContentVersions(exhibitionId, null).size)

            it.admin().contentVersions.create(exhibitionId, ContentVersion(name = "created name", language = "FI", rooms = arrayOf(roomId)))
            it.admin().contentVersions.create(exhibitionId, ContentVersion(name = "another name", language = "FI", rooms = arrayOf(roomId)))
            it.admin().contentVersions.create(exhibitionId = exhibitionId)

            val contentVersionsWithoutRoomId = it.admin().contentVersions.listContentVersions(exhibitionId, null)
            assertEquals(3, contentVersionsWithoutRoomId.size)

            val contentVersionsWithRoomId = it.admin().contentVersions.listContentVersions(exhibitionId, roomId)
            assertEquals(2, contentVersionsWithRoomId.size)
        }
    }
}