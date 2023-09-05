package fi.metatavu.noheva.api

import fi.metatavu.noheva.api.spec.VisitorSessionsApi
import fi.metatavu.noheva.api.spec.model.VisitorSession
import fi.metatavu.noheva.api.spec.model.VisitorSessionV2
import fi.metatavu.noheva.api.translate.VisitorSessionTranslator
import fi.metatavu.noheva.api.translate.VisitorSessionV2Translator
import fi.metatavu.noheva.devices.ExhibitionDeviceGroupController
import fi.metatavu.noheva.exhibitions.ExhibitionController
import fi.metatavu.noheva.realtime.RealtimeNotificationController
import fi.metatavu.noheva.visitors.VisitorController
import fi.metatavu.noheva.visitors.VisitorSessionController
import java.time.OffsetDateTime
import java.util.*
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.transaction.Transactional
import javax.ws.rs.core.Response

/**
 * Visitor sessions api implementation
 */
@RequestScoped
@Transactional
class VisitorSessionsApiImpl : VisitorSessionsApi, AbstractApi() {

    @Inject
    lateinit var exhibitionController: ExhibitionController

    @Inject
    lateinit var visitorSessionController: VisitorSessionController

    @Inject
    lateinit var visitorSessionTranslator: VisitorSessionTranslator

    @Inject
    lateinit var visitorController: VisitorController

    @Inject
    lateinit var exhibitionDeviceGroupController: ExhibitionDeviceGroupController

    @Inject
    lateinit var realtimeNotificationController: RealtimeNotificationController

    @Inject
    lateinit var visitorSessionV2Translator: VisitorSessionV2Translator

    /* Version 1 endpoints */

    override fun listVisitorSessions(exhibitionId: UUID, tagId: String?): Response {
        loggedUserId ?: return createUnauthorized(UNAUTHORIZED)

        val exhibition = exhibitionController.findExhibitionById(exhibitionId)
            ?: return createNotFound("Exhibition $exhibitionId not found")
        val visitorSessions = visitorSessionController.listVisitorSessions(
            exhibition = exhibition,
            tagId = tagId,
            modifiedAfter = null
        )

        return createOk(visitorSessions.map(visitorSessionTranslator::translate))
    }

    override fun createVisitorSession(exhibitionId: UUID, visitorSession: VisitorSession): Response {
        val userId = loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId)
            ?: return createNotFound("Exhibition $exhibitionId not found")

        visitorSession.variables?.forEach { variable ->
            if (!visitorSessionController.isValidVisitorSessionVariable(
                    exhibition = exhibition,
                    visitorSessionVariable = variable
                )
            ) {
                return createBadRequest("Variable ${variable.name} is not valid")
            }
        }

        val visitors = mutableListOf<fi.metatavu.noheva.persistence.model.Visitor>()
        for (visitorId in visitorSession.visitorIds) {
            val visitor = visitorController.findVisitorById(visitorId)
            visitor ?: return createBadRequest("Invalid visitor $visitorId")
            visitors.add(visitor)
        }

        val visitedDeviceGroupList: MutableList<fi.metatavu.noheva.persistence.model.ExhibitionDeviceGroup> =
            mutableListOf()
        visitorSession.visitedDeviceGroups?.forEach { visitedDeviceGroup ->
            val deviceGroup = exhibitionDeviceGroupController.findDeviceGroupById(visitedDeviceGroup.deviceGroupId)
            deviceGroup ?: return createBadRequest("Invalid visitor ${visitedDeviceGroup.deviceGroupId}")
            visitedDeviceGroupList.add(deviceGroup)
        }

        val created = visitorSessionController.createVisitorSession(
            exhibition = exhibition,
            state = visitorSession.state,
            language = visitorSession.language,
            creatorId = userId
        )

        visitorSessionController.setVisitorSessionVisitors(created, visitors)
        visitorSessionController.setVisitorSessionVariables(created, visitorSession.variables ?: emptyList())
        visitorSessionController.setVisitorSessionVisitedDeviceGroups(
            created,
            visitorSession.visitedDeviceGroups ?: emptyList(),
            visitedDeviceGroupList
        )
        realtimeNotificationController.notifyExhibitionVisitorSessionCreate(exhibitionId, created.id!!)

        return createOk(visitorSessionTranslator.translate(created))
    }

    override fun findVisitorSession(exhibitionId: UUID, visitorSessionId: UUID): Response {
        loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        exhibitionController.findExhibitionById(exhibitionId)
            ?: return createNotFound("Exhibition $exhibitionId not found")
        val visitorSession = visitorSessionController.findVisitorSessionById(id = visitorSessionId)
            ?: return createNotFound("Visitor session $visitorSessionId not found")

        return createOk(visitorSessionTranslator.translate(visitorSession))
    }

    override fun updateVisitorSession(
        exhibitionId: UUID,
        visitorSessionId: UUID,
        visitorSession: VisitorSession
    ): Response {
        val userId = loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId)
            ?: return createNotFound("Exhibition $exhibitionId not found")

        visitorSession.variables?.forEach { variable ->
            if (!visitorSessionController.isValidVisitorSessionVariable(
                    exhibition = exhibition,
                    visitorSessionVariable = variable
                )
            ) {
                return createBadRequest("Variable ${variable.name} is not valid")
            }
        }

        val visitorSessionExisting = visitorSessionController.findVisitorSessionById(id = visitorSessionId)
            ?: return createNotFound("Visitor session $visitorSessionId not found")

        val visitors = mutableListOf<fi.metatavu.noheva.persistence.model.Visitor>()
        for (visitorId in visitorSession.visitorIds) {
            val visitor = visitorController.findVisitorById(visitorId)
            visitor ?: return createBadRequest("Invalid visitor $visitorId")
            visitors.add(visitor)
        }

        val visitedDeviceGroupList: MutableList<fi.metatavu.noheva.persistence.model.ExhibitionDeviceGroup> =
            mutableListOf()
        visitorSession.visitedDeviceGroups?.forEach { visitedDeviceGroup ->
            val deviceGroup = exhibitionDeviceGroupController.findDeviceGroupById(visitedDeviceGroup.deviceGroupId)
            deviceGroup ?: return createBadRequest("Invalid visitor ${visitedDeviceGroup.deviceGroupId}")
            visitedDeviceGroupList.add(deviceGroup)
        }

        val result = visitorSessionController.updateVisitorSession(
            visitorSession = visitorSessionExisting,
            state = visitorSession.state,
            language = visitorSession.language,
            lastModfierId = userId
        )

        val usersChanged = visitorSessionController.setVisitorSessionVisitors(visitorSessionExisting, visitors)
        val variablesChanged =
            visitorSessionController.setVisitorSessionVariables(result, visitorSession.variables ?: emptyList())
        visitorSessionController.setVisitorSessionVisitedDeviceGroups(
            visitorSessionExisting,
            visitorSession.visitedDeviceGroups ?: emptyList(),
            visitedDeviceGroupList
        )

        realtimeNotificationController.notifyExhibitionVisitorSessionUpdate(
            exhibitionId,
            visitorSessionId,
            variablesChanged,
            usersChanged
        )

        return createOk(visitorSessionTranslator.translate(result))
    }

    override fun deleteVisitorSession(exhibitionId: UUID, visitorSessionId: UUID): Response {
        loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        exhibitionController.findExhibitionById(exhibitionId)
            ?: return createNotFound("Exhibition $exhibitionId not found")
        val visitorSession = visitorSessionController.findVisitorSessionById(id = visitorSessionId)
            ?: return createNotFound("Visitor session $visitorSessionId not found")

        visitorSessionController.deleteVisitorSession(visitorSession)

        realtimeNotificationController.notifyExhibitionVisitorSessionDelete(exhibitionId, visitorSessionId)

        return createNoContent()
    }

    /* Version 2 endpoints */

    override fun listVisitorSessionsV2(exhibitionId: UUID, tagId: String?, modifiedAfter: String?): Response {
        loggedUserId ?: return createUnauthorized(UNAUTHORIZED)

        val exhibition = exhibitionController.findExhibitionById(exhibitionId)
            ?: return createNotFound("Exhibition $exhibitionId not found")
        val visitorSessions = visitorSessionController.listVisitorSessions(
            exhibition = exhibition,
            tagId = tagId,
            modifiedAfter = if (modifiedAfter != null) OffsetDateTime.parse(modifiedAfter) else null
        )

        return createOk(visitorSessions.map(visitorSessionV2Translator::translate))
    }

    override fun createVisitorSessionV2(exhibitionId: UUID, visitorSessionV2: VisitorSessionV2): Response {
        val userId = loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId)
            ?: return createNotFound("Exhibition $exhibitionId not found")

        visitorSessionV2.variables?.forEach { variable ->
            if (!visitorSessionController.isValidVisitorSessionVariable(
                    exhibition = exhibition,
                    visitorSessionVariable = variable
                )
            ) {
                return createBadRequest("Variable ${variable.name} is not valid")
            }
        }

        val visitors = mutableListOf<fi.metatavu.noheva.persistence.model.Visitor>()
        for (visitorId in visitorSessionV2.visitorIds) {
            val visitor = visitorController.findVisitorById(visitorId)
            visitor ?: return createBadRequest("Invalid visitor $visitorId")
            visitors.add(visitor)
        }

        val visitedDeviceGroupList: MutableList<fi.metatavu.noheva.persistence.model.ExhibitionDeviceGroup> =
            mutableListOf()
        visitorSessionV2.visitedDeviceGroups?.forEach { visitedDeviceGroup ->
            val deviceGroup = exhibitionDeviceGroupController.findDeviceGroupById(visitedDeviceGroup.deviceGroupId)
            deviceGroup ?: return createBadRequest("Invalid visitor ${visitedDeviceGroup.deviceGroupId}")
            visitedDeviceGroupList.add(deviceGroup)
        }

        val created = visitorSessionController.createVisitorSession(
            exhibition = exhibition,
            state = visitorSessionV2.state,
            language = visitorSessionV2.language,
            creatorId = userId
        )

        visitorSessionController.setVisitorSessionVisitors(created, visitors)
        visitorSessionController.setVisitorSessionVariables(created, visitorSessionV2.variables ?: emptyList())
        visitorSessionController.setVisitorSessionVisitedDeviceGroups(
            created,
            visitorSessionV2.visitedDeviceGroups ?: emptyList(),
            visitedDeviceGroupList
        )
        realtimeNotificationController.notifyExhibitionVisitorSessionCreate(exhibitionId, created.id!!)

        return createOk(visitorSessionV2Translator.translate(created))
    }

    override fun findVisitorSessionV2(exhibitionId: UUID, visitorSessionId: UUID): Response {
        loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        exhibitionController.findExhibitionById(exhibitionId)
            ?: return createNotFound("Exhibition $exhibitionId not found")
        val visitorSession = visitorSessionController.findVisitorSessionById(id = visitorSessionId)
            ?: return createNotFound("Visitor session $visitorSessionId not found")

        return createOk(visitorSessionV2Translator.translate(visitorSession))
    }

    override fun updateVisitorSessionV2(
        exhibitionId: UUID,
        visitorSessionId: UUID,
        visitorSessionV2: VisitorSessionV2
    ): Response {
        val userId = loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId)
            ?: return createNotFound("Exhibition $exhibitionId not found")

        visitorSessionV2.variables?.forEach { variable ->
            if (!visitorSessionController.isValidVisitorSessionVariable(
                    exhibition = exhibition,
                    visitorSessionVariable = variable
                )
            ) {
                return createBadRequest("Variable ${variable.name} is not valid")
            }
        }

        val visitorSessionExisting = visitorSessionController.findVisitorSessionById(id = visitorSessionId)
            ?: return createNotFound("Visitor session $visitorSessionId not found")

        val visitors = mutableListOf<fi.metatavu.noheva.persistence.model.Visitor>()
        for (visitorId in visitorSessionV2.visitorIds) {
            val visitor = visitorController.findVisitorById(visitorId)
            visitor ?: return createBadRequest("Invalid visitor $visitorId")
            visitors.add(visitor)
        }

        val visitedDeviceGroupList: MutableList<fi.metatavu.noheva.persistence.model.ExhibitionDeviceGroup> =
            mutableListOf()
        visitorSessionV2.visitedDeviceGroups?.forEach { visitedDeviceGroup ->
            val deviceGroup = exhibitionDeviceGroupController.findDeviceGroupById(visitedDeviceGroup.deviceGroupId)
            deviceGroup ?: return createBadRequest("Invalid visitor ${visitedDeviceGroup.deviceGroupId}")
            visitedDeviceGroupList.add(deviceGroup)
        }

        val result = visitorSessionController.updateVisitorSession(
            visitorSession = visitorSessionExisting,
            state = visitorSessionV2.state,
            language = visitorSessionV2.language,
            lastModfierId = userId
        )

        val usersChanged = visitorSessionController.setVisitorSessionVisitors(visitorSessionExisting, visitors)
        val variablesChanged =
            visitorSessionController.setVisitorSessionVariables(result, visitorSessionV2.variables ?: emptyList())
        visitorSessionController.setVisitorSessionVisitedDeviceGroups(
            visitorSessionExisting,
            visitorSessionV2.visitedDeviceGroups ?: emptyList(),
            visitedDeviceGroupList
        )

        realtimeNotificationController.notifyExhibitionVisitorSessionUpdate(
            exhibitionId,
            visitorSessionId,
            variablesChanged,
            usersChanged
        )

        return createOk(visitorSessionV2Translator.translate(result))
    }

    override fun deleteVisitorSessionV2(exhibitionId: UUID, visitorSessionId: UUID): Response {
        loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        exhibitionController.findExhibitionById(exhibitionId)
            ?: return createNotFound("Exhibition $exhibitionId not found")
        val visitorSession = visitorSessionController.findVisitorSessionById(id = visitorSessionId)
            ?: return createNotFound("Visitor session $visitorSessionId not found")

        visitorSessionController.deleteVisitorSession(visitorSession)

        realtimeNotificationController.notifyExhibitionVisitorSessionDelete(exhibitionId, visitorSessionId)

        return createNoContent()
    }
}
