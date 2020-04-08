package fi.metatavu.muisti.exhibitions

import fi.metatavu.muisti.persistence.dao.ExhibitionContentVersionDAO
import fi.metatavu.muisti.persistence.model.Exhibition
import fi.metatavu.muisti.persistence.model.ExhibitionContentVersion
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Controller for exhibition content versions
 */
@ApplicationScoped
class ExhibitionContentVersionController() {

    @Inject
    private lateinit var exhibitionContentVersionDAO: ExhibitionContentVersionDAO

    /**
     * Creates new exhibition content version
     *
     * @param name content version name
     * @param creatorId creating user id
     * @return created exhibition content version
     */
    fun createExhibitionContentVersion(exhibition: Exhibition, name: String, creatorId: UUID): ExhibitionContentVersion {
        return exhibitionContentVersionDAO.create(UUID.randomUUID(), exhibition, name, creatorId, creatorId)
    }

    /**
     * Finds an exhibition content version by id
     *
     * @param id exhibition content version id
     * @return found exhibition content version or null if not found
     */
    fun findExhibitionContentVersionById(id: UUID): ExhibitionContentVersion? {
        return exhibitionContentVersionDAO.findById(id)
    }

    /**
     * Lists content versions in an exhibitions
     *
     * @returns all contentVersions in an exhibition
     */
    fun listExhibitionContentVersions(exhibition: Exhibition): List<ExhibitionContentVersion> {
        return exhibitionContentVersionDAO.listByExhibition(exhibition)
    }

    /**
     * Updates an exhibition content version
     *
     * @param exhibitionContentVersion exhibition content version to be updated
     * @param name group name
     * @param modifierId modifying user id
     * @return updated exhibition
     */
    fun updateExhibitionContentVersion(exhibitionContentVersion: ExhibitionContentVersion, name: String, modifierId: UUID): ExhibitionContentVersion {
      return exhibitionContentVersionDAO.updateName(exhibitionContentVersion, name, modifierId)
    }

    /**
     * Deletes an exhibition content version
     *
     * @param exhibitionContentVersion exhibition content version to be deleted
     */
    fun deleteExhibitionContentVersion(exhibitionContentVersion: ExhibitionContentVersion) {
        return exhibitionContentVersionDAO.delete(exhibitionContentVersion)
    }

}