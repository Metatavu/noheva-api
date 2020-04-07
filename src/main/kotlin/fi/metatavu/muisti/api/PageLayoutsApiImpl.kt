package fi.metatavu.muisti.api

import fi.metatavu.muisti.api.spec.PageLayoutsApi
import fi.metatavu.muisti.api.spec.model.PageLayout
import fi.metatavu.muisti.api.spec.model.ScreenOrientation
import fi.metatavu.muisti.api.translate.PageLayoutTranslator
import fi.metatavu.muisti.pages.PageLayoutController
import java.util.*
import javax.ejb.Stateful
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.ws.rs.PathParam
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
    private lateinit var pageLayoutTranslator: PageLayoutTranslator

    /* Page layouts */

    override fun createPageLayout(payload: PageLayout?): Response {
        payload ?: return createBadRequest("Missing request body")
        val userId = loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        val name = payload.name
        val data = payload.data
        val thumbnailUrl = payload.thumbnailUrl
        val modelId = payload.modelId
        val screenOrientation = payload.screenOrientation

        val pageLayout = pageLayoutController.createPageLayout(name, data, thumbnailUrl, modelId, screenOrientation, userId)

        return createOk(pageLayoutTranslator.translate(pageLayout))
    }

    override fun findPageLayout(pageLayoutId: UUID?): Response {
        pageLayoutId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        val pageLayout = pageLayoutController.findPageLayoutById(pageLayoutId) ?: return createNotFound("Layout $pageLayoutId not found")
        return createOk(pageLayoutTranslator.translate(pageLayout))
    }

    override fun listPageLayouts(deviceModelId: UUID?, screenOrientation: String?): Response? {
        val result = if (deviceModelId !== null && screenOrientation !== null) {
            val parsedOrientation = if (screenOrientation == "landscape") ScreenOrientation.LANDSCAPE else ScreenOrientation.PORTRAIT
            pageLayoutController.findPageLayoutsByDeviceModelIdAndOrientation(deviceModelId, parsedOrientation)
        } else if (deviceModelId !== null) {
            pageLayoutController.findPageLayoutsByDeviceModelId(deviceModelId)
        } else if (screenOrientation !== null) {
            val parsedOrientation = if (screenOrientation == "landscape") ScreenOrientation.LANDSCAPE else ScreenOrientation.PORTRAIT
            pageLayoutController.findPageLayoutsByScreenOrientation(parsedOrientation)
        } else {
            pageLayoutController.listPageLayouts()
        }

        return createOk(result.map (pageLayoutTranslator::translate))
    }

    override fun updatePageLayout(pageLayoutId: UUID?, payload: PageLayout?): Response {
        payload ?: return createBadRequest("Missing request body")
        pageLayoutId ?: return createNotFound(EXHIBITION_NOT_FOUND)

        val userId = loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        val name = payload.name
        val data = payload.data
        val thumbnailUrl = payload.thumbnailUrl
        val modelId = payload.modelId
        val screenOrientation = payload.screenOrientation

        val pageLayout = pageLayoutController.findPageLayoutById(pageLayoutId) ?: return createNotFound("Layout $pageLayoutId not found")
        val result = pageLayoutController.updatePageLayout(pageLayout, name, data, thumbnailUrl, modelId, screenOrientation, userId)

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