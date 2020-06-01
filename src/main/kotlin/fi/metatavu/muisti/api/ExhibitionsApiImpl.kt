package fi.metatavu.muisti.api

import fi.metatavu.muisti.api.spec.ExhibitionsApi
import fi.metatavu.muisti.api.spec.model.*
import fi.metatavu.muisti.api.translate.*
import fi.metatavu.muisti.contents.ExhibitionPageController
import fi.metatavu.muisti.contents.PageLayoutController
import fi.metatavu.muisti.devices.DeviceModelController
import fi.metatavu.muisti.devices.ExhibitionDeviceController
import fi.metatavu.muisti.devices.ExhibitionDeviceGroupController
import fi.metatavu.muisti.exhibitions.ExhibitionContentVersionController
import fi.metatavu.muisti.exhibitions.ExhibitionController
import fi.metatavu.muisti.exhibitions.ExhibitionFloorController
import fi.metatavu.muisti.exhibitions.ExhibitionRoomController
import fi.metatavu.muisti.keycloak.KeycloakController
import fi.metatavu.muisti.realtime.RealtimeNotificationController
import fi.metatavu.muisti.sessions.VisitorSessionController
import fi.metatavu.muisti.visitors.VisitorController
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
@Suppress("unused")
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
    private lateinit var visitorController: VisitorController

    @Inject
    private lateinit var visitorTranslator: VisitorTranslator

    @Inject
    private lateinit var exhibitionRoomController: ExhibitionRoomController

    @Inject
    private lateinit var exhibitionRoomTranslator: ExhibitionRoomTranslator
    
    @Inject
    private lateinit var exhibitionFloorController: ExhibitionFloorController

    @Inject
    private lateinit var exhibitionFloorTranslator: ExhibitionFloorTranslator

    @Inject
    private lateinit var exhibitionDeviceGroupController: ExhibitionDeviceGroupController

    @Inject
    private lateinit var exhibitionDeviceGroupTranslator: ExhibitionDeviceGroupTranslator

    @Inject
    private lateinit var exhibitionContentVersionController: ExhibitionContentVersionController

    @Inject
    private lateinit var exhibitionContentVersionTranslator: ExhibitionContentVersionTranslator

    @Inject
    private lateinit var exhibitionDeviceController: ExhibitionDeviceController

    @Inject
    private lateinit var exhibitionDeviceTranslator: ExhibitionDeviceTranslator

    @Inject
    private lateinit var pageLayoutController: PageLayoutController

    @Inject
    private lateinit var exhibitionPageController: ExhibitionPageController

    @Inject
    private lateinit var exhibitionPageTranslator: ExhibitionPageTranslator

    @Inject
    private lateinit var deviceModelController: DeviceModelController

    @Inject
    private lateinit var realtimeNotificationController: RealtimeNotificationController

    @Inject
    private lateinit var keycloakController: KeycloakController

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

    /* Visitors */

    override fun createVisitor(exhibitionId: UUID?, payload: Visitor?): Response {
        payload ?: return createBadRequest("Missing request body")
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val userId = loggerUserId ?: return createUnauthorized(UNAUTHORIZED)

        var userRepresentation = keycloakController.findUserByEmail(payload.email)
        if (userRepresentation == null) {
            userRepresentation = keycloakController.createUser(email = payload.email, realmRoles = listOf("visitor"))
        }

        userRepresentation ?: return createInternalServerError("Failed to create visitor user")
        val visitor = visitorController.createVisitor(
            exhibition = exhibition,
            userRepresentation = userRepresentation,
            tagId = payload.tagId,
            creatorId = userId
        )

        return createOk(visitorTranslator.translate(visitor))
    }

    override fun findVisitor(exhibitionId: UUID?, visitorId: UUID?): Response {
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        visitorId ?: return createNotFound("Visitor not found")
        loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val visitor = visitorController.findVisitorById(visitorId) ?: return createNotFound("Visitor session $visitorId not found")
        return createOk(visitorTranslator.translate(visitor))
    }

    override fun listVisitors(exhibitionId: UUID?, tagId: String?): Response {
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        loggerUserId ?: return createUnauthorized(UNAUTHORIZED)

        val visitors = visitorController.listVisitors(
            exhibition = exhibition
        )

        return createOk(visitors.map (visitorTranslator::translate))
    }

    override fun updateVisitor(exhibitionId: UUID?, visitorId: UUID?, payload: Visitor?): Response {
        payload ?: return createBadRequest("Missing request body")
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        visitorId ?: return createNotFound("Visitor not found")

        val userId = loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val visitor = visitorController.findVisitorById(visitorId) ?: return createNotFound("Visitor $visitorId not found")

        val result = visitorController.updateVisitor(
            visitor = visitor,
            tagId = payload.tagId,
            lastModfierId = userId
        )

        return createOk(visitorTranslator.translate(result))
    }

    override fun deleteVisitor(exhibitionId: UUID?, visitorId: UUID?): Response {
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        visitorId ?: return createNotFound("Visitor not found")
        loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val visitor = visitorController.findVisitorById(visitorId) ?: return createNotFound("Visitor $visitorId not found")
        visitorController.deleteVisitor(visitor)
        return createNoContent()
    }

    override fun findVisitorTag(exhibitionId: UUID?, tagId: String?): Response {
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        tagId ?: return createNotFound("tagId not found")
        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val visitor = visitorController.findVisitorByTagId(
            exhibition = exhibition,
            tagId = tagId
        )

        visitor ?: return createNotFound("Visitor tag not found")

        val result = VisitorTag()
        result.tagId = tagId
        return createOk(result)
    }

    /* VisitorSessions */

    override fun createVisitorSession(exhibitionId: UUID?, payload: VisitorSession?): Response {
        payload ?: return createBadRequest("Missing request body")
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val userId = loggerUserId ?: return createUnauthorized(UNAUTHORIZED)

        val visitors = mutableListOf<fi.metatavu.muisti.persistence.model.Visitor>()
        for (visitorId in payload.visitorIds) {
            val visitor = visitorController.findVisitorById(visitorId)
            visitor ?: return createBadRequest("Invalid visitor $visitorId")
            visitors.add(visitor)
        }

        val visitorSession = visitorSessionController.createVisitorSession(exhibition, payload.state, userId)
        visitorSessionController.setVisitorSessionVisitors(visitorSession, visitors)
        visitorSessionController.setVisitorSessionVariables(visitorSession, payload.variables)
        realtimeNotificationController.notifyExhibitionVisitorSessionCreate(exhibitionId,  visitorSession.id!!)

        return createOk(visitorSessionTranslator.translate(visitorSession))
    }

    override fun findVisitorSession(exhibitionId: UUID?, visitorSessionId: UUID?): Response {
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        visitorSessionId ?: return createNotFound("Visitor session not found")
        loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val visitorSession = visitorSessionController.findVisitorSessionById(visitorSessionId) ?: return createNotFound("Visitor session $visitorSessionId not found")

        return createOk(visitorSessionTranslator.translate(visitorSession))
    }

    override fun listVisitorSessions(exhibitionId: UUID?, tagId: String?): Response {
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        loggerUserId ?: return createUnauthorized(UNAUTHORIZED)

        val visitorSessions = visitorSessionController.listVisitorSessions(
            exhibition = exhibition,
            tagId = tagId
        )

        return createOk(visitorSessions.map (visitorSessionTranslator::translate))

    }

    override fun updateVisitorSession(exhibitionId: UUID?, visitorSessionId: UUID?, payload: VisitorSession?): Response {
        payload ?: return createBadRequest("Missing request body")
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        visitorSessionId ?: return createNotFound("Visitor session not found")

        val userId = loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val visitorSession = visitorSessionController.findVisitorSessionById(visitorSessionId) ?: return createNotFound("Visitor session $visitorSessionId not found")

        val visitors = mutableListOf<fi.metatavu.muisti.persistence.model.Visitor>()
        for (visitorId in payload.visitorIds) {
            val visitor = visitorController.findVisitorById(visitorId)
            visitor ?: return createBadRequest("Invalid visitor $visitorId")
            visitors.add(visitor)
        }

        val result = visitorSessionController.updateVisitorSession(visitorSession, payload.state, userId)
        val usersChanged = visitorSessionController.setVisitorSessionVisitors(visitorSession, visitors)
        val variablesChanged = visitorSessionController.setVisitorSessionVariables(result, payload.variables)

        realtimeNotificationController.notifyExhibitionVisitorSessionUpdate(exhibitionId,  visitorSessionId, variablesChanged, usersChanged)

        return createOk(visitorSessionTranslator.translate(result))
    }

    override fun deleteVisitorSession(exhibitionId: UUID?, visitorSessionId: UUID?): Response {
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        visitorSessionId ?: return createNotFound("Visitor session not found")
        loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val visitorSession = visitorSessionController.findVisitorSessionById(visitorSessionId) ?: return createNotFound("Visitor session $visitorSessionId not found")

        visitorSessionController.deleteVisitorSession(visitorSession)

        realtimeNotificationController.notifyExhibitionVisitorSessionDelete(exhibitionId,  visitorSessionId)

        return createNoContent()
    }

    /* Rooms */

    override fun createExhibitionRoom(exhibitionId: UUID?, payload: ExhibitionRoom?): Response {
        payload ?: return createBadRequest("Missing request body")
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)

        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val floor = exhibitionFloorController.findExhibitionFloorById(payload.floorId) ?: return createBadRequest("Exhibition floor ${payload.floorId} not found")
        val userId = loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        val exhibitionRoom = exhibitionRoomController.createExhibitionRoom(
            exhibition = exhibition,
            name = payload.name,
            geoShape = payload.geoShape,
            floor = floor,
            creatorId = userId
        )

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

    override fun listExhibitionRooms(exhibitionId: UUID?, floorId: UUID?): Response {
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId)?: return createNotFound("Exhibition $exhibitionId not found")
        var floor: fi.metatavu.muisti.persistence.model.ExhibitionFloor? = null

        if (floorId != null) {
            floor = exhibitionFloorController.findExhibitionFloorById(floorId)
        }

        val exhibitionRooms = exhibitionRoomController.listExhibitionRooms(exhibition, floor)

        return createOk(exhibitionRooms.map (exhibitionRoomTranslator::translate))
    }

    override fun updateExhibitionRoom(exhibitionId: UUID?, roomId: UUID?, payload: ExhibitionRoom?): Response {
        payload ?: return createBadRequest("Missing request body")
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        roomId ?: return createNotFound("Room not found")

        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val floor = exhibitionFloorController.findExhibitionFloorById(payload.floorId) ?: return createBadRequest("Exhibition floor ${payload.floorId} not found")
        val userId = loggerUserId ?: return createUnauthorized(UNAUTHORIZED)

        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val exhibitionRoom = exhibitionRoomController.findExhibitionRoomById(roomId) ?: return createNotFound("Room $roomId not found")

        val result = exhibitionRoomController.updateExhibitionRoom(
            exhibitionRoom = exhibitionRoom,
            name = payload.name,
            geoShape = payload.geoShape,
            floor = floor,
            modifierId = userId
        )

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
        payload ?: return createBadRequest("Missing request body")
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        payload.groupId ?: return createBadRequest("Missing exhibition group id")

        val exhibitionGroup = exhibitionDeviceGroupController.findExhibitionDeviceGroupById(payload.groupId) ?: return createBadRequest("Invalid exhibition group id ${payload.groupId}")
        val model = deviceModelController.findDeviceModelById(payload.modelId) ?: return createBadRequest("Device model ${payload.modelId} not found")
        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val userId = loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        val location = payload.location
        val screenOrientation = payload.screenOrientation

        var indexPage: fi.metatavu.muisti.persistence.model.ExhibitionPage? = null
        if (payload.indexPageId != null) {
            indexPage = exhibitionPageController.findExhibitionPageById(payload.indexPageId) ?: return createBadRequest("Specified index page ${payload.indexPageId} does not exist")
        }

        val exhibitionDevice = exhibitionDeviceController.createExhibitionDevice(
            exhibition = exhibition,
            exhibitionDeviceGroup = exhibitionGroup,
            indexPage = indexPage,
            deviceModel = model,
            name = payload.name,
            location = location,
            screenOrientation = screenOrientation,
            creatorId = userId
        )

        return createOk(exhibitionDeviceTranslator.translate(exhibitionDevice))
    }

    override fun findExhibitionDevice(exhibitionId: UUID?, deviceId: UUID?): Response {
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        deviceId ?: return createNotFound("Device not found")

        loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val exhibitionDevice = exhibitionDeviceController.findExhibitionDeviceById(deviceId) ?: return createNotFound("Device $deviceId not found")

        if (!exhibitionDevice.exhibition?.id?.equals(exhibition.id)!!) {
            return createNotFound("Device not found")
        }

        return createOk(exhibitionDeviceTranslator.translate(exhibitionDevice))
    }

    override fun listExhibitionDevices(exhibitionId: UUID?, exhibitionDeviceGroupId: UUID?): Response {
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)

        val exhibition = exhibitionController.findExhibitionById(exhibitionId)?: return createNotFound("Exhibition $exhibitionId not found")

        var exhibitionDeviceGroup: fi.metatavu.muisti.persistence.model.ExhibitionDeviceGroup? = null
        if (exhibitionDeviceGroupId != null) {
            exhibitionDeviceGroup = exhibitionDeviceGroupController.findExhibitionDeviceGroupById(exhibitionDeviceGroupId)
        }

        val exhibitionDevices = exhibitionDeviceController.listExhibitionDevices(exhibition, exhibitionDeviceGroup)

        return createOk(exhibitionDevices.map (exhibitionDeviceTranslator::translate))
    }

    override fun updateExhibitionDevice(exhibitionId: UUID?, deviceId: UUID?, payload: ExhibitionDevice?): Response {
        payload ?: return createBadRequest("Missing request body")
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        deviceId ?: return createNotFound("Device not found")
        payload.groupId ?: return createBadRequest("Missing exhibition group id")
        val exhibitionGroup = exhibitionDeviceGroupController.findExhibitionDeviceGroupById(payload.groupId) ?: return createBadRequest("Invalid exhibition group id ${payload.groupId}")

        var indexPage: fi.metatavu.muisti.persistence.model.ExhibitionPage? = null
        if (payload.indexPageId != null) {
            indexPage = exhibitionPageController.findExhibitionPageById(payload.indexPageId) ?: return createBadRequest("Specified index page ${payload.indexPageId} does not exist")
        }

        val model = deviceModelController.findDeviceModelById(payload.modelId) ?: return createBadRequest("Device model $payload.modelId not found")
        val userId = loggerUserId ?: return createUnauthorized(UNAUTHORIZED)

        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val exhibitionDevice = exhibitionDeviceController.findExhibitionDeviceById(deviceId) ?: return createNotFound("Device $deviceId not found")
        val location = payload.location
        val screenOrientation = payload.screenOrientation
        val result = exhibitionDeviceController.updateExhibitionDevice(
            exhibitionDevice = exhibitionDevice,
            exhibitionDeviceGroup = exhibitionGroup,
            deviceModel = model,
            indexPage = indexPage,
            name = payload.name,
            location = location,
            screenOrientation = screenOrientation,
            modifierId = userId
        )

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
        payload ?: return createBadRequest("Missing request body")
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val userId = loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        val room = exhibitionRoomController.findExhibitionRoomById(payload.roomId)  ?: return createNotFound("Exhibition room ${payload.roomId} not found")

        val exhibitionDeviceGroup = exhibitionDeviceGroupController.createExhibitionDeviceGroup(exhibition,
            name = payload.name,
            room = room,
            creatorId = userId
        )

        return createOk(exhibitionDeviceGroupTranslator.translate(exhibitionDeviceGroup))
    }

    override fun findExhibitionDeviceGroup(exhibitionId: UUID?, deviceGroupId: UUID?): Response {
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        deviceGroupId?: return createNotFound("Device group not found")

        loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val exhibitionDeviceGroup = exhibitionDeviceGroupController.findExhibitionDeviceGroupById(deviceGroupId) ?: return createNotFound("Room $deviceGroupId not found")

        if (!exhibitionDeviceGroup.exhibition?.id?.equals(exhibition.id)!!) {
            return createNotFound("Room not found")
        }

        return createOk(exhibitionDeviceGroupTranslator.translate(exhibitionDeviceGroup))
    }

    override fun listExhibitionDeviceGroups(exhibitionId: UUID?, roomId: UUID?): Response {
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId)?: return createNotFound("Exhibition $exhibitionId not found")
        var room: fi.metatavu.muisti.persistence.model.ExhibitionRoom? = null

        if (roomId != null) {
            room = exhibitionRoomController.findExhibitionRoomById(roomId) ?: return createBadRequest("Could not find room $roomId")
        }

        val exhibitionDeviceGroups = exhibitionDeviceGroupController.listExhibitionDeviceGroups(exhibition, room)

        return createOk(exhibitionDeviceGroups.map (exhibitionDeviceGroupTranslator::translate))
    }

    override fun updateExhibitionDeviceGroup(exhibitionId: UUID?, deviceGroupId: UUID?, payload: ExhibitionDeviceGroup?): Response {
        payload ?: return createBadRequest("Missing request body")
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        deviceGroupId?: return createNotFound("Device group not found")
        val userId = loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val room = exhibitionRoomController.findExhibitionRoomById(payload.roomId)  ?: return createNotFound("Exhibition room ${payload.roomId} not found")

        val exhibitionDeviceGroup = exhibitionDeviceGroupController.findExhibitionDeviceGroupById(deviceGroupId) ?: return createNotFound("Room $deviceGroupId not found")
        val result = exhibitionDeviceGroupController.updateExhibitionDeviceGroup(
            exhibitionDeviceGroup = exhibitionDeviceGroup,
            room = room,
            name = payload.name,
            modifierId = userId
        )

        return createOk(exhibitionDeviceGroupTranslator.translate(result))
    }

    override fun deleteExhibitionDeviceGroup(exhibitionId: UUID?, deviceGroupId: UUID?): Response {
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        deviceGroupId?: return createNotFound("Device group not found")
        loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val exhibitionDeviceGroup = exhibitionDeviceGroupController.findExhibitionDeviceGroupById(deviceGroupId) ?: return createNotFound("Room $deviceGroupId not found")
        exhibitionDeviceGroupController.deleteExhibitionDeviceGroup(exhibitionDeviceGroup)
        return createNoContent()
    }

    /* Pages */

    override fun createExhibitionPage(exhibitionId: UUID?, payload: ExhibitionPage?): Response {
        payload ?: return createBadRequest("Missing request body")
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val layout = pageLayoutController.findPageLayoutById(payload.layoutId) ?: return createBadRequest("Layout $payload.layoutId not found")
        val device = exhibitionDeviceController.findExhibitionDeviceById(payload.deviceId) ?: return createBadRequest("Device ${payload.deviceId} not found")
        val contentVersion = exhibitionContentVersionController.findExhibitionContentVersionById(payload.contentVersionId) ?: return createBadRequest("Content version ${payload.contentVersionId} not found")

        val userId = loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        val name = payload.name
        val resources = payload.resources
        val eventTriggers = payload.eventTriggers
        val enterTransitions = payload.enterTransitions
        val exitTransitions = payload.exitTransitions

        val exhibitionPage = exhibitionPageController.createExhibitionPage(
            exhibition = exhibition,
            device = device,
            contentVersion = contentVersion,
            layout = layout,
            name = name,
            resources = resources,
            eventTriggers = eventTriggers,
            enterTransitions = enterTransitions,
            exitTransitions = exitTransitions,
            creatorId = userId
        )

        realtimeNotificationController.notifyExhibitionPageCreate(exhibitionId, exhibitionPage.id!!)

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

    override fun listExhibitionPages(exhibitionId: UUID?, exhibitionContentVersionId: UUID?, exhibitionDeviceId: UUID?): Response {
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)

        val exhibition = exhibitionController.findExhibitionById(exhibitionId)?: return createNotFound("Exhibition $exhibitionId not found")
        var exhibitionDevice : fi.metatavu.muisti.persistence.model.ExhibitionDevice? = null
        if (exhibitionDeviceId != null) {
            exhibitionDevice = exhibitionDeviceController.findExhibitionDeviceById(exhibitionDeviceId)
        }

        var exhibitionContentVersion: fi.metatavu.muisti.persistence.model.ExhibitionContentVersion? = null
        if (exhibitionContentVersionId != null) {
            exhibitionContentVersion = exhibitionContentVersionController.findExhibitionContentVersionById(exhibitionContentVersionId)
            exhibitionContentVersion ?: return createBadRequest("Content version not found")
        }

        val exhibitionPages = exhibitionPageController.listExhibitionPages(exhibition, exhibitionDevice, exhibitionContentVersion)
        return createOk(exhibitionPages.map (exhibitionPageTranslator::translate))
    }

    override fun updateExhibitionPage(exhibitionId: UUID?, pageId: UUID?, payload: ExhibitionPage?): Response {
        payload ?: return createBadRequest("Missing request body")
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        pageId ?: return createNotFound(EXHIBITION_NOT_FOUND)

        val userId = loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        val layout = pageLayoutController.findPageLayoutById(payload.layoutId) ?: return createBadRequest("Layout $payload.layoutId not found")
        val device = exhibitionDeviceController.findExhibitionDeviceById(payload.deviceId) ?: return createBadRequest("Device ${payload.deviceId} not found")
        val name = payload.name
        val resources = payload.resources
        val eventTriggers = payload.eventTriggers
        val contentVersion = exhibitionContentVersionController.findExhibitionContentVersionById(payload.contentVersionId) ?: return createBadRequest("Content version ${payload.contentVersionId} not found")
        val enterTransitions = payload.enterTransitions
        val exitTransitions = payload.exitTransitions

        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val exhibitionPage = exhibitionPageController.findExhibitionPageById(pageId) ?: return createNotFound("Page $pageId not found")
        val updatedPage = exhibitionPageController.updateExhibitionPage(exhibitionPage,
            device = device,
            layout = layout,
            contentVersion = contentVersion,
            name = name,
            resources = resources,
            eventTriggers = eventTriggers,
            enterTransitions = enterTransitions,
            exitTransitions = exitTransitions,
            modifierId = userId
        )

        realtimeNotificationController.notifyExhibitionPageUpdate(exhibitionId, pageId)

        return createOk(exhibitionPageTranslator.translate(updatedPage))
    }

    override fun deleteExhibitionPage(exhibitionId: UUID?, pageId: UUID?): Response {
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        pageId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val exhibitionPage = exhibitionPageController.findExhibitionPageById(pageId) ?: return createNotFound("Page $pageId not found")
        exhibitionPageController.deleteExhibitionPage(exhibitionPage)
        realtimeNotificationController.notifyExhibitionPageDelete(exhibitionId, pageId)

        return createNoContent()
    }

    /* content version */

    override fun createExhibitionContentVersion(exhibitionId: UUID?, payload: ExhibitionContentVersion?): Response {
        payload ?: return createBadRequest("Missing request body")
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val userId = loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        val exhibitionContentVersion = exhibitionContentVersionController.createExhibitionContentVersion(exhibition, payload.name, userId)
        return createOk(exhibitionContentVersionTranslator.translate(exhibitionContentVersion))
    }

    override fun findExhibitionContentVersion(exhibitionId: UUID?, contentVersionId: UUID?): Response {
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        contentVersionId?: return createNotFound("Device group not found")

        loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val exhibitionContentVersion = exhibitionContentVersionController.findExhibitionContentVersionById(contentVersionId) ?: return createNotFound("Room $contentVersionId not found")

        if (!exhibitionContentVersion.exhibition?.id?.equals(exhibition.id)!!) {
            return createNotFound("Room not found")
        }

        return createOk(exhibitionContentVersionTranslator.translate(exhibitionContentVersion))
    }

    override fun listExhibitionContentVersions(exhibitionId: UUID?): Response {
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId)?: return createNotFound("Exhibition $exhibitionId not found")
        val exhibitionContentVersions = exhibitionContentVersionController.listExhibitionContentVersions(exhibition)

        return createOk(exhibitionContentVersions.map (exhibitionContentVersionTranslator::translate))
    }

    override fun updateExhibitionContentVersion(exhibitionId: UUID?, contentVersionId: UUID?, payload: ExhibitionContentVersion?): Response {
        payload ?: return createBadRequest("Missing request body")
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        contentVersionId?: return createNotFound("Device group not found")
        val userId = loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val exhibitionContentVersion = exhibitionContentVersionController.findExhibitionContentVersionById(contentVersionId) ?: return createNotFound("Room $contentVersionId not found")
        val result = exhibitionContentVersionController.updateExhibitionContentVersion(exhibitionContentVersion, payload.name, userId)

        return createOk(exhibitionContentVersionTranslator.translate(result))
    }

    override fun deleteExhibitionContentVersion(exhibitionId: UUID?, contentVersionId: UUID?): Response {
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        contentVersionId?: return createNotFound("Device group not found")
        loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val exhibitionContentVersion = exhibitionContentVersionController.findExhibitionContentVersionById(contentVersionId) ?: return createNotFound("Room $contentVersionId not found")
        exhibitionContentVersionController.deleteExhibitionContentVersion(exhibitionContentVersion)
        return createNoContent()
    }

    /* Floors */

    override fun createExhibitionFloor(exhibitionId: UUID?, payload: ExhibitionFloor?): Response {
        payload ?: return createBadRequest("Missing request body")
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)

        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val userId = loggerUserId ?: return createUnauthorized(UNAUTHORIZED)

        val name = payload.name
        val floorPlanUrl = payload.floorPlanUrl
        val floorPlanBounds = payload.floorPlanBounds
        val exhibitionFloor = exhibitionFloorController.createExhibitionFloor(exhibition, name, floorPlanUrl, floorPlanBounds, userId)

        return createOk(exhibitionFloorTranslator.translate(exhibitionFloor))
    }

    override fun findExhibitionFloor(exhibitionId: UUID?, floorId: UUID?): Response {
        if (exhibitionId == null || floorId == null) {
            return createNotFound(EXHIBITION_NOT_FOUND)
        }

        loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val exhibitionFloor = exhibitionFloorController.findExhibitionFloorById(floorId) ?: return createNotFound("Floor $floorId not found")

        if (!exhibitionFloor.exhibition?.id?.equals(exhibition.id)!!) {
            return createNotFound("Floor not found")
        }

        return createOk(exhibitionFloorTranslator.translate(exhibitionFloor))
    }

    override fun listExhibitionFloors(exhibitionId: UUID?): Response {
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)

        val exhibition = exhibitionController.findExhibitionById(exhibitionId)?: return createNotFound("Exhibition $exhibitionId not found")
        val exhibitionFloors = exhibitionFloorController.listExhibitionFloors(exhibition)

        return createOk(exhibitionFloors.map (exhibitionFloorTranslator::translate))
    }

    override fun updateExhibitionFloor(exhibitionId: UUID?, floorId: UUID?, payload: ExhibitionFloor?): Response {
        payload ?: return createBadRequest("Missing request body")

        if (exhibitionId == null || floorId == null) {
            return createNotFound(EXHIBITION_NOT_FOUND)
        }

        val userId = loggerUserId ?: return createUnauthorized(UNAUTHORIZED)

        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val exhibitionFloor = exhibitionFloorController.findExhibitionFloorById(floorId) ?: return createNotFound("Floor $floorId not found")
        val name = payload.name
        val floorPlanUrl = payload.floorPlanUrl
        val floorPlanBounds = payload.floorPlanBounds
        val result = exhibitionFloorController.updateExhibitionFloor(exhibitionFloor, name, floorPlanUrl, floorPlanBounds, userId)

        return createOk(exhibitionFloorTranslator.translate(result))
    }

    override fun deleteExhibitionFloor(exhibitionId: UUID?, floorId: UUID?): Response {
        if (exhibitionId == null || floorId == null) {
            return createNotFound(EXHIBITION_NOT_FOUND)
        }

        loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val exhibitionFloor = exhibitionFloorController.findExhibitionFloorById(floorId) ?: return createNotFound("Floor $floorId not found")

        exhibitionFloorController.deleteExhibitionFloor(exhibitionFloor)

        return createNoContent()
    }

}
