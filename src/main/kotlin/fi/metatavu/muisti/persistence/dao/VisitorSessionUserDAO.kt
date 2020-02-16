package fi.metatavu.muisti.persistence.dao

import fi.metatavu.muisti.persistence.model.VisitorSession
import fi.metatavu.muisti.persistence.model.VisitorSessionUser
import fi.metatavu.muisti.persistence.model.VisitorSessionUser_
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.persistence.TypedQuery
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Root


/**
 * DAO class for VisitorSessionUser
 *
 * @author Antti Leppä
 */
@ApplicationScoped
open class VisitorSessionUserDAO() : AbstractDAO<VisitorSessionUser>() {

    /**
     * Creates new VisitorSessionUser
     *
     * @param id id
     * @param visitorSession visitorSession
     * @param userId userId
     * @param tagId tagId
     * @return created VisitorSessionUser
     */
    open fun create(id: UUID, visitorSession: VisitorSession, userId: UUID, tagId: String): VisitorSessionUser {
        val result = VisitorSessionUser()
        result.visitorSession = visitorSession
        result.userId = userId
        result.tagId = tagId
        result.id = id
        return persist(result)
    }

    /**
     * Lists visitor session users by visitorSession
     *
     * @param visitorSession visitor session
     * @return List of visitor sessions
     */
    open fun listByVisitorSession(visitorSession: VisitorSession): List<VisitorSessionUser> {
        val entityManager = getEntityManager()
        val criteriaBuilder = entityManager.criteriaBuilder
        val criteria: CriteriaQuery<VisitorSessionUser> = criteriaBuilder.createQuery(VisitorSessionUser::class.java)
        val root: Root<VisitorSessionUser> = criteria.from(VisitorSessionUser::class.java)
        criteria.select(root)
        criteria.where(criteriaBuilder.equal(root.get(VisitorSessionUser_.visitorSession), visitorSession))
        val query: TypedQuery<VisitorSessionUser> = entityManager.createQuery<VisitorSessionUser>(criteria)
        return query.getResultList()
    }

}