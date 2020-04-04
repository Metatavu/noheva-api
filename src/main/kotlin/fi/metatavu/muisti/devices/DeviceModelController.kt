package fi.metatavu.muisti.devices

import fi.metatavu.muisti.api.spec.model.DeviceModelDisplayMetrics
import fi.metatavu.muisti.persistence.dao.DeviceModelDAO
import fi.metatavu.muisti.persistence.model.DeviceModel
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Controller for exhibition device models
 */
@ApplicationScoped
class DeviceModelController() {

    @Inject
    private lateinit var deviceModelDAO: DeviceModelDAO

    /**
     * Creates new exhibition device model
     *
     * @param manufacturer device manufacturer
     * @param model device model
     * @param dimensionWidth device physical width
     * @param dimensionHeight device physical height
     * @param displayMetrics display metrics
     * @param capabilityTouch whether device has touch capability
     * @param creatorId creating user id
     * @return created exhibition device model
     */
    fun createDeviceModel(manufacturer: String, model: String, dimensionWidth: Double?, dimensionHeight: Double?, displayMetrics: DeviceModelDisplayMetrics, capabilityTouch: Boolean, creatorId: UUID): DeviceModel {
        return deviceModelDAO.create(UUID.randomUUID(),
                manufacturer = manufacturer,
                model = model,
                dimensionWidth = dimensionWidth,
                dimensionHeight = dimensionHeight,
                widthPixels = displayMetrics.widthPixels,
                heightPixels = displayMetrics.heightPixels,
                density = displayMetrics.density,
                xdpi = displayMetrics.xdpi,
                ydpi = displayMetrics.ydpi,
                capabilityTouch = capabilityTouch,
                creatorId = creatorId,
                lastModifierId = creatorId)
    }

    /**
     * Finds an exhibition device model by id
     *
     * @param id exhibition device model id
     * @return found exhibition device model or null if not found
     */
    fun findDeviceModelById(id: UUID): DeviceModel? {
        return deviceModelDAO.findById(id)
    }

    /**
     * Lists device models
     *
     * @returns all deviceModels
     */
    fun listDeviceModels(): List<DeviceModel> {
        return deviceModelDAO.listAll()
    }

    /**
     * Updates an exhibition device model
     *
     * @param deviceModel exhibition device model to be updated
     * @param manufacturer device manufacturer
     * @param model device model
     * @param dimensionWidth device physical width
     * @param dimensionHeight device physical height
     * @param displayMetrics display metrics
     * @param capabilityTouch whether device has touch capability
     * @param modifierId modifying user id
     * @return updated exhibition
     */
    fun updateDeviceModel(deviceModel: DeviceModel, manufacturer: String, model: String, dimensionWidth: Double?, dimensionHeight: Double?, displayMetrics: DeviceModelDisplayMetrics, capabilityTouch: Boolean, modifierId: UUID): DeviceModel {
        deviceModelDAO.updateManufacturer(deviceModel, manufacturer, modifierId)
        deviceModelDAO.updateModel(deviceModel, model, modifierId)
        deviceModelDAO.updateDimensionWidth(deviceModel, dimensionWidth, modifierId)
        deviceModelDAO.updateDimensionHeight(deviceModel, dimensionHeight, modifierId)
        deviceModelDAO.updateWidthPixels(deviceModel, displayMetrics.widthPixels, modifierId)
        deviceModelDAO.updateHeightPixels(deviceModel, displayMetrics.heightPixels, modifierId)
        deviceModelDAO.updateDensity(deviceModel, displayMetrics.density, modifierId)
        deviceModelDAO.updateXdpi(deviceModel, displayMetrics.xdpi, modifierId)
        deviceModelDAO.updateYdpi(deviceModel, displayMetrics.ydpi, modifierId)
        deviceModelDAO.updateCapabilityTouch(deviceModel, capabilityTouch, modifierId)
        return deviceModel
    }

    /**
     * Deletes an exhibition device model
     *
     * @param deviceModel exhibition device model to be deleted
     */
    fun deleteDeviceModel(deviceModel: DeviceModel) {
        return deviceModelDAO.delete(deviceModel)
    }

}