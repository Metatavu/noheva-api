package fi.metatavu.muisti.api.translate

import com.vividsolutions.jts.geom.Point
import fi.metatavu.muisti.api.spec.model.ExhibitionFloor
import fi.metatavu.muisti.geometry.getBounds
import javax.enterprise.context.ApplicationScoped

/**
 * Translator for translating JPA exhibition floor entities into REST resources
 */
@ApplicationScoped
class ExhibitionFloorTranslator :
    AbstractTranslator<fi.metatavu.muisti.persistence.model.ExhibitionFloor, ExhibitionFloor>() {

    override fun translate(entity: fi.metatavu.muisti.persistence.model.ExhibitionFloor): ExhibitionFloor {
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

