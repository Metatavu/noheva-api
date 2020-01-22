package fi.metatavu.muisti.api.translate

import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
open class ExhibitionTranslator: AbstractTranslator<fi.metatavu.muisti.persistence.model.Exhibition, fi.metatavu.muisti.api.spec.model.Exhibition>() {

    override fun translate(entity: fi.metatavu.muisti.persistence.model.Exhibition?): fi.metatavu.muisti.api.spec.model.Exhibition? {
        if (entity == null) {
            return null
        }

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

