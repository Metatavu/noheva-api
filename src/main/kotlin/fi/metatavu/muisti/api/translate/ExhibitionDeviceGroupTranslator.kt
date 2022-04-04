package fi.metatavu.muisti.api.translate

import javax.enterprise.context.ApplicationScoped

/**
 * Translator for translating JPA exhibition device group entities into REST resources
 */
@ApplicationScoped
class ExhibitionDeviceGroupTranslator: AbstractTranslator<fi.metatavu.muisti.persistence.model.ExhibitionDeviceGroup, fi.metatavu.muisti.api.spec.model.ExhibitionDeviceGroup>() {

    override fun translate(entity: fi.metatavu.muisti.persistence.model.ExhibitionDeviceGroup): fi.metatavu.muisti.api.spec.model.ExhibitionDeviceGroup {
        val result: fi.metatavu.muisti.api.spec.model.ExhibitionDeviceGroup = fi.metatavu.muisti.api.spec.model.ExhibitionDeviceGroup()
        result.id = entity.id
        result.exhibitionId = entity.exhibition?.id
        result.roomId = entity.room?.id
        result.name = entity.name
        result.allowVisitorSessionCreation = entity.allowVisitorSessionCreation
        result.visitorSessionEndTimeout = entity.visitorSessionEndTimeout
        result.visitorSessionStartStrategy = entity.visitorSessionStartStrategy
        result.indexPageTimeout = entity.indexPageTimeout
        result.creatorId = entity.creatorId
        result.lastModifierId = entity.lastModifierId
        result.createdAt = entity.createdAt
        result.modifiedAt = entity.modifiedAt
        return result
    }

}

