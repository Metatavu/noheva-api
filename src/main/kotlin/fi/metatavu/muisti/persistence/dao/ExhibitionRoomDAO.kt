package fi.metatavu.muisti.persistence.dao

import fi.metatavu.muisti.persistence.model.Exhibition
import fi.metatavu.muisti.persistence.model.ExhibitionRoom
import fi.metatavu.muisti.persistence.model.ExhibitionRoom_
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.persistence.TypedQuery
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Root

/**
 * DAO class for ExhibitionRoom
 *
 * @author Antti Leppä
 */
@ApplicationScoped
open class ExhibitionRoomDAO() : AbstractDAO<ExhibitionRoom>() {

    /**
     * Creates new ExhibitionRoom
     *
     * @param id id
     * @param exhibition exhibition
     * @param name name
     * @param creatorId creator's id
     * @param lastModifierId last modifier's id
     * @return created exhibitionRoom
     */
    open fun create(id: UUID, exhibition: Exhibition, name: String, creatorId: UUID, lastModifierId: UUID): ExhibitionRoom {
        val exhibitionRoom = ExhibitionRoom()
        exhibitionRoom.id = id
        exhibitionRoom.name = name
        exhibitionRoom.exhibition = exhibition
        exhibitionRoom.creatorId = creatorId
        exhibitionRoom.lastModifierId = lastModifierId
        return persist(exhibitionRoom)
    }

    /**
     * Lists ExhibitionRooms by exhibition
     *
     * @param exhibition exhibition
     * @return List of ExhibitionRooms
     */
    open fun listByExhibition(exhibition: Exhibition): List<ExhibitionRoom> {
        val entityManager = getEntityManager()
        val criteriaBuilder = entityManager.criteriaBuilder
        val criteria: CriteriaQuery<ExhibitionRoom> = criteriaBuilder.createQuery(ExhibitionRoom::class.java)
        val root: Root<ExhibitionRoom> = criteria.from(ExhibitionRoom::class.java)
        criteria.select(root)
        criteria.where(criteriaBuilder.equal(root.get(ExhibitionRoom_.exhibition), exhibition))
        val query: TypedQuery<ExhibitionRoom> = entityManager.createQuery<ExhibitionRoom>(criteria)
        return query.getResultList()
    }

    /**
     * Updates name
     *
     * @param name name
     * @param lastModifierId last modifier's id
     * @return updated exhibitionRoom
     */
    open fun updateName(exhibitionRoom: ExhibitionRoom, name: String, lastModifierId: UUID): ExhibitionRoom {
        exhibitionRoom.lastModifierId = lastModifierId
        exhibitionRoom.name = name
        return persist(exhibitionRoom)
    }

}