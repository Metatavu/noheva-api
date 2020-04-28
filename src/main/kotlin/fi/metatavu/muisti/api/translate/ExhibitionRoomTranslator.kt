package fi.metatavu.muisti.api.translate

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import fi.metatavu.muisti.api.spec.model.Polygon
import javax.enterprise.context.ApplicationScoped

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

    /**
     * Read geoShape value from entity
     *
     * @param geoShape geoShape string
     * @return null or GeoJSON with polygon data
     */
    private fun getGeoShape(geoShape: String?): Polygon? {
        geoShape ?: return null
        val objectMapper = ObjectMapper()
        return objectMapper.readValue(geoShape)
    }

}

