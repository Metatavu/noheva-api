package fi.metatavu.muisti.devices

import fi.metatavu.muisti.api.spec.model.Point
import fi.metatavu.muisti.exhibitions.ExhibitionRoomController
import fi.metatavu.muisti.persistence.dao.RfidAntennaDAO
import fi.metatavu.muisti.persistence.model.*
import fi.metatavu.muisti.utils.CopyException
import fi.metatavu.muisti.utils.IdMapper
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

  @Inject
  private lateinit var roomController: ExhibitionRoomController

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
   * Creates a copy of an antenna
   *
   * @param sourceAntenna source device
   * @param targetDeviceGroup target device for the copied device
   * @param idMapper id mapper
   * @param creatorId id of user that created the copy
   */
  fun copyAntenna(
    sourceAntenna: RfidAntenna,
    targetDeviceGroup: ExhibitionDeviceGroup,
    idMapper: IdMapper,
    creatorId: UUID
  ): RfidAntenna {
    val id = idMapper.getNewId(sourceAntenna.id) ?: throw CopyException("Target antenna id not found")
    val targetExhibition = targetDeviceGroup.exhibition ?: throw CopyException("Target exhibition not found")
    val sourceRoom = sourceAntenna.room ?: throw CopyException("Source room not found")
    val sameExhibition = targetExhibition.id == sourceAntenna.exhibition?.id

    val targetRoom = if (sameExhibition) {
      sourceRoom
    } else {
       val targetRoomId = idMapper.getNewId(sourceRoom.id) ?: throw CopyException("Target room id not found")
       roomController.findExhibitionRoomById(targetRoomId)
    } ?: throw CopyException("Target room not found")

    return rfidAntennaDAO.create(
      id = id,
      exhibition = targetExhibition,
      deviceGroup = targetDeviceGroup,
      room = targetRoom,
      name = sourceAntenna.name ?: throw CopyException("Source antenna name not found"),
      readerId = sourceAntenna.readerId ?: throw CopyException("Source antenna readerId not found"),
      antennaNumber = sourceAntenna.antennaNumber ?: throw CopyException("Source antenna antennaNumber not found"),
      locationX = sourceAntenna.locationX,
      locationY = sourceAntenna.locationY,
      visitorSessionStartThreshold = sourceAntenna.visitorSessionStartThreshold ?: throw CopyException("Source antenna antennaNumber not found"),
      visitorSessionEndThreshold = sourceAntenna.visitorSessionEndThreshold ?: throw CopyException("Source antenna antennaNumber not found"),
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
