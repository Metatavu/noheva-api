package fi.metatavu.muisti.api

import fi.metatavu.muisti.api.spec.ExhibitionFloorsApi
import fi.metatavu.muisti.api.spec.model.ExhibitionFloor
import fi.metatavu.muisti.api.translate.ExhibitionFloorTranslator
import fi.metatavu.muisti.exhibitions.ExhibitionController
import fi.metatavu.muisti.exhibitions.ExhibitionFloorController
import java.util.*
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.ws.rs.core.Response

@RequestScoped
class ExhibitionFloorsApiImpl : ExhibitionFloorsApi, AbstractApi() {

    @Inject
    lateinit var exhibitionController: ExhibitionController

    @Inject
    lateinit var exhibitionFloorController: ExhibitionFloorController

    @Inject
    lateinit var exhibitionFloorTranslator: ExhibitionFloorTranslator

    /* V1 */
    override fun listExhibitionFloors(exhibitionId: UUID): Response {
        val exhibition = exhibitionController.findExhibitionById(exhibitionId)?: return createNotFound("Exhibition $exhibitionId not found")
        val exhibitionFloors = exhibitionFloorController.listExhibitionFloors(exhibition)

        return createOk(exhibitionFloors.map (exhibitionFloorTranslator::translate))
    }

    override fun createExhibitionFloor(exhibitionId: UUID, exhibitionFloor: ExhibitionFloor): Response {
        val userId = loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")

        val name = exhibitionFloor.name
        val floorPlanUrl = exhibitionFloor.floorPlanUrl
        val floorPlanBounds = exhibitionFloor.floorPlanBounds
        val result = exhibitionFloorController.createExhibitionFloor(exhibition, name, floorPlanUrl, floorPlanBounds, userId)

        return createOk(exhibitionFloorTranslator.translate(result))
    }

    override fun findExhibitionFloor(exhibitionId: UUID, floorId: UUID): Response {
        loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val exhibitionFloor = exhibitionFloorController.findExhibitionFloorById(floorId) ?: return createNotFound("Floor $floorId not found")

        if (!exhibitionFloor.exhibition?.id?.equals(exhibition.id)!!) {
            return createNotFound("Floor not found")
        }

        return createOk(exhibitionFloorTranslator.translate(exhibitionFloor))
    }

    override fun updateExhibitionFloor(exhibitionId: UUID, floorId: UUID, exhibitionFloor: ExhibitionFloor): Response {
        val userId = loggedUserId ?: return createUnauthorized(UNAUTHORIZED)

        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val foundExhibitionFloor = exhibitionFloorController.findExhibitionFloorById(floorId) ?: return createNotFound("Floor $floorId not found")
        val name = exhibitionFloor.name
        val floorPlanUrl = exhibitionFloor.floorPlanUrl
        val floorPlanBounds = exhibitionFloor.floorPlanBounds
        val result = exhibitionFloorController.updateExhibitionFloor(foundExhibitionFloor, name, floorPlanUrl, floorPlanBounds, userId)

        return createOk(exhibitionFloorTranslator.translate(result))
    }

    override fun deleteExhibitionFloor(exhibitionId: UUID, floorId: UUID): Response {
        loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val exhibitionFloor = exhibitionFloorController.findExhibitionFloorById(floorId) ?: return createNotFound("Floor $floorId not found")

        exhibitionFloorController.deleteExhibitionFloor(exhibitionFloor)

        return createNoContent()
    }
}
