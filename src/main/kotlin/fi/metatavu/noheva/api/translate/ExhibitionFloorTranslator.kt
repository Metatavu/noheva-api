package fi.metatavu.noheva.api.translate

import fi.metatavu.noheva.api.spec.model.ExhibitionFloor
import fi.metatavu.noheva.geometry.getBounds
import javax.enterprise.context.ApplicationScoped

/**
 * Translator for translating JPA exhibition floor entities into REST resources
 */
@ApplicationScoped
class ExhibitionFloorTranslator :
    AbstractTranslator<fi.metatavu.noheva.persistence.model.ExhibitionFloor, ExhibitionFloor>() {

    override fun translate(entity: fi.metatavu.noheva.persistence.model.ExhibitionFloor): ExhibitionFloor {
        return ExhibitionFloor(
            id = entity.id,
            exhibitionId = entity.exhibition?.id,
            name = entity.name!!,
            floorPlanUrl = entity.floorPlanUrl,
            floorPlanBounds = getBounds(entity.neBoundPoint, entity.swBoundPoint),
            creatorId = entity.creatorId,
            lastModifierId = entity.lastModifierId,
            createdAt = entity.createdAt,
            modifiedAt = entity.modifiedAt
        )
    }

}

