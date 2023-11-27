package fi.metatavu.noheva.api.translate

import fi.metatavu.noheva.api.spec.model.Exhibition
import javax.enterprise.context.ApplicationScoped

/**
 * Translator for translating JPA exhibition entities into REST resources
 */
@ApplicationScoped
class ExhibitionTranslator :
    AbstractTranslator<fi.metatavu.noheva.persistence.model.Exhibition, Exhibition>() {

    override fun translate(entity: fi.metatavu.noheva.persistence.model.Exhibition): Exhibition {
        return Exhibition(
            id = entity.id,
            name = entity.name!!,
            active = entity.active,
            creatorId = entity.creatorId,
            lastModifierId = entity.lastModifierId,
            createdAt = entity.createdAt,
            modifiedAt = entity.modifiedAt
        )
    }

}

