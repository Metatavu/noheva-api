package fi.metatavu.muisti.api.translate

import fi.metatavu.muisti.persistence.dao.VisitorSessionUserDAO
import fi.metatavu.muisti.persistence.dao.VisitorSessionVariableDAO
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import kotlin.streams.toList

/**
 * Translator for translating JPA visitor session entities into REST resources
 */
@ApplicationScoped
open class VisitorSessionTranslator: AbstractTranslator<fi.metatavu.muisti.persistence.model.VisitorSession, fi.metatavu.muisti.api.spec.model.VisitorSession>() {

    @Inject
    private lateinit var visitorSessionVariableDAO: VisitorSessionVariableDAO

    @Inject
    private lateinit var visitorSessionUserDAO: VisitorSessionUserDAO

    override fun translate(entity: fi.metatavu.muisti.persistence.model.VisitorSession?): fi.metatavu.muisti.api.spec.model.VisitorSession? {
        if (entity == null) {
            return null
        }

        val variables = visitorSessionVariableDAO.listByVisitorSession(entity).stream()
            .map ( this::tranlateVariable )
            .toList()

        val users = visitorSessionUserDAO.listByVisitorSession(entity).stream()
            .map ( this::tranlateUser )
            .toList()

        val result: fi.metatavu.muisti.api.spec.model.VisitorSession = fi.metatavu.muisti.api.spec.model.VisitorSession()
        result.id = entity.id
        result.exhibitionId = entity.exhibition?.id
        result.state = entity.state
        result.users = users
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

    /**
     * Translates user into REST format
     *
     * @param entity JPA entity
     * @return REST resource
     */
    private fun tranlateUser(entity: fi.metatavu.muisti.persistence.model.VisitorSessionUser): fi.metatavu.muisti.api.spec.model.VisitorSessionUser {
        val result = fi.metatavu.muisti.api.spec.model.VisitorSessionUser()
        result.userId = entity.userId
        result.tagId = entity.tagId
        return result
    }

}

