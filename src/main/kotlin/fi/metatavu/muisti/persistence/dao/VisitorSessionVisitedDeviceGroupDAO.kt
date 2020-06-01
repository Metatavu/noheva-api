package fi.metatavu.muisti.persistence.dao

import fi.metatavu.muisti.persistence.model.ExhibitionDeviceGroup
import fi.metatavu.muisti.persistence.model.VisitorSession
import fi.metatavu.muisti.persistence.model.VisitorSessionVisitedDeviceGroup
import fi.metatavu.muisti.persistence.model.VisitorSessionVisitedDeviceGroup_
import java.time.OffsetDateTime
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.persistence.TypedQuery
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Root

/**
 * DAO class for VisitorSessionVisitedDeviceGroup
 *
 * @author Antti Leppä
 */
@ApplicationScoped
class VisitorSessionVisitedDeviceGroupDAO() : AbstractDAO<VisitorSessionVisitedDeviceGroup>() {

    /**
     * Creates new VisitorSessionVisitedDeviceGroup
     *
     * @param id id
     * @param visitorSession visitorSession
     * @param deviceGroup device group
     * @return created VisitorSessionVisitedDeviceGroup
     */
    fun create(id: UUID, visitorSession: VisitorSession, deviceGroup: ExhibitionDeviceGroup, enteredAt: OffsetDateTime, exitedAt: OffsetDateTime): VisitorSessionVisitedDeviceGroup {
        val result = VisitorSessionVisitedDeviceGroup()
        result.visitorSession = visitorSession
        result.deviceGroup = deviceGroup
        result.id = id
        result.enteredAt = enteredAt
        result.exitedAt = exitedAt
        return persist(result)
    }

    /**
     * Lists visitor session visitors by visitorSession
     *
     * @param visitorSession visitor session
     * @return List of visitor sessions
     */
    fun listByVisitorSession(visitorSession: VisitorSession): List<VisitorSessionVisitedDeviceGroup> {
        val entityManager = getEntityManager()
        val criteriaBuilder = entityManager.criteriaBuilder
        val criteria: CriteriaQuery<VisitorSessionVisitedDeviceGroup> = criteriaBuilder.createQuery(VisitorSessionVisitedDeviceGroup::class.java)
        val root: Root<VisitorSessionVisitedDeviceGroup> = criteria.from(VisitorSessionVisitedDeviceGroup::class.java)
        criteria.select(root)
        criteria.where(criteriaBuilder.equal(root.get(VisitorSessionVisitedDeviceGroup_.visitorSession), visitorSession))
        val query: TypedQuery<VisitorSessionVisitedDeviceGroup> = entityManager.createQuery(criteria)
        return query.resultList
    }

    /**
     * Updates entered at
     *
     * @param visitorSessionVisitedDeviceGroup visitor session visited device group
     * @param enteredAt entered at
     * @return updated entity
     */
    fun updateEnteredAt(visitorSessionVisitedDeviceGroup: VisitorSessionVisitedDeviceGroup, enteredAt: OffsetDateTime): VisitorSessionVisitedDeviceGroup {
        visitorSessionVisitedDeviceGroup.enteredAt = enteredAt
        return persist(visitorSessionVisitedDeviceGroup)
    }

    /**
     * Updates exited at
     *
     * @param visitorSessionVisitedDeviceGroup visitor session visited device group
     * @param exitedAt exited at
     * @return updated entity
     */
    fun updateExitedAt(visitorSessionVisitedDeviceGroup: VisitorSessionVisitedDeviceGroup, exitedAt: OffsetDateTime): VisitorSessionVisitedDeviceGroup {
        visitorSessionVisitedDeviceGroup.exitedAt = exitedAt
        return persist(visitorSessionVisitedDeviceGroup)
    }

}