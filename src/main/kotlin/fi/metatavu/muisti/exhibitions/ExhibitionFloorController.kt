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
     * @param exhibition exhibition
     * @param name floor name
     * @param floorPlanUrl floor plan url
     * @param creatorId creating user id
     * @return created exhibition floor
     */
    fun createExhibitionFloor(exhibition: Exhibition, name: String, floorPlanUrl: String?, creatorId: UUID): ExhibitionFloor {
        return exhibitionFloorDAO.create(UUID.randomUUID(), exhibition, name, floorPlanUrl, creatorId, creatorId)
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
     * Lists floors in an exhibition
     *
     * @param exhibition exhibition
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
     * @param floorPlanUrl floor plan url
     * @param modifierId modifying user id
     * @return updated exhibition
     */
    fun updateExhibitionFloor(exhibitionFloor: ExhibitionFloor, name: String, floorPlanUrl: String?, modifierId: UUID): ExhibitionFloor {
      var result = exhibitionFloorDAO.updateName(exhibitionFloor, name, modifierId)
      result = exhibitionFloorDAO.updateFloorPlanUrl(exhibitionFloor, floorPlanUrl, modifierId)
      return result
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