package fi.metatavu.muisti.api

import fi.metatavu.muisti.api.spec.ExhibitionsApi
import fi.metatavu.muisti.api.spec.model.Exhibition
import fi.metatavu.muisti.api.spec.model.VisitorSession
import fi.metatavu.muisti.api.translate.ExhibitionTranslator
import fi.metatavu.muisti.api.translate.VisitorSessionTranslator
import fi.metatavu.muisti.exhibitions.ExhibitionController
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

}