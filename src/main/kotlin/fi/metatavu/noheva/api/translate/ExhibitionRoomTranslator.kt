package fi.metatavu.noheva.api.translate

import fi.metatavu.noheva.api.spec.model.ExhibitionRoom
import fi.metatavu.noheva.geometry.getGeoShape
import javax.enterprise.context.ApplicationScoped

/**
 * Translator for translating JPA exhibition room entities into REST resources
 */
@ApplicationScoped
class ExhibitionRoomTranslator :
    AbstractTranslator<fi.metatavu.noheva.persistence.model.ExhibitionRoom, ExhibitionRoom>() {

    override fun translate(entity: fi.metatavu.noheva.persistence.model.ExhibitionRoom): ExhibitionRoom {
        return ExhibitionRoom(
            id = entity.id,
            exhibitionId = entity.exhibition?.id,
            floorId = entity.floor!!.id!!,
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

