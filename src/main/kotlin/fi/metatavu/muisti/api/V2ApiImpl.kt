package fi.metatavu.muisti.api

import fi.metatavu.muisti.api.spec.V2Api
import fi.metatavu.muisti.api.spec.model.VisitorSessionV2
import fi.metatavu.muisti.api.translate.VisitorSessionV2Translator
import fi.metatavu.muisti.devices.ExhibitionDeviceGroupController
import fi.metatavu.muisti.exhibitions.ExhibitionController
import fi.metatavu.muisti.realtime.RealtimeNotificationController
import fi.metatavu.muisti.visitors.VisitorController
import fi.metatavu.muisti.visitors.VisitorSessionController
import java.time.OffsetDateTime
import java.util.*
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.transaction.Transactional
import javax.ws.rs.core.Response

/**
 * V2 API REST endpoints
 *
 * @author Antti Leppä
 * @author Jari Nykänen
 */
@RequestScoped
@Transactional
@Suppress("unused")
class V2ApiImpl: V2Api, AbstractApi() {

    @Inject
    private lateinit var exhibitionController: ExhibitionController

    @Inject
    private lateinit var visitorController: VisitorController

    @Inject
    private lateinit var visitorSessionController: VisitorSessionController

    @Inject
    private lateinit var exhibitionDeviceGroupController: ExhibitionDeviceGroupController

    @Inject
    private lateinit var realtimeNotificationController: RealtimeNotificationController

    @Inject
    private lateinit var visitorSessionV2Translator: VisitorSessionV2Translator

    /* Visitor Sessions */

    override fun createVisitorSessionV2(
        exhibitionId: UUID?,
        payload: VisitorSessionV2?
    ): Response {
        payload ?: return createBadRequest(MISSING_REQUEST_BODY)
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val userId = loggedUserId ?: return createUnauthorized(UNAUTHORIZED)

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

        return createOk(visitorSessionV2Translator.translate(visitorSession))
    }

    override fun findVisitorSessionV2(
        exhibitionId: UUID?,
        visitorSessionId: UUID?
    ): Response {
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        visitorSessionId ?: return createNotFound(VISITOR_SESSION_NOT_FOUND)
        loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val visitorSession = visitorSessionController.findVisitorSessionById(id = visitorSessionId) ?: return createNotFound("Visitor session $visitorSessionId not found")

        return createOk(visitorSessionV2Translator.translate(visitorSession))
    }

    override fun listVisitorSessionsV2(
        exhibitionId: UUID?,
        tagId: String?,
        modifiedAfterStr: String?
    ): Response {
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        val modifiedAfter = if (modifiedAfterStr != null) OffsetDateTime.parse(modifiedAfterStr) else null

        val visitorSessions = visitorSessionController.listVisitorSessions(
            exhibition = exhibition,
            tagId = tagId,
            modifiedAfter = modifiedAfter
        )

        return createOk(visitorSessions.map (visitorSessionV2Translator::translate))
    }

    override fun updateVisitorSessionV2(
        exhibitionId: UUID?,
        visitorSessionId: UUID?,
        payload: VisitorSessionV2?
    ): Response {
        payload ?: return createBadRequest(MISSING_REQUEST_BODY)
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        visitorSessionId ?: return createNotFound(VISITOR_SESSION_NOT_FOUND)
        val userId = loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
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

        return createOk(visitorSessionV2Translator.translate(result))
    }

    override fun deleteVisitorSessionV2(
        exhibitionId: UUID?,
        visitorSessionId: UUID?
    ): Response {
        exhibitionId ?: return createNotFound(EXHIBITION_NOT_FOUND)
        visitorSessionId ?: return createNotFound(VISITOR_SESSION_NOT_FOUND)
        loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val visitorSession = visitorSessionController.findVisitorSessionById(id = visitorSessionId) ?: return createNotFound("Visitor session $visitorSessionId not found")

        visitorSessionController.deleteVisitorSession(visitorSession)

        realtimeNotificationController.notifyExhibitionVisitorSessionDelete(exhibitionId,  visitorSessionId)

        return createNoContent()
    }

}
