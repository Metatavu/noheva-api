package fi.metatavu.noheva.persistence.dao

import fi.metatavu.noheva.persistence.model.*
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.persistence.TypedQuery
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Root

/**
 * DAO class for ContentVersionRoom
 *
 * @author Jari Nykänen
 */
@ApplicationScoped
class ContentVersionRoomDAO() : AbstractDAO<ContentVersionRoom>() {

    /**
     * Creates new VisitorSessionVisitor
     *
     * @param id id
     * @param contentVersion content version
     * @param room exhibition room
     * @return created ContentVersionRoom
     */
    fun create(id: UUID, contentVersion: ContentVersion, room: ExhibitionRoom): ContentVersionRoom {
        val result = ContentVersionRoom()
        result.contentVersion = contentVersion
        result.exhibitionRoom = room
        result.id = id
        return persist(result)
    }

    /**
     * Lists exhibition rooms by content version
     *
     * @param contentVersion content version
     * @return List of content version rooms
     */
    fun listRoomsByContentVersion(contentVersion: ContentVersion): List<ContentVersionRoom> {
        
        val criteriaBuilder = getEntityManager().criteriaBuilder
        val criteria: CriteriaQuery<ContentVersionRoom> = criteriaBuilder.createQuery(ContentVersionRoom::class.java)
        val root: Root<ContentVersionRoom> = criteria.from(ContentVersionRoom::class.java)
        criteria.select(root)
        criteria.where(criteriaBuilder.equal(root.get(ContentVersionRoom_.contentVersion), contentVersion))
        val query: TypedQuery<ContentVersionRoom> = getEntityManager().createQuery<ContentVersionRoom>(criteria)
        return query.resultList
    }

    /**
     * Lists content versions by exhibition room
     *
     * @param exhibitionRoom exhibition room
     * @return List of content versions
     */
    fun listContentVersionsByRoom(exhibitionRoom: ExhibitionRoom): List<ContentVersion> {
        val criteriaBuilder = getEntityManager().criteriaBuilder
        val criteria: CriteriaQuery<ContentVersion> = criteriaBuilder.createQuery(ContentVersion::class.java)
        val root: Root<ContentVersionRoom> = criteria.from(ContentVersionRoom::class.java)
        criteria.select(root.get(ContentVersionRoom_.contentVersion))
        criteria.where(criteriaBuilder.equal(root.get(ContentVersionRoom_.exhibitionRoom), exhibitionRoom))
        return getEntityManager().createQuery(criteria).resultList
    }
}
