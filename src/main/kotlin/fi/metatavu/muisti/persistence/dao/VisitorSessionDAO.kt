package fi.metatavu.muisti.persistence.dao

import fi.metatavu.muisti.api.spec.model.VisitorSessionState
import fi.metatavu.muisti.persistence.model.Exhibition
import fi.metatavu.muisti.persistence.model.VisitorSession
import fi.metatavu.muisti.persistence.model.VisitorSession_
import java.time.OffsetDateTime
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.persistence.TypedQuery
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

/**
 * DAO class for VisitorSession
 *
 * @author Antti Leppä
 */
@ApplicationScoped
class VisitorSessionDAO : AbstractDAO<VisitorSession>() {

    /**
     * Creates new VisitorSession
     *
     * @param id id
     * @param exhibition exhibition
     * @param state state
     * @param language language
     * @param creatorId creator's id
     * @param lastModifierId last modifier's id
     * @return created visitorSession
     */
    fun create(id: UUID, exhibition: Exhibition, state: VisitorSessionState, language: String, creatorId: UUID, lastModifierId: UUID): VisitorSession {
        val visitorSession = VisitorSession()
        visitorSession.exhibition = exhibition
        visitorSession.id = id
        visitorSession.state = state
        visitorSession.language = language
        visitorSession.creatorId = creatorId
        visitorSession.lastModifierId = lastModifierId
        return persist(visitorSession)
    }

    /**
     * Find visitor session
     *
     * @param id visitor session id
     * @param createdAfter created after specified time
     * @return Found visitor session or null if not found
     */
    fun find(id: UUID, createdAfter: OffsetDateTime): VisitorSession? {
        val entityManager = getEntityManager()
        val criteriaBuilder = entityManager.criteriaBuilder
        val criteria: CriteriaQuery<VisitorSession> = criteriaBuilder.createQuery(VisitorSession::class.java)
        val root: Root<VisitorSession> = criteria.from(VisitorSession::class.java)

        val restrictions = ArrayList<Predicate>()
        restrictions.add(criteriaBuilder.equal(root.get(VisitorSession_.id), id))
        restrictions.add(criteriaBuilder.greaterThan(root.get(VisitorSession_.createdAt), createdAfter))

        criteria.select(root)
        criteria.where(*restrictions.toTypedArray())

        return getSingleResult(entityManager.createQuery(criteria))
    }

    /**
     * Lists visitor sessions
     *
     * @param exhibition exhibition
     * @param createdAfter created after specified time
     * @return List of visitor sessions
     */
    fun list(exhibition: Exhibition, createdAfter: OffsetDateTime): List<VisitorSession> {
        val entityManager = getEntityManager()
        val criteriaBuilder = entityManager.criteriaBuilder
        val criteria: CriteriaQuery<VisitorSession> = criteriaBuilder.createQuery(VisitorSession::class.java)
        val root: Root<VisitorSession> = criteria.from(VisitorSession::class.java)

        val restrictions = ArrayList<Predicate>()
        restrictions.add(criteriaBuilder.equal(root.get(VisitorSession_.exhibition), exhibition))
        restrictions.add(criteriaBuilder.greaterThan(root.get(VisitorSession_.createdAt), createdAfter))

        criteria.select(root)
        criteria.where(*restrictions.toTypedArray())
        val query: TypedQuery<VisitorSession> = entityManager.createQuery(criteria)
        return query.resultList
    }

    /**
     * Updates state
     *
     * @param visitorSession visitor session
     * @param state state
     * @param lastModifierId last modifier's id
     * @return updated visitorSession
     */
    fun updateState(visitorSession: VisitorSession, state: VisitorSessionState, lastModifierId: UUID): VisitorSession {
        visitorSession.lastModifierId = lastModifierId
        visitorSession.state = state
        return persist(visitorSession)
    }

    /**
     * Updates language
     *
     * @param visitorSession visitor session
     * @param language language
     * @param lastModifierId last modifier's id
     * @return updated visitorSession
     */
    fun updateLanguage(visitorSession: VisitorSession, language: String, lastModifierId: UUID): VisitorSession {
        visitorSession.lastModifierId = lastModifierId
        visitorSession.language = language
        return persist(visitorSession)
    }

}