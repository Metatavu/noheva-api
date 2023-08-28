package fi.metatavu.noheva.devices

import fi.metatavu.noheva.api.spec.model.DeviceImageLoadStrategy
import fi.metatavu.noheva.api.spec.model.Point
import fi.metatavu.noheva.api.spec.model.ScreenOrientation
import fi.metatavu.noheva.persistence.dao.ExhibitionDeviceDAO
import fi.metatavu.noheva.persistence.model.*
import fi.metatavu.noheva.utils.CopyException
import fi.metatavu.noheva.utils.IdMapper
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Controller for exhibition devices
 */
@ApplicationScoped
class ExhibitionDeviceController {

    @Inject
    lateinit var exhibitionDeviceDAO: ExhibitionDeviceDAO

    /**
     * Creates new exhibition device 
     *
     * @param exhibition exhibition
     * @param exhibitionDeviceGroup exhibition device group
     * @param device device
     * @param name device name
     * @param location location
     * @param screenOrientation screen orientation
     * @param imageLoadStrategy image load strategy
     * @param idlePage idle page
     * @param creatorId creating user id
     * @return created exhibition device 
     */
    fun createExhibitionDevice(
        exhibition: Exhibition,
        exhibitionDeviceGroup: ExhibitionDeviceGroup,
        device: Device?,
        name: String,
        location: Point?,
        screenOrientation: ScreenOrientation,
        imageLoadStrategy: DeviceImageLoadStrategy,
        idlePage: ExhibitionPage?,
        creatorId: UUID
    ): ExhibitionDevice {
        return exhibitionDeviceDAO.create(
            id = UUID.randomUUID(),
            exhibition = exhibition,
            exhibitionDeviceGroup = exhibitionDeviceGroup,
            device = device,
            name = name,
            locationX = location?.x,
            locationY = location?.y,
            screenOrientation = screenOrientation,
            imageLoadStrategy = imageLoadStrategy,
            idlePage = idlePage,
            creatorId = creatorId,
            lastModifierId = creatorId
        )
    }

    /**
     * Creates a copy of a device
     *
     * @param sourceDevice source device
     * @param targetDeviceGroup target device for the copied device
     * @param idlePage target idle page for the copied device
     * @param idMapper id mapper
     * @param creatorId id of user that created the copy
     */
    fun copyDevice(
        sourceDevice: ExhibitionDevice,
        targetDeviceGroup: ExhibitionDeviceGroup,
        idlePage: ExhibitionPage?,
        idMapper: IdMapper,
        creatorId: UUID
    ): ExhibitionDevice {
        val id = idMapper.getNewId(sourceDevice.id) ?: throw CopyException("Target device id not found")
        val targetExhibition = targetDeviceGroup.exhibition ?: throw CopyException("Target exhibition not found")

        return exhibitionDeviceDAO.create(
            id = id,
            exhibition = targetExhibition,
            exhibitionDeviceGroup = targetDeviceGroup,
            device = sourceDevice.device,
            name = sourceDevice.name ?: throw CopyException("Source device name not found"),
            locationX = sourceDevice.locationX,
            locationY = sourceDevice.locationY,
            screenOrientation = sourceDevice.screenOrientation ?: throw CopyException("Source device screen orientation not found"),
            imageLoadStrategy = sourceDevice.imageLoadStrategy ?: throw CopyException("Source device image load strategy not found"),
            idlePage = idlePage,
            creatorId = creatorId,
            lastModifierId = creatorId
        )
    }

    /**
     * Finds an exhibition device by id
     *
     * @param id exhibition device id
     * @return found exhibition device or null if not found
     */
    fun findExhibitionDeviceById(id: UUID): ExhibitionDevice? {
        return exhibitionDeviceDAO.findById(id)
    }

    /**
     * Lists devices in an exhibitions
     *
     * @param exhibition exhibition
     * @param exhibitionDeviceGroup filter by exhibition device group
     * @param deviceModel filter by device model
     * @returns all devices in an exhibition
     */
    fun listExhibitionDevices(
        exhibition: Exhibition,
        exhibitionDeviceGroup: ExhibitionDeviceGroup?,
        deviceModel: DeviceModel?
    ): List<ExhibitionDevice> {
        return exhibitionDeviceDAO.list(
            exhibition = exhibition,
            exhibitionDeviceGroup = exhibitionDeviceGroup,
            deviceModel = deviceModel
        )
    }

    /**
     * Lists exhibition devices by device
     *
     * @param device device
     * @return list of exhibition devices
     */
    fun listByDevice(device: Device): List<ExhibitionDevice> {
        return exhibitionDeviceDAO.listByDevice(device = device)
    }

    /**
     * Lists devices by idle page
     *
     * @param idlePage device idle page
     * @return List of devices
     */
    fun listDevicesByIdlePage(idlePage: ExhibitionPage): List<ExhibitionDevice> {
        return exhibitionDeviceDAO.listByIdlePage(idlePage = idlePage)
    }

    /**
     * Updates an exhibition device
     *
     * @param exhibitionDevice exhibition device to be updated
     * @param exhibitionDeviceGroup exhibition device group
     * @param device device
     * @param name device name
     * @param location location
     * @param screenOrientation screen orientation
     * @param imageLoadStrategy image load strategy
     * @param idlePage idle page
     * @param modifierId modifying user id
     * @return updated exhibition
     */
    fun updateExhibitionDevice(
        exhibitionDevice: ExhibitionDevice,
        exhibitionDeviceGroup: ExhibitionDeviceGroup,
        device: Device?,
        name: String,
        location: Point?,
        screenOrientation: ScreenOrientation,
        imageLoadStrategy: DeviceImageLoadStrategy,
        idlePage: ExhibitionPage?,
        modifierId: UUID
    ): ExhibitionDevice {
        var result = exhibitionDeviceDAO.updateName(exhibitionDevice, name, modifierId)
        result = exhibitionDeviceDAO.updateExhibitionDeviceGroup(result, exhibitionDeviceGroup, modifierId)
        result = exhibitionDeviceDAO.updateExhibitionDevicesDevice(result, device, modifierId)
        result = exhibitionDeviceDAO.updateLocationX(result, location?.x, modifierId)
        result = exhibitionDeviceDAO.updateLocationY(result, location?.y, modifierId)
        result = exhibitionDeviceDAO.updateScreenOrientation(result, screenOrientation, modifierId)
        result = exhibitionDeviceDAO.updateImageLoadStrategy(result, imageLoadStrategy, modifierId)
        result = exhibitionDeviceDAO.updateIdlePage(result, idlePage, modifierId)
        return result
    }

    /**
     * Updates device's idle page
     *
     * @param device device
     * @param idlePage idle page
     * @param modifierId modifying user id
     * @return updated page
     */
    fun updateDeviceIdlePage(device: ExhibitionDevice, idlePage: ExhibitionPage?, modifierId: UUID): ExhibitionDevice {
        return exhibitionDeviceDAO.updateIdlePage(device, idlePage, modifierId)
    }

    /**
     * Deletes an exhibition device 
     *
     * @param exhibitionDevice exhibition device to be deleted
     */
    fun deleteExhibitionDevice(exhibitionDevice: ExhibitionDevice) {
        return exhibitionDeviceDAO.delete(exhibitionDevice)
    }

}
