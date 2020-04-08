package fi.metatavu.muisti.persistence.dao

import fi.metatavu.muisti.persistence.model.Exhibition
import fi.metatavu.muisti.persistence.model.ExhibitionFloor
import fi.metatavu.muisti.persistence.model.ExhibitionFloor_
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.persistence.TypedQuery
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Root

/**
 * DAO class for ExhibitionFloor
 *
 * @author Antti Leppä
 */
@ApplicationScoped
class ExhibitionFloorDAO() : AbstractDAO<ExhibitionFloor>() {

    /**
     * Creates new ExhibitionFloor
     *
     * @param id id
     * @param exhibition exhibition
     * @param name name
     * @param creatorId creator's id
     * @param lastModifierId last modifier's id
     * @return created exhibitionFloor
     */
    fun create(id: UUID, exhibition: Exhibition, name: String, creatorId: UUID, lastModifierId: UUID): ExhibitionFloor {
        val exhibitionFloor = ExhibitionFloor()
        exhibitionFloor.id = id
        exhibitionFloor.name = name
        exhibitionFloor.exhibition = exhibition
        exhibitionFloor.creatorId = creatorId
        exhibitionFloor.lastModifierId = lastModifierId
        return persist(exhibitionFloor)
    }

    /**
     * Lists ExhibitionFloors by exhibition
     *
     * @param exhibition exhibition
     * @return List of ExhibitionFloors
     */
    fun listByExhibition(exhibition: Exhibition): List<ExhibitionFloor> {
        val entityManager = getEntityManager()
        val criteriaBuilder = entityManager.criteriaBuilder
        val criteria: CriteriaQuery<ExhibitionFloor> = criteriaBuilder.createQuery(ExhibitionFloor::class.java)
        val root: Root<ExhibitionFloor> = criteria.from(ExhibitionFloor::class.java)
        criteria.select(root)
        criteria.where(criteriaBuilder.equal(root.get(ExhibitionFloor_.exhibition), exhibition))
        val query: TypedQuery<ExhibitionFloor> = entityManager.createQuery<ExhibitionFloor>(criteria)
        return query.getResultList()
    }

    /**
     * Updates name
     *
     * @param name name
     * @param lastModifierId last modifier's id
     * @return updated exhibitionFloor
     */
    fun updateName(exhibitionFloor: ExhibitionFloor, name: String, lastModifierId: UUID): ExhibitionFloor {
        exhibitionFloor.lastModifierId = lastModifierId
        exhibitionFloor.name = name
        return persist(exhibitionFloor)
    }

}