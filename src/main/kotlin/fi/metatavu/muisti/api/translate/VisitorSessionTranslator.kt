package fi.metatavu.muisti.api.translate

import fi.metatavu.muisti.persistence.dao.VisitorSessionVariableDAO
import fi.metatavu.muisti.persistence.dao.VisitorSessionVisitorDAO
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import kotlin.streams.toList

/**
 * Translator for translating JPA visitor session entities into REST resources
 */
@ApplicationScoped
class VisitorSessionTranslator: AbstractTranslator<fi.metatavu.muisti.persistence.model.VisitorSession, fi.metatavu.muisti.api.spec.model.VisitorSession>() {

    @Inject
    private lateinit var visitorSessionVariableDAO: VisitorSessionVariableDAO

    @Inject
    private lateinit var visitorSessionVisitorDAO: VisitorSessionVisitorDAO

    override fun translate(entity: fi.metatavu.muisti.persistence.model.VisitorSession): fi.metatavu.muisti.api.spec.model.VisitorSession {
        val variables = visitorSessionVariableDAO.listByVisitorSession(entity).stream()
            .map ( this::tranlateVariable )
            .toList()

        val visitorIds = visitorSessionVisitorDAO.listByVisitorSession(entity).stream()
            .map { it.visitor?.id!! }
            .toList()

        val result: fi.metatavu.muisti.api.spec.model.VisitorSession = fi.metatavu.muisti.api.spec.model.VisitorSession()
        result.id = entity.id
        result.exhibitionId = entity.exhibition?.id
        result.state = entity.state
        result.visitorIds = visitorIds
        result.variables = variables
        result.creatorId = entity.creatorId
        result.lastModifierId = entity.lastModifierId
        result.createdAt = entity.createdAt
        result.modifiedAt = entity.modifiedAt
        return result
    }

    /**
     * Translates variable into REST format
     *
     * @param entity JPA entity
     * @return REST resource
     */
    private fun tranlateVariable(entity: fi.metatavu.muisti.persistence.model.VisitorSessionVariable): fi.metatavu.muisti.api.spec.model.VisitorSessionVariable {
        val result = fi.metatavu.muisti.api.spec.model.VisitorSessionVariable()
        result.value = entity.value
        result.name = entity.name
        return result
    }

}

