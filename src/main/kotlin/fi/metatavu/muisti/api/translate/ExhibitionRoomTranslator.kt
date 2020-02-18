package fi.metatavu.muisti.api.translate

import javax.enterprise.context.ApplicationScoped

/**
 * Translator for translating JPA exhibition room entities into REST resources
 */
@ApplicationScoped
open class ExhibitionRoomTranslator: AbstractTranslator<fi.metatavu.muisti.persistence.model.ExhibitionRoom, fi.metatavu.muisti.api.spec.model.ExhibitionRoom>() {

    override fun translate(entity: fi.metatavu.muisti.persistence.model.ExhibitionRoom?): fi.metatavu.muisti.api.spec.model.ExhibitionRoom? {
        if (entity == null) {
            return null
        }

        val result: fi.metatavu.muisti.api.spec.model.ExhibitionRoom = fi.metatavu.muisti.api.spec.model.ExhibitionRoom()
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

