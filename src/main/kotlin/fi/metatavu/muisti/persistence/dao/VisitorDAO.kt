package fi.metatavu.muisti.persistence.dao

import fi.metatavu.muisti.persistence.model.Exhibition
import fi.metatavu.muisti.persistence.model.Visitor
import fi.metatavu.muisti.persistence.model.Visitor_
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.persistence.TypedQuery
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

/**
 * DAO class for Visitor
 *
 * @author Antti Leppä
 */
@ApplicationScoped
class VisitorDAO : AbstractDAO<Visitor>() {

    /**
     * Creates new Visitor
     *
     * @param id id
     * @param exhibition exhibition
     * @param tagId visitor's tag id
     * @param userId visitor's user id
     * @param creatorId creator's id
     * @param lastModifierId last modifier's id
     * @return created visitor
     */
    fun create(id: UUID, exhibition: Exhibition, tagId: String, userId: UUID, creatorId: UUID, lastModifierId: UUID): Visitor {
        val visitor = Visitor()
        visitor.exhibition = exhibition
        visitor.id = id
        visitor.tagId = tagId
        visitor.userId = userId
        visitor.creatorId = creatorId
        visitor.lastModifierId = lastModifierId
        return persist(visitor)
    }

    /**
     * Find visitor by tag id
     *
     * @param exhibition exhibition
     * @param tagId tagId
     * @return Found visitor or null if not found
     */
    fun findByExhibitionAndTagId(exhibition: Exhibition, tagId: String): Visitor? {
        val entityManager = getEntityManager()
        val criteriaBuilder = entityManager.criteriaBuilder
        val criteria: CriteriaQuery<Visitor> = criteriaBuilder.createQuery(Visitor::class.java)
        val root: Root<Visitor> = criteria.from(Visitor::class.java)
        criteria.select(root)

        criteria.where(
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get(Visitor_.exhibition), exhibition),
                criteriaBuilder.equal(root.get(Visitor_.tagId), tagId)
            )
        )

        val query: TypedQuery<Visitor> = entityManager.createQuery(criteria)
        return getSingleResult(query)
    }

    /**
     * Lists visitors
     *
     * @param exhibition exhibition
     * @param tagId tagId
     * @return List of visitors
     */
    fun list(exhibition: Exhibition?, tagId: String?): List<Visitor> {
        val entityManager = getEntityManager()
        val criteriaBuilder = entityManager.criteriaBuilder
        val criteria: CriteriaQuery<Visitor> = criteriaBuilder.createQuery(Visitor::class.java)
        val root: Root<Visitor> = criteria.from(Visitor::class.java)
        val restrictions = ArrayList<Predicate>()
        restrictions.add(criteriaBuilder.equal(root.get(Visitor_.exhibition), exhibition))

        if (tagId != null) {
            restrictions.add(criteriaBuilder.equal(root.get(Visitor_.tagId), tagId))
        }

        criteria.select(root)
        criteria.where(*restrictions.toTypedArray())
        val query: TypedQuery<Visitor> = entityManager.createQuery(criteria)
        return query.resultList
    }

    /**
     * Updates tagId
     *
     * @param visitor visitor
     * @param tagId tagId
     * @param lastModifierId last modifier's id
     * @return updated visitor
     */
    fun updateTagId(visitor: Visitor, tagId: String, lastModifierId: UUID): Visitor {
        visitor.lastModifierId = lastModifierId
        visitor.tagId = tagId
        return persist(visitor)
    }

    /**
     * Updates userId
     *
     * @param userId userId
     * @param lastModifierId last modifier's id
     * @return updated visitor
     */
    fun updateUserId(visitor: Visitor, userId: UUID, lastModifierId: UUID): Visitor {
        visitor.lastModifierId = lastModifierId
        visitor.userId = userId
        return persist(visitor)
    }

}
