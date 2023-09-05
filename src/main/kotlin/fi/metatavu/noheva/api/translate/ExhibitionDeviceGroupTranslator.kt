package fi.metatavu.noheva.api.translate

import javax.enterprise.context.ApplicationScoped

/**
 * Translator for translating JPA exhibition device group entities into REST resources
 */
@ApplicationScoped
class ExhibitionDeviceGroupTranslator :
    AbstractTranslator<fi.metatavu.noheva.persistence.model.ExhibitionDeviceGroup, fi.metatavu.noheva.api.spec.model.ExhibitionDeviceGroup>() {

    override fun translate(entity: fi.metatavu.noheva.persistence.model.ExhibitionDeviceGroup): fi.metatavu.noheva.api.spec.model.ExhibitionDeviceGroup {
        return fi.metatavu.noheva.api.spec.model.ExhibitionDeviceGroup(
            id = entity.id,
            exhibitionId = entity.exhibition?.id,
            roomId = entity.room?.id,
            name = entity.name!!,
            allowVisitorSessionCreation = entity.allowVisitorSessionCreation!!,
            visitorSessionEndTimeout = entity.visitorSessionEndTimeout!!,
            visitorSessionStartStrategy = entity.visitorSessionStartStrategy!!,
            indexPageTimeout = entity.indexPageTimeout,
            creatorId = entity.creatorId,
            lastModifierId = entity.lastModifierId,
            createdAt = entity.createdAt,
            modifiedAt = entity.modifiedAt
        )
    }

}

