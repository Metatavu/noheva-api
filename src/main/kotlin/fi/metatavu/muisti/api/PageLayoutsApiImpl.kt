package fi.metatavu.muisti.api

import fi.metatavu.muisti.api.spec.PageLayoutsApi
import fi.metatavu.muisti.api.spec.model.PageLayout
import fi.metatavu.muisti.api.spec.model.ScreenOrientation
import fi.metatavu.muisti.api.translate.PageLayoutTranslator
import fi.metatavu.muisti.contents.PageLayoutController
import fi.metatavu.muisti.devices.DeviceModelController
import fi.metatavu.muisti.persistence.model.DeviceModel
import java.util.*
import javax.ejb.Stateful
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.ws.rs.core.Response

/**
 * Page layouts API REST endpoints
 *
 * @author Antti Leppä
 */
@RequestScoped
@Stateful
class PageLayoutsApiImpl: PageLayoutsApi, AbstractApi() {

    @Inject
    private lateinit var pageLayoutController: PageLayoutController

    @Inject
    private lateinit var deviceModelController: DeviceModelController

    @Inject
    private lateinit var pageLayoutTranslator: PageLayoutTranslator

    /* Page layouts */

    override fun createPageLayout(payload: PageLayout?): Response {
        payload ?: return createBadRequest("Missing request body")
        val userId = loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        val name = payload.name
        val data = payload.data
        val thumbnailUrl = payload.thumbnailUrl
        val deviceModelId = payload.modelId
        val deviceModel = deviceModelController.findDeviceModelById(deviceModelId) ?: return createBadRequest("Device model $deviceModelId could not be found")
        val screenOrientation = payload.screenOrientation

        val pageLayout = pageLayoutController.createPageLayout(name, data, thumbnailUrl, deviceModel, screenOrientation, userId)

        return createOk(pageLayoutTranslator.translate(pageLayout))
    }

    override fun findPageLayout(pageLayoutId: UUID?): Response {
        pageLayoutId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        val pageLayout = pageLayoutController.findPageLayoutById(pageLayoutId) ?: return createNotFound("Layout $pageLayoutId not found")
        return createOk(pageLayoutTranslator.translate(pageLayout))
    }

    override fun listPageLayouts(deviceModelId: UUID?, screenOrientation: String?): Response? {
        var deviceModel: DeviceModel? = null
        var parsedScreenOrientation: ScreenOrientation? = null
        if (deviceModelId !== null) {
            deviceModel = deviceModelController.findDeviceModelById(deviceModelId) ?: return createBadRequest("Device model with id $deviceModelId was not found")
        }
        if (screenOrientation !== null) {
            parsedScreenOrientation = convertStringToScreenOrientation(screenOrientation) ?: return createBadRequest("Screen orientation $screenOrientation could not be converted")
        }

        val result = pageLayoutController.listPageLayouts(deviceModel, parsedScreenOrientation)
        return createOk(result.map (pageLayoutTranslator::translate))
    }

    override fun updatePageLayout(pageLayoutId: UUID?, payload: PageLayout?): Response {
        payload ?: return createBadRequest("Missing request body")
        pageLayoutId ?: return createNotFound(EXHIBITION_NOT_FOUND)

        val userId = loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        val name = payload.name
        val data = payload.data
        val thumbnailUrl = payload.thumbnailUrl
        val deviceModelId = payload.modelId
        val deviceModel = deviceModelController.findDeviceModelById(deviceModelId) ?: return createBadRequest("Device model $deviceModelId could not be found")
        val screenOrientation = payload.screenOrientation

        val pageLayout = pageLayoutController.findPageLayoutById(pageLayoutId) ?: return createNotFound("Layout $pageLayoutId not found")
        val result = pageLayoutController.updatePageLayout(pageLayout, name, data, thumbnailUrl, deviceModel, screenOrientation, userId)

        return createOk(pageLayoutTranslator.translate(result))
    }

    override fun deletePageLayout(pageLayoutId: UUID?): Response {
        pageLayoutId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        val pageLayout = pageLayoutController.findPageLayoutById(pageLayoutId) ?: return createNotFound("Layout $pageLayoutId not found")

        pageLayoutController.deletePageLayout(pageLayout)

        return createNoContent()
    }

}