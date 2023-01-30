package fi.metatavu.muisti.api.test.functional

import fi.metatavu.muisti.api.client.models.ContentVersion
import fi.metatavu.muisti.api.client.models.ExhibitionRoom
import fi.metatavu.muisti.api.client.models.Polygon
import fi.metatavu.muisti.api.test.functional.resources.KeycloakResource
import fi.metatavu.muisti.api.test.functional.resources.MqttResource
import fi.metatavu.muisti.api.test.functional.resources.MysqlResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import java.util.*

/**
 * Test class for testing exhibition rooms API
 *
 * @author Antti Leppä
 */
@QuarkusTest
@QuarkusTestResource.List(
    QuarkusTestResource(MysqlResource::class),
    QuarkusTestResource(KeycloakResource::class),
    QuarkusTestResource(MqttResource::class)
)
class ExhibitionRoomTestsIT: AbstractFunctionalTest() {

    @Test
    fun testCreateExhibitionRoom() {
        createTestBuilder().use {
            val exhibition = it.admin().exhibitions.create()
            val floor = it.admin().exhibitionFloors.create(exhibition.id!!)
            val floorId = floor.id!!
            val createdExhibitionRoom = it.admin().exhibitionRooms.create(
                exhibition.id, ExhibitionRoom(
                name = "name",
                floorId = floorId
            ))

            assertNotNull(createdExhibitionRoom)
        }
   }

    @Test
    fun testFindExhibitionRoom() {
        createTestBuilder().use {
            val exhibition = it.admin().exhibitions.create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()
            val nonExistingExhibitionRoomId = UUID.randomUUID()
            val floor = it.admin().exhibitionFloors.create(exhibition.id)
            val floorId = floor.id!!

            val createdExhibitionRoom = it.admin().exhibitionRooms.create(exhibitionId = exhibitionId, floorId = floorId)
            val createdExhibitionRoomId = createdExhibitionRoom.id!!

            it.admin().exhibitionRooms.assertFindFail(404, exhibitionId, nonExistingExhibitionRoomId)
            it.admin().exhibitionRooms.assertFindFail(404, nonExistingExhibitionId, nonExistingExhibitionRoomId)
            it.admin().exhibitionRooms.assertFindFail(404, nonExistingExhibitionId, createdExhibitionRoomId)
            assertNotNull(it.admin().exhibitionRooms.findExhibitionRoom(exhibitionId, createdExhibitionRoomId))
        }
    }

    @Test
    fun testListExhibitionRooms() {
        createTestBuilder().use {
            val exhibition = it.admin().exhibitions.create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()
            val floor1 = it.admin().exhibitionFloors.create(exhibition.id)
            val floor1Id = floor1.id!!

            val floor2 = it.admin().exhibitionFloors.create(exhibition.id)
            val floor2Id = floor2.id!!

            it.admin().exhibitionRooms.assertListFail(expectedStatus = 404, exhibitionId = nonExistingExhibitionId, floorId = null)
            assertEquals(0, it.admin().exhibitionRooms.listExhibitionRooms(exhibitionId = exhibitionId, floorId = null).size)

            val createdExhibitionRoom1 = it.admin().exhibitionRooms.create(exhibitionId = exhibitionId, floorId = floor1Id)
            it.admin().exhibitionRooms.create(exhibitionId = exhibitionId, floorId = floor2Id)
            it.admin().exhibitionRooms.create(exhibitionId = exhibitionId, floorId = floor2Id)

            it.admin().exhibitionRooms.assertCount(1, exhibitionId = exhibitionId, floorId = floor1Id)
            it.admin().exhibitionRooms.assertCount(2, exhibitionId = exhibitionId, floorId = floor2Id)
            it.admin().exhibitionRooms.assertCount(3, exhibitionId = exhibitionId, floorId = null)

            val createdExhibitionRoomId1 = createdExhibitionRoom1.id!!

            val exhibitionRooms = it.admin().exhibitionRooms.listExhibitionRooms(exhibitionId = exhibitionId, floorId = floor1Id)
            assertEquals(1, exhibitionRooms.size)

            assertEquals(createdExhibitionRoomId1, exhibitionRooms[0].id)
            it.admin().exhibitionRooms.delete(exhibitionId, createdExhibitionRoomId1)
            assertEquals(0, it.admin().exhibitionRooms.listExhibitionRooms(exhibitionId = exhibitionId, floorId = floor1Id).size)
        }
    }

    @Test
    fun testUpdateExhibitionRoom() {
        createTestBuilder().use {
            val exhibition = it.admin().exhibitions.create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()
            val floor = it.admin().exhibitionFloors.create(exhibitionId)
            val floorId = floor.id!!

            val createdExhibitionRoom = it.admin().exhibitionRooms.create(exhibitionId, ExhibitionRoom(
                name = "created name",
                color = "#00ff00",
                floorId = floorId
            ))

            val createdExhibitionRoomId = createdExhibitionRoom.id!!

            val foundCreatedExhibitionRoom = it.admin().exhibitionRooms.findExhibitionRoom(exhibitionId, createdExhibitionRoomId)
            assertEquals(createdExhibitionRoom.id, foundCreatedExhibitionRoom.id)
            assertEquals("created name", createdExhibitionRoom.name)
            assertEquals("#00ff00", createdExhibitionRoom.color)

            val point1 = arrayOf(30.0,10.0)
            val point2 = arrayOf(40.0,40.0)
            val point3 = arrayOf(20.0,40.0)
            val point4 = arrayOf(10.0,20.0)
            val point5 = arrayOf(30.0,10.0)
            val coordinatePointsArray = arrayOf(point1, point2, point3, point4, point5)
            val polygonCoordinateList = arrayOf(coordinatePointsArray)
            val testPolygon = Polygon(coordinates = polygonCoordinateList, type = "Polygon")

            val updatedExhibitionRoom = it.admin().exhibitionRooms.updateExhibitionRoom(exhibitionId, ExhibitionRoom(
                name = "updated name",
                color = "#ff0000",
                geoShape = testPolygon,
                id = createdExhibitionRoomId,
                floorId = floorId
            ))

            val foundUpdatedExhibitionRoom = it.admin().exhibitionRooms.findExhibitionRoom(exhibitionId, createdExhibitionRoomId)

            assertEquals(updatedExhibitionRoom.id, foundUpdatedExhibitionRoom.id)
            assertEquals("updated name", foundUpdatedExhibitionRoom.name)
            assertEquals("#ff0000", foundUpdatedExhibitionRoom.color)

            val firstShape = foundUpdatedExhibitionRoom.geoShape?.coordinates?.get(0)
            val firstPoint = firstShape?.get(0)
            val secondPoint = firstShape?.get(1)
            val thirdPoint = firstShape?.get(2)
            val fourthPoint = firstShape?.get(3)
            val fifthPoint = firstShape?.get(4)
            assertArrayEquals(point1, firstPoint)
            assertArrayEquals(point2, secondPoint)
            assertArrayEquals(point3, thirdPoint)
            assertArrayEquals(point4, fourthPoint)
            assertArrayEquals(point5, fifthPoint)
            assertEquals(testPolygon.type, foundUpdatedExhibitionRoom.geoShape?.type)

            it.admin().exhibitionRooms.assertUpdateFail(404, nonExistingExhibitionId, ExhibitionRoom(
                name = "name",
                id = createdExhibitionRoomId,
                floorId = floorId
            ))
        }
    }

    @Test
    fun testDeleteExhibitionRoom() {
        createTestBuilder().use {
            val exhibition = it.admin().exhibitions.create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()
            val nonExistingSessionVariableId = UUID.randomUUID()
            val floor = it.admin().exhibitionFloors.create(exhibition.id)
            val floorId = floor.id!!
            val createdExhibitionRoom = it.admin().exhibitionRooms.create(exhibitionId = exhibitionId, floorId = floorId)
            val createdExhibitionRoomId = createdExhibitionRoom.id!!

            val contentVersionToCreate = ContentVersion(
                name = "created name",
                language = "FI",
                rooms = arrayOf(createdExhibitionRoomId)
            )
            val createdContentVersion = it.admin().contentVersions.create(exhibitionId, contentVersionToCreate)

            assertNotNull(it.admin().exhibitionRooms.findExhibitionRoom(exhibitionId, createdExhibitionRoomId))
            it.admin().exhibitionRooms.assertDeleteFail(404, exhibitionId, nonExistingSessionVariableId)
            it.admin().exhibitionRooms.assertDeleteFail(404, nonExistingExhibitionId, createdExhibitionRoomId)
            it.admin().exhibitionRooms.assertDeleteFail(404, nonExistingExhibitionId, nonExistingSessionVariableId)
            it.admin().exhibitionRooms.assertDeleteFail(400, exhibitionId, createdExhibitionRoomId)

            it.admin().contentVersions.delete(exhibitionId = exhibitionId, contentVersion = createdContentVersion)

            it.admin().exhibitionRooms.delete(exhibitionId, createdExhibitionRoom)

            it.admin().exhibitionRooms.assertDeleteFail(404, exhibitionId, createdExhibitionRoomId)
        }
    }

}