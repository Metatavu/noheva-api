package fi.metatavu.muisti.exhibitions

import fi.metatavu.muisti.persistence.dao.ExhibitionFloorDAO
import fi.metatavu.muisti.persistence.model.Exhibition
import fi.metatavu.muisti.persistence.model.ExhibitionFloor
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Controller for exhibition floors
 */
@ApplicationScoped
class ExhibitionFloorController() {

    @Inject
    private lateinit var exhibitionFloorDAO: ExhibitionFloorDAO

    /**
     * Creates new exhibition floor
     *
     * @param name floor name
     * @param creatorId creating user id
     * @return created exhibition floor
     */
    fun createExhibitionFloor(exhibition: Exhibition, name: String, creatorId: UUID): ExhibitionFloor {
        return exhibitionFloorDAO.create(UUID.randomUUID(), exhibition, name, creatorId, creatorId)
    }

    /**
     * Finds an exhibition floor by id
     *
     * @param id exhibition floor id
     * @return found exhibition floor or null if not found
     */
    fun findExhibitionFloorById(id: UUID): ExhibitionFloor? {
        return exhibitionFloorDAO.findById(id)
    }

    /**
     * Lists floors in an exhibitions
     *
     * @returns all floors in an exhibition
     */
    fun listExhibitionFloors(exhibition: Exhibition): List<ExhibitionFloor> {
        return exhibitionFloorDAO.listByExhibition(exhibition)
    }

    /**
     * Updates an exhibition floor
     *
     * @param exhibitionFloor exhibition floor to be updated
     * @param name floor name
     * @param modifierId modifying user id
     * @return updated exhibition
     */
    fun updateExhibitionFloor(exhibitionFloor: ExhibitionFloor, name: String, modifierId: UUID): ExhibitionFloor {
      return exhibitionFloorDAO.updateName(exhibitionFloor, name, modifierId)
    }

    /**
     * Deletes an exhibition floor
     *
     * @param exhibitionFloor exhibition floor to be deleted
     */
    fun deleteExhibitionFloor(exhibitionFloor: ExhibitionFloor) {
        return exhibitionFloorDAO.delete(exhibitionFloor)
    }

}