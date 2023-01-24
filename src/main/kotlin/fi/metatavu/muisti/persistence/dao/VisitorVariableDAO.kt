package fi.metatavu.muisti.persistence.dao

import fi.metatavu.muisti.api.spec.model.VisitorVariableType
import fi.metatavu.muisti.persistence.model.*
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.persistence.TypedQuery
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

/**
 * DAO class for VisitorVariable
 *
 * @author Antti Leppä
 */
@ApplicationScoped
class VisitorVariableDAO() : AbstractDAO<VisitorVariable>() {

    /**
     * Creates new visitor variable
     *
     * @param id id
     * @param exhibition exhibition
     * @param name name
     * @param type type
     * @param enum enumerated type allowed values
     * @param editableFromUI whether the visitor variable should be editable from the customer service UI
     * @param creatorId creator's id
     * @param lastModifierId last modifier's id
     * @return created VisitorVariable
     */
    fun create(id: UUID, exhibition: Exhibition, name: String, type: VisitorVariableType, enum: String?, editableFromUI: Boolean, creatorId: UUID, lastModifierId: UUID): VisitorVariable {
        val result = VisitorVariable()
        result.exhibition = exhibition
        result.name = name
        result.type = type
        result.enum = enum
        result.id = id
        result.editableFromUI = editableFromUI
        result.creatorId = creatorId
        result.lastModifierId = lastModifierId
        return persist(result)
    }

    /**
     * Lists visitor variables
     *
     * @param exhibition exhibition
     * @param name name
     * @return List of visitor variables
     */
    fun list(exhibition: Exhibition, name: String?): List<VisitorVariable> {
        
        val criteriaBuilder = entityManager.criteriaBuilder
        val criteria: CriteriaQuery<VisitorVariable> = criteriaBuilder.createQuery(VisitorVariable::class.java)
        val root: Root<VisitorVariable> = criteria.from(VisitorVariable::class.java)

        val restrictions = ArrayList<Predicate>()
        restrictions.add(criteriaBuilder.equal(root.get(VisitorVariable_.exhibition), exhibition))

        if (name != null) {
          restrictions.add(criteriaBuilder.equal(root.get(VisitorVariable_.name), name))
        }

        criteria.select(root)
        criteria.where(*restrictions.toTypedArray())
        val query: TypedQuery<VisitorVariable> = entityManager.createQuery<VisitorVariable>(criteria)
        return query.resultList
    }

    /**
     * Updates name
     *
     * @param visitorVariable visitor variable
     * @param name name
     * @param lastModifierId last modifier's id
     * @return updated visitor variable
     */
    fun updateName(visitorVariable: VisitorVariable, name: String, lastModifierId: UUID): VisitorVariable {
        visitorVariable.lastModifierId = lastModifierId
        visitorVariable.name = name
        return persist(visitorVariable)
    }

    /**
     * Updates type
     *
     * @param visitorVariable visitor variable
     * @param type type
     * @param lastModifierId last modifier's id
     * @return updated visitor variable
     */
    fun updateType(visitorVariable: VisitorVariable, type: VisitorVariableType, lastModifierId: UUID): VisitorVariable {
        visitorVariable.lastModifierId = lastModifierId
        visitorVariable.type = type
        return persist(visitorVariable)
    }

    /**
     * Updates enum
     *
     * @param visitorVariable visitor variable
     * @param enum enumerated type allowed values
     * @param lastModifierId last modifier's id
     * @return updated visitor variable
     */
    fun updateEnum(visitorVariable: VisitorVariable, enum: String?, lastModifierId: UUID): VisitorVariable {
        visitorVariable.lastModifierId = lastModifierId
        visitorVariable.enum = enum
        return persist(visitorVariable)
    }

    /**
     * Updates editableFromUI
     *
     * @param visitorVariable visitor variable
     * @param editableFromUI whether the visitor variable should be editable from the customer service UI
     * @param lastModifierId last modifier's id
     * @return updated visitor variable
     */
    fun updateEditableFromUI(visitorVariable: VisitorVariable, editableFromUI: Boolean, lastModifierId: UUID): VisitorVariable {
        visitorVariable.lastModifierId = lastModifierId
        visitorVariable.editableFromUI = editableFromUI
        return persist(visitorVariable)
    }


}

