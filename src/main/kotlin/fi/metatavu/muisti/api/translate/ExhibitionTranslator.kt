package fi.metatavu.muisti.api.translate

import fi.metatavu.muisti.api.spec.model.Exhibition
import javax.enterprise.context.ApplicationScoped

/**
 * Translator for translating JPA exhibition entities into REST resources
 */
@ApplicationScoped
class ExhibitionTranslator :
    AbstractTranslator<fi.metatavu.muisti.persistence.model.Exhibition, Exhibition>() {

    override fun translate(entity: fi.metatavu.muisti.persistence.model.Exhibition): Exhibition {
        return Exhibition(
            id = entity.id,
            name = entity.name!!,
            creatorId = entity.creatorId,
            lastModifierId = entity.lastModifierId,
            createdAt = entity.createdAt,
            modifiedAt = entity.modifiedAt
        )
    }

}

