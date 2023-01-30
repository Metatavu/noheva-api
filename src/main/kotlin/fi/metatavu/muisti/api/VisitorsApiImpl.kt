package fi.metatavu.muisti.api

import fi.metatavu.muisti.api.spec.VisitorsApi
import fi.metatavu.muisti.api.spec.model.Visitor
import fi.metatavu.muisti.api.spec.model.VisitorTag
import fi.metatavu.muisti.api.translate.VisitorTranslator
import fi.metatavu.muisti.exhibitions.ExhibitionController
import fi.metatavu.muisti.keycloak.KeycloakController
import fi.metatavu.muisti.realtime.RealtimeNotificationController
import fi.metatavu.muisti.visitors.VisitorController
import java.util.*
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.ws.rs.core.Response

@RequestScoped
class VisitorsApiImpl : VisitorsApi, AbstractApi() {

    @Inject
    lateinit var exhibitionController: ExhibitionController

    @Inject
    lateinit var keycloakController: KeycloakController

    @Inject
    lateinit var visitorController: VisitorController

    @Inject
    lateinit var visitorTranslator: VisitorTranslator

    @Inject
    lateinit var realtimeNotificationController: RealtimeNotificationController

    /* V1 */

    override fun listVisitors(exhibitionId: UUID, tagId: String?, email: String?): Response {
        val exhibition = exhibitionController.findExhibitionById(exhibitionId)
            ?: return createNotFound("Exhibition $exhibitionId not found")
        loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        var userId: UUID? = null

        if (email != null) {
            val userRepresentation =
                keycloakController.findUserByEmail(email = email) ?: return createOk(arrayOf<Visitor>())
            userId = UUID.fromString(userRepresentation.id)
        }

        val visitors = visitorController.listVisitors(
            exhibition = exhibition,
            tagId = tagId,
            userId = userId
        )

        return createOk(visitors.map(visitorTranslator::translate))
    }

    override fun createVisitor(exhibitionId: UUID, visitor: Visitor): Response {
        val userId = loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId)
            ?: return createNotFound("Exhibition $exhibitionId not found")

        var userRepresentation = keycloakController.findUserByEmail(visitor.email)
        if (userRepresentation == null) {
            userRepresentation = keycloakController.createUser(
                email = visitor.email,
                birthYear = visitor.birthYear,
                firstName = visitor.firstName,
                language = visitor.language,
                lastName = visitor.lastName,
                phone = visitor.phone,
                realmRoles = listOf("visitor")
            )
        } else {
            userRepresentation = keycloakController.updateUser(
                userId = UUID.fromString(userRepresentation.id),
                userRepresentation = userRepresentation,
                birthYear = visitor.birthYear,
                firstName = visitor.firstName,
                language = visitor.language,
                lastName = visitor.lastName,
                phone = visitor.phone
            )
        }

        userRepresentation ?: return createInternalServerError("Failed to create visitor user")
        val created = visitorController.createVisitor(
            exhibition = exhibition,
            userRepresentation = userRepresentation,
            tagId = visitor.tagId,
            creatorId = userId
        )

        realtimeNotificationController.notifyVisitorCreate(exhibitionId, created.id!!)

        return createOk(visitorTranslator.translate(created))
    }

    override fun findVisitor(exhibitionId: UUID, visitorId: UUID): Response {
        loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        exhibitionController.findExhibitionById(exhibitionId)
            ?: return createNotFound("Exhibition $exhibitionId not found")
        val visitor = visitorController.findVisitorById(visitorId)
            ?: return createNotFound("Visitor session $visitorId not found")
        return createOk(visitorTranslator.translate(visitor))
    }

    override fun findVisitorTag(exhibitionId: UUID, tagId: String): Response {
        val exhibition = exhibitionController.findExhibitionById(exhibitionId)
            ?: return createNotFound("Exhibition $exhibitionId not found")
        val visitor = visitorController.findVisitorByTagId(
            exhibition = exhibition,
            tagId = tagId
        )

        visitor ?: return createNotFound("Visitor tag not found")

        return createOk(
            VisitorTag(
                tagId = tagId
            )
        )
    }

    override fun updateVisitor(exhibitionId: UUID, visitorId: UUID, visitor: Visitor): Response {
        val userId = loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        exhibitionController.findExhibitionById(exhibitionId)
            ?: return createNotFound("Exhibition $exhibitionId not found")
        val visitorFound =
            visitorController.findVisitorById(visitorId) ?: return createNotFound("Visitor $visitorId not found")

        val userRepresentation = keycloakController.findUserById(visitorFound.userId)
            ?: return createInternalServerError("Failed to find visitor user")

        keycloakController.updateUser(
            userId = UUID.fromString(userRepresentation.id),
            userRepresentation = userRepresentation,
            birthYear = visitor.birthYear,
            firstName = visitor.firstName,
            language = visitor.language,
            lastName = visitor.lastName,
            phone = visitor.phone
        )

        val result = visitorController.updateVisitor(
            visitor = visitorFound,
            tagId = visitor.tagId,
            lastModifierId = userId
        )

        realtimeNotificationController.notifyVisitorUpdate(exhibitionId, visitorId)

        return createOk(visitorTranslator.translate(result))
    }

    override fun deleteVisitor(exhibitionId: UUID, visitorId: UUID): Response {
        loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        exhibitionController.findExhibitionById(exhibitionId)
            ?: return createNotFound("Exhibition $exhibitionId not found")
        val visitor =
            visitorController.findVisitorById(visitorId) ?: return createNotFound("Visitor $visitorId not found")
        visitorController.deleteVisitor(visitor)

        realtimeNotificationController.notifyVisitorDelete(exhibitionId, visitorId)

        return createNoContent()
    }
}
