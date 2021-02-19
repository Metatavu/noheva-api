package fi.metatavu.muisti.devices

import fi.metatavu.muisti.api.spec.model.DeviceGroupVisitorSessionStartStrategy
import fi.metatavu.muisti.contents.ExhibitionPageController
import fi.metatavu.muisti.exhibitions.ContentVersionController
import fi.metatavu.muisti.exhibitions.GroupContentVersionController
import fi.metatavu.muisti.persistence.dao.ExhibitionDeviceGroupDAO
import fi.metatavu.muisti.persistence.model.*
import fi.metatavu.muisti.utils.CopyException
import fi.metatavu.muisti.utils.IdMapper
import org.slf4j.Logger
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Controller for exhibition device groups
 */
@ApplicationScoped
class ExhibitionDeviceGroupController {

  @Inject
  private lateinit var logger: Logger

  @Inject
  private lateinit var exhibitionDeviceGroupDAO: ExhibitionDeviceGroupDAO

  @Inject
  private lateinit var deviceController: ExhibitionDeviceController

  @Inject
  private lateinit var pageController: ExhibitionPageController

  @Inject
  private lateinit var contentVersionController: ContentVersionController

  @Inject
  private lateinit var groupContentVersionController: GroupContentVersionController

  /**
   * Creates new exhibition device group
   *
   * @param exhibition exhibition
   * @param room room the device group is in
   * @param name device group name
   * @param allowVisitorSessionCreation whether the group allows new visitor session creation
   * @param visitorSessionEndTimeout visitor session end timeout in milliseconds
   * @param visitorSessionStartStrategy visitor session start strategy
   * @param creatorId creating user id
   * @return created exhibition device group
   */
  fun createExhibitionDeviceGroup(
    exhibition: Exhibition,
    room: ExhibitionRoom,
    name: String,
    allowVisitorSessionCreation: Boolean,
    visitorSessionEndTimeout: Long,
    visitorSessionStartStrategy: DeviceGroupVisitorSessionStartStrategy,
    creatorId: UUID
  ): ExhibitionDeviceGroup {
    return exhibitionDeviceGroupDAO.create(
      id = UUID.randomUUID(),
      exhibition = exhibition,
      room = room,
      name = name,
      allowVisitorSessionCreation = allowVisitorSessionCreation,
      visitorSessionEndTimeout = visitorSessionEndTimeout,
      visitorSessionStartStrategy = visitorSessionStartStrategy,
      creatorId = creatorId,
      lastModifierId = creatorId
    )
  }

  /**
   * Finds a device group by id
   *
   * @param id exhibition device group id
   * @return found exhibition device group or null if not found
   */
  fun findDeviceGroupById(id: UUID): ExhibitionDeviceGroup? {
    return exhibitionDeviceGroupDAO.findById(id)
  }

  /**
   * Finds a device group by id
   *
   * @param name name
   * @param room room
   * @return found exhibition device group or null if not found
   */
  fun findDeviceGroupByNameAndRoom(name: String, room: ExhibitionRoom): ExhibitionDeviceGroup? {
    return exhibitionDeviceGroupDAO.findByNameAndRoom(
      name = name,
      room = room
    )
  }

  /**
   * Lists exhibition device groups
   *
   * @param exhibition exhibition
   * @param room filter by room. Ignored if null
   * @return List exhibition device groups
   */
  fun listExhibitionDeviceGroups(exhibition: Exhibition, room: ExhibitionRoom?): List<ExhibitionDeviceGroup> {
    return exhibitionDeviceGroupDAO.list(exhibition, room)
  }

  /**
   * Updates an exhibition device group
   *
   * @param exhibitionDeviceGroup exhibition device group to be updated
   * @param name group name
   * @param allowVisitorSessionCreation whether the group allows new visitor session creation
   * @param visitorSessionEndTimeout visitor session end timeout in milliseconds
   * @param visitorSessionStartStrategy visitor session start strategy
   * @param room room
   * @param modifierId modifying user id
   * @return updated exhibition
   */
  fun updateExhibitionDeviceGroup(
    exhibitionDeviceGroup: ExhibitionDeviceGroup,
    name: String,
    allowVisitorSessionCreation: Boolean,
    visitorSessionEndTimeout: Long,
    visitorSessionStartStrategy: DeviceGroupVisitorSessionStartStrategy,
    room: ExhibitionRoom,
    modifierId: UUID
  ): ExhibitionDeviceGroup {
    var result = exhibitionDeviceGroupDAO.updateName(exhibitionDeviceGroup, name, modifierId)
    result = exhibitionDeviceGroupDAO.updateRoom(result, room, modifierId)
    result = exhibitionDeviceGroupDAO.updateAllowVisitorSessionCreation(result, allowVisitorSessionCreation, modifierId)
    result = exhibitionDeviceGroupDAO.updateVisitorSessionEndTimeout(result, visitorSessionEndTimeout, modifierId)
    result = exhibitionDeviceGroupDAO.updateVisitorSessionStartStrategy(result, visitorSessionStartStrategy, modifierId)
    return result
  }

  /**
   * Copies device group including all it's contents (devices, content versions, group content versions and pages).
   *
   * @param sourceDeviceGroup source device group
   * @param creatorId id of user that created the copy
   * @return copied device group
   */
  fun copyDeviceGroup(
    sourceDeviceGroup: ExhibitionDeviceGroup,
    creatorId: UUID
  ): ExhibitionDeviceGroup {
    logger.debug("Creating copy of device group {}", sourceDeviceGroup.id)

    val idMapper = IdMapper()

    val id = idMapper.assignId(sourceDeviceGroup.id) ?: throw CopyException("Could not assign target source group id")
    val exhibition = sourceDeviceGroup.exhibition ?: throw CopyException("Source device group exhibition not found")
    val sourceName = sourceDeviceGroup.name ?: throw CopyException("Source device group name not found")
    val sourceRoom = sourceDeviceGroup.room ?: throw CopyException("Source device group room not found")

    val name = getUniqueName(
      desiredName = sourceName,
      room = sourceRoom
    )

    val targetDeviceGroup = exhibitionDeviceGroupDAO.create(
      id = id,
      exhibition = exhibition,
      room = sourceRoom,
      name = name,
      allowVisitorSessionCreation = sourceDeviceGroup.allowVisitorSessionCreation ?: throw CopyException("Source device group allowVisitorSessionCreation not found"),
      visitorSessionEndTimeout = sourceDeviceGroup.visitorSessionEndTimeout ?: throw CopyException("Source device group visitorSessionEndTimeout not found"),
      visitorSessionStartStrategy = sourceDeviceGroup.visitorSessionStartStrategy ?: throw CopyException("Source device group visitorSessionStartStrategy not found"),
      creatorId = creatorId,
      lastModifierId = creatorId
    )

    copyResources(
      exhibition = exhibition,
      sourceDeviceGroup = sourceDeviceGroup,
      targetDeviceGroup = targetDeviceGroup,
      idMapper = idMapper,
      creatorId = creatorId
    )

    logger.debug("Copied device group {} -> {}", sourceDeviceGroup.id, targetDeviceGroup.id)

    return targetDeviceGroup
  }

  /**
   * Deletes an exhibition device group
   *
   * @param exhibitionDeviceGroup exhibition device group to be deleted
   */
  fun deleteExhibitionDeviceGroup(exhibitionDeviceGroup: ExhibitionDeviceGroup) {
    return exhibitionDeviceGroupDAO.delete(exhibitionDeviceGroup)
  }

  /**
   * Copies resources related to source device group into target device group
   *
   * @param exhibition exhibition
   * @param sourceDeviceGroup copy source device group
   * @param targetDeviceGroup copy target device group
   * @param idMapper id mapper
   * @param creatorId id of user that created the copy
   */
  private fun copyResources(
    exhibition: Exhibition,
    sourceDeviceGroup: ExhibitionDeviceGroup,
    targetDeviceGroup: ExhibitionDeviceGroup,
    idMapper: IdMapper,
    creatorId: UUID
  ) {
    // Resolve source resources

    val sourceDevices = deviceController.listExhibitionDevices(
      exhibition = exhibition,
      exhibitionDeviceGroup = sourceDeviceGroup
    )

    val sourceGroupContentVersions = groupContentVersionController.listGroupContentVersions(
      exhibition = exhibition,
      deviceGroup = sourceDeviceGroup,
      contentVersion = null
    )

    val sourcePages = pageController.listDeviceGroupPages(
      deviceGroup = sourceDeviceGroup
    )

    val sourceContentVersions = sourceGroupContentVersions
      .mapNotNull(GroupContentVersion::contentVersion)
      .distinctBy(ContentVersion::id)

    // Assign ids for target resources

    sourceDevices.map(ExhibitionDevice::id).map(idMapper::assignId)
    sourceGroupContentVersions.map(GroupContentVersion::id).map(idMapper::assignId)
    sourceContentVersions.map(ContentVersion::id).map(idMapper::assignId)
    sourcePages.map(ExhibitionPage::id).map(idMapper::assignId)

    logger.info(
      "Copying {} devices, {} content versions, {} group content versions and {} pages).",
      sourceDevices.size, sourceContentVersions.size, sourceGroupContentVersions.size, sourcePages.size
    )

    val targetContentVersions = copyContentVersions(
      sourceContentVersions = sourceContentVersions,
      idMapper = idMapper,
      creatorId = creatorId
    )

    val targetDevices = copyDevices(
      sourceDevices = sourceDevices,
      targetDeviceGroup = targetDeviceGroup,
      idMapper = idMapper,
      creatorId = creatorId
    )

    val targetPages = copyPages(
      sourcePages = sourcePages,
      idMapper = idMapper,
      targetDevices = targetDevices,
      targetContentVersions = targetContentVersions,
      creatorId = creatorId
    )

    copyGroupContentVersions(
      sourceGroupContentVersions = sourceGroupContentVersions,
      idMapper = idMapper,
      targetContentVersions = targetContentVersions,
      targetDeviceGroup = targetDeviceGroup,
      creatorId = creatorId
    )

    updateDeviceIdlePages(sourceDevices, idMapper, targetDevices, targetPages, creatorId)
  }

  /**
   * Copies group content versions
   *
   * @param sourceGroupContentVersions copy source group content versions
   * @param targetContentVersions copy target content versions
   * @param targetDeviceGroup copy target device group
   * @param idMapper id mapper
   * @param creatorId id of user that created the copy
   * @return copied content versions
   */
  private fun copyGroupContentVersions(
    sourceGroupContentVersions: List<GroupContentVersion>,
    targetContentVersions: List<ContentVersion>,
    targetDeviceGroup: ExhibitionDeviceGroup,
    idMapper: IdMapper,
    creatorId: UUID
  ): List<GroupContentVersion> {
    return sourceGroupContentVersions.map { sourceGroupContentVersion ->
      val targetContentVersion = getCopyTargetContentVersion(
        idMapper = idMapper,
        source = sourceGroupContentVersion.contentVersion,
        targets = targetContentVersions
      )

      val targetGroupContentVersion = groupContentVersionController.copyGroupContentVersion(
        sourceGroupContentVersion = sourceGroupContentVersion,
        deviceGroup = targetDeviceGroup,
        targetContentVersion = targetContentVersion ?: throw CopyException("Target content version not found"),
        idMapper = idMapper,
        creatorId = creatorId
      )

      logger.debug("Copied group content version {} -> {}", sourceGroupContentVersion.id, targetGroupContentVersion.id)

      targetGroupContentVersion
    }
  }

  /**
   * Copies pages
   *
   * @param sourcePages copy source pages
   * @param targetDevices copy target devices
   * @param targetContentVersions copy target content versions
   * @param idMapper id mapper
   * @param creatorId id of user that created the copy
   * @return copied pages
   */
  private fun copyPages(
    sourcePages: List<ExhibitionPage>,
    targetDevices: List<ExhibitionDevice>,
    targetContentVersions: List<ContentVersion>,
    idMapper: IdMapper,
    creatorId: UUID
  ): List<ExhibitionPage> {
    return sourcePages.map { sourcePage ->
      val targetDevice = getCopyTargetDevice(source = sourcePage.device, idMapper = idMapper, targets = targetDevices)
      val targetContentVersion = getCopyTargetContentVersion(
        idMapper = idMapper,
        source = sourcePage.contentVersion,
        targets = targetContentVersions
      )

      val targetPage = pageController.copyPage(
        sourcePage = sourcePage,
        contentVersion = targetContentVersion ?: throw CopyException("Target content version not found"),
        device = targetDevice ?: throw CopyException("Target device not found"),
        idMapper = idMapper,
        creatorId = creatorId
      )

      logger.debug("Copied content version {} -> {}", sourcePage.id, targetPage.id)

      targetPage
    }
  }

  /**
   * Copies content versions
   *
   * @param sourceContentVersions copy source content versions
   * @param idMapper id mapper
   * @param creatorId id of user that created the copy
   * @return copied pages
   */
  private fun copyContentVersions(
    sourceContentVersions: List<ContentVersion>,
    idMapper: IdMapper,
    creatorId: UUID
  ): List<ContentVersion> {
    return sourceContentVersions.map { sourceContentVersion ->
      val targetContentVersion = contentVersionController.copyContentVersion(
        sourceContentVersion = sourceContentVersion,
        idMapper = idMapper,
        creatorId = creatorId
      )

      logger.debug("Copied content version {} -> {}", sourceContentVersion.id, targetContentVersion.id)

      targetContentVersion
    }
  }

  /**
   * Copies devices
   *
   * @param sourceDevices copy source devices
   * @param targetDeviceGroup copy target device group
   * @param idMapper id mapper
   * @param creatorId id of user that created the copy
   * @return copied devices
   */
  private fun copyDevices(
    sourceDevices: List<ExhibitionDevice>,
    targetDeviceGroup: ExhibitionDeviceGroup,
    idMapper: IdMapper,
    creatorId: UUID
  ): List<ExhibitionDevice> {
    return sourceDevices.map { sourceDevice ->
      val targetDevice = deviceController.copyDevice(
        sourceDevice = sourceDevice,
        deviceGroup = targetDeviceGroup,
        idlePage = null,
        idMapper = idMapper,
        creatorId = creatorId
      )

      logger.debug("Copied device {} -> {}", sourceDevice.id, targetDevice.id)

      targetDevice
    }
  }

  /**
   * Updates idle page for copied devices
   *
   * @param sourceDevices copy source devices
   * @param targetDevices copy target devices
   * @param idMapper id mapper
   * @param targetPages copy target pages
   * @param creatorId id of user that created the copy
   */
  private fun updateDeviceIdlePages(
    sourceDevices: List<ExhibitionDevice>,
    idMapper: IdMapper,
    targetDevices: List<ExhibitionDevice>,
    targetPages: List<ExhibitionPage>,
    creatorId: UUID
  ) {
    sourceDevices.forEach { sourceDevice ->
      val sourceIdlePage = sourceDevice.idlePage ?: return@forEach
      val targetDevice = getCopyTargetDevice(source = sourceDevice, idMapper = idMapper, targets = targetDevices)
      val targetIdlePage = getCopyTargetPage(source = sourceIdlePage, idMapper = idMapper, targets = targetPages)

      deviceController.updateDeviceIdlePage(
        device = targetDevice ?: throw CopyException("Target device not found"),
        idlePage = targetIdlePage ?: throw CopyException("Target idle page not found"),
        modifierId = creatorId
      )
    }
  }

  /**
   * Resolves copy target device for source device
   *
   * @param idMapper id mapper
   * @param targets target devices
   * @param source source device
   * @return target device or null if not found
   */
  private fun getCopyTargetDevice(idMapper: IdMapper, targets: List<ExhibitionDevice>, source: ExhibitionDevice?): ExhibitionDevice? {
    val targetId = idMapper.getNewId(source?.id)
    return targets.find { it.id == targetId }
  }

  /**
   * Resolves copy target content version for source content vesion
   *
   * @param idMapper id mapper
   * @param targets target content versions
   * @param source source content version
   * @return target content version or null if not found
   */
  private fun getCopyTargetContentVersion(idMapper: IdMapper, targets: List<ContentVersion>, source: ContentVersion?): ContentVersion? {
    val targetId = idMapper.getNewId(source?.id)
    return targets.find { it.id == targetId }
  }

  /**
   * Resolves copy target page for source page
   *
   * @param idMapper id mapper
   * @param targets target pages
   * @param source source page
   * @return target page or null if not found
   */
  private fun getCopyTargetPage(idMapper: IdMapper, targets: List<ExhibitionPage>, source: ExhibitionPage?): ExhibitionPage? {
    val targetId = idMapper.getNewId(source?.id)
    return targets.find { it.id == targetId }
  }

  /**
   * Returns unique name for device group
   *
   * @param desiredName desired name
   * @param room room of device group
   * @return unique name for device group
   */
  private fun getUniqueName(
    desiredName: String,
    room: ExhibitionRoom
  ): String {
    var result = desiredName
    var index = 1

    do {
      findDeviceGroupByNameAndRoom(
        name = result,
        room = room
      ) ?: return result

      index++

      result = "$desiredName $index"
    } while (true)
  }

}
