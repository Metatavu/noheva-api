package fi.metatavu.muisti.exhibitions

import fi.metatavu.muisti.persistence.dao.ContentVersionDAO
import fi.metatavu.muisti.persistence.model.Exhibition
import fi.metatavu.muisti.persistence.model.ContentVersion
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Controller for content versions
 */
@ApplicationScoped
class ContentVersionController() {

    @Inject
    private lateinit var contentVersionDAO: ContentVersionDAO

    /**
     * Creates new content version
     *
     * @param name content version name
     * @param creatorId creating user id
     * @return created exhibition content version
     */
    fun createContentVersion(exhibition: Exhibition, name: String, language: String, creatorId: UUID): ContentVersion {
        return contentVersionDAO.create(UUID.randomUUID(), exhibition, name, language, creatorId, creatorId)
    }

    /**
     * Finds content version by id
     *
     * @param id content version id
     * @return found exhibition content version or null if not found
     */
    fun findContentVersionById(id: UUID): ContentVersion? {
        return contentVersionDAO.findById(id)
    }

    /**
     * Lists content versions in an exhibitions
     *
     * @returns all contentVersions in an exhibition
     */
    fun listContentVersions(exhibition: Exhibition): List<ContentVersion> {
        return contentVersionDAO.listByExhibition(exhibition)
    }

    /**
     * Updates content version
     *
     * @param contentVersion content version to be updated
     * @param name group name
     * @param modifierId modifying user id
     * @return updated ContentVersion
     */
    fun updateContentVersion(contentVersion: ContentVersion, name: String, language: String, modifierId: UUID): ContentVersion {
      var result = contentVersionDAO.updateName(contentVersion, name, modifierId)
      result = contentVersionDAO.updateLanguage(contentVersion, language, modifierId)
      return result
    }

    /**
     * Deletes a content version
     *
     * @param contentVersion content version to be deleted
     */
    fun deleteContentVersion(contentVersion: ContentVersion) {
        return contentVersionDAO.delete(contentVersion)
    }

}