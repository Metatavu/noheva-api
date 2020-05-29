package fi.metatavu.muisti.visitors

import fi.metatavu.muisti.persistence.dao.VisitorDAO
import fi.metatavu.muisti.persistence.model.Exhibition
import fi.metatavu.muisti.persistence.model.Visitor
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Controller for visitor s
 */
@ApplicationScoped
class VisitorController {

    @Inject
    private lateinit var visitorDAO: VisitorDAO

    /**
     * Creates new visitor 
     */
    fun createVisitor(exhibition: Exhibition, email: String, tagId: String, userId: UUID, creatorId: UUID): Visitor {
        return visitorDAO.create(id = UUID.randomUUID(),
            exhibition = exhibition,
            email = email,
            tagId = tagId,
            userId =  userId,
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
     * @return list of visitors in exhibition
     */
    fun listVisitors(exhibition: Exhibition): List<Visitor> {
        return visitorDAO.list(
            exhibition = exhibition
        )
    }

    /**
     * Creates new visitor
     */
    fun updateVisitor(visitor: Visitor, email: String, tagId: String, userId: UUID, lastModfierId: UUID): Visitor {
        var result = visitorDAO.updateEmail(visitor, email, lastModfierId)
        result = visitorDAO.updateTagId(result, tagId, lastModfierId)
        return visitorDAO.updateUserId(result, userId, lastModfierId)
    }

    /**
     * Deletes visitor 
     *
     * @param visitor visitor 
     */
    fun deleteVisitor(visitor: Visitor) {
        visitorDAO.delete(visitor)
    }

}