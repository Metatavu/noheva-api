package fi.metatavu.muisti.persistence.dao

import fi.metatavu.muisti.persistence.model.Exhibition
import fi.metatavu.muisti.persistence.model.ExhibitionFloor
import fi.metatavu.muisti.persistence.model.ExhibitionRoom
import fi.metatavu.muisti.persistence.model.ExhibitionRoom_
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.persistence.TypedQuery
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

/**
 * DAO class for ExhibitionRoom
 *
 * @author Antti Leppä
 */
@ApplicationScoped
class ExhibitionRoomDAO() : AbstractDAO<ExhibitionRoom>() {

    /**
     * Creates new ExhibitionRoom
     *
     * @param id id
     * @param exhibition exhibition
     * @param floor floor where the room is
     * @param name name
     * @param creatorId creator's id
     * @param lastModifierId last modifier's id
     * @return created exhibitionRoom
     */
    fun create(id: UUID, exhibition: Exhibition, floor: ExhibitionFloor, name: String, creatorId: UUID, lastModifierId: UUID): ExhibitionRoom {
        val exhibitionRoom = ExhibitionRoom()
        exhibitionRoom.id = id
        exhibitionRoom.name = name
        exhibitionRoom.exhibition = exhibition
        exhibitionRoom.floor = floor
        exhibitionRoom.creatorId = creatorId
        exhibitionRoom.lastModifierId = lastModifierId
        return persist(exhibitionRoom)
    }

    /**
     * Lists exhibition rooms
     *
     * @param exhibition exhibition
     * @param floor floor filter by floor. Ignored if null
     * @return List of ExhibitionRooms
     */
    fun list(exhibition: Exhibition, floor: ExhibitionFloor?): List<ExhibitionRoom> {
        val entityManager = getEntityManager()
        val criteriaBuilder = entityManager.criteriaBuilder
        val criteria: CriteriaQuery<ExhibitionRoom> = criteriaBuilder.createQuery(ExhibitionRoom::class.java)
        val root: Root<ExhibitionRoom> = criteria.from(ExhibitionRoom::class.java)

        val restrictions = ArrayList<Predicate>()
        restrictions.add(criteriaBuilder.equal(root.get(ExhibitionRoom_.exhibition), exhibition))

        if (floor != null) {
            restrictions.add(criteriaBuilder.equal(root.get(ExhibitionRoom_.floor), floor))
        }

        criteria.select(root)
        criteria.where(*restrictions.toTypedArray())
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
    fun updateName(exhibitionRoom: ExhibitionRoom, name: String, lastModifierId: UUID): ExhibitionRoom {
        exhibitionRoom.lastModifierId = lastModifierId
        exhibitionRoom.name = name
        return persist(exhibitionRoom)
    }

    /**
     * Updates floor
     *
     * @param floor floor
     * @param lastModifierId last modifier's id
     * @return updated exhibitionRoom
     */
    fun updateFloor(exhibitionRoom: ExhibitionRoom, floor: ExhibitionFloor, lastModifierId: UUID): ExhibitionRoom {
        exhibitionRoom.lastModifierId = lastModifierId
        exhibitionRoom.floor = floor
        return persist(exhibitionRoom)
    }

}