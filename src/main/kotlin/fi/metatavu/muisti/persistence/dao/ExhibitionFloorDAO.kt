package fi.metatavu.muisti.persistence.dao

import org.locationtech.jts.geom.Point
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
     * @param floorPlanUrl floor plan url
     * @param neBoundPoint North East bound point
     * @param swBoundPoint South West bound point
     * @param creatorId creator's id
     * @param lastModifierId last modifier's id
     * @return created exhibitionFloor
     */
    fun create(id: UUID, exhibition: Exhibition, name: String, floorPlanUrl: String?, neBoundPoint: Point?, swBoundPoint: Point?, creatorId: UUID, lastModifierId: UUID): ExhibitionFloor {
        val exhibitionFloor = ExhibitionFloor()
        exhibitionFloor.id = id
        exhibitionFloor.name = name
        exhibitionFloor.floorPlanUrl = floorPlanUrl
        exhibitionFloor.neBoundPoint = neBoundPoint
        exhibitionFloor.swBoundPoint = swBoundPoint
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
        
        val criteriaBuilder = getEntityManager().criteriaBuilder
        val criteria: CriteriaQuery<ExhibitionFloor> = criteriaBuilder.createQuery(ExhibitionFloor::class.java)
        val root: Root<ExhibitionFloor> = criteria.from(ExhibitionFloor::class.java)
        criteria.select(root)
        criteria.where(criteriaBuilder.equal(root.get(ExhibitionFloor_.exhibition), exhibition))
        val query: TypedQuery<ExhibitionFloor> = getEntityManager().createQuery<ExhibitionFloor>(criteria)
        return query.getResultList()
    }

    /**
     * Updates name
     *
     * @param exhibitionFloor exhibition floor to be updated
     * @param name name
     * @param lastModifierId last modifier's id
     * @return updated exhibitionFloor
     */
    fun updateName(exhibitionFloor: ExhibitionFloor, name: String, lastModifierId: UUID): ExhibitionFloor {
        exhibitionFloor.lastModifierId = lastModifierId
        exhibitionFloor.name = name
        return persist(exhibitionFloor)
    }

    /**
     * Updates floorPlanUrl
     *
     * @param exhibitionFloor exhibition floor to be updated
     * @param floorPlanUrl floor plan URL
     * @param lastModifierId last modifier's id
     * @return updated exhibitionFloor
     */
    fun updateFloorPlanUrl(exhibitionFloor: ExhibitionFloor, floorPlanUrl: String?, lastModifierId: UUID): ExhibitionFloor {
        exhibitionFloor.lastModifierId = lastModifierId
        exhibitionFloor.floorPlanUrl = floorPlanUrl
        return persist(exhibitionFloor)
    }

    /**
     * Updates floors North East bound point
     *
     * @param exhibitionFloor exhibition floor to be updated
     * @param neBoundPoint North East bound point
     * @param lastModifierId last modifier's id
     * @return updated exhibitionFloor
     */
    fun updateFloorNEBound(exhibitionFloor: ExhibitionFloor, neBoundPoint: Point?, lastModifierId: UUID): ExhibitionFloor {
        exhibitionFloor.lastModifierId = lastModifierId
        exhibitionFloor.neBoundPoint = neBoundPoint
        return persist(exhibitionFloor)
    }

    /**
     * Updates floors South West bound point
     *
     * @param exhibitionFloor exhibition floor to be updated
     * @param swBoundPoint South Westbound point
     * @param lastModifierId last modifier's id
     * @return updated exhibitionFloor
     */
    fun updateFloorSWBound(exhibitionFloor: ExhibitionFloor, swBoundPoint: Point?, lastModifierId: UUID): ExhibitionFloor {
        exhibitionFloor.lastModifierId = lastModifierId
        exhibitionFloor.swBoundPoint = swBoundPoint
        return persist(exhibitionFloor)
    }

}