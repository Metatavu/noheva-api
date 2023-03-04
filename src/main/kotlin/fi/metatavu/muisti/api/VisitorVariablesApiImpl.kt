package fi.metatavu.muisti.api

import fi.metatavu.muisti.api.spec.VisitorVariablesApi
import fi.metatavu.muisti.api.spec.model.VisitorVariable
import fi.metatavu.muisti.api.translate.VisitorVariableTranslator
import fi.metatavu.muisti.exhibitions.ExhibitionController
import fi.metatavu.muisti.visitors.VisitorVariableController
import java.util.*
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.transaction.Transactional
import javax.ws.rs.core.Response

/**
 * Visitor variables api implementation
 */
@RequestScoped
@Transactional
class VisitorVariablesApiImpl: VisitorVariablesApi, AbstractApi() {

    @Inject
    lateinit var exhibitionController: ExhibitionController

    @Inject
    lateinit var visitorVariableController: VisitorVariableController

    @Inject
    lateinit var visitorVariableTranslator: VisitorVariableTranslator

    override fun listVisitorVariables(exhibitionId: UUID, name: String?): Response {
        loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")

        val visitorVariables = visitorVariableController.listVisitorVariables(
            exhibition = exhibition,
            name = name
        )

        return createOk(visitorVariables.map (visitorVariableTranslator::translate))
    }

    override fun createVisitorVariable(exhibitionId: UUID, visitorVariable: VisitorVariable): Response {
        val userId = loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")

        val created = visitorVariableController.createVisitorVariable(
            exhibition = exhibition,
            name = visitorVariable.name,
            type = visitorVariable.type,
            enum = visitorVariable.enum ?: emptyList(),
            editableFromUI = visitorVariable.editableFromUI,
            creatorId = userId
        )

        return createOk(visitorVariableTranslator.translate(created))
    }

    override fun findVisitorVariable(exhibitionId: UUID, visitorVariableId: UUID): Response {
        loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val visitorVariable = visitorVariableController.findVisitorVariableById(id = visitorVariableId) ?: return createNotFound("Visitor variable $visitorVariableId not found")

        return createOk(visitorVariableTranslator.translate(visitorVariable))
    }

    override fun updateVisitorVariable(
        exhibitionId: UUID,
        visitorVariableId: UUID,
        visitorVariable: VisitorVariable
    ): Response {
        val userId = loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val visitorVariableExsiting = visitorVariableController.findVisitorVariableById(id = visitorVariableId) ?: return createNotFound("Visitor variable $visitorVariableId not found")

        val result = visitorVariableController.updateVisitorVariable(
            visitorVariable = visitorVariableExsiting,
            name = visitorVariable.name,
            type = visitorVariable.type,
            enum = visitorVariable.enum,
            editableFromUI = visitorVariable.editableFromUI,
            lastModifierId = userId
        )

        return createOk(visitorVariableTranslator.translate(result))
    }

    override fun deleteVisitorVariable(exhibitionId: UUID, visitorVariableId: UUID): Response {
        loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val visitorVariable = visitorVariableController.findVisitorVariableById(id = visitorVariableId) ?: return createNotFound("Visitor variable $visitorVariableId not found")

        visitorVariableController.deleteVisitorVariable(visitorVariable)

        return createNoContent()
    }
}
