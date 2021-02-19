package fi.metatavu.muisti.devices

import fi.metatavu.muisti.api.spec.model.Point
import fi.metatavu.muisti.api.spec.model.ScreenOrientation
import fi.metatavu.muisti.persistence.dao.ExhibitionDeviceDAO
import fi.metatavu.muisti.persistence.model.*
import fi.metatavu.muisti.utils.CopyException
import fi.metatavu.muisti.utils.IdMapper
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Controller for exhibition devices
 */
@ApplicationScoped
class ExhibitionDeviceController {

    @Inject
    private lateinit var exhibitionDeviceDAO: ExhibitionDeviceDAO

    /**
     * Creates new exhibition device 
     *
     * @param exhibition exhibition
     * @param exhibitionDeviceGroup exhibition device group
     * @param deviceModel device model
     * @param name device name
     * @param location location
     * @param screenOrientation screen orientation
     * @param idlePage idle page
     * @param creatorId creating user id
     * @return created exhibition device 
     */
    fun createExhibitionDevice(exhibition: Exhibition, exhibitionDeviceGroup: ExhibitionDeviceGroup, deviceModel: DeviceModel, name: String, location: Point?, screenOrientation: ScreenOrientation, idlePage: ExhibitionPage?, creatorId: UUID): ExhibitionDevice {
        return exhibitionDeviceDAO.create(id = UUID.randomUUID(),
            exhibition = exhibition,
            exhibitionDeviceGroup = exhibitionDeviceGroup,
            deviceModel = deviceModel,
            name = name,
            locationX = location?.x,
            locationY = location?.y,
            screenOrientation = screenOrientation,
            idlePage = idlePage,
            creatorId = creatorId,
            lastModifierId = creatorId
        )
    }

    /**
     * Creates a copy of a device
     *
     * @param sourceDevice source device
     * @param deviceGroup target device for the copied device
     * @param idlePage target idle page for the copied device
     * @param idMapper id mapper
     * @param creatorId id of user that created the copy
     */
    fun copyDevice(
        sourceDevice: ExhibitionDevice,
        deviceGroup: ExhibitionDeviceGroup,
        idlePage: ExhibitionPage?,
        idMapper: IdMapper,
        creatorId: UUID
    ): ExhibitionDevice {
        val id = idMapper.getNewId(sourceDevice.id) ?: throw CopyException("Target device id not found")

        return exhibitionDeviceDAO.create(
            id = id,
            exhibition = sourceDevice.exhibition ?: throw CopyException("Source device exhibition not found"),
            exhibitionDeviceGroup = deviceGroup,
            deviceModel = sourceDevice.deviceModel ?: throw CopyException("Source device model not found"),
            name = sourceDevice.name ?: throw CopyException("Source device name not found"),
            locationX = sourceDevice.locationX,
            locationY = sourceDevice.locationY,
            screenOrientation = sourceDevice.screenOrientation ?: throw CopyException("Source device screen orientation not found"),
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
     * Lists device s in an exhibitions
     *
     * @returns all devices in an exhibition
     */
    fun listExhibitionDevices(exhibition: Exhibition, exhibitionDeviceGroup: ExhibitionDeviceGroup?): List<ExhibitionDevice> {
        return exhibitionDeviceDAO.list(exhibition, exhibitionDeviceGroup)
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
     * @param deviceModel device model
     * @param name device name
     * @param location location
     * @param screenOrientation screen orientation
     * @param idlePage idle page
     * @param modifierId modifying user id
     * @return updated exhibition
     */
    fun updateExhibitionDevice(exhibitionDevice: ExhibitionDevice, exhibitionDeviceGroup: ExhibitionDeviceGroup, deviceModel: DeviceModel, name: String, location: Point?, screenOrientation: ScreenOrientation, idlePage: ExhibitionPage?, modifierId: UUID): ExhibitionDevice {
        var result = exhibitionDeviceDAO.updateName(exhibitionDevice, name, modifierId)
        result = exhibitionDeviceDAO.updateExhibitionDeviceGroup(result, exhibitionDeviceGroup, modifierId)
        result = exhibitionDeviceDAO.updateExhibitionDeviceModel(result, deviceModel, modifierId)
        result = exhibitionDeviceDAO.updateLocationX(result, location?.x, modifierId)
        result = exhibitionDeviceDAO.updateLocationY(result, location?.y, modifierId)
        result = exhibitionDeviceDAO.updateScreenOrientation(result, screenOrientation, modifierId)
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
