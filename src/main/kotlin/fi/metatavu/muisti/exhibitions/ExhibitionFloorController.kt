package fi.metatavu.muisti.exhibitions

import com.fasterxml.jackson.databind.ObjectMapper
import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.CoordinateSequence
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.geom.Point
import fi.metatavu.muisti.api.spec.model.Bounds
import fi.metatavu.muisti.api.spec.model.Coordinates
import fi.metatavu.muisti.persistence.dao.ExhibitionFloorDAO
import fi.metatavu.muisti.persistence.model.Exhibition
import fi.metatavu.muisti.persistence.model.ExhibitionFloor
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * @author Jari Nykänen
 * Controller for exhibition floors
 */
@ApplicationScoped
class ExhibitionFloorController() {

    @Inject
    private lateinit var exhibitionFloorDAO: ExhibitionFloorDAO

    /**
     * Creates new exhibition floor
     *
     * @param exhibition exhibition
     * @param name floor name
     * @param floorPlanUrl floor plan url
     * @param floorPlanBounds floor plan bounds
     * @param creatorId creating user id
     * @return created exhibition floor
     */
    fun createExhibitionFloor(exhibition: Exhibition, name: String, floorPlanUrl: String?, floorPlanBounds: Bounds?, creatorId: UUID): ExhibitionFloor {
      var neBoundPoint: Point? = getDefaultPoints()
      var swBoundPoint: Point? = getDefaultPoints()
      if (floorPlanBounds !== null) {
        neBoundPoint = getGeometryPoint(floorPlanBounds.northEastCorner)
        swBoundPoint = getGeometryPoint(floorPlanBounds.southWestCorner)
      }

      return exhibitionFloorDAO.create(UUID.randomUUID(), exhibition, name, floorPlanUrl, neBoundPoint, swBoundPoint, creatorId, creatorId)
    }

    /**
     * Finds an exhibition floor by id
     *
     * @param id exhibition floor id
     * @return found exhibition floor or null if not found
     */
    fun findExhibitionFloorById(id: UUID): ExhibitionFloor? {
        return exhibitionFloorDAO.findById(id)
    }

    /**
     * Lists floors in an exhibition
     *
     * @param exhibition exhibition
     * @returns all floors in an exhibition
     */
    fun listExhibitionFloors(exhibition: Exhibition): List<ExhibitionFloor> {
        return exhibitionFloorDAO.listByExhibition(exhibition)
    }

    /**
     * Updates an exhibition floor
     *
     * @param exhibitionFloor exhibition floor to be updated
     * @param name floor name
     * @param floorPlanUrl floor plan url
     * @param floorPlanBounds floor plan bounds
     * @param modifierId modifying user id
     * @return updated exhibition
     */
    fun updateExhibitionFloor(exhibitionFloor: ExhibitionFloor, name: String, floorPlanUrl: String?, floorPlanBounds: Bounds?, modifierId: UUID): ExhibitionFloor {
      var result = exhibitionFloorDAO.updateName(exhibitionFloor, name, modifierId)
      result = exhibitionFloorDAO.updateFloorPlanUrl(result, floorPlanUrl, modifierId)

      floorPlanBounds ?: return result

      if (floorPlanBounds.northEastCorner !== null) {
        val neBoundPoint = getGeometryPoint(floorPlanBounds.northEastCorner)
        result = exhibitionFloorDAO.updateFloorNEBound(result, neBoundPoint, modifierId)
      }

      if (floorPlanBounds.southWestCorner !== null) {
        val swBoundPoint = getGeometryPoint(floorPlanBounds.southWestCorner)
        result = exhibitionFloorDAO.updateFloorSWBound(result, swBoundPoint, modifierId)
      }

      return result
    }

    /**
     * Deletes an exhibition floor
     *
     * @param exhibitionFloor exhibition floor to be deleted
     */
    fun deleteExhibitionFloor(exhibitionFloor: ExhibitionFloor) {
        return exhibitionFloorDAO.delete(exhibitionFloor)
    }

    /**
    * Create default bound points
    *
    * @return Geometry Point
    */
    private fun getDefaultPoints(): Point? {
      val geometryFactory = GeometryFactory()
      return geometryFactory.createPoint(Coordinate(0.0, 0.0))
    }

    /**
     * Converts spec Coordinate to Geometry Point
     *
     * @param coordinates coordinates
     * @return null or Geometry Point
     */
    fun getGeometryPoint(coordinates: Coordinates): Point? {
      val geometryFactory = GeometryFactory()
      return geometryFactory.createPoint(Coordinate(coordinates.latitude, coordinates.longitude))
    }

}
