package fi.metatavu.muisti.api.translate

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.vividsolutions.jts.geom.Point
import fi.metatavu.muisti.api.spec.model.Bounds
import fi.metatavu.muisti.api.spec.model.Coordinates
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
        result.floorPlanUrl = entity.floorPlanUrl
        result.floorPlanBounds = getBounds(entity.neBoundPoint, entity.swBoundPoint)
        result.creatorId = entity.creatorId
        result.lastModifierId = entity.lastModifierId
        result.createdAt = entity.createdAt
        result.modifiedAt = entity.modifiedAt
        return result
    }

    /**
     * Convert Geometry Points to api spec Bounds
     *
     * @param neBoundPoint North East geometry point
     * @param swBoundPoint South West geometry point
     * @return bounds
     */
    private fun getBounds(neBoundPoint: Point?, swBoundPoint: Point?): Bounds? {

        neBoundPoint ?: return null
        swBoundPoint ?: return null

        val result = Bounds()
        result.southWestCorner = Coordinates()
        result.southWestCorner.latitude = swBoundPoint.x
        result.southWestCorner.longitude = swBoundPoint.y

        result.northEastCorner = Coordinates()
        result.northEastCorner.latitude = neBoundPoint.x
        result.northEastCorner.longitude = neBoundPoint.y

        return result
    }

}

