package fi.metatavu.muisti.persistence.dao

import fi.metatavu.muisti.persistence.model.Exhibition
import fi.metatavu.muisti.persistence.model.ExhibitionDeviceGroup
import fi.metatavu.muisti.persistence.model.ExhibitionDeviceGroup_
import fi.metatavu.muisti.persistence.model.ExhibitionRoom
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.persistence.TypedQuery
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

/**
 * DAO class for ExhibitionDeviceGroup
 *
 * @author Antti Leppä
 */
@ApplicationScoped
class ExhibitionDeviceGroupDAO() : AbstractDAO<ExhibitionDeviceGroup>() {

  /**
   * Creates new ExhibitionDeviceGroup
   *
   * @param id id
   * @param exhibition exhibition
   * @param room room where the group is
   * @param name name
   * @param allowVisitorSessionCreation whether the group allows new visitor session creation
   * @param visitorSessionEndTimeout visitor session end timeout in milliseconds
   * @param creatorId creator's id
   * @param lastModifierId last modifier's id
   * @return created exhibitionDeviceGroup
   */
  fun create(
    id: UUID,
    exhibition: Exhibition,
    room: ExhibitionRoom,
    name: String,
    allowVisitorSessionCreation: Boolean,
    visitorSessionEndTimeout: Long,
    creatorId: UUID,
    lastModifierId: UUID
  ): ExhibitionDeviceGroup {
    val exhibitionDeviceGroup = ExhibitionDeviceGroup()
    exhibitionDeviceGroup.id = id
    exhibitionDeviceGroup.name = name
    exhibitionDeviceGroup.exhibition = exhibition
    exhibitionDeviceGroup.room = room
    exhibitionDeviceGroup.allowVisitorSessionCreation = allowVisitorSessionCreation
    exhibitionDeviceGroup.visitorSessionEndTimeout = visitorSessionEndTimeout
    exhibitionDeviceGroup.creatorId = creatorId
    exhibitionDeviceGroup.lastModifierId = lastModifierId
    return persist(exhibitionDeviceGroup)
  }

  /**
   * Lists exhibition device groups
   *
   * @param exhibition exhibition
   * @param room filter by room. Ignored if null
   * @return List exhibition device groups
   */
  fun list(exhibition: Exhibition, room: ExhibitionRoom?): List<ExhibitionDeviceGroup> {
    val entityManager = getEntityManager()
    val criteriaBuilder = entityManager.criteriaBuilder
    val criteria: CriteriaQuery<ExhibitionDeviceGroup> = criteriaBuilder.createQuery(ExhibitionDeviceGroup::class.java)
    val root: Root<ExhibitionDeviceGroup> = criteria.from(ExhibitionDeviceGroup::class.java)

    val restrictions = ArrayList<Predicate>()
    restrictions.add(criteriaBuilder.equal(root.get(ExhibitionDeviceGroup_.exhibition), exhibition))

    if (room != null) {
      restrictions.add(criteriaBuilder.equal(root.get(ExhibitionDeviceGroup_.room), room))
    }

    criteria.select(root)
    criteria.where(*restrictions.toTypedArray())
    val query: TypedQuery<ExhibitionDeviceGroup> = entityManager.createQuery<ExhibitionDeviceGroup>(criteria)
    return query.getResultList()
  }

  /**
   * Updates name
   *
   * @param exhibitionDeviceGroup exhibition device group to be updated
   * @param name name
   * @param lastModifierId last modifier's id
   * @return updated exhibitionDeviceGroup
   */
  fun updateName(exhibitionDeviceGroup: ExhibitionDeviceGroup, name: String, lastModifierId: UUID): ExhibitionDeviceGroup {
    exhibitionDeviceGroup.lastModifierId = lastModifierId
    exhibitionDeviceGroup.name = name
    return persist(exhibitionDeviceGroup)
  }

  /**
   * Updates allow visitor session creation
   *
   * @param exhibitionDeviceGroup exhibition device group to be updated
   * @param allowVisitorSessionCreation allowVisitorSessionCreation
   * @param lastModifierId last modifier's id
   * @return updated exhibitionDeviceGroup
   */
  fun updateAllowVisitorSessionCreation(exhibitionDeviceGroup: ExhibitionDeviceGroup, allowVisitorSessionCreation: Boolean, lastModifierId: UUID): ExhibitionDeviceGroup {
    exhibitionDeviceGroup.lastModifierId = lastModifierId
    exhibitionDeviceGroup.allowVisitorSessionCreation = allowVisitorSessionCreation
    return persist(exhibitionDeviceGroup)
  }

  /**
   * Updates visitor session end timeout
   *
   * @param exhibitionDeviceGroup exhibition device group to be updated
   * @param visitorSessionEndTimeout visitor session end timeout in milliseconds
   * @param lastModifierId last modifier's id
   * @return updated exhibitionDeviceGroup
   */
  fun updateVisitorSessionEndTimeout(exhibitionDeviceGroup: ExhibitionDeviceGroup, visitorSessionEndTimeout: Long, lastModifierId: UUID): ExhibitionDeviceGroup {
    exhibitionDeviceGroup.lastModifierId = lastModifierId
    exhibitionDeviceGroup.visitorSessionEndTimeout = visitorSessionEndTimeout
    return persist(exhibitionDeviceGroup)
  }

  /**
   * Updates room
   *
   * @param exhibitionDeviceGroup exhibition device group to be updated
   * @param room room
   * @param lastModifierId last modifier's id
   * @return updated exhibitionDeviceGroup
   */
  fun updateRoom(exhibitionDeviceGroup: ExhibitionDeviceGroup, room: ExhibitionRoom, lastModifierId: UUID): ExhibitionDeviceGroup {
    exhibitionDeviceGroup.lastModifierId = lastModifierId
    exhibitionDeviceGroup.room = room
    return persist(exhibitionDeviceGroup)
  }

}