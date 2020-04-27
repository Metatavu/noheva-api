package fi.metatavu.muisti.api.translate

import javax.enterprise.context.ApplicationScoped

/**
 * Translator for translating JPA exhibition floor entities into REST resources
 */
@ApplicationScoped
class ExhibitionFloorTranslator: AbstractTranslator<fi.metatavu.muisti.persistence.model.ExhibitionFloor, fi.metatavu.muisti.api.spec.model.ExhibitionFloor>() {

    override fun translate(entity: fi.metatavu.muisti.persistence.model.ExhibitionFloor): fi.metatavu.muisti.api.spec.model.ExhibitionFloor {
        val result: fi.metatavu.muisti.api.spec.model.ExhibitionFloor = fi.metatavu.muisti.api.spec.model.ExhibitionFloor()
        result.id = entity.id
        result.exhibitionId = entity.exhibition?.id
        result.name = entity.name
        result.floorPlanUrl = entity.name
        result.creatorId = entity.creatorId
        result.lastModifierId = entity.lastModifierId
        result.createdAt = entity.createdAt
        result.modifiedAt = entity.modifiedAt
        return result
    }

}

