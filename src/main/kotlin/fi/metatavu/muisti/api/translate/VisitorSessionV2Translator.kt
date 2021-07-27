package fi.metatavu.muisti.api.translate

import fi.metatavu.muisti.persistence.dao.VisitorSessionVariableDAO
import fi.metatavu.muisti.persistence.dao.VisitorSessionVisitedDeviceGroupDAO
import fi.metatavu.muisti.persistence.dao.VisitorSessionVisitorDAO
import fi.metatavu.muisti.persistence.model.Visitor
import fi.metatavu.muisti.persistence.model.VisitorSessionVisitor
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import kotlin.streams.toList

/**
 * Translator for translating JPA visitor session entities into REST resources
 */
@ApplicationScoped
class VisitorSessionV2Translator: AbstractTranslator<fi.metatavu.muisti.persistence.model.VisitorSession, fi.metatavu.muisti.api.spec.model.VisitorSessionV2>() {

    @Inject
    private lateinit var visitorSessionVariableDAO: VisitorSessionVariableDAO

    @Inject
    private lateinit var visitorSessionVisitorDAO: VisitorSessionVisitorDAO

    @Inject
    private lateinit var visitorSessionVisitedDeviceGroupDAO: VisitorSessionVisitedDeviceGroupDAO

    override fun translate(entity: fi.metatavu.muisti.persistence.model.VisitorSession): fi.metatavu.muisti.api.spec.model.VisitorSessionV2 {
        val variables = visitorSessionVariableDAO.listByVisitorSession(entity).stream()
            .map ( this::translateVariable )
            .toList()

        val visitors = visitorSessionVisitorDAO.listByVisitorSession(entity)
            .mapNotNull(VisitorSessionVisitor::visitor)

        val visitorIds = visitors
            .mapNotNull(Visitor::id)

        val visitorTags = visitors
            .mapNotNull(Visitor::tagId)

        val visitedDeviceGroups = visitorSessionVisitedDeviceGroupDAO.listByVisitorSession(entity)
            .map(this::translateVisitedDeviceGroup)

        val result: fi.metatavu.muisti.api.spec.model.VisitorSessionV2 = fi.metatavu.muisti.api.spec.model.VisitorSessionV2()
        result.id = entity.id
        result.exhibitionId = entity.exhibition?.id
        result.state = entity.state
        result.language = entity.language
        result.visitorIds = visitorIds
        result.tags = visitorTags
        result.visitedDeviceGroups = visitedDeviceGroups
        result.variables = variables
        result.expiresAt = entity.expiresAt
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

