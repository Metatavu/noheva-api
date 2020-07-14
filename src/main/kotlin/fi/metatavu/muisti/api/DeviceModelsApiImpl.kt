package fi.metatavu.muisti.api

import fi.metatavu.muisti.api.spec.DeviceModelsApi
import fi.metatavu.muisti.api.spec.model.DeviceModel
import fi.metatavu.muisti.api.translate.DeviceModelTranslator
import fi.metatavu.muisti.contents.PageLayoutController
import fi.metatavu.muisti.devices.DeviceModelController
import java.util.*
import javax.ejb.Stateful
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.ws.rs.core.Response

/**
 * Page device model API REST endpoints
 *
 * @author Antti Leppä
 */
@RequestScoped
@Stateful
@Suppress("unused")
class DeviceModelsApiImpl: DeviceModelsApi, AbstractApi() {

    @Inject
    private lateinit var deviceModelController: DeviceModelController

    @Inject
    private lateinit var deviceModelTranslator: DeviceModelTranslator

    @Inject
    private lateinit var pageLayoutController: PageLayoutController

    /* Device models */

    override fun createDeviceModel(payload: DeviceModel?): Response {
        payload ?: return createBadRequest("Missing request body")
        val userId = loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        val manufacturer = payload.manufacturer
        val model = payload.model
        val dimensions = payload.dimensions
        val displayMetrics = payload.displayMetrics
        val capabilityTouch = payload.capabilities.touch
        val screenOrientation = payload.screenOrientation
        val deviceModel = deviceModelController.createDeviceModel(manufacturer, model, dimensions, displayMetrics, capabilityTouch, screenOrientation, userId)
        return createOk(deviceModelTranslator.translate(deviceModel))
    }

    override fun findDeviceModel(deviceModelId: UUID?): Response {
        deviceModelId ?: return createNotFound(DEVICE_MODEL_NOT_FOUND)
        loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        val deviceModel = deviceModelController.findDeviceModelById(deviceModelId) ?: return createNotFound("Device model $deviceModelId not found")
        return createOk(deviceModelTranslator.translate(deviceModel))
    }

    override fun listDeviceModels(): Response {
        val deviceModels = deviceModelController.listDeviceModels()

        return createOk(deviceModels.map (deviceModelTranslator::translate))
    }

    override fun updateDeviceModel(deviceModelId: UUID?, payload: DeviceModel?): Response {
        payload ?: return createBadRequest("Missing request body")
        deviceModelId ?: return createNotFound(DEVICE_MODEL_NOT_FOUND)
        val userId = loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        val manufacturer = payload.manufacturer
        val model = payload.model
        val dimensions = payload.dimensions

        val displayMetrics = payload.displayMetrics
        val capabilityTouch = payload.capabilities.touch
        val screenOrientation = payload.screenOrientation

        val deviceModel = deviceModelController.findDeviceModelById(deviceModelId) ?: return createNotFound("Device model $deviceModelId not found")
        val result = deviceModelController.updateDeviceModel(deviceModel, manufacturer, model, dimensions, displayMetrics, capabilityTouch, screenOrientation, userId)

        return createOk(deviceModelTranslator.translate(result))
    }

    override fun deleteDeviceModel(deviceModelId: UUID?): Response {
        deviceModelId ?: return createNotFound(DEVICE_MODEL_NOT_FOUND)
        loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        val deviceModel = deviceModelController.findDeviceModelById(deviceModelId) ?: return createNotFound("Device model $deviceModelId not found")

        val layouts = pageLayoutController.listPageLayouts(deviceModel = deviceModel, screenOrientation = null)

        if (layouts.isNotEmpty()) {
            val layoutIds = layouts.map { it.id }.joinToString()
            return createBadRequest("Device model $deviceModelId cannot be deleted because layouts $layoutIds are using it")
        }
        
        deviceModelController.deleteDeviceModel(deviceModel)
        return createNoContent()
    }

    companion object {
        protected const val DEVICE_MODEL_NOT_FOUND = "Device model not found"
    }


}