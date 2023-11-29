package fi.metatavu.noheva.persistence.dao

import fi.metatavu.noheva.persistence.model.Exhibition
import fi.metatavu.noheva.persistence.model.Exhibition_
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Root

/**
 * DAO class for Exhibition
 *
 * @author Antti Leppä
 */
@ApplicationScoped
class ExhibitionDAO : AbstractDAO<Exhibition>() {

    /**
     * Creates new Exhibition
     *
     * @param id id
     * @param name name
     * @param creatorId creator's id
     * @param lastModifierId last modifier's id
     * @return created exhibition
     */
    fun create(id: UUID, name: String, creatorId: UUID, lastModifierId: UUID): Exhibition {
        val exhibition = Exhibition()
        exhibition.name = name
        exhibition.id = id
        exhibition.creatorId = creatorId
        exhibition.lastModifierId = lastModifierId
        return persist(exhibition)
    }

    /**
     * Finds an exhibition by name
     *
     * @param name name
     * @return found exhibition or null if not found
     */
    fun findByName(name: String): Exhibition? {
        val criteriaBuilder = getEntityManager().criteriaBuilder
        val criteria: CriteriaQuery<Exhibition> = criteriaBuilder.createQuery(Exhibition::class.java)
        val root: Root<Exhibition> = criteria.from(Exhibition::class.java)
        criteria.select(root)
        criteria.where(criteriaBuilder.equal(root.get(Exhibition_.name), name))
        return getSingleResult(getEntityManager().createQuery<Exhibition>(criteria))
    }

    /**
     * Finds an active exhibition
     *
     * @return found exhibition or null if not found
     */
    fun findActive(): Exhibition? {
        val criteriaBuilder = getEntityManager().criteriaBuilder
        val criteria: CriteriaQuery<Exhibition> = criteriaBuilder.createQuery(Exhibition::class.java)
        val root: Root<Exhibition> = criteria.from(Exhibition::class.java)
        criteria.select(root)
        criteria.where(criteriaBuilder.equal(root.get(Exhibition_.active), true))
        return getSingleResult(getEntityManager().createQuery<Exhibition>(criteria))
    }

    /**
     * Lists active exhibitions
     *
     * @return list of exhibitions
     */
    fun listActive(): List<Exhibition> {
        val criteriaBuilder = getEntityManager().criteriaBuilder
        val criteria: CriteriaQuery<Exhibition> = criteriaBuilder.createQuery(Exhibition::class.java)
        val root: Root<Exhibition> = criteria.from(Exhibition::class.java)
        criteria.select(root)
        criteria.where(criteriaBuilder.equal(root.get(Exhibition_.active), true))
        return getEntityManager().createQuery<Exhibition>(criteria).resultList
    }

    /**
     * Updates name
     *
     * @param name name
     * @param lastModifierId last modifier's id
     * @return updated exhibition
     */
    fun updateName(exhibition: Exhibition, name: String, lastModifierId: UUID): Exhibition {
        exhibition.lastModifierId = lastModifierId
        exhibition.name = name
        return persist(exhibition)
    }

    /**
     * Updates active
     *
     * @param active active
     * @param lastModifierId last modifier's id
     * @return updated exhibition
     */
    fun updateActive(exhibition: Exhibition, active: Boolean, lastModifierId: UUID): Exhibition {
        exhibition.lastModifierId = lastModifierId
        exhibition.active = active
        return persist(exhibition)
    }
}