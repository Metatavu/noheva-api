package fi.metatavu.muisti.devices

import fi.metatavu.muisti.api.spec.model.Point
import fi.metatavu.muisti.api.spec.model.ScreenOrientation
import fi.metatavu.muisti.persistence.dao.ExhibitionDeviceDAO
import fi.metatavu.muisti.persistence.model.DeviceModel
import fi.metatavu.muisti.persistence.model.Exhibition
import fi.metatavu.muisti.persistence.model.ExhibitionDevice
import fi.metatavu.muisti.persistence.model.ExhibitionDeviceGroup
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Controller for exhibition device s
 */
@ApplicationScoped
class ExhibitionDeviceController() {

    @Inject
    private lateinit var exhibitionDeviceDAO: ExhibitionDeviceDAO

    /**
     * Creates new exhibition device 
     *
     * @param exhibition exhibition
     * @param exhibitionDeviceGroup exhibition device group
     * @param name device  name
     * @param location location
     * @param screenOrientation screen orientation
     * @param creatorId creating user id
     * @return created exhibition device 
     */
    fun createExhibitionDevice(exhibition: Exhibition, exhibitionDeviceGroup: ExhibitionDeviceGroup, deviceModel: DeviceModel, name: String, location: Point?, screenOrientation: ScreenOrientation, creatorId: UUID): ExhibitionDevice {
        return exhibitionDeviceDAO.create(UUID.randomUUID(), exhibition, exhibitionDeviceGroup, deviceModel, name, location?.x, location?.y, screenOrientation, creatorId, creatorId)
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
     * @param name name
     * @param location location
     * @param screenOrientation screen orientation
     * @param deviceModel model
     * @param modifierId modifying user id
     * @return updated exhibition
     */
    fun updateExhibitionDevice(exhibitionDevice: ExhibitionDevice, deviceModel: DeviceModel, name: String, location: Point?, screenOrientation: ScreenOrientation, modifierId: UUID): ExhibitionDevice {
        var result = exhibitionDeviceDAO.updateName(exhibitionDevice, name, modifierId)
        result = exhibitionDeviceDAO.updateLocationX(result, location?.x, modifierId)
        result = exhibitionDeviceDAO.updateLocationY(result, location?.y, modifierId)
        result = exhibitionDeviceDAO.updateScreenOrientation(result, screenOrientation, modifierId)
        result = exhibitionDeviceDAO.updateExhibitionDeviceModel(result, deviceModel, modifierId)
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