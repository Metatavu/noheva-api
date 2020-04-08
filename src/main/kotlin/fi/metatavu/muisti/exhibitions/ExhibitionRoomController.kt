package fi.metatavu.muisti.exhibitions

import fi.metatavu.muisti.persistence.dao.ExhibitionRoomDAO
import fi.metatavu.muisti.persistence.model.Exhibition
import fi.metatavu.muisti.persistence.model.ExhibitionFloor
import fi.metatavu.muisti.persistence.model.ExhibitionRoom
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Controller for exhibition rooms
 */
@ApplicationScoped
class ExhibitionRoomController() {

    @Inject
    private lateinit var exhibitionRoomDAO: ExhibitionRoomDAO

    /**
     * Creates new exhibition room
     *
     * @param name room name
     * @param creatorId creating user id
     * @return created exhibition room
     */
    fun createExhibitionRoom(exhibition: Exhibition, floor: ExhibitionFloor, name: String, creatorId: UUID): ExhibitionRoom {
        return exhibitionRoomDAO.create(id = UUID.randomUUID(), exhibition = exhibition, floor = floor, name = name, creatorId = creatorId, lastModifierId = creatorId)
    }

    /**
     * Finds an exhibition room by id
     *
     * @param id exhibition room id
     * @return found exhibition room or null if not found
     */
    fun findExhibitionRoomById(id: UUID): ExhibitionRoom? {
        return exhibitionRoomDAO.findById(id)
    }

    /**
     * Lists rooms in an exhibitions
     *
     * @returns all rooms in an exhibition
     */
    fun listExhibitionRooms(exhibition: Exhibition): List<ExhibitionRoom> {
        return exhibitionRoomDAO.listByExhibition(exhibition)
    }

    /**
     * Updates an exhibition room
     *
     * @param exhibitionRoom exhibition room to be updated
     * @param name room name
     * @param floor floor
     * @param modifierId modifying user id
     * @return updated exhibition
     */
    fun updateExhibitionRoom(exhibitionRoom: ExhibitionRoom, floor: ExhibitionFloor, name: String, modifierId: UUID): ExhibitionRoom {
      var result = exhibitionRoomDAO.updateName(exhibitionRoom, name, modifierId)
      result = exhibitionRoomDAO.updateFloor(result, floor, modifierId)
      return result
    }

    /**
     * Deletes an exhibition room
     *
     * @param exhibitionRoom exhibition room to be deleted
     */
    fun deleteExhibitionRoom(exhibitionRoom: ExhibitionRoom) {
        return exhibitionRoomDAO.delete(exhibitionRoom)
    }

}