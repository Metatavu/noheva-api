package fi.metatavu.noheva.api


import fi.metatavu.noheva.api.spec.ExhibitionDevicesApi
import fi.metatavu.noheva.api.spec.model.ExhibitionDevice
import fi.metatavu.noheva.api.translate.ExhibitionDeviceTranslator
import fi.metatavu.noheva.contents.ExhibitionPageController
import fi.metatavu.noheva.devices.DeviceModelController
import fi.metatavu.noheva.devices.ExhibitionDeviceController
import fi.metatavu.noheva.devices.ExhibitionDeviceGroupController
import fi.metatavu.noheva.exhibitions.ExhibitionController
import fi.metatavu.noheva.realtime.RealtimeNotificationController
import java.util.*
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.transaction.Transactional
import javax.ws.rs.core.Response

/**
 * Exhibition device api implmentation
 */
@RequestScoped
@Transactional
class ExhibitionDeviceApiImpl : ExhibitionDevicesApi, AbstractApi() {

    @Inject
    lateinit var exhibitionController: ExhibitionController

    @Inject
    lateinit var exhibitionDeviceController: ExhibitionDeviceController

    @Inject
    lateinit var exhibitionDeviceGroupController: ExhibitionDeviceGroupController

    @Inject
    lateinit var deviceModelController: DeviceModelController

    @Inject
    lateinit var exhibitionDeviceTranslator: ExhibitionDeviceTranslator

    @Inject
    lateinit var exhibitionPageController: ExhibitionPageController

    @Inject
    lateinit var realtimeNotificationController: RealtimeNotificationController

    override fun listExhibitionDevices(exhibitionId: UUID, exhibitionGroupId: UUID?, deviceModelId: UUID?): Response {
        val exhibition = exhibitionController.findExhibitionById(exhibitionId)
            ?: return createNotFound("Exhibition $exhibitionId not found")

        var exhibitionDeviceGroup: fi.metatavu.noheva.persistence.model.ExhibitionDeviceGroup? = null
        if (exhibitionGroupId != null) {
            exhibitionDeviceGroup = exhibitionDeviceGroupController.findDeviceGroupById(exhibitionGroupId)
        }

        var deviceModel: fi.metatavu.noheva.persistence.model.DeviceModel? = null
        if (deviceModelId != null) {
            deviceModel = deviceModelController.findDeviceModelById(deviceModelId)
        }

        val exhibitionDevices =
            exhibitionDeviceController.listExhibitionDevices(exhibition, exhibitionDeviceGroup, deviceModel)

        return createOk(exhibitionDevices.map(exhibitionDeviceTranslator::translate))
    }

    override fun createExhibitionDevice(exhibitionId: UUID, exhibitionDevice: ExhibitionDevice): Response {
        val exhibitionGroup = exhibitionDeviceGroupController.findDeviceGroupById(exhibitionDevice.groupId)
            ?: return createBadRequest("Invalid exhibition group id ${exhibitionDevice.groupId}")
        val model = deviceModelController.findDeviceModelById(exhibitionDevice.modelId)
            ?: return createBadRequest("Device model ${exhibitionDevice.modelId} not found")
        val exhibition = exhibitionController.findExhibitionById(exhibitionId)
            ?: return createNotFound("Exhibition $exhibitionId not found")
        val userId = loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        val location = exhibitionDevice.location
        val screenOrientation = exhibitionDevice.screenOrientation

        var idlePage: fi.metatavu.noheva.persistence.model.ExhibitionPage? = null
        if (exhibitionDevice.idlePageId != null) {
            idlePage = exhibitionPageController.findExhibitionPageById(exhibitionDevice.idlePageId)
                ?: return createBadRequest("Idle page ${exhibitionDevice.idlePageId} not found")
        }

        val result = exhibitionDeviceController.createExhibitionDevice(
            exhibition = exhibition,
            exhibitionDeviceGroup = exhibitionGroup,
            deviceModel = model,
            name = exhibitionDevice.name,
            location = location,
            screenOrientation = screenOrientation,
            imageLoadStrategy = exhibitionDevice.imageLoadStrategy,
            idlePage = idlePage,
            creatorId = userId
        )

        realtimeNotificationController.notifyDeviceCreate(id = result.id!!, exhibitionId = exhibitionId)

        return createOk(exhibitionDeviceTranslator.translate(result))
    }

    override fun findExhibitionDevice(exhibitionId: UUID, deviceId: UUID): Response {
        loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId)
            ?: return createNotFound("Exhibition $exhibitionId not found")
        val exhibitionDevice = exhibitionDeviceController.findExhibitionDeviceById(deviceId)
            ?: return createNotFound("Device $deviceId not found")

        if (!exhibitionDevice.exhibition?.id?.equals(exhibition.id)!!) {
            return createNotFound("Device not found")
        }

        return createOk(exhibitionDeviceTranslator.translate(exhibitionDevice))
    }

    override fun updateExhibitionDevice(
        exhibitionId: UUID,
        deviceId: UUID,
        exhibitionDevice: ExhibitionDevice
    ): Response {
        val userId = loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        val exhibitionGroup = exhibitionDeviceGroupController.findDeviceGroupById(exhibitionDevice.groupId)
            ?: return createBadRequest("Invalid exhibition group id ${exhibitionDevice.groupId}")
        val model = deviceModelController.findDeviceModelById(exhibitionDevice.modelId)
            ?: return createBadRequest("Device model $exhibitionDevice.modelId not found")

        exhibitionController.findExhibitionById(exhibitionId)
            ?: return createNotFound("Exhibition $exhibitionId not found")
        val foundExhibitionDevice = exhibitionDeviceController.findExhibitionDeviceById(deviceId)
            ?: return createNotFound("Device $deviceId not found")
        val groupChanged = foundExhibitionDevice.exhibitionDeviceGroup?.id != exhibitionGroup.id
        val location = exhibitionDevice.location
        val screenOrientation = exhibitionDevice.screenOrientation

        var idlePage: fi.metatavu.noheva.persistence.model.ExhibitionPage? = null
        if (exhibitionDevice.idlePageId != null) {
            idlePage = exhibitionPageController.findExhibitionPageById(exhibitionDevice.idlePageId)
                ?: return createBadRequest("Idle page ${exhibitionDevice.idlePageId} not found")
        }

        val result = exhibitionDeviceController.updateExhibitionDevice(
            exhibitionDevice = foundExhibitionDevice,
            exhibitionDeviceGroup = exhibitionGroup,
            deviceModel = model,
            name = exhibitionDevice.name,
            location = location,
            screenOrientation = screenOrientation,
            imageLoadStrategy = exhibitionDevice.imageLoadStrategy,
            idlePage = idlePage,
            modifierId = userId
        )

        realtimeNotificationController.notifyDeviceUpdate(
            id = deviceId,
            exhibitionId = exhibitionId,
            groupChanged = groupChanged
        )

        return createOk(exhibitionDeviceTranslator.translate(result))
    }

    override fun deleteExhibitionDevice(exhibitionId: UUID, deviceId: UUID): Response {
        loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId)
            ?: return createNotFound("Exhibition $exhibitionId not found")
        val exhibitionDevice = exhibitionDeviceController.findExhibitionDeviceById(deviceId)
            ?: return createNotFound("Device $deviceId not found")

        val devicePages = exhibitionPageController.listExhibitionPages(
            exhibition = exhibition,
            exhibitionDevice = exhibitionDevice,
            contentVersion = null,
            pageLayout = null
        )

        if (devicePages.isNotEmpty()) {
            val devicePageIds = devicePages.map { it.id }.joinToString()
            return createBadRequest("Cannot delete device $deviceId because it's pages $devicePageIds are assigned to the device")
        }

        exhibitionDeviceController.deleteExhibitionDevice(exhibitionDevice)
        realtimeNotificationController.notifyDeviceDelete(id = deviceId, exhibitionId = exhibitionId)

        return createNoContent()
    }
}
