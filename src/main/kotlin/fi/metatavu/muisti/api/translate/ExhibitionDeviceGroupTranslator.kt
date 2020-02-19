package fi.metatavu.muisti.api.translate

import javax.enterprise.context.ApplicationScoped

/**
 * Translator for translating JPA exhibition device group entities into REST resources
 */
@ApplicationScoped
open class ExhibitionDeviceGroupTranslator: AbstractTranslator<fi.metatavu.muisti.persistence.model.ExhibitionDeviceGroup, fi.metatavu.muisti.api.spec.model.ExhibitionDeviceGroup>() {

    override fun translate(entity: fi.metatavu.muisti.persistence.model.ExhibitionDeviceGroup?): fi.metatavu.muisti.api.spec.model.ExhibitionDeviceGroup? {
        if (entity == null) {
            return null
        }

        val result: fi.metatavu.muisti.api.spec.model.ExhibitionDeviceGroup = fi.metatavu.muisti.api.spec.model.ExhibitionDeviceGroup()
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

