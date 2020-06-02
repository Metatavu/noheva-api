package fi.metatavu.muisti.exhibitions

import fi.metatavu.muisti.persistence.dao.ContentVersionDAO
import fi.metatavu.muisti.persistence.model.Exhibition
import fi.metatavu.muisti.persistence.model.ContentVersion
import fi.metatavu.muisti.persistence.model.ExhibitionRoom
import fi.metatavu.muisti.persistence.model.GroupContentVersion
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
     * @param language language code
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
     * @return found content version or null if not found
     */
    fun findContentVersionById(id: UUID): ContentVersion? {
        return contentVersionDAO.findById(id)
    }

    /**
     * Lists content versions in an exhibitions
     *
     * @returns all contentVersions in an exhibition
     */
    fun listContentVersions(exhibition: Exhibition, exhibitionRoom: ExhibitionRoom?): List<ContentVersion> {
        return contentVersionDAO.list(exhibition, exhibitionRoom)
    }

    /**
     * Updates content version
     *
     * @param contentVersion content version to be updated
     * @param name group name
     * @param language language code
     * @param modifierId modifying user id
     * @return updated ContentVersion
     */
    fun updateContentVersion(contentVersion: ContentVersion, name: String, language: String, modifierId: UUID): ContentVersion {
      var result = contentVersionDAO.updateName(contentVersion, name, modifierId)
      result = contentVersionDAO.updateLanguage(result, language, modifierId)
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