package fi.metatavu.noheva.api

import fi.metatavu.noheva.api.spec.ExhibitionPagesApi
import fi.metatavu.noheva.api.spec.model.ExhibitionPage
import fi.metatavu.noheva.api.translate.ExhibitionPageTranslator
import fi.metatavu.noheva.contents.ContentVersionController
import fi.metatavu.noheva.contents.ExhibitionPageController
import fi.metatavu.noheva.contents.GroupContentVersionController
import fi.metatavu.noheva.contents.PageLayoutController
import fi.metatavu.noheva.devices.ExhibitionDeviceController
import fi.metatavu.noheva.exhibitions.ExhibitionController
import fi.metatavu.noheva.realtime.RealtimeNotificationController
import java.util.*
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.transaction.Transactional
import javax.ws.rs.core.Response

/**
 * Echibition pages api implementation
 */
@RequestScoped
@Transactional
class ExhibitionPagesApiImpl : ExhibitionPagesApi, AbstractApi() {

    @Inject
    lateinit var exhibitionController: ExhibitionController

    @Inject
    lateinit var realtimeNotificationController: RealtimeNotificationController

    @Inject
    lateinit var exhibitionPageController: ExhibitionPageController

    @Inject
    lateinit var exhibitionDeviceController: ExhibitionDeviceController

    @Inject
    lateinit var pageLayoutController: PageLayoutController

    @Inject
    lateinit var groupContentVersionController: GroupContentVersionController

    @Inject
    lateinit var contentVersionController: ContentVersionController

    @Inject
    lateinit var exhibitionPageTranslator: ExhibitionPageTranslator

    override fun listExhibitionPages(
        exhibitionId: UUID,
        contentVersionId: UUID?,
        exhibitionDeviceId: UUID?,
        pageLayoutId: UUID?
    ): Response {
        val exhibition = exhibitionController.findExhibitionById(exhibitionId)
            ?: return createNotFound("Exhibition $exhibitionId not found")
        var exhibitionDevice: fi.metatavu.noheva.persistence.model.ExhibitionDevice? = null
        if (exhibitionDeviceId != null) {
            exhibitionDevice = exhibitionDeviceController.findExhibitionDeviceById(exhibitionDeviceId)
        }

        var contentVersion: fi.metatavu.noheva.persistence.model.ContentVersion? = null
        if (contentVersionId != null) {
            contentVersion = contentVersionController.findContentVersionById(contentVersionId)
            contentVersion ?: return createBadRequest("Content version $contentVersionId not found")
        }

        var pageLayout: fi.metatavu.noheva.persistence.model.PageLayout? = null
        if (pageLayoutId != null) {
            pageLayout = pageLayoutController.findPageLayoutById(pageLayoutId)
            pageLayout ?: return createBadRequest("Page layout with ID $pageLayoutId not found")
        }

        val exhibitionPages = exhibitionPageController.listExhibitionPages(
            exhibition = exhibition,
            exhibitionDevice = exhibitionDevice,
            contentVersion = contentVersion,
            pageLayout = pageLayout
        )

        return createOk(exhibitionPages.map(exhibitionPageTranslator::translate))
    }

    override fun createExhibitionPage(exhibitionId: UUID, exhibitionPage: ExhibitionPage): Response {
        val userId = loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId)
            ?: return createNotFound("Exhibition $exhibitionId not found")
        val layout = pageLayoutController.findPageLayoutById(exhibitionPage.layoutId)
            ?: return createBadRequest("Layout $exhibitionPage.layoutId not found")
        val device = exhibitionDeviceController.findExhibitionDeviceById(exhibitionPage.deviceId)
            ?: return createBadRequest("Device ${exhibitionPage.deviceId} not found")
        val contentVersion =
            contentVersionController.findContentVersionById(exhibitionPage.contentVersionId) ?: return createBadRequest(
                "Content version ${exhibitionPage.contentVersionId} not found"
            )
        val contentGroupVersions = groupContentVersionController.listGroupContentVersions(
            exhibition = exhibition,
            deviceGroup = device.exhibitionDeviceGroup,
            contentVersion = contentVersion
        )

        if (contentGroupVersions.isEmpty()) {
            return createBadRequest(
                "Cannot create page for device ${device.id} and content version ${contentVersion.id} because they are not connected by any contentGroupVersions"
            )
        }

        val name = exhibitionPage.name
        val resources = exhibitionPage.resources
        val eventTriggers = exhibitionPage.eventTriggers
        val enterTransitions = exhibitionPage.enterTransitions
        val exitTransitions = exhibitionPage.exitTransitions
        val orderNumber = exhibitionPage.orderNumber

        val result = exhibitionPageController.createPage(
            exhibition = exhibition,
            device = device,
            contentVersion = contentVersion,
            layout = layout,
            name = name,
            orderNumber = orderNumber,
            resources = resources,
            eventTriggers = eventTriggers,
            enterTransitions = enterTransitions,
            exitTransitions = exitTransitions,
            creatorId = userId
        )

        realtimeNotificationController.notifyExhibitionPageCreate(exhibitionId, result.id!!)

        return createOk(exhibitionPageTranslator.translate(result))
    }

    override fun findExhibitionPage(exhibitionId: UUID, pageId: UUID): Response {
        loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId)
            ?: return createNotFound("Exhibition $exhibitionId not found")
        val exhibitionPage =
            exhibitionPageController.findExhibitionPageById(pageId) ?: return createNotFound("Page $pageId not found")

        if (!exhibitionPage.exhibition?.id?.equals(exhibition.id)!!) {
            return createNotFound("Room not found")
        }

        return createOk(exhibitionPageTranslator.translate(exhibitionPage))
    }

    override fun updateExhibitionPage(exhibitionId: UUID, pageId: UUID, exhibitionPage: ExhibitionPage): Response {
        val userId = loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound(EXHIBITION_NOT_FOUND)
        val layout = pageLayoutController.findPageLayoutById(exhibitionPage.layoutId)
            ?: return createBadRequest("Layout $exhibitionPage.layoutId not found")
        val device = exhibitionDeviceController.findExhibitionDeviceById(exhibitionPage.deviceId)
            ?: return createBadRequest("Device ${exhibitionPage.deviceId} not found")
        val name = exhibitionPage.name
        val resources = exhibitionPage.resources
        val eventTriggers = exhibitionPage.eventTriggers
        val contentVersion =
            contentVersionController.findContentVersionById(exhibitionPage.contentVersionId) ?: return createBadRequest(
                "Content version ${exhibitionPage.contentVersionId} not found"
            )
        val enterTransitions = exhibitionPage.enterTransitions
        val exitTransitions = exhibitionPage.exitTransitions
        val orderNumber = exhibitionPage.orderNumber

        exhibitionController.findExhibitionById(exhibitionId)
            ?: return createNotFound("Exhibition $exhibitionId not found")
        val foundExhibitionPage =
            exhibitionPageController.findExhibitionPageById(pageId) ?: return createNotFound("Page $pageId not found")
        val updatedPage = exhibitionPageController.updateExhibitionPage(
            foundExhibitionPage,
            device = device,
            layout = layout,
            contentVersion = contentVersion,
            name = name,
            resources = resources,
            eventTriggers = eventTriggers,
            enterTransitions = enterTransitions,
            exitTransitions = exitTransitions,
            orderNumber = orderNumber,
            modifierId = userId
        )

        realtimeNotificationController.notifyExhibitionPageUpdate(exhibitionId, pageId)

        return createOk(exhibitionPageTranslator.translate(updatedPage))
    }

    override fun deleteExhibitionPage(exhibitionId: UUID, pageId: UUID): Response {
        loggedUserId ?: return createUnauthorized(UNAUTHORIZED)

        exhibitionController.findExhibitionById(exhibitionId)
            ?: return createNotFound("Exhibition $exhibitionId not found")
        val page =
            exhibitionPageController.findExhibitionPageById(pageId) ?: return createNotFound("Page $pageId not found")

        val idlePageDevices = exhibitionDeviceController.listDevicesByIdlePage(idlePage = page)
        if (idlePageDevices.isNotEmpty()) {
            val idlePageDeviceIds = idlePageDevices.map { it.id }.joinToString()
            return createBadRequest("Cannot delete page $pageId because it's used as idle page in devices $idlePageDeviceIds")
        }

        exhibitionPageController.deleteExhibitionPage(page)
        realtimeNotificationController.notifyExhibitionPageDelete(exhibitionId, pageId)
        return createNoContent()
    }
}