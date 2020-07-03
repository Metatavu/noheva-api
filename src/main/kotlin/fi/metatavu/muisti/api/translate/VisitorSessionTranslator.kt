package fi.metatavu.muisti.api.translate

import fi.metatavu.muisti.persistence.dao.VisitorSessionVariableDAO
import fi.metatavu.muisti.persistence.dao.VisitorSessionVisitedDeviceGroupDAO
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

    @Inject
    private lateinit var visitorSessionVisitedDeviceGroupDAO: VisitorSessionVisitedDeviceGroupDAO

    override fun translate(entity: fi.metatavu.muisti.persistence.model.VisitorSession): fi.metatavu.muisti.api.spec.model.VisitorSession {
        val variables = visitorSessionVariableDAO.listByVisitorSession(entity).stream()
            .map ( this::translateVariable )
            .toList()

        val visitorIds = visitorSessionVisitorDAO.listByVisitorSession(entity).stream()
            .map { it.visitor?.id!! }
            .toList()

        val visitedDeviceGroups = visitorSessionVisitedDeviceGroupDAO.listByVisitorSession(entity).stream()
            .map( this::translateVisitedDeviceGroup )
            .toList()

        val result: fi.metatavu.muisti.api.spec.model.VisitorSession = fi.metatavu.muisti.api.spec.model.VisitorSession()
        result.id = entity.id
        result.exhibitionId = entity.exhibition?.id
        result.state = entity.state
        result.visitorIds = visitorIds
        result.visitedDeviceGroups = visitedDeviceGroups
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
    private fun translateVariable(entity: fi.metatavu.muisti.persistence.model.VisitorSessionVariable): fi.metatavu.muisti.api.spec.model.VisitorSessionVariable {
        val result = fi.metatavu.muisti.api.spec.model.VisitorSessionVariable()
        result.value = entity.value
        result.name = entity.name
        return result
    }

    /**
     * Translates visited device group into REST format
     *
     * @param entity JPA entity
     * @return REST resource
     */
    private fun translateVisitedDeviceGroup(entity: fi.metatavu.muisti.persistence.model.VisitorSessionVisitedDeviceGroup): fi.metatavu.muisti.api.spec.model.VisitorSessionVisitedDeviceGroup {
        val result = fi.metatavu.muisti.api.spec.model.VisitorSessionVisitedDeviceGroup()
        result.deviceGroupId = entity.deviceGroup?.id
        result.enteredAt = entity.enteredAt
        result.exitedAt = entity.exitedAt
        return result
    }

}

