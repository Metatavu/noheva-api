package fi.metatavu.muisti.api.translate

import javax.enterprise.context.ApplicationScoped

/**
 * Translator for translating JPA exhibition device entities into REST resources
 */
@ApplicationScoped
class ExhibitionDeviceTranslator: AbstractTranslator<fi.metatavu.muisti.persistence.model.ExhibitionDevice, fi.metatavu.muisti.api.spec.model.ExhibitionDevice>() {

    override fun translate(entity: fi.metatavu.muisti.persistence.model.ExhibitionDevice?): fi.metatavu.muisti.api.spec.model.ExhibitionDevice? {
        if (entity == null) {
            return null
        }

        val result: fi.metatavu.muisti.api.spec.model.ExhibitionDevice = fi.metatavu.muisti.api.spec.model.ExhibitionDevice()
        result.id = entity.id
        result.exhibitionId = entity.exhibition?.id
        result.name = entity.name
        result.groupId = entity.exhibitionDeviceGroup?.id
        result.creatorId = entity.creatorId
        result.lastModifierId = entity.lastModifierId
        result.createdAt = entity.createdAt
        result.modifiedAt = entity.modifiedAt
        return result
    }

}

