package fi.metatavu.muisti.api.test.functional

import fi.metatavu.muisti.api.client.models.ExhibitionRoom
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.util.*

/**
 * Test class for testing exhibition rooms API
 *
 * @author Antti Leppä
 */
class ExhibitionRoomTestsIT: AbstractFunctionalTest() {

    @Test
    fun testCreateExhibitionRoomn() {
        TestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val createdExhibitionRoom = it.admin().exhibitionRooms().create(exhibition.id!!, "name")
            assertNotNull(createdExhibitionRoom)
            it.admin().exhibitions().assertCreateFail(400, "")
        }
   }

    @Test
    fun testFindExhibitionRoom() {
        TestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()
            val nonExistingExhibitionRoomId = UUID.randomUUID()
            val createdExhibitionRoom = it.admin().exhibitionRooms().create(exhibitionId)
            val createdExhibitionRoomId = createdExhibitionRoom.id!!

            it.admin().exhibitionRooms().assertFindFail(404, exhibitionId, nonExistingExhibitionRoomId)
            it.admin().exhibitionRooms().assertFindFail(404, nonExistingExhibitionId, nonExistingExhibitionRoomId)
            it.admin().exhibitionRooms().assertFindFail(404, nonExistingExhibitionId, createdExhibitionRoomId)
            assertNotNull(it.admin().exhibitionRooms().findExhibitionRoom(exhibitionId, createdExhibitionRoomId))
        }
    }

    @Test
    fun testListExhibitionRooms() {
        TestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()

            it.admin().exhibitionRooms().assertListFail(404, nonExistingExhibitionId)
            assertEquals(0, it.admin().exhibitionRooms().listExhibitionRooms(exhibitionId).size)

            val createdExhibitionRoom = it.admin().exhibitionRooms().create(exhibitionId)
            val createdExhibitionRoomId = createdExhibitionRoom.id!!
            val exhibitionRooms = it.admin().exhibitionRooms().listExhibitionRooms(exhibitionId)
            assertEquals(1, exhibitionRooms.size)
            assertEquals(createdExhibitionRoomId, exhibitionRooms[0].id)
            it.admin().exhibitionRooms().delete(exhibitionId, createdExhibitionRoomId)
            assertEquals(0, it.admin().exhibitionRooms().listExhibitionRooms(exhibitionId).size)
        }
    }

    @Test
    fun testUpdateExhibition() {
        TestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()

            val createdExhibitionRoom = it.admin().exhibitionRooms().create(exhibitionId, "created name")
            val createdExhibitionRoomId = createdExhibitionRoom.id!!

            val foundCreatedExhibitionRoom = it.admin().exhibitionRooms().findExhibitionRoom(exhibitionId, createdExhibitionRoomId)
            assertEquals(createdExhibitionRoom.id, foundCreatedExhibitionRoom?.id)
            assertEquals("created name", createdExhibitionRoom.name)

            val updatedExhibitionRoom = it.admin().exhibitionRooms().updateExhibitionRoom(exhibitionId, ExhibitionRoom("updated name", createdExhibitionRoomId))
            val foundUpdatedExhibitionRoom = it.admin().exhibitionRooms().findExhibitionRoom(exhibitionId, createdExhibitionRoomId)

            assertEquals(updatedExhibitionRoom!!.id, foundUpdatedExhibitionRoom?.id)
            assertEquals("updated name", updatedExhibitionRoom.name)

            it.admin().exhibitionRooms().assertUpdateFail(404, nonExistingExhibitionId, ExhibitionRoom("name", createdExhibitionRoomId))
        }
    }

    @Test
    fun testDeleteExhibition() {
        TestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()
            val nonExistingSessionVariableId = UUID.randomUUID()
            val createdExhibitionRoom = it.admin().exhibitionRooms().create(exhibitionId)
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