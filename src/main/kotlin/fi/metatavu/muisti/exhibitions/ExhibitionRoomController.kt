package fi.metatavu.muisti.exhibitions

import fi.metatavu.muisti.persistence.dao.ExhibitionRoomDAO
import fi.metatavu.muisti.persistence.model.Exhibition
import fi.metatavu.muisti.persistence.model.ExhibitionRoom
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Controller for exhibitions
 */
@ApplicationScoped
open class ExhibitionRoomController() {

    @Inject
    private lateinit var exhibitionRoomDAO: ExhibitionRoomDAO

    /**
     * Creates new exhibition
     *
     * @param name exhibition name
     * @param creatorId creating user id
     * @return created exhibition
     */
    open fun createExhibitionRoom(exhibition: Exhibition, name: String, creatorId: UUID): ExhibitionRoom {
        return exhibitionRoomDAO.create(UUID.randomUUID(), exhibition, name, creatorId, creatorId)
    }

    /**
     * Finds an exhibition room by id
     *
     * @param id exhibition room id
     * @return found exhibition room or null if not found
     */
    open fun findExhibitionRoomById(id: UUID): ExhibitionRoom? {
        return exhibitionRoomDAO.findById(id)
    }

    /**
     * Lists rooms in an exhibitions in a system
     *
     * @returns all rooms in an exhibition
     */
    open fun listExhibitionRooms(exhibition: Exhibition): List<ExhibitionRoom> {
        return exhibitionRoomDAO.listByExhibition(exhibition)
    }

    /**
     * Updates an exhibition room
     *
     * @param exhibitionRoom exhibition room to be updated
     * @param name new name for exhibition
     * @param modifierId modifying user id
     * @return updated exhibition
     */
    open fun updateExhibitionRoom(exhibitionRoom: ExhibitionRoom, name: String, modifierId: UUID): ExhibitionRoom {
      return exhibitionRoomDAO.updateName(exhibitionRoom, name, modifierId)
    }

    /**
     * Deletes an exhibition room
     *
     * @param exhibitionRoom exhibition room to be deleted
     */
    open fun deleteExhibitionRoom(exhibitionRoom: ExhibitionRoom) {
        return exhibitionRoomDAO.delete(exhibitionRoom)
    }

}