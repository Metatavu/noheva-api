package fi.metatavu.muisti.api.test.functional

import com.fasterxml.jackson.databind.ObjectMapper
import fi.metatavu.muisti.api.client.models.ExhibitionRoom
import fi.metatavu.muisti.api.client.models.Polygon
import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal
import java.util.*

/**
 * Test class for testing exhibition rooms API
 *
 * @author Antti Leppä
 */
class ExhibitionRoomTestsIT: AbstractFunctionalTest() {

    @Test
    fun testCreateExhibitionRoom() {
        ApiTestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val floor = it.admin().exhibitionFloors().create(exhibition.id!!)
            val floorId = floor.id!!
            val createdExhibitionRoom = it.admin().exhibitionRooms().create(exhibition.id!!, ExhibitionRoom(
                name = "name",
                floorId = floorId
            ))

            assertNotNull(createdExhibitionRoom)
            it.admin().exhibitions().assertCreateFail(400, "")
        }
   }

    @Test
    fun testFindExhibitionRoom() {
        ApiTestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()
            val nonExistingExhibitionRoomId = UUID.randomUUID()
            val floor = it.admin().exhibitionFloors().create(exhibition.id!!)
            val floorId = floor.id!!

            val createdExhibitionRoom = it.admin().exhibitionRooms().create(exhibitionId = exhibitionId, floorId = floorId)
            val createdExhibitionRoomId = createdExhibitionRoom.id!!

            it.admin().exhibitionRooms().assertFindFail(404, exhibitionId, nonExistingExhibitionRoomId)
            it.admin().exhibitionRooms().assertFindFail(404, nonExistingExhibitionId, nonExistingExhibitionRoomId)
            it.admin().exhibitionRooms().assertFindFail(404, nonExistingExhibitionId, createdExhibitionRoomId)
            assertNotNull(it.admin().exhibitionRooms().findExhibitionRoom(exhibitionId, createdExhibitionRoomId))
        }
    }

    @Test
    fun testListExhibitionRooms() {
        ApiTestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()
            val floor1 = it.admin().exhibitionFloors().create(exhibition.id!!)
            val floor1Id = floor1.id!!

            val floor2 = it.admin().exhibitionFloors().create(exhibition.id!!)
            val floor2Id = floor2.id!!

            it.admin().exhibitionRooms().assertListFail(expectedStatus = 404, exhibitionId = nonExistingExhibitionId, floorId = null)
            assertEquals(0, it.admin().exhibitionRooms().listExhibitionRooms(exhibitionId = exhibitionId, floorId = null).size)

            val createdExhibitionRoom1 = it.admin().exhibitionRooms().create(exhibitionId = exhibitionId, floorId = floor1Id)
            it.admin().exhibitionRooms().create(exhibitionId = exhibitionId, floorId = floor2Id)
            it.admin().exhibitionRooms().create(exhibitionId = exhibitionId, floorId = floor2Id)

            it.admin().exhibitionRooms().assertCount(1, exhibitionId = exhibitionId, floorId = floor1Id)
            it.admin().exhibitionRooms().assertCount(2, exhibitionId = exhibitionId, floorId = floor2Id)
            it.admin().exhibitionRooms().assertCount(3, exhibitionId = exhibitionId, floorId = null)

            val createdExhibitionRoomId1 = createdExhibitionRoom1.id!!

            val exhibitionRooms = it.admin().exhibitionRooms().listExhibitionRooms(exhibitionId = exhibitionId, floorId = floor1Id)
            assertEquals(1, exhibitionRooms.size)

            assertEquals(createdExhibitionRoomId1, exhibitionRooms[0].id)
            it.admin().exhibitionRooms().delete(exhibitionId, createdExhibitionRoomId1)
            assertEquals(0, it.admin().exhibitionRooms().listExhibitionRooms(exhibitionId = exhibitionId, floorId = floor1Id).size)
        }
    }

    @Test
    fun testUpdateExhibition() {
        ApiTestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()
            val floor = it.admin().exhibitionFloors().create(exhibitionId)
            val floorId = floor.id!!

            val createdExhibitionRoom = it.admin().exhibitionRooms().create(exhibitionId, ExhibitionRoom(
                name = "created name",
                floorId = floorId
            ))

            val createdExhibitionRoomId = createdExhibitionRoom.id!!

            val foundCreatedExhibitionRoom = it.admin().exhibitionRooms().findExhibitionRoom(exhibitionId, createdExhibitionRoomId)
            assertEquals(createdExhibitionRoom.id, foundCreatedExhibitionRoom?.id)
            assertEquals("created name", createdExhibitionRoom.name)

            val point1 = arrayOf<Double>(30.0,10.0)
            val point2 = arrayOf<Double>(40.0,40.0)
            val point3 = arrayOf<Double>(20.0,40.0)
            val point4 = arrayOf<Double>(10.0,20.0)
            val point5 = arrayOf<Double>(30.0,10.0)
            val coordinatePointsArray = arrayOf<Array<Double>>(point1, point2, point3, point4, point5)
            val polygonCoordinateList = arrayOf<Array<Array<Double>>>(coordinatePointsArray)
            val testPolygon = Polygon(shapes = polygonCoordinateList)

            val updatedExhibitionRoom = it.admin().exhibitionRooms().updateExhibitionRoom(exhibitionId, ExhibitionRoom(
                name = "updated name",
                geoShape = testPolygon,
                id = createdExhibitionRoomId,
                floorId = floorId
            ))

            val foundUpdatedExhibitionRoom = it.admin().exhibitionRooms().findExhibitionRoom(exhibitionId, createdExhibitionRoomId)

            assertEquals(updatedExhibitionRoom!!.id, foundUpdatedExhibitionRoom?.id)
            assertEquals("updated name", foundUpdatedExhibitionRoom?.name)

            val firstShape = foundUpdatedExhibitionRoom?.geoShape?.shapes?.get(0)
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

            it.admin().exhibitionRooms().assertUpdateFail(404, nonExistingExhibitionId, ExhibitionRoom(
                name = "name",
                id = createdExhibitionRoomId,
                floorId = floorId
            ))
        }
    }

    @Test
    fun testDeleteExhibition() {
        ApiTestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()
            val nonExistingSessionVariableId = UUID.randomUUID()
            val floor = it.admin().exhibitionFloors().create(exhibition.id!!)
            val floorId = floor.id!!
            val createdExhibitionRoom = it.admin().exhibitionRooms().create(exhibitionId = exhibitionId, floorId = floorId)
            val createdExhibitionRoomId = createdExhibitionRoom.id!!

            assertNotNull(it.admin().exhibitionRooms().findExhibitionRoom(exhibitionId, createdExhibitionRoomId))
            it.admin().exhibitionRooms().assertDeleteFail(404, exhibitionId, nonExistingSessionVariableId)
            it.admin().exhibitionRooms().assertDeleteFail(404, nonExistingExhibitionId, createdExhibitionRoomId)
            it.admin().exhibitionRooms().assertDeleteFail(404, nonExistingExhibitionId, nonExistingSessionVariableId)

            it.admin().exhibitionRooms().delete(exhibitionId, createdExhibitionRoom)

            it.admin().exhibitionRooms().assertDeleteFail(404, exhibitionId, createdExhibitionRoomId)
        }
    }

}