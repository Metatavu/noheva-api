package fi.metatavu.muisti.persistence.dao

import fi.metatavu.muisti.api.spec.model.DeviceGroupVisitorSessionStartStrategy
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
   * @param visitorSessionStartStrategy visitor session start strategy
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
    visitorSessionStartStrategy: DeviceGroupVisitorSessionStartStrategy,
    indexPageTimeout: Long?,
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
    exhibitionDeviceGroup.visitorSessionStartStrategy = visitorSessionStartStrategy
    exhibitionDeviceGroup.indexPageTimeout = indexPageTimeout
    exhibitionDeviceGroup.creatorId = creatorId
    exhibitionDeviceGroup.lastModifierId = lastModifierId
    return persist(exhibitionDeviceGroup)
  }

  /**
   * Finds a device group name and room
   *
   * @param name name
   * @param room room
   * @return found exhibition device group or null if not found
   */
  fun findByNameAndRoom(name: String, room: ExhibitionRoom?): ExhibitionDeviceGroup? {
    val entityManager = getEntityManager()
    val criteriaBuilder = entityManager.criteriaBuilder
    val criteria: CriteriaQuery<ExhibitionDeviceGroup> = criteriaBuilder.createQuery(ExhibitionDeviceGroup::class.java)
    val root: Root<ExhibitionDeviceGroup> = criteria.from(ExhibitionDeviceGroup::class.java)

    criteria.select(root)
    criteria.where(criteriaBuilder.and(
      criteriaBuilder.equal(root.get(ExhibitionDeviceGroup_.name), name),
      criteriaBuilder.equal(root.get(ExhibitionDeviceGroup_.room), room)
    ))

    return getSingleResult(entityManager.createQuery<ExhibitionDeviceGroup>(criteria))
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
   * Updates visitor session start strategy
   *
   * @param exhibitionDeviceGroup exhibition device group to be updated
   * @param visitorSessionStartStrategy visitor session start strategy
   * @param lastModifierId last modifier's id
   * @return updated exhibitionDeviceGroup
   */
  fun updateVisitorSessionStartStrategy(exhibitionDeviceGroup: ExhibitionDeviceGroup, visitorSessionStartStrategy: DeviceGroupVisitorSessionStartStrategy, lastModifierId: UUID): ExhibitionDeviceGroup {
    exhibitionDeviceGroup.lastModifierId = lastModifierId
    exhibitionDeviceGroup.visitorSessionStartStrategy = visitorSessionStartStrategy
    return persist(exhibitionDeviceGroup)
  }

  /**
   * Updates visitor session start strategy
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
   * Updates visitor session index page timeout
   *
   * @param exhibitionDeviceGroup exhibition device group to be updated
   * @param indexPageTimeout visitor session index page timeout in milliseconds
   * @param lastModifierId last modifier's id
   * @return updated exhibitionDeviceGroup
   */
  fun updateIndexPageTimeout(exhibitionDeviceGroup: ExhibitionDeviceGroup, indexPageTimeout: Long?, lastModifierId: UUID): ExhibitionDeviceGroup {
    exhibitionDeviceGroup.lastModifierId = lastModifierId
    exhibitionDeviceGroup.indexPageTimeout = indexPageTimeout
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
