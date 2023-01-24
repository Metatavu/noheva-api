package fi.metatavu.muisti.visitors

import fi.metatavu.muisti.persistence.dao.VisitorDAO
import fi.metatavu.muisti.persistence.model.Exhibition
import fi.metatavu.muisti.persistence.model.Visitor
import fi.metatavu.muisti.settings.SettingsController
import org.keycloak.representations.idm.UserRepresentation
import java.time.OffsetDateTime
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Controller for visitors
 */
@ApplicationScoped
class VisitorController {

    @Inject
    lateinit var settingsController: SettingsController

    @Inject
    lateinit var visitorDAO: VisitorDAO

    /**
     * Creates new visitor
     *
     * @param exhibition exhibition
     * @param userRepresentation Keycloak user representation
     * @param tagId visitor tag id
     * @param creatorId creator's user id
     * @return created visitor
    */
    fun createVisitor(exhibition: Exhibition, userRepresentation: UserRepresentation, tagId: String, creatorId: UUID): Visitor {
        return visitorDAO.create(id = UUID.randomUUID(),
            exhibition = exhibition,
            tagId = tagId,
            userId = UUID.fromString(userRepresentation.id),
            creatorId = creatorId,
            lastModifierId = creatorId
        )
    }

    /**
     * Finds a visitor by id
     *
     * @param id id
     * @return visitor id
     */
    fun findVisitorById(id: UUID): Visitor? {
        return visitorDAO.findById(id)
    }

    /**
     * Find visitor by tag id
     *
     * @param exhibition exhibition
     * @param tagId tagId
     * @return Found visitor or null if not found
     */
    fun findVisitorByTagId(exhibition: Exhibition, tagId: String): Visitor? {
        return visitorDAO.findByExhibitionAndTagId(exhibition = exhibition, tagId = tagId)
    }

    /**
     * Lists visitors by exhibition
     *
     * @param exhibition exhibition
     * @param tagId filter results by tag id
     * @param userId filter results by user id
     * @return list of visitors in exhibition
     */
    fun listVisitors(exhibition: Exhibition, tagId: String?, userId: UUID?): List<Visitor> {
        var createdAfter: OffsetDateTime? = null
        if (tagId.isNullOrEmpty() && userId == null) {
            createdAfter = settingsController.getVisitorSessionValidAfter()
        }

        return visitorDAO.list(
            exhibition = exhibition,
            tagId = tagId,
            userId = userId,
            createdAfter = createdAfter
        )
    }

    /**
     * Updates a visitor
     *
     * @param visitor visitor
     * @param tagId tag id
     * @return updated visitor
     */
    fun updateVisitor(visitor: Visitor, tagId: String, lastModifierId: UUID): Visitor {
        return visitorDAO.updateTagId(visitor, tagId, lastModifierId)
    }

    /**
     * Deletes a visitor 
     *
     * @param visitor visitor 
     */
    fun deleteVisitor(visitor: Visitor) {
        visitorDAO.delete(visitor)
    }

}
