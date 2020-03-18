package fi.metatavu.muisti.api

import fi.metatavu.muisti.api.spec.ExhibitionsApi
import fi.metatavu.muisti.api.spec.model.*
import fi.metatavu.muisti.api.translate.*
import fi.metatavu.muisti.devices.ExhibitionDeviceController
import fi.metatavu.muisti.devices.ExhibitionDeviceGroupController
import fi.metatavu.muisti.devices.ExhibitionDeviceModelController
import fi.metatavu.muisti.exhibitions.ExhibitionController
import fi.metatavu.muisti.exhibitions.ExhibitionRoomController
import fi.metatavu.muisti.pages.ExhibitionPageController
import fi.metatavu.muisti.pages.PageLayoutController
import fi.metatavu.muisti.sessions.VisitorSessionController
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import java.util.*
import java.util.stream.Collectors
import javax.ejb.Stateful
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.ws.rs.core.Response

/**
 * Exhibitions API REST endpoints
 *
 * @author Antti Leppä
 */
@RequestScoped
@Stateful
class ExhibitionsApiImpl(): ExhibitionsApi, AbstractApi() {

    @Inject
    private lateinit var logger: Logger

    @Inject
    private lateinit var exhibitionController: ExhibitionController

    @Inject
    private lateinit var exhibitionTranslator: ExhibitionTranslator

    @Inject
    private lateinit var visitorSessionController: VisitorSessionController

    @Inject
    private lateinit var visitorSessionTranslator: VisitorSessionTranslator

    @Inject
    private lateinit var exhibitionRoomController: ExhibitionRoomController

    @Inject
    private lateinit var exhibitionRoomTranslator: ExhibitionRoomTranslator

    @Inject
    private lateinit var exhibitionDeviceGroupController: ExhibitionDeviceGroupController

    @Inject
    private lateinit var exhibitionDeviceGroupTranslator: ExhibitionDeviceGroupTranslator

    @Inject
    private lateinit var exhibitionDeviceController: ExhibitionDeviceController

    @Inject
    private lateinit var exhibitionDeviceTranslator: ExhibitionDeviceTranslator

    @Inject
    private lateinit var exhibitionDeviceModelController: ExhibitionDeviceModelController

    @Inject
    private lateinit var exhibitionDeviceModelTranslator: ExhibitionDeviceModelTranslator

    @Inject
    private lateinit var pageLayoutController: PageLayoutController

    @Inject
    private lateinit var exhibitionPageController: ExhibitionPageController

    @Inject
    private lateinit var exhibitionPageTranslator: ExhibitionPageTranslator

    /* Exhibitions */

    override fun createExhibition(payload: Exhibition?): Response? {
        if (payload == null) {
            return createBadRequest("Missing request body")
        }

        if (StringUtils.isBlank(payload.name)) {
            return createBadRequest("Missing exhibition name")
        }

        val userId = loggerUserId ?: return createUnauthorized(UNAUTHORIZED)

        val exhibition = exhibitionController.createExhibition(payload.name, userId)

        return createOk(exhibitionTranslator.translate(exhibition))
    }

    override fun findExhibition(exhibitionId: UUID?): Response? {
        if (exhibitionId == null) {
            return createNotFound(EXHIBITION_NOT_FOUND)
        }

        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        return createOk(exhibitionTranslator.translate(exhibition))
    }

    override fun listExhibitions(): Response? {
        val result = exhibitionController.listExhibitions().stream().map {
            exhibitionTranslator.translate(it)
        }.collect(Collectors.toList())

        return createOk(result)
    }

    override fun updateExhibition(exhibitionId: UUID?, payload: Exhibition?): Response? {
        if (payload == null) {
            return createBadRequest("Missing request body")
        }

        if (StringUtils.isBlank(payload.name)) {
            return createBadRequest("Missing exhibition name")
        }

        if (exhibitionId == null) {
            return createNotFound(EXHIBITION_NOT_FOUND)
        }

        val userId = loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val updatedExhibition = exhibitionController.updateExhibition(exhibition, payload.name, userId)

        return createOk(exhibitionTranslator.translate(updatedExhibition))
    }

    override fun deleteExhibition(exhibitionId: UUID?): Response? {
        if (exhibitionId == null) {
            return createNotFound(EXHIBITION_NOT_FOUND)
        }

        loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")

        exhibitionController.deleteExhibition(exhibition)

        return createNoContent()
    }

    /* VisitorSessions */

    override fun listVisitorSessions(exhibitionId: UUID?): Response {
        if (exhibitionId == null) {
            return createNotFound(EXHIBITION_NOT_FOUND)
        }

        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        loggerUserId ?: return createUnauthorized(UNAUTHORIZED)

        val visitorSessions = visitorSessionController.listVisitorSessions(exhibition)

        return createOk(visitorSessions.map (visitorSessionTranslator::translate))

    }

    override fun createVisitorSession(exhibitionId: UUID?, payload: VisitorSession?): Response {
        if (payload == null) {
            return createBadRequest("Missing request body")
        }

        if (exhibitionId == null) {
            return createNotFound(EXHIBITION_NOT_FOUND)
        }

        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val userId = loggerUserId ?: return createUnauthorized(UNAUTHORIZED)

        val visitorSession = visitorSessionController.createVisitorSession(exhibition, payload.state, userId)
        visitorSessionController.setVisitorSessionUsers(visitorSession, payload.users)
        visitorSessionController.setVisitorSessionVariables(visitorSession, payload.variables)

        return createOk(visitorSessionTranslator.translate(visitorSession))
    }

    override fun findVisitorSession(exhibitionId: UUID?, visitorSessionId: UUID?): Response {
        if (exhibitionId == null || visitorSessionId == null) {
            return createNotFound(EXHIBITION_NOT_FOUND)
        }

        loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val visitorSession = visitorSessionController.findVisitorSessionById(visitorSessionId) ?: return createNotFound("Visitor session $visitorSessionId not found")

        return createOk(visitorSessionTranslator.translate(visitorSession))
    }

    override fun deleteVisitorSession(exhibitionId: UUID?, visitorSessionId: UUID?): Response {
        if (exhibitionId == null || visitorSessionId == null) {
            return createNotFound(EXHIBITION_NOT_FOUND)
        }

        loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val visitorSession = visitorSessionController.findVisitorSessionById(visitorSessionId) ?: return createNotFound("Visitor session $visitorSessionId not found")

        visitorSessionController.deleteVisitorSession(visitorSession)

        return createNoContent()
    }

    override fun updateVisitorSession(exhibitionId: UUID?, visitorSessionId: UUID?, payload: VisitorSession?): Response {
        if (payload == null) {
            return createBadRequest("Missing request body")
        }

        if (exhibitionId == null || visitorSessionId == null) {
            return createNotFound(EXHIBITION_NOT_FOUND)
        }

        val userId = loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val visitorSession = visitorSessionController.findVisitorSessionById(visitorSessionId) ?: return createNotFound("Visitor session $visitorSessionId not found")

        val result = visitorSessionController.updateVisitorSession(visitorSession, payload.state, userId)
        visitorSessionController.setVisitorSessionUsers(result, payload.users)
        visitorSessionController.setVisitorSessionVariables(result, payload.variables)

        return createOk(visitorSessionTranslator.translate(result))
    }

    /* Rooms */

    override fun createExhibitionRoom(exhibitionId: UUID?, payload: ExhibitionRoom?): Response {
        if (payload == null) {
            return createBadRequest("Missing request body")
        }

        if (exhibitionId == null) {
            return createNotFound(EXHIBITION_NOT_FOUND)
        }

        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val userId = loggerUserId ?: return createUnauthorized(UNAUTHORIZED)

        val exhibitionRoom = exhibitionRoomController.createExhibitionRoom(exhibition, payload.name, userId)

        return createOk(exhibitionRoomTranslator.translate(exhibitionRoom))
    }

    override fun findExhibitionRoom(exhibitionId: UUID?, roomId: UUID?): Response {
        if (exhibitionId == null || roomId == null) {
            return createNotFound(EXHIBITION_NOT_FOUND)
        }

        loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val exhibitionRoom = exhibitionRoomController.findExhibitionRoomById(roomId) ?: return createNotFound("Room $roomId not found")

        if (!exhibitionRoom.exhibition?.id?.equals(exhibition.id)!!) {
            return createNotFound("Room not found")
        }

        return createOk(exhibitionRoomTranslator.translate(exhibitionRoom))
    }

    override fun listExhibitionRooms(exhibitionId: UUID?): Response {
        if (exhibitionId == null) {
            return createNotFound(EXHIBITION_NOT_FOUND)
        }

        val exhibition = exhibitionController.findExhibitionById(exhibitionId)?: return createNotFound("Exhibition $exhibitionId not found")
        val exhibitionRooms = exhibitionRoomController.listExhibitionRooms(exhibition)

        return createOk(exhibitionRooms.map (exhibitionRoomTranslator::translate))
    }

    override fun updateExhibitionRoom(exhibitionId: UUID?, roomId: UUID?, payload: ExhibitionRoom?): Response {
        if (payload == null) {
            return createBadRequest("Missing request body")
        }

        if (exhibitionId == null || roomId == null) {
            return createNotFound(EXHIBITION_NOT_FOUND)
        }

        val userId = loggerUserId ?: return createUnauthorized(UNAUTHORIZED)

        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val exhibitionRoom = exhibitionRoomController.findExhibitionRoomById(roomId) ?: return createNotFound("Room $roomId not found")
        val result = exhibitionRoomController.updateExhibitionRoom(exhibitionRoom, payload.name, userId)

        return createOk(exhibitionRoomTranslator.translate(result))
    }

    override fun deleteExhibitionRoom(exhibitionId: UUID?, roomId: UUID?): Response {
        if (exhibitionId == null || roomId == null) {
            return createNotFound(EXHIBITION_NOT_FOUND)
        }

        loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val exhibitionRoom = exhibitionRoomController.findExhibitionRoomById(roomId) ?: return createNotFound("Room $roomId not found")

        exhibitionRoomController.deleteExhibitionRoom(exhibitionRoom)

        return createNoContent()
    }

    /* Devices */

    override fun createExhibitionDevice(exhibitionId: UUID?, payload: ExhibitionDevice?): Response {
        if (payload == null) {
            return createBadRequest("Missing request body")
        }

        if (payload.groupId == null) {
            return createBadRequest("Missing exhibition group id")
        }

        if (exhibitionId == null) {
            return createNotFound(EXHIBITION_NOT_FOUND)
        }

        val exhibitionGroup = exhibitionDeviceGroupController.findExhibitionDeviceGroupById(payload.groupId) ?: return createBadRequest("Invalid exhibition group id ${payload.groupId}")
        val model = exhibitionDeviceModelController.findExhibitionDeviceModelById(payload.modelId) ?: return createBadRequest("Device model $payload.modelId not found")
        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val userId = loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        val location = payload.location
        val screenOrientation = payload.screenOrientation

        val exhibitionDevice = exhibitionDeviceController.createExhibitionDevice(exhibition, exhibitionGroup, model, payload.name, location, screenOrientation, userId)

        return createOk(exhibitionDeviceTranslator.translate(exhibitionDevice))
    }

    override fun findExhibitionDevice(exhibitionId: UUID?, deviceId: UUID?): Response {
        if (exhibitionId == null || deviceId == null) {
            return createNotFound(EXHIBITION_NOT_FOUND)
        }

        loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val exhibitionDevice = exhibitionDeviceController.findExhibitionDeviceById(deviceId) ?: return createNotFound("Device $deviceId not found")

        if (!exhibitionDevice.exhibition?.id?.equals(exhibition.id)!!) {
            return createNotFound("Device not found")
        }

        return createOk(exhibitionDeviceTranslator.translate(exhibitionDevice))
    }

    override fun listExhibitionDevices(exhibitionId: UUID?, exhibitionDeviceGroupId: UUID?): Response {
        if (exhibitionId == null) {
            return createNotFound(EXHIBITION_NOT_FOUND)
        }

        val exhibition = exhibitionController.findExhibitionById(exhibitionId)?: return createNotFound("Exhibition $exhibitionId not found")

        var exhibitionDeviceGroup: fi.metatavu.muisti.persistence.model.ExhibitionDeviceGroup? = null
        if (exhibitionDeviceGroupId != null) {
            exhibitionDeviceGroup = exhibitionDeviceGroupController.findExhibitionDeviceGroupById(exhibitionDeviceGroupId)
        }

        val exhibitionDevices = exhibitionDeviceController.listExhibitionDevices(exhibition, exhibitionDeviceGroup)

        return createOk(exhibitionDevices.map (exhibitionDeviceTranslator::translate))
    }

    override fun updateExhibitionDevice(exhibitionId: UUID?, deviceId: UUID?, payload: ExhibitionDevice?): Response {
        if (payload == null) {
            return createBadRequest("Missing request body")
        }

        if (exhibitionId == null || deviceId == null) {
            return createNotFound(EXHIBITION_NOT_FOUND)
        }

        val exhibitionGroup: fi.metatavu.muisti.persistence.model.ExhibitionDeviceGroup?
        if (payload.groupId != null) {
            exhibitionGroup = exhibitionDeviceGroupController.findExhibitionDeviceGroupById(payload.groupId)
            if (exhibitionGroup == null) {
                return createBadRequest("Invalid exhibition group id ${payload.groupId}")
            }
        }

        val model = exhibitionDeviceModelController.findExhibitionDeviceModelById(payload.modelId) ?: return createBadRequest("Device model $payload.modelId not found")
        val userId = loggerUserId ?: return createUnauthorized(UNAUTHORIZED)

        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val exhibitionDevice = exhibitionDeviceController.findExhibitionDeviceById(deviceId) ?: return createNotFound("Device $deviceId not found")
        val location = payload.location
        val screenOrientation = payload.screenOrientation
        val result = exhibitionDeviceController.updateExhibitionDevice(exhibitionDevice, model, payload.name, location, screenOrientation, userId)

        return createOk(exhibitionDeviceTranslator.translate(result))
    }

    override fun deleteExhibitionDevice(exhibitionId: UUID?, deviceId: UUID?): Response {
        if (exhibitionId == null || deviceId == null) {
            return createNotFound(EXHIBITION_NOT_FOUND)
        }

        loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val exhibitionDevice = exhibitionDeviceController.findExhibitionDeviceById(deviceId) ?: return createNotFound("Device $deviceId not found")

        exhibitionDeviceController.deleteExhibitionDevice(exhibitionDevice)

        return createNoContent()
    }

    /* Exhibition device groups */

    override fun createExhibitionDeviceGroup(exhibitionId: UUID?, payload: ExhibitionDeviceGroup?): Response {
        if (payload == null) {
            return createBadRequest("Missing request body")
        }

        if (exhibitionId == null) {
            return createNotFound(EXHIBITION_NOT_FOUND)
        }

        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val userId = loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        val exhibitionDeviceGroup = exhibitionDeviceGroupController.createExhibitionDeviceGroup(exhibition, payload.name, userId)

        return createOk(exhibitionDeviceGroupTranslator.translate(exhibitionDeviceGroup))
    }

    override fun findExhibitionDeviceGroup(exhibitionId: UUID?, deviceGroupId: UUID?): Response {
        if (exhibitionId == null || deviceGroupId == null) {
            return createNotFound(EXHIBITION_NOT_FOUND)
        }

        loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val exhibitionDeviceGroup = exhibitionDeviceGroupController.findExhibitionDeviceGroupById(deviceGroupId) ?: return createNotFound("Room $deviceGroupId not found")

        if (!exhibitionDeviceGroup.exhibition?.id?.equals(exhibition.id)!!) {
            return createNotFound("Room not found")
        }

        return createOk(exhibitionDeviceGroupTranslator.translate(exhibitionDeviceGroup))
    }

    override fun listExhibitionDeviceGroups(exhibitionId: UUID?): Response {
        if (exhibitionId == null) {
            return createNotFound(EXHIBITION_NOT_FOUND)
        }

        val exhibition = exhibitionController.findExhibitionById(exhibitionId)?: return createNotFound("Exhibition $exhibitionId not found")
        val exhibitionDeviceGroups = exhibitionDeviceGroupController.listExhibitionDeviceGroups(exhibition)

        return createOk(exhibitionDeviceGroups.map (exhibitionDeviceGroupTranslator::translate))
    }

    override fun updateExhibitionDeviceGroup(exhibitionId: UUID?, deviceGroupId: UUID?, payload: ExhibitionDeviceGroup?): Response {
        if (payload == null) {
            return createBadRequest("Missing request body")
        }

        if (exhibitionId == null || deviceGroupId == null) {
            return createNotFound(EXHIBITION_NOT_FOUND)
        }

        val userId = loggerUserId ?: return createUnauthorized(UNAUTHORIZED)

        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val exhibitionDeviceGroup = exhibitionDeviceGroupController.findExhibitionDeviceGroupById(deviceGroupId) ?: return createNotFound("Room $deviceGroupId not found")
        val result = exhibitionDeviceGroupController.updateExhibitionDeviceGroup(exhibitionDeviceGroup, payload.name, userId)

        return createOk(exhibitionDeviceGroupTranslator.translate(result))
    }

    override fun deleteExhibitionDeviceGroup(exhibitionId: UUID?, deviceGroupId: UUID?): Response {
        if (exhibitionId == null || deviceGroupId == null) {
            return createNotFound(EXHIBITION_NOT_FOUND)
        }

        loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val exhibitionDeviceGroup = exhibitionDeviceGroupController.findExhibitionDeviceGroupById(deviceGroupId) ?: return createNotFound("Room $deviceGroupId not found")

        exhibitionDeviceGroupController.deleteExhibitionDeviceGroup(exhibitionDeviceGroup)

        return createNoContent()
    }

    /* Device models */

    override fun createExhibitionDeviceModel(exhibitionId: UUID?, payload: ExhibitionDeviceModel?): Response {
        if (payload == null) {
            return createBadRequest("Missing request body")
        }

        if (exhibitionId == null) {
            return createNotFound(EXHIBITION_NOT_FOUND)
        }

        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val userId = loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        val manufacturer = payload.manufacturer
        val model = payload.model
        val dimensionWidth = payload.dimensions.width
        val dimensionHeight = payload.dimensions.height
        val displayMetrics = payload.displayMetrics
        val capabilityTouch = payload.capabilities.touch

        val exhibitionDeviceModel = exhibitionDeviceModelController.createExhibitionDeviceModel(exhibition, manufacturer, model, dimensionWidth, dimensionHeight, displayMetrics, capabilityTouch, userId)

        return createOk(exhibitionDeviceModelTranslator.translate(exhibitionDeviceModel))
    }

    override fun findExhibitionDeviceModel(exhibitionId: UUID?, deviceModelId: UUID?): Response {
        if (exhibitionId == null || deviceModelId == null) {
            return createNotFound(EXHIBITION_NOT_FOUND)
        }

        loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val exhibitionDeviceModel = exhibitionDeviceModelController.findExhibitionDeviceModelById(deviceModelId) ?: return createNotFound("Device model $deviceModelId not found")

        if (!exhibitionDeviceModel.exhibition?.id?.equals(exhibition.id)!!) {
            return createNotFound("Device model not found")
        }

        return createOk(exhibitionDeviceModelTranslator.translate(exhibitionDeviceModel))
    }

    override fun listExhibitionDeviceModels(exhibitionId: UUID?): Response {
        if (exhibitionId == null) {
            return createNotFound(EXHIBITION_NOT_FOUND)
        }

        val exhibition = exhibitionController.findExhibitionById(exhibitionId)?: return createNotFound("Exhibition $exhibitionId not found")
        val exhibitionDeviceModels = exhibitionDeviceModelController.listExhibitionDeviceModels(exhibition)

        return createOk(exhibitionDeviceModels.map (exhibitionDeviceModelTranslator::translate))
    }

    override fun updateExhibitionDeviceModel(exhibitionId: UUID?, deviceModelId: UUID?, payload: ExhibitionDeviceModel?): Response {
        if (payload == null) {
            return createBadRequest("Missing request body")
        }

        if (exhibitionId == null || deviceModelId == null) {
            return createNotFound(EXHIBITION_NOT_FOUND)
        }

        val userId = loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        val manufacturer = payload.manufacturer
        val model = payload.model
        val dimensionWidth = payload.dimensions.width
        val dimensionHeight = payload.dimensions.height
        val displayMetrics = payload.displayMetrics
        val capabilityTouch = payload.capabilities.touch

        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val exhibitionDeviceModel = exhibitionDeviceModelController.findExhibitionDeviceModelById(deviceModelId) ?: return createNotFound("Device model $deviceModelId not found")
        val result = exhibitionDeviceModelController.updateExhibitionDeviceModel(exhibitionDeviceModel, manufacturer, model, dimensionWidth, dimensionHeight, displayMetrics, capabilityTouch, userId)

        return createOk(exhibitionDeviceModelTranslator.translate(result))
    }

    override fun deleteExhibitionDeviceModel(exhibitionId: UUID?, deviceModelId: UUID?): Response {
        if (exhibitionId == null || deviceModelId == null) {
            return createNotFound(EXHIBITION_NOT_FOUND)
        }

        loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val exhibitionDeviceModel = exhibitionDeviceModelController.findExhibitionDeviceModelById(deviceModelId) ?: return createNotFound("Device model $deviceModelId not found")

        exhibitionDeviceModelController.deleteExhibitionDeviceModel(exhibitionDeviceModel)

        return createNoContent()
    }

    /* Pages */

    override fun createExhibitionPage(exhibitionId: UUID?, payload: ExhibitionPage?): Response {
        payload ?: return createBadRequest("Missing request body")
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val layout = pageLayoutController.findPageLayoutById(payload.layoutId) ?: return createBadRequest("Layout $payload.layoutId not found")
        val userId = loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        val name = payload.name
        val resources = payload.resources
        val eventTriggers = payload.eventTriggers

        val exhibitionPage = exhibitionPageController.createExhibitionPage(exhibition, layout, name, resources, eventTriggers, userId)

        return createOk(exhibitionPageTranslator.translate(exhibitionPage))
    }

    override fun findExhibitionPage(exhibitionId: UUID?, pageId: UUID?): Response {
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        pageId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val exhibitionPage = exhibitionPageController.findExhibitionPageById(pageId) ?: return createNotFound("Page $pageId not found")

        if (!exhibitionPage.exhibition?.id?.equals(exhibition.id)!!) {
            return createNotFound("Room not found")
        }

        return createOk(exhibitionPageTranslator.translate(exhibitionPage))
    }

    override fun listExhibitionPages(exhibitionId: UUID?): Response {
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId)?: return createNotFound("Exhibition $exhibitionId not found")
        val exhibitionPages = exhibitionPageController.listExhibitionPages(exhibition)

        return createOk(exhibitionPages.map (exhibitionPageTranslator::translate))
    }

    override fun updateExhibitionPage(exhibitionId: UUID?, pageId: UUID?, payload: ExhibitionPage?): Response {
        payload ?: return createBadRequest("Missing request body")
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        pageId ?: return createNotFound(EXHIBITION_NOT_FOUND)

        val userId = loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        val layout = pageLayoutController.findPageLayoutById(payload.layoutId) ?: return createBadRequest("Layout $payload.layoutId not found")
        val name = payload.name
        val resources = payload.resources
        val eventTriggers = payload.eventTriggers

        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val exhibitionPage = exhibitionPageController.findExhibitionPageById(pageId) ?: return createNotFound("Page $pageId not found")
        val result = exhibitionPageController.updateExhibitionPage(exhibitionPage, layout, name, resources, eventTriggers, userId)

        return createOk(exhibitionPageTranslator.translate(result))
    }

    override fun deleteExhibitionPage(exhibitionId: UUID?, pageId: UUID?): Response {
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        pageId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val exhibitionPage = exhibitionPageController.findExhibitionPageById(pageId) ?: return createNotFound("Page $pageId not found")

        exhibitionPageController.deleteExhibitionPage(exhibitionPage)

        return createNoContent()
    }


}