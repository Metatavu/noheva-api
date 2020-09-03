package fi.metatavu.muisti.devices

import fi.metatavu.muisti.api.spec.model.Point
import fi.metatavu.muisti.persistence.dao.RfidAntennaDAO
import fi.metatavu.muisti.persistence.model.Exhibition
import fi.metatavu.muisti.persistence.model.ExhibitionDeviceGroup
import fi.metatavu.muisti.persistence.model.ExhibitionRoom
import fi.metatavu.muisti.persistence.model.RfidAntenna
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Controller for RFID antennas
 */
@ApplicationScoped
class RfidAntennaController {

  @Inject
  private lateinit var rfidAntennaDAO: RfidAntennaDAO

  /**
   * Creates new RFID antenna
   *
   * @param exhibition exhibition
   * @param deviceGroup deviceGroup
   * @param room room
   * @param name Human-readable name for the antenna
   * @param readerId RFID reader module id
   * @param antennaNumber RFID antenna number
   * @param location location
   * @param visitorSessionStartThreshold visitor session start threshold (%)
   * @param visitorSessionEndThreshold visitor session end threshold (%)
   * @param creatorId creator's id
   * @return created rfidAntenna
   */
  fun createRfidAntenna(
    exhibition: Exhibition,
    deviceGroup: ExhibitionDeviceGroup?,
    room: ExhibitionRoom?,
    name: String,
    readerId: String,
    antennaNumber: Int,
    location: Point,
    visitorSessionStartThreshold: Int,
    visitorSessionEndThreshold: Int,
    creatorId: UUID
  ): RfidAntenna {
    return rfidAntennaDAO.create(id = UUID.randomUUID(),
      exhibition = exhibition,
      deviceGroup = deviceGroup,
      room = room,
      name = name,
      readerId = readerId,
      antennaNumber = antennaNumber,
      locationX = location.x,
      locationY = location.y,
      visitorSessionStartThreshold = visitorSessionStartThreshold,
      visitorSessionEndThreshold = visitorSessionEndThreshold,
      creatorId = creatorId,
      lastModifierId = creatorId
    )
  }

  /**
   * Finds an RFID antenna by id
   *
   * @param id RFID antenna id
   * @return found RFID antenna or null if not found
   */
  fun findRfidAntennaById(id: UUID): RfidAntenna? {
    return rfidAntennaDAO.findById(id)
  }

  /**
   * Lists RFID antennas
   *
   * @param exhibition exhibition to list antennas from
   * @param deviceGroup filter by device group
   * @param room filter by room
   * @returns RFID antennas
   */
  fun listRfidAntennas(exhibition: Exhibition,  deviceGroup: ExhibitionDeviceGroup?, room: ExhibitionRoom?): List<RfidAntenna> {
    return rfidAntennaDAO.list(
      exhibition = exhibition,
      deviceGroup = deviceGroup,
      room = room
    )
  }

  /**
   * Updates an RFID antenna
   *
   * @param rfidAntenna RFID antenna to be updated
   * @param deviceGroup deviceGroup
   * @param room room
   * @param name Human-readable name for the antenna
   * @param readerId RFID reader module id
   * @param antennaNumber RFID antenna number
   * @param location location
   * @param visitorSessionStartThreshold visitor session start threshold (%)
   * @param visitorSessionEndThreshold visitor session end threshold (%)
   * @param modifierId modifying user id
   * @return created rfidAntenna
   */
  fun updateRfidAntenna(
    rfidAntenna: RfidAntenna,
    deviceGroup: ExhibitionDeviceGroup?,
    room: ExhibitionRoom?,
    name: String,
    readerId: String,
    antennaNumber: Int,
    location: Point,
    visitorSessionStartThreshold: Int,
    visitorSessionEndThreshold: Int,
    modifierId: UUID
  ): RfidAntenna {
    var result = rfidAntennaDAO.updateName(rfidAntenna, name, modifierId)
    result = rfidAntennaDAO.updateDeviceGroup(result, deviceGroup, modifierId)
    result = rfidAntennaDAO.updateRoom(result, room, modifierId)
    result = rfidAntennaDAO.updateReaderId(result, readerId, modifierId)
    result = rfidAntennaDAO.updateAntennaNumber(result, antennaNumber, modifierId)
    result = rfidAntennaDAO.updateLocationX(result, location.x, modifierId)
    result = rfidAntennaDAO.updateLocationY(result, location.y, modifierId)
    result = rfidAntennaDAO.updateVisitorSessionStartThreshold(result, visitorSessionStartThreshold, modifierId)
    result = rfidAntennaDAO.updateVisitorSessionEndThreshold(result, visitorSessionEndThreshold, modifierId)
    return result
  }

  /**
   * Deletes an RFID antenna
   *
   * @param rfidAntenna RFID antenna to be deleted
   */
  fun deleteRfidAntenna(rfidAntenna: RfidAntenna) {
    return rfidAntennaDAO.delete(rfidAntenna)
  }
}
