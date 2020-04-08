package fi.metatavu.muisti.api.translate

import javax.enterprise.context.ApplicationScoped

/**
 * Translator for translating JPA exhibition content version entities into REST resources
 */
@ApplicationScoped
class ExhibitionContentVersionTranslator: AbstractTranslator<fi.metatavu.muisti.persistence.model.ExhibitionContentVersion, fi.metatavu.muisti.api.spec.model.ExhibitionContentVersion>() {

    override fun translate(entity: fi.metatavu.muisti.persistence.model.ExhibitionContentVersion): fi.metatavu.muisti.api.spec.model.ExhibitionContentVersion {
        val result: fi.metatavu.muisti.api.spec.model.ExhibitionContentVersion = fi.metatavu.muisti.api.spec.model.ExhibitionContentVersion()
        result.id = entity.id
        result.exhibitionId = entity.exhibition?.id
        result.name = entity.name
        result.creatorId = entity.creatorId
        result.lastModifierId = entity.lastModifierId
        result.createdAt = entity.createdAt
        result.modifiedAt = entity.modifiedAt
        return result
    }

}

