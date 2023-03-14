package fi.metatavu.noheva.devices

import fi.metatavu.noheva.api.spec.model.DeviceModelDimensions
import fi.metatavu.noheva.api.spec.model.DeviceModelDisplayMetrics
import fi.metatavu.noheva.api.spec.model.ScreenOrientation
import fi.metatavu.noheva.persistence.dao.DeviceModelDAO
import fi.metatavu.noheva.persistence.model.DeviceModel
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Controller for exhibition device models
 */
@ApplicationScoped
class DeviceModelController {

    @Inject
    lateinit var deviceModelDAO: DeviceModelDAO

    /**
     * Creates new exhibition device model
     *
     * @param manufacturer device manufacturer
     * @param model device model
     * @param dimensions device physical dimensions
     * @param displayMetrics display metrics
     * @param capabilityTouch whether device has touch capability
     * @param screenOrientation screen orientation
     * @param creatorId creating user id
     * @return created exhibition device model
     */
    fun createDeviceModel(manufacturer: String, model: String, dimensions: DeviceModelDimensions, displayMetrics: DeviceModelDisplayMetrics, capabilityTouch: Boolean, screenOrientation: ScreenOrientation, creatorId: UUID): DeviceModel {
        return deviceModelDAO.create(UUID.randomUUID(),
                manufacturer = manufacturer,
                model = model,
                deviceWidth = dimensions.deviceWidth,
                deviceHeight = dimensions.deviceHeight,
                deviceDepth = dimensions.deviceDepth,
                screenWidth = dimensions.screenWidth,
                screenHeight = dimensions.screenHeight,
                widthPixels = displayMetrics.widthPixels,
                heightPixels = displayMetrics.heightPixels,
                density = displayMetrics.density,
                xdpi = displayMetrics.xdpi,
                ydpi = displayMetrics.ydpi,
                capabilityTouch = capabilityTouch,
                screenOrientation = screenOrientation,
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
     * @param dimensions device physical dimensions
     * @param displayMetrics display metrics
     * @param capabilityTouch whether device has touch capability
     * @param screenOrientation screen orientation
     * @param modifierId modifying user id
     * @return updated exhibition
     */
    fun updateDeviceModel(deviceModel: DeviceModel, manufacturer: String, model: String, dimensions: DeviceModelDimensions, displayMetrics: DeviceModelDisplayMetrics, capabilityTouch: Boolean, screenOrientation: ScreenOrientation, modifierId: UUID): DeviceModel {
        deviceModelDAO.updateManufacturer(deviceModel, manufacturer, modifierId)
        deviceModelDAO.updateModel(deviceModel, model, modifierId)
        deviceModelDAO.updateDeviceWidth(deviceModel, dimensions.deviceWidth, modifierId)
        deviceModelDAO.updateDeviceHeight(deviceModel, dimensions.deviceHeight, modifierId)
        deviceModelDAO.updateDeviceDepth(deviceModel, dimensions.deviceDepth, modifierId)
        deviceModelDAO.updateScreenWidth(deviceModel, dimensions.screenWidth, modifierId)
        deviceModelDAO.updateScreenHeight(deviceModel, dimensions.screenHeight, modifierId)
        deviceModelDAO.updateWidthPixels(deviceModel, displayMetrics.widthPixels, modifierId)
        deviceModelDAO.updateHeightPixels(deviceModel, displayMetrics.heightPixels, modifierId)
        deviceModelDAO.updateDensity(deviceModel, displayMetrics.density, modifierId)
        deviceModelDAO.updateXdpi(deviceModel, displayMetrics.xdpi, modifierId)
        deviceModelDAO.updateYdpi(deviceModel, displayMetrics.ydpi, modifierId)
        deviceModelDAO.updateCapabilityTouch(deviceModel, capabilityTouch, modifierId)
        deviceModelDAO.updateScreenOrientation(deviceModel, screenOrientation, modifierId)
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