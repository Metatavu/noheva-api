package fi.metatavu.muisti.persistence.dao

import fi.metatavu.muisti.persistence.model.Visitor
import fi.metatavu.muisti.persistence.model.VisitorSession
import fi.metatavu.muisti.persistence.model.VisitorSessionVisitor
import fi.metatavu.muisti.persistence.model.VisitorSessionVisitor_
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.persistence.TypedQuery
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Root

/**
 * DAO class for VisitorSessionVisitor
 *
 * @author Antti Leppä
 */
@ApplicationScoped
class VisitorSessionVisitorDAO() : AbstractDAO<VisitorSessionVisitor>() {

    /**
     * Creates new VisitorSessionVisitor
     *
     * @param id id
     * @param visitorSession visitorSession
     * @param visitor visitor
     * @return created VisitorSessionVisitor
     */
    fun create(id: UUID, visitorSession: VisitorSession, visitor: Visitor): VisitorSessionVisitor {
        val result = VisitorSessionVisitor()
        result.visitorSession = visitorSession
        result.visitor = visitor
        result.id = id
        return persist(result)
    }

    /**
     * Lists visitor session visitors by visitorSession
     *
     * @param visitorSession visitor session
     * @return List of visitor session visitors
     */
    fun listByVisitorSession(visitorSession: VisitorSession): List<VisitorSessionVisitor> {
        val entityManager = getEntityManager()
        val criteriaBuilder = entityManager.criteriaBuilder
        val criteria: CriteriaQuery<VisitorSessionVisitor> = criteriaBuilder.createQuery(VisitorSessionVisitor::class.java)
        val root: Root<VisitorSessionVisitor> = criteria.from(VisitorSessionVisitor::class.java)
        criteria.select(root)
        criteria.where(criteriaBuilder.equal(root.get(VisitorSessionVisitor_.visitorSession), visitorSession))
        val query: TypedQuery<VisitorSessionVisitor> = entityManager.createQuery<VisitorSessionVisitor>(criteria)
        return query.getResultList()
    }

    /**
     * Lists visitor sessions by visitors
     *
     * @param visitor visitor
     * @return List of visitor sessions
     */
    fun listSessionsByVisitor(visitor: Visitor): List<VisitorSession> {
        val entityManager = getEntityManager()
        val criteriaBuilder = entityManager.criteriaBuilder
        val criteria: CriteriaQuery<VisitorSession> = criteriaBuilder.createQuery(VisitorSession::class.java)
        val root: Root<VisitorSessionVisitor> = criteria.from(VisitorSessionVisitor::class.java)
        criteria.select(root.get(VisitorSessionVisitor_.visitorSession))
        criteria.where(criteriaBuilder.equal(root.get(VisitorSessionVisitor_.visitor), visitor))
        return entityManager.createQuery(criteria).getResultList()
    }
}
