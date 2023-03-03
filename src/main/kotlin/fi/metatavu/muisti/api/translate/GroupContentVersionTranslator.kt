package fi.metatavu.muisti.api.translate

import fi.metatavu.muisti.api.spec.model.GroupContentVersion
import javax.enterprise.context.ApplicationScoped

/**
 * Translator for translating JPA group content version entities into REST resources
 */
@ApplicationScoped
class GroupContentVersionTranslator :
    AbstractTranslator<fi.metatavu.muisti.persistence.model.GroupContentVersion, GroupContentVersion>() {

    override fun translate(entity: fi.metatavu.muisti.persistence.model.GroupContentVersion): GroupContentVersion {
        return GroupContentVersion(
            id = entity.id,
            exhibitionId = entity.exhibition?.id,
            name = entity.name!!,
            status = entity.status!!,
            contentVersionId = entity.contentVersion!!.id!!,
            deviceGroupId = entity.deviceGroup!!.id!!,
            creatorId = entity.creatorId,
            lastModifierId = entity.lastModifierId,
            createdAt = entity.createdAt,
            modifiedAt = entity.modifiedAt
        )
    }

}

