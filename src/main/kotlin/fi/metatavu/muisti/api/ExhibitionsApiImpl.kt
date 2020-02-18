package fi.metatavu.muisti.api

import fi.metatavu.muisti.api.spec.ExhibitionsApi
import fi.metatavu.muisti.api.spec.model.*
import fi.metatavu.muisti.api.translate.ExhibitionRoomTranslator
import fi.metatavu.muisti.api.translate.ExhibitionTranslator
import fi.metatavu.muisti.api.translate.VisitorSessionTranslator
import fi.metatavu.muisti.exhibitions.ExhibitionController
import fi.metatavu.muisti.exhibitions.ExhibitionRoomController
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
open class ExhibitionsApiImpl(): ExhibitionsApi, AbstractApi() {

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

    /* Exhibitions */

    override fun createExhibition(payload: Exhibition?): Response? {
        if (payload == null) {
            return createBadRequest("Missing request body")
        }

        if (StringUtils.isBlank(payload.name)) {
            return createBadRequest("Missing exhibition name")
        }

        val userId = loggerUserId
        if (userId == null) {
            return createUnauthorized("Unauthorized")
        }

        val exhibition = exhibitionController.createExhibition(payload.name, userId)

        return createOk(exhibitionTranslator.translate(exhibition))
    }

    override fun findExhibition(exhibitionId: UUID?): Response? {
        if (exhibitionId == null) {
            return createNotFound("Exhibition not found")
        }

        val exhibition = exhibitionController.findExhibitionById(exhibitionId)
        if (exhibition == null) {
            return createNotFound("Exhibition $exhibitionId not found")
        }

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
            return createNotFound("Exhibition not found")
        }

        val userId = loggerUserId
        if (userId == null) {
            return createUnauthorized("Unauthorized")
        }

        val exhibition = exhibitionController.findExhibitionById(exhibitionId)
        if (exhibition == null) {
            return createNotFound("Exhibition $exhibitionId not found")
        }

        val updatedExhibition = exhibitionController.updateExhibition(exhibition, payload.name, userId)

        return createOk(exhibitionTranslator.translate(updatedExhibition))
    }

    override fun deleteExhibition(exhibitionId: UUID?): Response? {
        if (exhibitionId == null) {
            return createNotFound("Exhibition not found")
        }

        val userId = loggerUserId
        if (userId == null) {
            return createUnauthorized("Unauthorized")
        }

        val exhibition = exhibitionController.findExhibitionById(exhibitionId)
        if (exhibition == null) {
            return createNotFound("Exhibition $exhibitionId not found")
        }

        exhibitionController.deleteExhibition(exhibition)

        return createNoContent()
    }

    /* VisitorSessions */

    override fun listVisitorSessions(exhibitionId: UUID?): Response {
        if (exhibitionId == null) {
            return createNotFound("Exhibition not found")
        }

        val exhibition = exhibitionController.findExhibitionById(exhibitionId)
        if (exhibition == null) {
            return createNotFound("Exhibition $exhibitionId not found")
        }

        val userId = loggerUserId
        if (userId == null) {
            return createUnauthorized("Unauthorized")
        }

        val visitorSessions = visitorSessionController.listVisitorSessions(exhibition)

        return createOk(visitorSessions.map (visitorSessionTranslator::translate))

    }

    override fun createVisitorSession(exhibitionId: UUID?, payload: VisitorSession?): Response {
        if (payload == null) {
            return createBadRequest("Missing request body")
        }

        if (exhibitionId == null) {
            return createNotFound("Exhibition not found")
        }

        val exhibition = exhibitionController.findExhibitionById(exhibitionId)
        if (exhibition == null) {
            return createNotFound("Exhibition $exhibitionId not found")
        }

        val userId = loggerUserId
        if (userId == null) {
            return createUnauthorized("Unauthorized")
        }

        val visitorSession = visitorSessionController.createVisitorSession(exhibition, payload.state, userId)
        visitorSessionController.setVisitorSessionUsers(visitorSession, payload.users)
        visitorSessionController.setVisitorSessionVariables(visitorSession, payload.variables)

        return createOk(visitorSessionTranslator.translate(visitorSession))
    }

    override fun findVisitorSession(exhibitionId: UUID?, visitorSessionId: UUID?): Response {
        if (exhibitionId == null || visitorSessionId == null) {
            return createNotFound("Exhibition not found")
        }

        val userId = loggerUserId
        if (userId == null) {
            return createUnauthorized("Unauthorized")
        }

        val exhibition = exhibitionController.findExhibitionById(exhibitionId)
        if (exhibition == null) {
            return createNotFound("Exhibition $exhibitionId not found")
        }

        val visitorSession = visitorSessionController.findVisitorSessionById(visitorSessionId)
        if (visitorSession == null) {
            return createNotFound("Visitor session $visitorSessionId not found")
        }

        return createOk(visitorSessionTranslator.translate(visitorSession))
    }

    override fun deleteVisitorSession(exhibitionId: UUID?, visitorSessionId: UUID?): Response {
        if (exhibitionId == null || visitorSessionId == null) {
            return createNotFound("Exhibition not found")
        }

        val userId = loggerUserId
        if (userId == null) {
            return createUnauthorized("Unauthorized")
        }

        val exhibition = exhibitionController.findExhibitionById(exhibitionId)
        if (exhibition == null) {
            return createNotFound("Exhibition $exhibitionId not found")
        }

        val visitorSession = visitorSessionController.findVisitorSessionById(visitorSessionId)
        if (visitorSession == null) {
            return createNotFound("Visitor session $visitorSessionId not found")
        }

        visitorSessionController.deleteVisitorSession(visitorSession)

        return createNoContent()
    }

    override fun updateVisitorSession(exhibitionId: UUID?, visitorSessionId: UUID?, payload: VisitorSession?): Response {
        if (payload == null) {
            return createBadRequest("Missing request body")
        }

        if (exhibitionId == null || visitorSessionId == null) {
            return createNotFound("Exhibition not found")
        }

        val userId = loggerUserId
        if (userId == null) {
            return createUnauthorized("Unauthorized")
        }

        val exhibition = exhibitionController.findExhibitionById(exhibitionId)
        if (exhibition == null) {
            return createNotFound("Exhibition $exhibitionId not found")
        }

        val visitorSession = visitorSessionController.findVisitorSessionById(visitorSessionId)
        if (visitorSession == null) {
            return createNotFound("Visitor session $visitorSessionId not found")
        }

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
            return createNotFound("Exhibition not found")
        }

        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val userId = loggerUserId ?: return createUnauthorized("Unauthorized")

        val exhibitionRoom = exhibitionRoomController.createExhibitionRoom(exhibition, payload.name, userId)

        return createOk(exhibitionRoomTranslator.translate(exhibitionRoom))
    }

    override fun findExhibitionRoom(exhibitionId: UUID?, roomId: UUID?): Response {
        if (exhibitionId == null || roomId == null) {
            return createNotFound("Exhibition not found")
        }

        loggerUserId ?: return createUnauthorized("Unauthorized")
        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val exhibitionRoom = exhibitionRoomController.findExhibitionRoomById(roomId) ?: return createNotFound("Room $roomId not found")

        if (!exhibitionRoom.exhibition?.id?.equals(exhibition.id)!!) {
            return createNotFound("Room not found")
        }

        return createOk(exhibitionRoomTranslator.translate(exhibitionRoom))
    }

    override fun listExhibitionRooms(exhibitionId: UUID?): Response {
        if (exhibitionId == null) {
            return createNotFound("Exhibition not found")
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
            return createNotFound("Exhibition not found")
        }

        val userId = loggerUserId ?: return createUnauthorized("Unauthorized")

        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val exhibitionRoom = exhibitionRoomController.findExhibitionRoomById(roomId) ?: return createNotFound("Room $roomId not found")
        val result = exhibitionRoomController.updateExhibitionRoom(exhibitionRoom, payload.name, userId)

        return createOk(exhibitionRoomTranslator.translate(result))
    }

    override fun deleteExhibitionRoom(exhibitionId: UUID?, roomId: UUID?): Response {
        if (exhibitionId == null || roomId == null) {
            return createNotFound("Exhibition not found")
        }

        loggerUserId ?: return createUnauthorized("Unauthorized")
        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val exhibitionRoom = exhibitionRoomController.findExhibitionRoomById(roomId) ?: return createNotFound("Room $roomId not found")

        exhibitionRoomController.deleteExhibitionRoom(exhibitionRoom)

        return createNoContent()
    }

    /* Devices */

    override fun createDevice(exhibitionId: UUID?, device: Device?): Response {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findDevice(exhibitionId: UUID?, deviceId: UUID?): Response {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun listDevices(exhibitionId: UUID?): Response {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateDevice(exhibitionId: UUID?, deviceId: UUID?, device: Device?): Response {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteDevice(exhibitionId: UUID?, deviceId: UUID?): Response {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    /* Device groups */

    override fun createDeviceGroup(exhibitionId: UUID?, deviceGroup: DeviceGroup?): Response {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findDeviceGroup(exhibitionId: UUID?, deviceGroupId: UUID?): Response {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun listDeviceGroups(exhibitionId: UUID?): Response {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateDeviceGroup(exhibitionId: UUID?, deviceGroupId: UUID?, deviceGroup: DeviceGroup?): Response {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteDeviceGroup(exhibitionId: UUID?, deviceGroupId: UUID?): Response {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}