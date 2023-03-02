package fi.metatavu.muisti.persistence.dao

import fi.metatavu.muisti.persistence.model.*
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.persistence.TypedQuery
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root
import kotlin.collections.ArrayList

/**
 * DAO class for RfidAntenna entity
 *
 * @author Antti Leppä
 */
@ApplicationScoped
class RfidAntennaDAO : AbstractDAO<RfidAntenna>() {

  /**
   * Creates new RFID antenna
   *
   * @param id id
   * @param exhibition exhibition
   * @param deviceGroup deviceGroup
   * @param room room
   * @param name Human-readable name for the antenna
   * @param readerId RFID reader module id
   * @param antennaNumber RFID antenna number
   * @param locationX location x
   * @param locationY location y
   * @param visitorSessionStartThreshold visitor session start threshold (%)
   * @param visitorSessionEndThreshold visitor session end threshold (%)
   * @param creatorId creator's id
   * @param lastModifierId last modifier's id
   * @return created rfidAntenna
   */
  fun create(
    id: UUID,
    exhibition: Exhibition,
    deviceGroup: ExhibitionDeviceGroup?,
    room: ExhibitionRoom,
    name: String,
    readerId: String,
    antennaNumber: Int,
    locationX: Double?,
    locationY: Double?,
    visitorSessionStartThreshold: Int,
    visitorSessionEndThreshold: Int,
    creatorId: UUID,
    lastModifierId: UUID
  ): RfidAntenna {
    val rfidAntenna = RfidAntenna()
    rfidAntenna.id = id
    rfidAntenna.exhibition = exhibition
    rfidAntenna.room = room
    rfidAntenna.deviceGroup = deviceGroup
    rfidAntenna.name = name
    rfidAntenna.readerId = readerId
    rfidAntenna.antennaNumber = antennaNumber
    rfidAntenna.locationX = locationX
    rfidAntenna.locationY = locationY
    rfidAntenna.visitorSessionStartThreshold = visitorSessionStartThreshold
    rfidAntenna.visitorSessionEndThreshold = visitorSessionEndThreshold
    rfidAntenna.creatorId = creatorId
    rfidAntenna.lastModifierId = lastModifierId
    return persist(rfidAntenna)
  }

  /**
   * Lists RFID antennas
   *
   * @param exhibition filter by exhibition
   * @param deviceGroup filter by device group. Ignored if null is passed
   * @param room filter by room. Ignored if null is passed
   * @return List of RFID antennas
   */
  fun list(exhibition: Exhibition, deviceGroup: ExhibitionDeviceGroup?, room: ExhibitionRoom?): List<RfidAntenna> {
    
    val criteriaBuilder = getEntityManager().criteriaBuilder
    val criteria: CriteriaQuery<RfidAntenna> = criteriaBuilder.createQuery(RfidAntenna::class.java)
    val root: Root<RfidAntenna> = criteria.from(RfidAntenna::class.java)

    val restrictions = ArrayList<Predicate>()
    restrictions.add(criteriaBuilder.equal(root.get(RfidAntenna_.exhibition), exhibition))

    if (deviceGroup != null) {
        restrictions.add(criteriaBuilder.equal(root.get(RfidAntenna_.deviceGroup), deviceGroup))
    }

    if (room != null) {
        restrictions.add(criteriaBuilder.equal(root.get(RfidAntenna_.room), room))
    }

    criteria.select(root)
    criteria.where(*restrictions.toTypedArray())

    val query: TypedQuery<RfidAntenna> = getEntityManager().createQuery(criteria)
    return query.resultList
  }

  /**
   * Updates RFID reader module id
   *
   * @param rfidAntenna RFID antenna to be updated
   * @param readerId RFID reader module id
   * @param lastModifierId last modifier's id
   * @return updated rfidAntenna
   */
  fun updateReaderId(rfidAntenna: RfidAntenna, readerId: String, lastModifierId: UUID): RfidAntenna {
    rfidAntenna.lastModifierId = lastModifierId
    rfidAntenna.readerId = readerId
    return persist(rfidAntenna)
  }

  /**
   * Updates antenna number
   *
   * @param rfidAntenna RFID antenna to be updated
   * @param antennaNumber antenna number
   * @param lastModifierId last modifier's id
   * @return updated rfidAntenna
   */
  fun updateAntennaNumber(rfidAntenna: RfidAntenna, antennaNumber: Int, lastModifierId: UUID): RfidAntenna {
    rfidAntenna.lastModifierId = lastModifierId
    rfidAntenna.antennaNumber = antennaNumber
    return persist(rfidAntenna)
  }

  /**
   * Updates name
   *
   * @param rfidAntenna RFID antenna to be updated
   * @param name name
   * @param lastModifierId last modifier's id
   * @return updated rfidAntenna
   */
  fun updateName(rfidAntenna: RfidAntenna, name: String, lastModifierId: UUID): RfidAntenna {
    rfidAntenna.lastModifierId = lastModifierId
    rfidAntenna.name = name
    return persist(rfidAntenna)
  }

  /**
   * Updates location x
   *
   * @param rfidAntenna RFID antenna to be updated
   * @param locationX location X
   * @param lastModifierId last modifier's id
   * @return updated rfidAntenna
   */
  fun updateLocationX(rfidAntenna: RfidAntenna, locationX: Double?, lastModifierId: UUID): RfidAntenna {
    rfidAntenna.lastModifierId = lastModifierId
    rfidAntenna.locationX = locationX
    return persist(rfidAntenna)
  }

  /**
   * Updates location y
   *
   * @param rfidAntenna RFID antenna to be updated
   * @param locationY location y
   * @param lastModifierId last modifier's id
   * @return updated rfidAntenna
   */
  fun updateLocationY(rfidAntenna: RfidAntenna, locationY: Double?, lastModifierId: UUID): RfidAntenna {
    rfidAntenna.lastModifierId = lastModifierId
    rfidAntenna.locationY = locationY
    return persist(rfidAntenna)
  }

  /**
   * Updates RFID antenna group
   *
   * @param rfidAntenna RFID antenna to be updated
   * @param deviceGroup RFID antenna group
   * @param lastModifierId last modifier's id
   * @return updated rfidAntenna
   */
  fun updateDeviceGroup(rfidAntenna: RfidAntenna, deviceGroup: ExhibitionDeviceGroup?, lastModifierId: UUID): RfidAntenna {
    rfidAntenna.lastModifierId = lastModifierId
    rfidAntenna.deviceGroup = deviceGroup
    return persist(rfidAntenna)
  }

  /**
   * Updates RFID antenna room
   *
   * @param rfidAntenna RFID antenna to be updated
   * @param room room where the antenna is located
   * @param lastModifierId last modifier's id
   * @return updated rfidAntenna
   */
  fun updateRoom(rfidAntenna: RfidAntenna, room: ExhibitionRoom, lastModifierId: UUID): RfidAntenna {
    rfidAntenna.lastModifierId = lastModifierId
    rfidAntenna.room = room
    return persist(rfidAntenna)
  }

  /**
   * Updates RFID antenna room
   *
   * @param rfidAntenna RFID antenna to be updated
   * @param visitorSessionStartThreshold visitor session start threshold (%)
   * @param lastModifierId last modifier's id
   * @return updated rfidAntenna
   */
  fun updateVisitorSessionStartThreshold(rfidAntenna: RfidAntenna, visitorSessionStartThreshold: Int, lastModifierId: UUID): RfidAntenna {
    rfidAntenna.lastModifierId = lastModifierId
    rfidAntenna.visitorSessionStartThreshold = visitorSessionStartThreshold
    return persist(rfidAntenna)
  }

  /**
   * Updates RFID antenna room
   *
   * @param rfidAntenna RFID antenna to be updated
   * @param visitorSessionEndThreshold visitor session end threshold (%)
   * @param lastModifierId last modifier's id
   * @return updated rfidAntenna
   */
  fun updateVisitorSessionEndThreshold(rfidAntenna: RfidAntenna, visitorSessionEndThreshold: Int?, lastModifierId: UUID): RfidAntenna {
    rfidAntenna.lastModifierId = lastModifierId
    rfidAntenna.visitorSessionEndThreshold = visitorSessionEndThreshold
    return persist(rfidAntenna)
  }

}
