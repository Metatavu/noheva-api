package fi.metatavu.muisti.exhibitions

import fi.metatavu.muisti.persistence.dao.ExhibitionDAO
import fi.metatavu.muisti.persistence.model.Exhibition
import org.slf4j.Logger
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Controller for exhibitions
 */
@ApplicationScoped
class ExhibitionController {

    @Inject
    private lateinit var exhibitionDAO: ExhibitionDAO

    /**
     * Creates new exhibition
     *
     * @param name exhibition name
     * @param creatorId creating user id
     * @return created exhibition
     */
    fun createExhibition(name: String, creatorId: UUID): Exhibition {
        return exhibitionDAO.create(UUID.randomUUID(), name, creatorId, creatorId)
    }

    /**
     * Finds an exhibition by id
     *
     * @param id exhibition id
     * @return found exhibition or null if not found
     */
    fun findExhibitionById(id: UUID): Exhibition? {
        return exhibitionDAO.findById(id)
    }

    /**
     * Lists all exhibitions in a system
     *
     * @returns all exhibitions in a system
     */
    fun listExhibitions(): List<Exhibition> {
        return exhibitionDAO.listAll()
    }

    /**
     * Updates an exhibition
     *
     * @param exhibition exhibition to be updated
     * @param name new name for exhibition
     * @param modifierId modifying user id
     * @return updated exhibition
     */
    fun updateExhibition(exhibition: Exhibition, name: String, modifierId: UUID): Exhibition {
      return exhibitionDAO.updateName(exhibition, name, modifierId)
    }

    /**
     * Deletes an exhibition
     *
     * @param exhibition exhibition to be deleted
     */
    fun deleteExhibition(exhibition: Exhibition) {
        return exhibitionDAO.delete(exhibition)
    }

}