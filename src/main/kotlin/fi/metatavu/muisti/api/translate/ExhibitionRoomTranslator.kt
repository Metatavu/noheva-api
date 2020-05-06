package fi.metatavu.muisti.api.translate

import com.fasterxml.jackson.databind.ObjectMapper
import fi.metatavu.muisti.api.spec.model.Polygon
import javax.enterprise.context.ApplicationScoped
import com.vividsolutions.jts.geom.Coordinate
import fi.metatavu.muisti.geometry.getGeoShape

/**
 * Translator for translating JPA exhibition room entities into REST resources
 */
@ApplicationScoped
class ExhibitionRoomTranslator: AbstractTranslator<fi.metatavu.muisti.persistence.model.ExhibitionRoom, fi.metatavu.muisti.api.spec.model.ExhibitionRoom>() {

    override fun translate(entity: fi.metatavu.muisti.persistence.model.ExhibitionRoom): fi.metatavu.muisti.api.spec.model.ExhibitionRoom {
        val result: fi.metatavu.muisti.api.spec.model.ExhibitionRoom = fi.metatavu.muisti.api.spec.model.ExhibitionRoom()
        result.id = entity.id
        result.exhibitionId = entity.exhibition?.id
        result.floorId = entity.floor?.id
        result.name = entity.name
        result.geoShape = getGeoShape(entity.geoShape)
        result.creatorId = entity.creatorId
        result.lastModifierId = entity.lastModifierId
        result.createdAt = entity.createdAt
        result.modifiedAt = entity.modifiedAt
        return result
    }
}

