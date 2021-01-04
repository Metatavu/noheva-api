package fi.metatavu.muisti.devices

import fi.metatavu.muisti.api.spec.model.Point
import fi.metatavu.muisti.api.spec.model.ScreenOrientation
import fi.metatavu.muisti.persistence.dao.ExhibitionDeviceDAO
import fi.metatavu.muisti.persistence.model.*
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
     * Deletes an exhibition device 
     *
     * @param exhibitionDevice exhibition device to be deleted
     */
    fun deleteExhibitionDevice(exhibitionDevice: ExhibitionDevice) {
        return exhibitionDeviceDAO.delete(exhibitionDevice)
    }

}
