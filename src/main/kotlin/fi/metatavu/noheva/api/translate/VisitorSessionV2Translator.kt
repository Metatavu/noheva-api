package fi.metatavu.noheva.api.translate

import fi.metatavu.noheva.api.spec.model.VisitorSessionV2
import fi.metatavu.noheva.api.spec.model.VisitorSessionVariable
import fi.metatavu.noheva.api.spec.model.VisitorSessionVisitedDeviceGroup
import fi.metatavu.noheva.persistence.dao.VisitorSessionVariableDAO
import fi.metatavu.noheva.persistence.dao.VisitorSessionVisitedDeviceGroupDAO
import fi.metatavu.noheva.persistence.dao.VisitorSessionVisitorDAO
import fi.metatavu.noheva.persistence.model.Visitor
import fi.metatavu.noheva.persistence.model.VisitorSessionVisitor
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import kotlin.streams.toList

/**
 * Translator for translating JPA visitor session entities into REST resources
 */
@ApplicationScoped
class VisitorSessionV2Translator: AbstractTranslator<fi.metatavu.noheva.persistence.model.VisitorSession, VisitorSessionV2>() {

    @Inject
    lateinit var visitorSessionVariableDAO: VisitorSessionVariableDAO

    @Inject
    lateinit var visitorSessionVisitorDAO: VisitorSessionVisitorDAO

    @Inject
    lateinit var visitorSessionVisitedDeviceGroupDAO: VisitorSessionVisitedDeviceGroupDAO

    override fun translate(entity: fi.metatavu.noheva.persistence.model.VisitorSession): VisitorSessionV2 {
        val variables = visitorSessionVariableDAO.listByVisitorSession(entity).stream()
            .map(this::translateVariable)
            .toList()

        val visitors = visitorSessionVisitorDAO.listByVisitorSession(entity)
            .mapNotNull(VisitorSessionVisitor::visitor)

        val visitorIds = visitors
            .mapNotNull(Visitor::id)

        val visitorTags = visitors
            .mapNotNull(Visitor::tagId)

        val visitedDeviceGroups = visitorSessionVisitedDeviceGroupDAO.listByVisitorSession(entity)
            .map(this::translateVisitedDeviceGroup)

        return VisitorSessionV2(
            id = entity.id,
            exhibitionId = entity.exhibition?.id,
            state = entity.state!!,
            language = entity.language!!,
            visitorIds = visitorIds,
            tags = visitorTags,
            visitedDeviceGroups = visitedDeviceGroups,
            variables = variables,
            expiresAt = entity.expiresAt,
            creatorId = entity.creatorId,
            lastModifierId = entity.lastModifierId,
            createdAt = entity.createdAt,
            modifiedAt = entity.modifiedAt
        )
    }

    /**
     * Translates variable into REST format
     *
     * @param entity JPA entity
     * @return REST resource
     */
    private fun translateVariable(entity: fi.metatavu.noheva.persistence.model.VisitorSessionVariable): VisitorSessionVariable {
        return VisitorSessionVariable(
            value = entity.value, name = entity.name!!
        )
    }

    /**
     * Translates visited device group into REST format
     *
     * @param entity JPA entity
     * @return REST resource
     */
    private fun translateVisitedDeviceGroup(entity: fi.metatavu.noheva.persistence.model.VisitorSessionVisitedDeviceGroup): VisitorSessionVisitedDeviceGroup {
        return VisitorSessionVisitedDeviceGroup(
            deviceGroupId = entity.deviceGroup!!.id!!, enteredAt = entity.enteredAt, exitedAt = entity.exitedAt
        )
    }

}

