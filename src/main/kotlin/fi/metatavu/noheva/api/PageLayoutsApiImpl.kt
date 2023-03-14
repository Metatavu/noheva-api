package fi.metatavu.noheva.api

import fi.metatavu.noheva.api.spec.PageLayoutsApi
import fi.metatavu.noheva.api.spec.model.PageLayout
import fi.metatavu.noheva.api.spec.model.ScreenOrientation
import fi.metatavu.noheva.api.translate.PageLayoutTranslator
import fi.metatavu.noheva.contents.ExhibitionPageController
import fi.metatavu.noheva.contents.PageLayoutController
import fi.metatavu.noheva.devices.DeviceModelController
import java.util.*
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.transaction.Transactional
import javax.ws.rs.core.Response

/**
 * Page layouts api implementation
 */
@RequestScoped
@Transactional
class PageLayoutsApiImpl: PageLayoutsApi, AbstractApi() {

    @Inject
    lateinit var deviceModelController: DeviceModelController

    @Inject
    lateinit var pageLayoutController: PageLayoutController

    @Inject
    lateinit var pageLayoutTranslator: PageLayoutTranslator

    @Inject
    lateinit var exhibitionPageController: ExhibitionPageController

    override fun listPageLayouts(deviceModelId: UUID?, screenOrientation: String?): Response {
        var deviceModel: fi.metatavu.noheva.persistence.model.DeviceModel? = null
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

    override fun createPageLayout(pageLayout: PageLayout): Response {
        val userId = loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        val name = pageLayout.name
        val data = pageLayout.data
        val thumbnailUrl = pageLayout.thumbnailUrl

        val deviceModelId = pageLayout.modelId ?: return createBadRequest("Device model could not be found")
        val deviceModel = deviceModelController.findDeviceModelById(deviceModelId) ?: return createBadRequest("Device model $deviceModelId could not be found")
        val screenOrientation = pageLayout.screenOrientation

        val created = pageLayoutController.createPageLayout(name, data, thumbnailUrl, deviceModel, screenOrientation, userId)

        return createOk(pageLayoutTranslator.translate(created))
    }

    override fun findPageLayout(pageLayoutId: UUID): Response {
        loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        val pageLayout = pageLayoutController.findPageLayoutById(pageLayoutId) ?: return createNotFound("Layout $pageLayoutId not found")
        return createOk(pageLayoutTranslator.translate(pageLayout))
    }

    override fun updatePageLayout(pageLayoutId: UUID, pageLayout: PageLayout): Response {
        val userId = loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        val name = pageLayout.name
        val data = pageLayout.data
        val thumbnailUrl = pageLayout.thumbnailUrl
        val deviceModelId = pageLayout.modelId ?: return createBadRequest("Device model could not be found")
        val deviceModel = deviceModelController.findDeviceModelById(deviceModelId) ?: return createBadRequest("Device model $deviceModelId could not be found")
        val screenOrientation = pageLayout.screenOrientation

        val pageLayoutFound = pageLayoutController.findPageLayoutById(pageLayoutId) ?: return createNotFound("Layout $pageLayoutId not found")
        val result = pageLayoutController.updatePageLayout(pageLayoutFound, name, data, thumbnailUrl, deviceModel, screenOrientation, userId)

        return createOk(pageLayoutTranslator.translate(result))
    }

    override fun deletePageLayout(pageLayoutId: UUID): Response {
        loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        val pageLayout = pageLayoutController.findPageLayoutById(pageLayoutId) ?: return createNotFound("Layout $pageLayoutId not found")

        val exhibitionPages = exhibitionPageController.listExhibitionLayoutPages(pageLayout)
        if (exhibitionPages.isNotEmpty()) {
            val exhibitionPageIds = exhibitionPages.map { it.id }.joinToString()
            return createBadRequest("Cannot delete page layout $pageLayout because it's used in pages $exhibitionPageIds")
        }

        pageLayoutController.deletePageLayout(pageLayout)
        return createNoContent()
    }

}
