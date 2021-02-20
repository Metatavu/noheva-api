package fi.metatavu.muisti.api

import fi.metatavu.muisti.api.spec.ExhibitionsApi
import fi.metatavu.muisti.api.spec.model.*
import fi.metatavu.muisti.api.translate.*
import fi.metatavu.muisti.contents.ContentVersionController
import fi.metatavu.muisti.contents.ExhibitionPageController
import fi.metatavu.muisti.contents.GroupContentVersionController
import fi.metatavu.muisti.contents.PageLayoutController
import fi.metatavu.muisti.devices.DeviceModelController
import fi.metatavu.muisti.devices.ExhibitionDeviceController
import fi.metatavu.muisti.devices.ExhibitionDeviceGroupController
import fi.metatavu.muisti.devices.RfidAntennaController
import fi.metatavu.muisti.exhibitions.*
import fi.metatavu.muisti.keycloak.KeycloakController
import fi.metatavu.muisti.realtime.RealtimeNotificationController
import fi.metatavu.muisti.settings.SettingsController
import fi.metatavu.muisti.utils.CopyException
import fi.metatavu.muisti.visitors.VisitorController
import fi.metatavu.muisti.visitors.VisitorSessionController
import fi.metatavu.muisti.visitors.VisitorVariableController
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
 * @author Jari Nykänen
 */
@RequestScoped
@Stateful
@Suppress("unused")
class ExhibitionsApiImpl: ExhibitionsApi, AbstractApi() {

    @Inject
    private lateinit var logger: Logger

    @Inject
    private lateinit var exhibitionController: ExhibitionController

    @Inject
    private lateinit var exhibitionTranslator: ExhibitionTranslator

    @Inject
    private lateinit var visitorSessionController: VisitorSessionController

    @Inject
    private lateinit var settingsController: SettingsController

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
    private lateinit var contentVersionController: ContentVersionController

    @Inject
    private lateinit var groupContentVersionController: GroupContentVersionController

    @Inject
    private lateinit var contentVersionTranslator: ContentVersionTranslator

    @Inject
    private lateinit var groupContentVersionTranslator: GroupContentVersionTranslator

    @Inject
    private lateinit var rfidAntennaController: RfidAntennaController

    @Inject
    private lateinit var rfidAntennaTranslator: RfidAntennaTranslator

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
    private lateinit var visitorVariableController: VisitorVariableController

    @Inject
    private lateinit var visitorVariableTranslator: VisitorVariableTranslator

    @Inject
    private lateinit var realtimeNotificationController: RealtimeNotificationController

    @Inject
    private lateinit var keycloakController: KeycloakController

    /* Exhibitions */

    override fun createExhibition(payload: Exhibition?): Response? {
        if (payload == null) {
            return createBadRequest(MISSING_REQUEST_BODY)
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
            return createBadRequest(MISSING_REQUEST_BODY)
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

        val contentVersions = contentVersionController.listContentVersions(exhibition = exhibition, exhibitionRoom = null)
        if (contentVersions.isNotEmpty()) {
            val contentVersionIds = contentVersions.map { it.id }.joinToString()
            return createBadRequest("Cannot delete exhibition $exhibitionId because it's used in content versions $contentVersionIds")
        }

        exhibitionController.deleteExhibition(exhibition)

        return createNoContent()
    }

    /* Visitors */

    override fun createVisitor(exhibitionId: UUID?, payload: Visitor?): Response {
        payload ?: return createBadRequest(MISSING_REQUEST_BODY)
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val userId = loggerUserId ?: return createUnauthorized(UNAUTHORIZED)

        var userRepresentation = keycloakController.findUserByEmail(payload.email)
        if (userRepresentation == null) {
            userRepresentation = keycloakController.createUser(
                email = payload.email,
                birthYear = payload.birthYear,
                firstName = payload.firstName,
                language = payload.language,
                lastName = payload.lastName,
                phone = payload.phone,
                realmRoles = listOf("visitor")
            )
        } else {
            userRepresentation = keycloakController.updateUser(
                userRepresentation = userRepresentation,
                birthYear = payload.birthYear,
                firstName = payload.firstName,
                language = payload.language,
                lastName = payload.lastName,
                phone = payload.phone
            )
        }

        userRepresentation ?: return createInternalServerError("Failed to create visitor user")
        val visitor = visitorController.createVisitor(
            exhibition = exhibition,
            userRepresentation = userRepresentation,
            tagId = payload.tagId,
            creatorId = userId
        )

        realtimeNotificationController.notifyVisitorCreate(exhibitionId, visitor.id!!)

        return createOk(visitorTranslator.translate(visitor))
    }

    override fun findVisitor(exhibitionId: UUID?, visitorId: UUID?): Response {
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        visitorId ?: return createNotFound(VISITOR_NOT_FOUND)
        loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val visitor = visitorController.findVisitorById(visitorId) ?: return createNotFound("Visitor session $visitorId not found")
        return createOk(visitorTranslator.translate(visitor))
    }

    override fun listVisitors(exhibitionId: UUID?, tagId: String?, email: String?): Response {
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        var userId: UUID? = null

        if (email != null) {
            val userRepresentation = keycloakController.findUserByEmail(email = email) ?: return createOk(arrayOf<Visitor>())
            userId = UUID.fromString(userRepresentation.id)
        }

        val visitors = visitorController.listVisitors(
            exhibition = exhibition,
            tagId = tagId,
            userId = userId
        )

        return createOk(visitors.map (visitorTranslator::translate))
    }

    override fun updateVisitor(exhibitionId: UUID?, visitorId: UUID?, payload: Visitor?): Response {
        payload ?: return createBadRequest(MISSING_REQUEST_BODY)
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        visitorId ?: return createNotFound(VISITOR_NOT_FOUND)

        val userId = loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val visitor = visitorController.findVisitorById(visitorId) ?: return createNotFound("Visitor $visitorId not found")

        val userRepresentation = keycloakController.findUserById(visitor.userId)?: return createInternalServerError("Failed to find visitor user")

        keycloakController.updateUser(
            userRepresentation = userRepresentation,
            birthYear = payload.birthYear,
            firstName = payload.firstName,
            language = payload.language,
            lastName = payload.lastName,
            phone = payload.phone
        )

        val result = visitorController.updateVisitor(
            visitor = visitor,
            tagId = payload.tagId,
            lastModifierId = userId
        )

        realtimeNotificationController.notifyVisitorUpdate(exhibitionId, visitorId)

        return createOk(visitorTranslator.translate(result))
    }

    override fun deleteVisitor(exhibitionId: UUID?, visitorId: UUID?): Response {
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        visitorId ?: return createNotFound(VISITOR_NOT_FOUND)
        loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val visitor = visitorController.findVisitorById(visitorId) ?: return createNotFound("Visitor $visitorId not found")
        visitorController.deleteVisitor(visitor)

        realtimeNotificationController.notifyVisitorDelete(exhibitionId, visitorId)

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

    override fun createVisitorVariable(exhibitionId: UUID?, payload: VisitorVariable?): Response {
        payload ?: return createBadRequest(MISSING_REQUEST_BODY)
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val userId = loggerUserId ?: return createUnauthorized(UNAUTHORIZED)

        val visitorVariable = visitorVariableController.createVisitorVariable(
                exhibition = exhibition,
                name = payload.name,
                type = payload.type,
                enum = payload.enum,
                editableFromUI = payload.editableFromUI,
                creatorId = userId
        )

        return createOk(visitorVariableTranslator.translate(visitorVariable))
    }

    override fun findVisitorVariable(exhibitionId: UUID?, visitorVariableId: UUID?): Response {
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        visitorVariableId ?: return createNotFound(VISITOR_VARIABLE_NOT_FOUND)
        loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val visitorVariable = visitorVariableController.findVisitorVariableById(id = visitorVariableId) ?: return createNotFound("Visitor variable $visitorVariableId not found")

        return createOk(visitorVariableTranslator.translate(visitorVariable))
    }

    override fun listVisitorVariables(exhibitionId: UUID?, name: String?): Response {
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        loggerUserId ?: return createUnauthorized(UNAUTHORIZED)

        val visitorVariables = visitorVariableController.listVisitorVariables(
            exhibition = exhibition,
            name = name
        )

        return createOk(visitorVariables.map (visitorVariableTranslator::translate))
    }

    override fun updateVisitorVariable(exhibitionId: UUID?, visitorVariableId: UUID?, payload: VisitorVariable?): Response {
        payload ?: return createBadRequest(MISSING_REQUEST_BODY)
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        visitorVariableId ?: return createNotFound(VISITOR_VARIABLE_NOT_FOUND)

        val userId = loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val visitorVariable = visitorVariableController.findVisitorVariableById(id = visitorVariableId) ?: return createNotFound("Visitor variable $visitorVariableId not found")

        val result = visitorVariableController.updateVisitorVariable(
                visitorVariable = visitorVariable,
                name = payload.name,
                type = payload.type,
                enum = payload.enum,
                editableFromUI = payload.editableFromUI,
                lastModifierId = userId
        )

        return createOk(visitorVariableTranslator.translate(result))
    }

    override fun deleteVisitorVariable(exhibitionId: UUID?, visitorVariableId: UUID?): Response {
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        visitorVariableId ?: return createNotFound(VISITOR_VARIABLE_NOT_FOUND)
        loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val visitorVariable = visitorVariableController.findVisitorVariableById(id = visitorVariableId) ?: return createNotFound("Visitor variable $visitorVariableId not found")

        visitorVariableController.deleteVisitorVariable(visitorVariable)

        return createNoContent()
    }

    /* VisitorSessions */

    override fun createVisitorSession(exhibitionId: UUID?, payload: VisitorSession?): Response {
        payload ?: return createBadRequest(MISSING_REQUEST_BODY)
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val userId = loggerUserId ?: return createUnauthorized(UNAUTHORIZED)

        for (variable in payload.variables) {
            if (!visitorSessionController.isValidVisitorSessionVariable(exhibition = exhibition, visitorSessionVariable = variable)) {
                return createBadRequest("Variable ${variable.name} is not valid")
            }
        }

        val visitors = mutableListOf<fi.metatavu.muisti.persistence.model.Visitor>()
        for (visitorId in payload.visitorIds) {
            val visitor = visitorController.findVisitorById(visitorId)
            visitor ?: return createBadRequest("Invalid visitor $visitorId")
            visitors.add(visitor)
        }

        val visitedDeviceGroupList: MutableList<fi.metatavu.muisti.persistence.model.ExhibitionDeviceGroup> = mutableListOf()
        for (visitedDeviceGroup in payload.visitedDeviceGroups) {
            val deviceGroup = exhibitionDeviceGroupController.findDeviceGroupById(visitedDeviceGroup.deviceGroupId)
            deviceGroup ?: return createBadRequest("Invalid visitor ${visitedDeviceGroup.deviceGroupId}")
            visitedDeviceGroupList.add(deviceGroup)
        }

        val visitorSession = visitorSessionController.createVisitorSession(
            exhibition = exhibition,
            state = payload.state,
            language = payload.language,
            creatorId = userId
        )

        visitorSessionController.setVisitorSessionVisitors(visitorSession, visitors)
        visitorSessionController.setVisitorSessionVariables(visitorSession, payload.variables)
        visitorSessionController.setVisitorSessionVisitedDeviceGroups(visitorSession, payload.visitedDeviceGroups, visitedDeviceGroupList)
        realtimeNotificationController.notifyExhibitionVisitorSessionCreate(exhibitionId,  visitorSession.id!!)

        return createOk(visitorSessionTranslator.translate(visitorSession))
    }

    override fun findVisitorSession(exhibitionId: UUID?, visitorSessionId: UUID?): Response {
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        visitorSessionId ?: return createNotFound(VISITOR_SESSION_NOT_FOUND)
        loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val visitorSession = visitorSessionController.findVisitorSessionById(id = visitorSessionId) ?: return createNotFound("Visitor session $visitorSessionId not found")

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
        payload ?: return createBadRequest(MISSING_REQUEST_BODY)
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        visitorSessionId ?: return createNotFound(VISITOR_SESSION_NOT_FOUND)
        val userId = loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")

        for (variable in payload.variables) {
            if (!visitorSessionController.isValidVisitorSessionVariable(exhibition = exhibition, visitorSessionVariable = variable)) {
                return createBadRequest("Variable ${variable.name} is not valid")
            }
        }

        val visitorSession = visitorSessionController.findVisitorSessionById(id = visitorSessionId) ?: return createNotFound("Visitor session $visitorSessionId not found")

        val visitors = mutableListOf<fi.metatavu.muisti.persistence.model.Visitor>()
        for (visitorId in payload.visitorIds) {
            val visitor = visitorController.findVisitorById(visitorId)
            visitor ?: return createBadRequest("Invalid visitor $visitorId")
            visitors.add(visitor)
        }

        val visitedDeviceGroupList: MutableList<fi.metatavu.muisti.persistence.model.ExhibitionDeviceGroup> = mutableListOf()
        for (visitedDeviceGroup in payload.visitedDeviceGroups) {
            val deviceGroup = exhibitionDeviceGroupController.findDeviceGroupById(visitedDeviceGroup.deviceGroupId)
            deviceGroup ?: return createBadRequest("Invalid visitor ${visitedDeviceGroup.deviceGroupId}")
            visitedDeviceGroupList.add(deviceGroup)
        }

        val result = visitorSessionController.updateVisitorSession(
            visitorSession = visitorSession,
            state = payload.state,
            language = payload.language,
            lastModfierId =  userId
        )

        val usersChanged = visitorSessionController.setVisitorSessionVisitors(visitorSession, visitors)
        val variablesChanged = visitorSessionController.setVisitorSessionVariables(result, payload.variables)
        visitorSessionController.setVisitorSessionVisitedDeviceGroups(visitorSession, payload.visitedDeviceGroups, visitedDeviceGroupList)

        realtimeNotificationController.notifyExhibitionVisitorSessionUpdate(exhibitionId,  visitorSessionId, variablesChanged, usersChanged)

        return createOk(visitorSessionTranslator.translate(result))
    }

    override fun deleteVisitorSession(exhibitionId: UUID?, visitorSessionId: UUID?): Response {
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        visitorSessionId ?: return createNotFound(VISITOR_SESSION_NOT_FOUND)
        loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val visitorSession = visitorSessionController.findVisitorSessionById(id = visitorSessionId) ?: return createNotFound("Visitor session $visitorSessionId not found")

        visitorSessionController.deleteVisitorSession(visitorSession)

        realtimeNotificationController.notifyExhibitionVisitorSessionDelete(exhibitionId,  visitorSessionId)

        return createNoContent()
    }

    /* Rooms */

    override fun createExhibitionRoom(exhibitionId: UUID?, payload: ExhibitionRoom?): Response {
        payload ?: return createBadRequest(MISSING_REQUEST_BODY)
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)

        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val floor = exhibitionFloorController.findExhibitionFloorById(payload.floorId) ?: return createBadRequest("Exhibition floor ${payload.floorId} not found")
        val userId = loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        val exhibitionRoom = exhibitionRoomController.createExhibitionRoom(
            exhibition = exhibition,
            name = payload.name,
            color = payload.color,
            geoShape = payload.geoShape,
            floor = floor,
            creatorId = userId
        )

        return createOk(exhibitionRoomTranslator.translate(exhibitionRoom))
    }

    override fun findExhibitionRoom(exhibitionId: UUID?, roomId: UUID?): Response {
        exhibitionId ?: return createBadRequest("Missing exhibition ID")
        roomId ?: return createBadRequest("Missing room ID")

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
        payload ?: return createBadRequest(MISSING_REQUEST_BODY)
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
            color = payload.color,
            geoShape = payload.geoShape,
            floor = floor,
            modifierId = userId
        )

        return createOk(exhibitionRoomTranslator.translate(result))
    }

    override fun deleteExhibitionRoom(exhibitionId: UUID?, roomId: UUID?): Response {
        exhibitionId ?: return createBadRequest("Missing exhibition ID")
        roomId ?: return createBadRequest("Missing room ID")

        loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val exhibitionRoom = exhibitionRoomController.findExhibitionRoomById(roomId) ?: return createNotFound("Room $roomId not found")
        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")

        val contentVersions = contentVersionController.listContentVersions(exhibition = exhibition, exhibitionRoom = exhibitionRoom)
        if (contentVersions.isNotEmpty()) {
            val contentVersionIds = contentVersions.map { it.id }.joinToString()
            return createBadRequest("Cannot delete room $roomId because it's used in content versions $contentVersionIds")
        }

        exhibitionRoomController.deleteExhibitionRoom(exhibitionRoom)

        return createNoContent()
    }

    /* Devices */

    override fun createExhibitionDevice(exhibitionId: UUID?, payload: ExhibitionDevice?): Response {
        payload ?: return createBadRequest(MISSING_REQUEST_BODY)
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        payload.groupId ?: return createBadRequest("Missing exhibition group id")

        val exhibitionGroup = exhibitionDeviceGroupController.findDeviceGroupById(payload.groupId) ?: return createBadRequest("Invalid exhibition group id ${payload.groupId}")
        val model = deviceModelController.findDeviceModelById(payload.modelId) ?: return createBadRequest("Device model ${payload.modelId} not found")
        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val userId = loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        val location = payload.location
        val screenOrientation = payload.screenOrientation

        var idlePage: fi.metatavu.muisti.persistence.model.ExhibitionPage? = null
        if (payload.idlePageId != null) {
            idlePage = exhibitionPageController.findExhibitionPageById(payload.idlePageId) ?: return createBadRequest("Idle page ${payload.idlePageId} not found")
        }

        val exhibitionDevice = exhibitionDeviceController.createExhibitionDevice(
            exhibition = exhibition,
            exhibitionDeviceGroup = exhibitionGroup,
            deviceModel = model,
            name = payload.name,
            location = location,
            screenOrientation = screenOrientation,
            idlePage = idlePage,
            creatorId = userId
        )

        realtimeNotificationController.notifyDeviceCreate(id = exhibitionDevice.id!!, exhibitionId = exhibitionId)

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
            exhibitionDeviceGroup = exhibitionDeviceGroupController.findDeviceGroupById(exhibitionDeviceGroupId)
        }

        val exhibitionDevices = exhibitionDeviceController.listExhibitionDevices(exhibition, exhibitionDeviceGroup)

        return createOk(exhibitionDevices.map (exhibitionDeviceTranslator::translate))
    }

    override fun updateExhibitionDevice(exhibitionId: UUID?, deviceId: UUID?, payload: ExhibitionDevice?): Response {
        payload ?: return createBadRequest(MISSING_REQUEST_BODY)
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        deviceId ?: return createNotFound("Device not found")
        payload.groupId ?: return createBadRequest("Missing exhibition group id")
        val exhibitionGroup = exhibitionDeviceGroupController.findDeviceGroupById(payload.groupId) ?: return createBadRequest("Invalid exhibition group id ${payload.groupId}")
        val model = deviceModelController.findDeviceModelById(payload.modelId) ?: return createBadRequest("Device model $payload.modelId not found")
        val userId = loggerUserId ?: return createUnauthorized(UNAUTHORIZED)

        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val exhibitionDevice = exhibitionDeviceController.findExhibitionDeviceById(deviceId) ?: return createNotFound("Device $deviceId not found")
        val groupChanged = exhibitionDevice.exhibitionDeviceGroup?.id != exhibitionGroup.id
        val location = payload.location
        val screenOrientation = payload.screenOrientation

        var idlePage: fi.metatavu.muisti.persistence.model.ExhibitionPage? = null
        if (payload.idlePageId != null) {
            idlePage = exhibitionPageController.findExhibitionPageById(payload.idlePageId) ?: return createBadRequest("Idle page ${payload.idlePageId} not found")
        }

        val result = exhibitionDeviceController.updateExhibitionDevice(
            exhibitionDevice = exhibitionDevice,
            exhibitionDeviceGroup = exhibitionGroup,
            deviceModel = model,
            name = payload.name,
            location = location,
            screenOrientation = screenOrientation,
            idlePage = idlePage,
            modifierId = userId
        )

        realtimeNotificationController.notifyDeviceUpdate(id = deviceId, exhibitionId = exhibitionId, groupChanged = groupChanged)

        return createOk(exhibitionDeviceTranslator.translate(result))
    }

    override fun deleteExhibitionDevice(exhibitionId: UUID?, deviceId: UUID?): Response {
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        deviceId ?: return createNotFound("Device not found")

        loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val exhibitionDevice = exhibitionDeviceController.findExhibitionDeviceById(deviceId) ?: return createNotFound("Device $deviceId not found")

        val devicePages = exhibitionPageController.listExhibitionPages(
            exhibition = exhibition,
            exhibitionDevice = exhibitionDevice,
            exhibitionContentVersion = null
        )

        if (devicePages.isNotEmpty()) {
            val devicePageIds = devicePages.map { it.id }.joinToString()
            return createBadRequest("Cannot delete device $deviceId because it's pages $devicePageIds are assigned to the device")
        }

        exhibitionDeviceController.deleteExhibitionDevice(exhibitionDevice)
        realtimeNotificationController.notifyDeviceDelete(id = deviceId, exhibitionId = exhibitionId)

        return createNoContent()
    }

    /* RFID antenna */

    override fun createRfidAntenna(exhibitionId: UUID?, payload: RfidAntenna?): Response {
        payload ?: return createBadRequest(MISSING_REQUEST_BODY)
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)

        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val userId = loggerUserId ?: return createUnauthorized(UNAUTHORIZED)

        if (payload.name.isNullOrEmpty()) {
            return createBadRequest("Name cannot be empty")
        }

        if (payload.readerId.isNullOrEmpty()) {
            return createBadRequest("ReaderId cannot be empty")
        }

        var deviceGroup: fi.metatavu.muisti.persistence.model.ExhibitionDeviceGroup? = null
        if (payload.groupId != null) {
            deviceGroup = exhibitionDeviceGroupController.findDeviceGroupById(payload.groupId) ?: return createBadRequest("Invalid device group id ${payload.groupId}")
        }

        var room: fi.metatavu.muisti.persistence.model.ExhibitionRoom? = null
        if (payload.roomId != null) {
            room = exhibitionRoomController.findExhibitionRoomById(payload.roomId) ?: return createBadRequest("Invalid room id ${payload.roomId}")
        }

        val visitorSessionStartThreshold = payload.visitorSessionStartThreshold
        val visitorSessionEndThreshold = payload.visitorSessionEndThreshold

        val rfidAntenna = rfidAntennaController.createRfidAntenna(
            exhibition = exhibition,
            deviceGroup = deviceGroup,
            room = room,
            name = payload.name,
            readerId = payload.readerId,
            antennaNumber = payload.antennaNumber,
            location = payload.location,
            visitorSessionStartThreshold = visitorSessionStartThreshold,
            visitorSessionEndThreshold = visitorSessionEndThreshold,
            creatorId = userId
        )

        realtimeNotificationController.notifyRfidAntennaCreate(id = rfidAntenna.id!!, exhibitionId = exhibitionId)

        return createOk(rfidAntennaTranslator.translate(rfidAntenna))
    }

    override fun findRfidAntenna(exhibitionId: UUID?, rfidAntennaId: UUID?): Response {
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        rfidAntennaId ?: return createNotFound(RFID_ANTENNA_NOT_FOUND)

        loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val rfidAntenna = rfidAntennaController.findRfidAntennaById(rfidAntennaId) ?: return createNotFound("RFID antenna $rfidAntennaId not found")

        if (!rfidAntenna.exhibition?.id?.equals(exhibition.id)!!) {
            return createNotFound("RFID antenna $rfidAntennaId not found")
        }

        return createOk(rfidAntennaTranslator.translate(rfidAntenna))
    }

    override fun listRfidAntennas(exhibitionId: UUID?, roomId: UUID?, deviceGroupId: UUID?): Response {
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)

        val exhibition = exhibitionController.findExhibitionById(exhibitionId)?: return createNotFound("Exhibition $exhibitionId not found")

        var deviceGroup: fi.metatavu.muisti.persistence.model.ExhibitionDeviceGroup? = null
        if (deviceGroupId != null) {
            deviceGroup = exhibitionDeviceGroupController.findDeviceGroupById(deviceGroupId) ?: return createBadRequest("Invalid device group id $deviceGroupId")
        }

        var room: fi.metatavu.muisti.persistence.model.ExhibitionRoom? = null
        if (roomId != null) {
            room = exhibitionRoomController.findExhibitionRoomById(roomId) ?: return createBadRequest("Invalid room id $roomId")
        }

        val rfidAntennas = rfidAntennaController.listRfidAntennas(
            exhibition = exhibition,
            room = room,
            deviceGroup = deviceGroup
        )

        return createOk(rfidAntennas.map (rfidAntennaTranslator::translate))
    }

    override fun updateRfidAntenna(exhibitionId: UUID?, rfidAntennaId: UUID?, payload: RfidAntenna?): Response {
        payload ?: return createBadRequest(MISSING_REQUEST_BODY)
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        rfidAntennaId ?: return createNotFound(RFID_ANTENNA_NOT_FOUND)

        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val userId = loggerUserId ?: return createUnauthorized(UNAUTHORIZED)

        val rfidAntenna = rfidAntennaController.findRfidAntennaById(rfidAntennaId) ?: return createNotFound("RFID antenna $rfidAntennaId not found")

        if (!rfidAntenna.exhibition?.id?.equals(exhibition.id)!!) {
            return createNotFound("RFID antenna $rfidAntennaId not found")
        }

        if (payload.name.isNullOrEmpty()) {
            return createBadRequest("Name cannot be empty")
        }

        if (payload.readerId.isNullOrEmpty()) {
            return createBadRequest("ReaderId cannot be empty")
        }

        var deviceGroup: fi.metatavu.muisti.persistence.model.ExhibitionDeviceGroup? = null
        if (payload.groupId != null) {
            deviceGroup = exhibitionDeviceGroupController.findDeviceGroupById(payload.groupId) ?: return createBadRequest("Invalid device group id ${payload.groupId}")
        }

        var room: fi.metatavu.muisti.persistence.model.ExhibitionRoom? = null
        if (payload.roomId != null) {
            room = exhibitionRoomController.findExhibitionRoomById(payload.roomId) ?: return createBadRequest("Invalid room id ${payload.roomId}")
        }

        val visitorSessionStartThreshold = payload.visitorSessionStartThreshold
        val visitorSessionEndThreshold = payload.visitorSessionEndThreshold

        val groupChanged = rfidAntenna.deviceGroup?.id != deviceGroup?.id

        val result = rfidAntennaController.updateRfidAntenna(
            rfidAntenna = rfidAntenna,
            deviceGroup = deviceGroup,
            room = room,
            name = payload.name,
            readerId = payload.readerId,
            antennaNumber = payload.antennaNumber,
            location = payload.location,
            visitorSessionStartThreshold = visitorSessionStartThreshold,
            visitorSessionEndThreshold = visitorSessionEndThreshold,
            modifierId = userId
        )

        realtimeNotificationController.notifyRfidAntennaUpdate(id = rfidAntennaId, exhibitionId = exhibitionId, groupChanged = groupChanged)

        return createOk(rfidAntennaTranslator.translate(result))
    }

    override fun deleteRfidAntenna(exhibitionId: UUID?, rfidAntennaId: UUID?): Response {
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        rfidAntennaId ?: return createNotFound(RFID_ANTENNA_NOT_FOUND)

        loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val rfidAntenna = rfidAntennaController.findRfidAntennaById(rfidAntennaId) ?: return createNotFound("RFID antenna $rfidAntennaId not found")
        if (!rfidAntenna.exhibition?.id?.equals(exhibition.id)!!) {
            return createNotFound("RFID antenna $rfidAntennaId not found")
        }

        rfidAntennaController.deleteRfidAntenna(rfidAntenna)
        realtimeNotificationController.notifyRfidAntennaDelete(id = rfidAntennaId, exhibitionId = exhibitionId)

        return createNoContent()
    }

    /* Exhibition device groups */

    override fun createExhibitionDeviceGroup(
        exhibitionId: UUID?,
        sourceDeviceGroupId: UUID?,
        payload: ExhibitionDeviceGroup?
    ): Response {
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val userId = loggerUserId ?: return createUnauthorized(UNAUTHORIZED)

        val deviceGroup = if (sourceDeviceGroupId != null) {
            val sourceDeviceGroup = exhibitionDeviceGroupController.findDeviceGroupById(id = sourceDeviceGroupId) ?: return createBadRequest("Source device group $sourceDeviceGroupId not found")

            try {
                exhibitionDeviceGroupController.copyDeviceGroup(
                    sourceDeviceGroup = sourceDeviceGroup,
                    creatorId = userId
                )
            } catch (e: CopyException) {
                logger.error("Failed to copy device group", e)
                return createInternalServerError("Failed to copy device group")
            }
        } else {
            payload ?: return createBadRequest(MISSING_REQUEST_BODY)
            val room = exhibitionRoomController.findExhibitionRoomById(payload.roomId) ?: return createNotFound("Exhibition room ${payload.roomId} not found")

            exhibitionDeviceGroupController.createExhibitionDeviceGroup(
                exhibition = exhibition,
                name = payload.name,
                allowVisitorSessionCreation = payload.allowVisitorSessionCreation,
                room = room,
                visitorSessionEndTimeout = payload.visitorSessionEndTimeout,
                visitorSessionStartStrategy = payload.visitorSessionStartStrategy,
                creatorId = userId
            )
        }

        realtimeNotificationController.notifyDeviceGroupCreate(id = deviceGroup.id!!, exhibitionId = exhibitionId)
        return createOk(exhibitionDeviceGroupTranslator.translate(deviceGroup))
    }

    override fun findExhibitionDeviceGroup(exhibitionId: UUID?, deviceGroupId: UUID?): Response {
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        deviceGroupId?: return createNotFound("Device group not found")

        loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val exhibitionDeviceGroup = exhibitionDeviceGroupController.findDeviceGroupById(deviceGroupId) ?: return createNotFound("Room $deviceGroupId not found")

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
        payload ?: return createBadRequest(MISSING_REQUEST_BODY)
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        deviceGroupId?: return createNotFound("Device group not found")
        val userId = loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val room = exhibitionRoomController.findExhibitionRoomById(payload.roomId)  ?: return createNotFound("Exhibition room ${payload.roomId} not found")
        val visitorSessionEndTimeout = payload.visitorSessionEndTimeout
        val visitorSessionStartStrategy = payload.visitorSessionStartStrategy

        val exhibitionDeviceGroup = exhibitionDeviceGroupController.findDeviceGroupById(deviceGroupId) ?: return createNotFound("Room $deviceGroupId not found")
        val result = exhibitionDeviceGroupController.updateExhibitionDeviceGroup(
            exhibitionDeviceGroup = exhibitionDeviceGroup,
            room = room,
            name = payload.name,
            allowVisitorSessionCreation = payload.allowVisitorSessionCreation,
            visitorSessionEndTimeout = visitorSessionEndTimeout,
            visitorSessionStartStrategy = visitorSessionStartStrategy,
            modifierId = userId
        )

        realtimeNotificationController.notifyDeviceGroupUpdate(id = deviceGroupId, exhibitionId = exhibitionId)

        return createOk(exhibitionDeviceGroupTranslator.translate(result))
    }

    override fun deleteExhibitionDeviceGroup(exhibitionId: UUID?, deviceGroupId: UUID?): Response {
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        deviceGroupId?: return createNotFound("Device group not found")
        loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val exhibitionDeviceGroup = exhibitionDeviceGroupController.findDeviceGroupById(deviceGroupId) ?: return createNotFound("Room $deviceGroupId not found")

        val groupContentVersions = groupContentVersionController.listGroupContentVersions(exhibition = exhibition, contentVersion = null, deviceGroup = exhibitionDeviceGroup)

        if (groupContentVersions.isNotEmpty()) {
            val groupContentVersionIds = groupContentVersions.map { it.id }.joinToString()
            return createBadRequest("Cannot delete device group $deviceGroupId because it's used in group content versions $groupContentVersionIds")
        }

        exhibitionDeviceGroupController.deleteExhibitionDeviceGroup(exhibitionDeviceGroup)

        realtimeNotificationController.notifyDeviceGroupDelete(id = deviceGroupId, exhibitionId = exhibitionId)

        return createNoContent()
    }

    /* Pages */

    override fun createExhibitionPage(exhibitionId: UUID?, payload: ExhibitionPage?): Response {
        payload ?: return createBadRequest(MISSING_REQUEST_BODY)
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val layout = pageLayoutController.findPageLayoutById(payload.layoutId) ?: return createBadRequest("Layout $payload.layoutId not found")
        val device = exhibitionDeviceController.findExhibitionDeviceById(payload.deviceId) ?: return createBadRequest("Device ${payload.deviceId} not found")
        val contentVersion = contentVersionController.findContentVersionById(payload.contentVersionId) ?: return createBadRequest("Content version ${payload.contentVersionId} not found")
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

        val userId = loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        val name = payload.name
        val resources = payload.resources
        val eventTriggers = payload.eventTriggers
        val enterTransitions = payload.enterTransitions
        val exitTransitions = payload.exitTransitions
        val orderNumber = payload.orderNumber

        val exhibitionPage = exhibitionPageController.createPage(
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

    override fun listExhibitionPages(exhibitionId: UUID?, contentVersionId: UUID?, exhibitionDeviceId: UUID?): Response {
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)

        val exhibition = exhibitionController.findExhibitionById(exhibitionId)?: return createNotFound("Exhibition $exhibitionId not found")
        var exhibitionDevice : fi.metatavu.muisti.persistence.model.ExhibitionDevice? = null
        if (exhibitionDeviceId != null) {
            exhibitionDevice = exhibitionDeviceController.findExhibitionDeviceById(exhibitionDeviceId)
        }

        var contentVersion: fi.metatavu.muisti.persistence.model.ContentVersion? = null
        if (contentVersionId != null) {
            contentVersion = contentVersionController.findContentVersionById(contentVersionId)
            contentVersion ?: return createBadRequest("Content version $contentVersionId not found")
        }

        val exhibitionPages = exhibitionPageController.listExhibitionPages(exhibition, exhibitionDevice, contentVersion)
        return createOk(exhibitionPages.map (exhibitionPageTranslator::translate))
    }

    override fun updateExhibitionPage(exhibitionId: UUID?, pageId: UUID?, payload: ExhibitionPage?): Response {
        payload ?: return createBadRequest(MISSING_REQUEST_BODY)
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        pageId ?: return createNotFound(EXHIBITION_NOT_FOUND)

        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound(EXHIBITION_NOT_FOUND)
        val userId = loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        val layout = pageLayoutController.findPageLayoutById(payload.layoutId) ?: return createBadRequest("Layout $payload.layoutId not found")
        val device = exhibitionDeviceController.findExhibitionDeviceById(payload.deviceId) ?: return createBadRequest("Device ${payload.deviceId} not found")
        val name = payload.name
        val resources = payload.resources
        val eventTriggers = payload.eventTriggers
        val contentVersion = contentVersionController.findContentVersionById(payload.contentVersionId) ?: return createBadRequest("Content version ${payload.contentVersionId} not found")
        val enterTransitions = payload.enterTransitions
        val exitTransitions = payload.exitTransitions
        val orderNumber = payload.orderNumber ?: return createBadRequest("Page $pageId didn't have order number")

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
            orderNumber = orderNumber,
            modifierId = userId
        )

        realtimeNotificationController.notifyExhibitionPageUpdate(exhibitionId, pageId)

        return createOk(exhibitionPageTranslator.translate(updatedPage))
    }

    override fun deleteExhibitionPage(exhibitionId: UUID?, pageId: UUID?): Response {
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        pageId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound(EXHIBITION_NOT_FOUND)
        loggerUserId ?: return createUnauthorized(UNAUTHORIZED)

        loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val page = exhibitionPageController.findExhibitionPageById(pageId) ?: return createNotFound("Page $pageId not found")

        val idlePageDevices = exhibitionDeviceController.listDevicesByIdlePage(idlePage = page)
        if (idlePageDevices.isNotEmpty()) {
            val idlePageDeviceIds = idlePageDevices.map { it.id }.joinToString()
            return createBadRequest("Cannot delete page $pageId because it's used as idle page in devices $idlePageDeviceIds")
        }

        exhibitionPageController.deleteExhibitionPage(page)
        realtimeNotificationController.notifyExhibitionPageDelete(exhibitionId, pageId)
        return createNoContent()
    }

    /* content version */

    override fun createContentVersion(exhibitionId: UUID?, payload: ContentVersion?): Response {
        payload ?: return createBadRequest(MISSING_REQUEST_BODY)
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val userId = loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        val name = payload.name ?: return createBadRequest("Missing name")
        val language = payload.language ?: return createBadRequest("Missing language")

        val exhibitionRooms = mutableListOf<fi.metatavu.muisti.persistence.model.ExhibitionRoom>()
        for (roomId in payload.rooms) {
            val room = exhibitionRoomController.findExhibitionRoomById(roomId)
            room ?: return createBadRequest("Invalid room $roomId")
            exhibitionRooms.add(room)

            val anotherContentVersion = contentVersionController.findContentVersionByNameRoomAndLanguage(
                name = name,
                language = language,
                room = room
            )

            if (anotherContentVersion != null) {
                return createBadRequest("Content version with same name and language already exists in given room")
            }
        }

        val contentVersion = contentVersionController.createContentVersion(
            exhibition = exhibition,
            name = name,
            language = language,
            activeCondition = payload.activeCondition,
            creatorId = userId
        )

        contentVersionController.setContentVersionRooms(contentVersion, exhibitionRooms)
        return createOk(contentVersionTranslator.translate(contentVersion))
    }

    override fun findContentVersion(exhibitionId: UUID?, contentVersionId: UUID?): Response {
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        contentVersionId?: return createNotFound(CONTENT_VERSION_NOT_FOUND)

        loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val contentVersion = contentVersionController.findContentVersionById(contentVersionId) ?: return createNotFound("Content version $contentVersionId not found")

        if (!contentVersion.exhibition?.id?.equals(exhibition.id)!!) {
            return createNotFound(CONTENT_VERSION_NOT_FOUND)
        }

        return createOk(contentVersionTranslator.translate(contentVersion))
    }

    override fun listContentVersions(exhibitionId: UUID?, roomId: UUID?): Response {
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val room = exhibitionRoomController.findExhibitionRoomById(roomId)
        val contentVersions = contentVersionController.listContentVersions(exhibition, room)
        return createOk(contentVersions.map (contentVersionTranslator::translate))
    }

    override fun updateContentVersion(exhibitionId: UUID?, contentVersionId: UUID?, payload: ContentVersion?): Response {
        payload ?: return createBadRequest(MISSING_REQUEST_BODY)
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        contentVersionId?: return createNotFound(CONTENT_VERSION_NOT_FOUND)
        val userId = loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val contentVersion = contentVersionController.findContentVersionById(contentVersionId) ?: return createNotFound("Content version $contentVersionId not found")
        val name = payload.name
        val language = payload.language
        val exhibitionRooms = mutableListOf<fi.metatavu.muisti.persistence.model.ExhibitionRoom>()
        for (roomId in payload.rooms) {
            val room = exhibitionRoomController.findExhibitionRoomById(roomId)
            room ?: return createBadRequest("Invalid room id $roomId")
            exhibitionRooms.add(room)

            val anotherContentVersion = contentVersionController.findContentVersionByNameRoomAndLanguage(
                name = name,
                language = language,
                room = room
            )

            if (anotherContentVersion != null && contentVersionId != contentVersion.id) {
                return createBadRequest("Another content version with same name and language already exists in given room")
            }
        }

        val result = contentVersionController.updateContentVersion(
            contentVersion = contentVersion,
            name = name,
            language = language,
            activeCondition = payload.activeCondition,
            modifierId =  userId
        )
        
        contentVersionController.setContentVersionRooms(result, exhibitionRooms)
        return createOk(contentVersionTranslator.translate(result))
    }

    override fun deleteContentVersion(exhibitionId: UUID?, contentVersionId: UUID?): Response {
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        contentVersionId?: return createNotFound(CONTENT_VERSION_NOT_FOUND)
        loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val contentVersion = contentVersionController.findContentVersionById(contentVersionId) ?: return createNotFound("Content version $contentVersionId not found")

        val groupContentVersions = groupContentVersionController.listGroupContentVersions(
            contentVersion = contentVersion,
            exhibition = exhibition,
            deviceGroup = null
        )

        if (groupContentVersions.isNotEmpty()) {
            val groupContentVersionIds = groupContentVersions.map { it.id }.joinToString()
            return createBadRequest("Cannot delete content version $contentVersionId because it's used in group content versions $groupContentVersionIds")
        }

        contentVersionController.deleteContentVersion(contentVersion)
        return createNoContent()
    }

    /* group content version */

    override fun createGroupContentVersion(exhibitionId: UUID?, payload: GroupContentVersion?): Response {
        payload ?: return createBadRequest(MISSING_REQUEST_BODY)
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val userId = loggerUserId ?: return createUnauthorized(UNAUTHORIZED)

        val contentVersionId = payload.contentVersionId
        val contentVersion = contentVersionController.findContentVersionById(contentVersionId) ?: return createNotFound("Content version $contentVersionId not found")

        val deviceGroupId = payload.deviceGroupId
        val deviceGroup = exhibitionDeviceGroupController.findDeviceGroupById(deviceGroupId) ?: return createBadRequest("Invalid exhibition group id $deviceGroupId")

        val name = payload.name
        val status = payload.status

        val groupContentVersion = groupContentVersionController.createGroupContentVersion(exhibition, name, status, contentVersion, deviceGroup, userId)
        return createOk(groupContentVersionTranslator.translate(groupContentVersion))
    }

    override fun findGroupContentVersion(exhibitionId: UUID?, groupContentVersionId: UUID?): Response {
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        groupContentVersionId?: return createNotFound(GROUP_CONTENT_VERSION_NOT_FOUND)

        loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val groupContentVersion = groupContentVersionController.findGroupContentVersionById(groupContentVersionId) ?: return createNotFound("Group content version $groupContentVersionId not found")

        if (!groupContentVersion.exhibition?.id?.equals(exhibition.id)!!) {
            return createNotFound(GROUP_CONTENT_VERSION_NOT_FOUND)
        }

        return createOk(groupContentVersionTranslator.translate(groupContentVersion))
    }

    override fun listGroupContentVersions(exhibitionId: UUID?, contentVersionId: UUID?, deviceGroupId: UUID?): Response {
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId)?: return createNotFound("Exhibition $exhibitionId not found")

        var contentVersion: fi.metatavu.muisti.persistence.model.ContentVersion? = null
        if (contentVersionId != null) {
            contentVersion = contentVersionController.findContentVersionById(contentVersionId) ?: return createBadRequest("Content version $contentVersionId not found")
        }

        var deviceGroup: fi.metatavu.muisti.persistence.model.ExhibitionDeviceGroup? = null
        if (deviceGroupId != null) {
            deviceGroup = exhibitionDeviceGroupController.findDeviceGroupById(deviceGroupId)?: return createBadRequest("Device group $deviceGroupId not found")
        }

        val groupContentVersions = groupContentVersionController.listGroupContentVersions(exhibition = exhibition, contentVersion = contentVersion, deviceGroup = deviceGroup)

        return createOk(groupContentVersions.map (groupContentVersionTranslator::translate))
    }

    override fun updateGroupContentVersion(exhibitionId: UUID?, groupContentVersionId: UUID?, payload: GroupContentVersion?): Response {
        payload ?: return createBadRequest(MISSING_REQUEST_BODY)
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        groupContentVersionId?: return createNotFound(GROUP_CONTENT_VERSION_NOT_FOUND)
        val userId = loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val groupContentVersion = groupContentVersionController.findGroupContentVersionById(groupContentVersionId) ?: return createNotFound("Group content version $groupContentVersionId not found")

        val contentVersionId = payload.contentVersionId
        val contentVersion = contentVersionController.findContentVersionById(contentVersionId) ?: return createNotFound("Content version $contentVersionId not found")

        val deviceGroupId = payload.deviceGroupId
        val deviceGroup = exhibitionDeviceGroupController.findDeviceGroupById(deviceGroupId) ?: return createBadRequest("Invalid exhibition group id $deviceGroupId")

        val name = payload.name
        val status = payload.status
        val result = groupContentVersionController.updateGroupContentVersion(groupContentVersion, name, status, contentVersion, deviceGroup, userId)

        return createOk(groupContentVersionTranslator.translate(result))
    }

    override fun deleteGroupContentVersion(exhibitionId: UUID?, groupContentVersionId: UUID?): Response {
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        groupContentVersionId?: return createNotFound(GROUP_CONTENT_VERSION_NOT_FOUND)
        loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val groupContentVersion = groupContentVersionController.findGroupContentVersionById(groupContentVersionId) ?: return createNotFound("Group content version $groupContentVersionId not found")
        groupContentVersionController.deleteGroupContentVersion(groupContentVersion)
        return createNoContent()
    }

    /* Floors */

    override fun createExhibitionFloor(exhibitionId: UUID?, payload: ExhibitionFloor?): Response {
        payload ?: return createBadRequest(MISSING_REQUEST_BODY)
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
        payload ?: return createBadRequest(MISSING_REQUEST_BODY)

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
