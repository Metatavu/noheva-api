package fi.metatavu.muisti.persistence.dao

import fi.metatavu.muisti.persistence.model.VisitorSession
import fi.metatavu.muisti.persistence.model.VisitorSessionVariable
import fi.metatavu.muisti.persistence.model.VisitorSessionVariable_
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.persistence.TypedQuery
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Root


/**
 * DAO class for VisitorSessionVariable
 *
 * @author Antti Leppä
 */
@ApplicationScoped
open class VisitorSessionVariableDAO() : AbstractDAO<VisitorSessionVariable>() {

    /**
     * Creates new VisitorSessionVariable
     *
     * @param id id
     * @param visitorSession visitorSession
     * @param name name
     * @param value value
     * @return created VisitorSessionVariable
     */
    open fun create(id: UUID, visitorSession: VisitorSession, name: String, value: String): VisitorSessionVariable {
        val result = VisitorSessionVariable()
        result.visitorSession = visitorSession
        result.name = name
        result.value = value
        result.id = id
        return persist(result)
    }

    /**
     * Finds VisitorSessionVariable by visitorSession and variable key
     *
     * @param visitorSession visitorSession
     * @return List of VisitorSessionVariables
     */
    open fun findByVisitorSessionAndName(visitorSession: VisitorSession, name: String): VisitorSessionVariable? {
        val entityManager = getEntityManager()
        val criteriaBuilder = getEntityManager().criteriaBuilder
        val criteria: CriteriaQuery<VisitorSessionVariable> = criteriaBuilder.createQuery(VisitorSessionVariable::class.java)
        val root: Root<VisitorSessionVariable> = criteria.from(VisitorSessionVariable::class.java)
        criteria.select(root)
        criteria.where(
            criteriaBuilder.equal(root.get(VisitorSessionVariable_.visitorSession), visitorSession),
            criteriaBuilder.equal(root.get(VisitorSessionVariable_.name), name)
        )

        val query: TypedQuery<VisitorSessionVariable> = entityManager.createQuery<VisitorSessionVariable>(criteria)
        return getSingleResult(query)
    }

    /**
     * Lists VisitorSessionVariable by visitorSession
     *
     * @param visitorSession visitorSession
     * @return List of VisitorSessionVariables
     */
    open fun listByVisitorSession(visitorSession: VisitorSession): List<VisitorSessionVariable> {
        val entityManager = getEntityManager()
        val criteriaBuilder = entityManager.criteriaBuilder
        val criteria: CriteriaQuery<VisitorSessionVariable> = criteriaBuilder.createQuery(VisitorSessionVariable::class.java)
        val root: Root<VisitorSessionVariable> = criteria.from(VisitorSessionVariable::class.java)
        criteria.select(root)
        criteria.where(criteriaBuilder.equal(root.get(VisitorSessionVariable_.visitorSession), visitorSession))
        val query: TypedQuery<VisitorSessionVariable> = entityManager.createQuery<VisitorSessionVariable>(criteria)
        return query.getResultList()
    }

    /**
     * Updates value
     *
     * @para visitorSession visitorSession
     * @param value value
     * @return updated VisitorSessionVariable
     */
    open fun updateValue(visitorSessionVariable: VisitorSessionVariable, value: String): VisitorSessionVariable {
        visitorSessionVariable.value = value
        return persist(visitorSessionVariable)
    }

}