package fi.metatavu.noheva.api

import fi.metatavu.noheva.api.spec.DeviceDataApi
import fi.metatavu.noheva.api.spec.model.*
import fi.metatavu.noheva.api.translate.DeviceDataLayoutTranslator
import fi.metatavu.noheva.api.translate.DeviceDataPageTranslator
import fi.metatavu.noheva.contents.ExhibitionPageController
import fi.metatavu.noheva.contents.PageLayoutController
import fi.metatavu.noheva.devices.ExhibitionDeviceController
import fi.metatavu.noheva.exhibitions.ExhibitionController
import java.util.*
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.transaction.Transactional
import javax.ws.rs.core.Response

/**
 * Device datas API implementation
 */
@RequestScoped
@Transactional
@Suppress("UNUSED")
class DeviceDatasApiImpl: DeviceDataApi, AbstractApi() {

    @Inject
    lateinit var exhibitionController: ExhibitionController

    @Inject
    lateinit var exhibitionDeviceController: ExhibitionDeviceController

    @Inject
    lateinit var exhibitionPageController: ExhibitionPageController

    @Inject
    lateinit var pageLayoutController: PageLayoutController

    @Inject
    lateinit var deviceDataPageTranslator: DeviceDataPageTranslator

    @Inject
    lateinit var deviceDataLayoutTranslator: DeviceDataLayoutTranslator

    override fun listDeviceDataLayouts(deviceId: UUID): Response {
        val device = deviceController.findDevice(id = deviceId) ?: return createNotFound("Device $deviceId not found")
        val activeExhibition = exhibitionController.findActiveExhibition() ?: return createNotFound("Active exhibition not found")
        val exhibitionDevice = exhibitionDeviceController.findExhibitionDeviceByExhibitionAndDevice(exhibition = activeExhibition, device = device) ?: return createNotFound("Exhibition device not found")

        if (!isAuthorizedDevice(deviceId = device.id)) {
            return createForbidden("Device $deviceId is not authorized to access this exhibition")
        }

        val layouts = pageLayoutController.listPageLayoutsForDevice(
            exhibitionDevice = exhibitionDevice
        )

        return createOk(layouts.map { deviceDataLayoutTranslator.translate(it) })
    }

    override fun listDeviceDataPages(deviceId: UUID): Response {
        val device = deviceController.findDevice(id = deviceId) ?: return createNotFound("Device $deviceId not found")
        val activeExhibition = exhibitionController.findActiveExhibition() ?: return createNotFound("Active exhibition not found")
        val exhibitionDevice = exhibitionDeviceController.findExhibitionDeviceByExhibitionAndDevice(exhibition = activeExhibition, device = device) ?: return createNotFound("Exhibition device not found")

        if (!isAuthorizedDevice(deviceId = device.id)) {
            return createForbidden("Device $deviceId is not authorized to access this exhibition")
        }

        val pages = exhibitionPageController.listExhibitionPages(
            exhibition = activeExhibition,
            exhibitionDevice = exhibitionDevice,
            contentVersion = null,
            pageLayout = null,
        )

        return createOk(pages.map { deviceDataPageTranslator.translate(it) })
    }

}