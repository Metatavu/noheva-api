package fi.metatavu.muisti.api

import fi.metatavu.muisti.api.spec.DeviceModelsApi
import fi.metatavu.muisti.api.spec.model.DeviceModel
import fi.metatavu.muisti.api.translate.DeviceModelTranslator
import fi.metatavu.muisti.contents.PageLayoutController
import fi.metatavu.muisti.devices.DeviceModelController
import java.util.*
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.transaction.Transactional
import javax.ws.rs.core.Response

/**
 * Device models api implementation
 */
@RequestScoped
@Transactional
class DeviceModelsApiImpl : DeviceModelsApi, AbstractApi() {

    @Inject
    lateinit var deviceModelController: DeviceModelController

    @Inject
    lateinit var deviceModelTranslator: DeviceModelTranslator

    @Inject
    lateinit var pageLayoutController: PageLayoutController

    override fun listDeviceModels(): Response {
        val deviceModels = deviceModelController.listDeviceModels()
        return createOk(deviceModels.map(deviceModelTranslator::translate))
    }

    override fun createDeviceModel(deviceModel: DeviceModel): Response {
        val userId = loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        val manufacturer = deviceModel.manufacturer
        val model = deviceModel.model
        val dimensions = deviceModel.dimensions
        val displayMetrics = deviceModel.displayMetrics
        val capabilityTouch = deviceModel.capabilities.touch
        val screenOrientation = deviceModel.screenOrientation
        val createdDeviceModel = deviceModelController.createDeviceModel(
            manufacturer,
            model,
            dimensions,
            displayMetrics,
            capabilityTouch,
            screenOrientation,
            userId
        )
        return createOk(deviceModelTranslator.translate(createdDeviceModel))
    }

    override fun findDeviceModel(deviceModelId: UUID): Response {
        loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        val deviceModel = deviceModelController.findDeviceModelById(deviceModelId)
            ?: return createNotFound("Device model $deviceModelId not found")
        return createOk(deviceModelTranslator.translate(deviceModel))
    }

    override fun updateDeviceModel(deviceModelId: UUID, deviceModel: DeviceModel): Response {
        val userId = loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        val manufacturer = deviceModel.manufacturer
        val model = deviceModel.model
        val dimensions = deviceModel.dimensions

        val displayMetrics = deviceModel.displayMetrics
        val capabilityTouch = deviceModel.capabilities.touch
        val screenOrientation = deviceModel.screenOrientation

        val foundDeviceModel = deviceModelController.findDeviceModelById(deviceModelId)
            ?: return createNotFound("Device model $deviceModelId not found")
        val result = deviceModelController.updateDeviceModel(
            foundDeviceModel,
            manufacturer,
            model,
            dimensions,
            displayMetrics,
            capabilityTouch,
            screenOrientation,
            userId
        )

        return createOk(deviceModelTranslator.translate(result))
    }

    override fun deleteDeviceModel(deviceModelId: UUID): Response {
        loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        val deviceModel = deviceModelController.findDeviceModelById(deviceModelId)
            ?: return createNotFound("Device model $deviceModelId not found")

        val layouts = pageLayoutController.listPageLayouts(deviceModel = deviceModel, screenOrientation = null)

        if (layouts.isNotEmpty()) {
            val layoutIds = layouts.map { it.id }.joinToString()
            return createBadRequest("Device model $deviceModelId cannot be deleted because layouts $layoutIds are using it")
        }

        deviceModelController.deleteDeviceModel(deviceModel)
        return createNoContent()
    }
}
