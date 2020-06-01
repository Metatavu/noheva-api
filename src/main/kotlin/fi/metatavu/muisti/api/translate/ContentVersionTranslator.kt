package fi.metatavu.muisti.api.translate

import javax.enterprise.context.ApplicationScoped

/**
 * Translator for translating JPA content version entities into REST resources
 */
@ApplicationScoped
class ContentVersionTranslator: AbstractTranslator<fi.metatavu.muisti.persistence.model.ContentVersion, fi.metatavu.muisti.api.spec.model.ContentVersion>() {

    override fun translate(entity: fi.metatavu.muisti.persistence.model.ContentVersion): fi.metatavu.muisti.api.spec.model.ContentVersion {
        val result: fi.metatavu.muisti.api.spec.model.ContentVersion = fi.metatavu.muisti.api.spec.model.ContentVersion()
        result.id = entity.id
        result.exhibitionId = entity.exhibition?.id
        result.name = entity.name
        result.language = entity.language
        result.creatorId = entity.creatorId
        result.lastModifierId = entity.lastModifierId
        result.createdAt = entity.createdAt
        result.modifiedAt = entity.modifiedAt
        return result
    }

}

