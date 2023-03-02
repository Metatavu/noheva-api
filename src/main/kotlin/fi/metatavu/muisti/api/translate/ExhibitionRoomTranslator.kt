package fi.metatavu.muisti.api.translate

import fi.metatavu.muisti.api.spec.model.ExhibitionRoom
import fi.metatavu.muisti.geometry.getGeoShape
import javax.enterprise.context.ApplicationScoped

/**
 * Translator for translating JPA exhibition room entities into REST resources
 */
@ApplicationScoped
class ExhibitionRoomTranslator :
    AbstractTranslator<fi.metatavu.muisti.persistence.model.ExhibitionRoom, ExhibitionRoom>() {

    override fun translate(entity: fi.metatavu.muisti.persistence.model.ExhibitionRoom): ExhibitionRoom {
        return ExhibitionRoom(
            id = entity.id,
            exhibitionId = entity.exhibition?.id,
            floorId = entity.floor.id,
            name = entity.name!!,
            color = entity.color,
            geoShape = getGeoShape(entity.geoShape),
            creatorId = entity.creatorId,
            lastModifierId = entity.lastModifierId,
            createdAt = entity.createdAt,
            modifiedAt = entity.modifiedAt
        )
    }
}

