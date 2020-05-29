package fi.metatavu.muisti.api.translate

import javax.enterprise.context.ApplicationScoped

/**
 * Translator for translating JPA group content version entities into REST resources
 */
@ApplicationScoped
class GroupContentVersionTranslator: AbstractTranslator<fi.metatavu.muisti.persistence.model.GroupContentVersion, fi.metatavu.muisti.api.spec.model.GroupContentVersion>() {

    override fun translate(entity: fi.metatavu.muisti.persistence.model.GroupContentVersion): fi.metatavu.muisti.api.spec.model.GroupContentVersion {
        val result: fi.metatavu.muisti.api.spec.model.GroupContentVersion = fi.metatavu.muisti.api.spec.model.GroupContentVersion()
        result.id = entity.id
        result.exhibitionId = entity.exhibition?.id
        result.name = entity.name
        result.status = entity.status
        result.contentVersionId = entity.contentVersion?.id
        result.deviceGroupId = entity.deviceGroup?.id
        result.creatorId = entity.creatorId
        result.lastModifierId = entity.lastModifierId
        result.createdAt = entity.createdAt
        result.modifiedAt = entity.modifiedAt
        return result
    }

}

