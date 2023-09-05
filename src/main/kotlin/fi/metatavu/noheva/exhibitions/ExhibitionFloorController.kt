package fi.metatavu.noheva.exhibitions

import fi.metatavu.noheva.api.spec.model.Bounds
import fi.metatavu.noheva.geometry.getGeometryPoint
import fi.metatavu.noheva.persistence.dao.ExhibitionFloorDAO
import fi.metatavu.noheva.persistence.model.Exhibition
import fi.metatavu.noheva.persistence.model.ExhibitionFloor
import fi.metatavu.noheva.utils.CopyException
import fi.metatavu.noheva.utils.IdMapper
import org.locationtech.jts.geom.Point
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
    lateinit var exhibitionFloorDAO: ExhibitionFloorDAO

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
    fun createExhibitionFloor(
        exhibition: Exhibition,
        name: String,
        floorPlanUrl: String?,
        floorPlanBounds: Bounds?,
        creatorId: UUID
    ): ExhibitionFloor {
        var neBoundPoint: Point? = null
        var swBoundPoint: Point? = null
        if (floorPlanBounds !== null) {
            neBoundPoint = getGeometryPoint(floorPlanBounds.northEastCorner)
            swBoundPoint = getGeometryPoint(floorPlanBounds.southWestCorner)
        }

        return exhibitionFloorDAO.create(
            UUID.randomUUID(),
            exhibition,
            name,
            floorPlanUrl,
            neBoundPoint,
            swBoundPoint,
            creatorId,
            creatorId
        )
    }

    /**
     * Copies floor
     *
     * @param idMapper id mapper
     * @param sourceFloor source floor
     * @param targetExhibition target exhibition
     * @param creatorId creator id
     * @return copied floor
     */
    fun copyExhibitionFloor(
        idMapper: IdMapper,
        sourceFloor: ExhibitionFloor,
        targetExhibition: Exhibition,
        creatorId: UUID
    ): ExhibitionFloor {
        val id = idMapper.getNewId(sourceFloor.id) ?: throw CopyException("Target floor id not found")

        return exhibitionFloorDAO.create(
            id = id,
            exhibition = targetExhibition,
            name = sourceFloor.name ?: throw CopyException("Target floor name not found"),
            floorPlanUrl = sourceFloor.floorPlanUrl,
            neBoundPoint = sourceFloor.neBoundPoint,
            swBoundPoint = sourceFloor.swBoundPoint,
            creatorId = creatorId,
            lastModifierId = creatorId
        )
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
    fun updateExhibitionFloor(
        exhibitionFloor: ExhibitionFloor,
        name: String,
        floorPlanUrl: String?,
        floorPlanBounds: Bounds?,
        modifierId: UUID
    ): ExhibitionFloor {
        var result = exhibitionFloorDAO.updateName(exhibitionFloor, name, modifierId)
        result = exhibitionFloorDAO.updateFloorPlanUrl(result, floorPlanUrl, modifierId)

        floorPlanBounds ?: return result

        val neBoundPoint = getGeometryPoint(floorPlanBounds.northEastCorner)
        result = exhibitionFloorDAO.updateFloorNEBound(result, neBoundPoint, modifierId)

        val swBoundPoint = getGeometryPoint(floorPlanBounds.southWestCorner)
        result = exhibitionFloorDAO.updateFloorSWBound(result, swBoundPoint, modifierId)

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

}
