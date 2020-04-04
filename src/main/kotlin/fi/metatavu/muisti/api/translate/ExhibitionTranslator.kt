package fi.metatavu.muisti.api.translate

import javax.enterprise.context.ApplicationScoped

/**
 * Translator for translating JPA exhibition entities into REST resources
 */
@ApplicationScoped
class ExhibitionTranslator: AbstractTranslator<fi.metatavu.muisti.persistence.model.Exhibition, fi.metatavu.muisti.api.spec.model.Exhibition>() {

    override fun translate(entity: fi.metatavu.muisti.persistence.model.Exhibition): fi.metatavu.muisti.api.spec.model.Exhibition {
        val result: fi.metatavu.muisti.api.spec.model.Exhibition = fi.metatavu.muisti.api.spec.model.Exhibition()
        result.setId(entity.id)
        result.setName(entity.name)
        result.setCreatorId(entity.creatorId)
        result.setLastModifierId(entity.lastModifierId)
        result.setCreatedAt(entity.createdAt)
        result.setModifiedAt(entity.modifiedAt)
        return result
    }

}

