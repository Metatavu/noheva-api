package fi.metatavu.noheva.api

import fi.metatavu.noheva.api.spec.ExhibitionRoomsApi
import fi.metatavu.noheva.api.spec.model.ExhibitionRoom
import fi.metatavu.noheva.api.translate.ExhibitionRoomTranslator
import fi.metatavu.noheva.contents.ContentVersionController
import fi.metatavu.noheva.exhibitions.ExhibitionController
import fi.metatavu.noheva.exhibitions.ExhibitionFloorController
import fi.metatavu.noheva.exhibitions.ExhibitionRoomController
import java.util.*
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.transaction.Transactional

import javax.ws.rs.core.Response

/**
 * Exhibition rooms api implementation
 */
@RequestScoped
@Transactional
@Suppress("unused")
class ExhibitionRoomsApiImpl: ExhibitionRoomsApi, AbstractApi() {

    @Inject
    lateinit var contentVersionController: ContentVersionController

    @Inject
    lateinit var exhibitionController: ExhibitionController

    @Inject
    lateinit var exhibitionFloorController: ExhibitionFloorController

    @Inject
    lateinit var exhibitionRoomController: ExhibitionRoomController

    @Inject
    lateinit var exhibitionRoomTranslator: ExhibitionRoomTranslator

    override fun listExhibitionRooms(exhibitionId: UUID, floorId: UUID?): Response {
        val exhibition = exhibitionController.findExhibitionById(exhibitionId)?: return createNotFound("Exhibition $exhibitionId not found")
        var floor: fi.metatavu.noheva.persistence.model.ExhibitionFloor? = null

        if (floorId != null) {
            floor = exhibitionFloorController.findExhibitionFloorById(floorId)
        }

        val exhibitionRooms = exhibitionRoomController.listExhibitionRooms(exhibition, floor)

        return createOk(exhibitionRooms.map (exhibitionRoomTranslator::translate))
    }

    override fun createExhibitionRoom(exhibitionId: UUID, exhibitionRoom: ExhibitionRoom): Response {
        val userId = loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val floor = exhibitionFloorController.findExhibitionFloorById(exhibitionRoom.floorId) ?: return createBadRequest("Exhibition floor ${exhibitionRoom.floorId} not found")
        val result = exhibitionRoomController.createExhibitionRoom(
            exhibition = exhibition,
            name = exhibitionRoom.name,
            color = exhibitionRoom.color,
            geoShape = exhibitionRoom.geoShape,
            floor = floor,
            creatorId = userId
        )

        return createOk(exhibitionRoomTranslator.translate(result))
    }

    override fun findExhibitionRoom(exhibitionId: UUID, roomId: UUID): Response {
        loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val exhibitionRoom = exhibitionRoomController.findExhibitionRoomById(roomId) ?: return createNotFound("Room $roomId not found")

        if (!exhibitionRoom.exhibition?.id?.equals(exhibition.id)!!) {
            return createNotFound("Room not found")
        }

        return createOk(exhibitionRoomTranslator.translate(exhibitionRoom))
    }

    override fun updateExhibitionRoom(exhibitionId: UUID, roomId: UUID, exhibitionRoom: ExhibitionRoom): Response {
        val userId = loggedUserId ?: return createUnauthorized(UNAUTHORIZED)

        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val floor = exhibitionFloorController.findExhibitionFloorById(exhibitionRoom.floorId) ?: return createBadRequest("Exhibition floor ${exhibitionRoom.floorId} not found")
        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val foundExhibitionRoom = exhibitionRoomController.findExhibitionRoomById(roomId) ?: return createNotFound("Room $roomId not found")

        val result = exhibitionRoomController.updateExhibitionRoom(
            exhibitionRoom = foundExhibitionRoom,
            name = exhibitionRoom.name,
            color = exhibitionRoom.color,
            geoShape = exhibitionRoom.geoShape,
            floor = floor,
            modifierId = userId
        )

        return createOk(exhibitionRoomTranslator.translate(result))
    }

    override fun deleteExhibitionRoom(exhibitionId: UUID, roomId: UUID): Response {
        loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")
        val exhibitionRoom = exhibitionRoomController.findExhibitionRoomById(roomId) ?: return createNotFound("Room $roomId not found")
        val exhibition = exhibitionController.findExhibitionById(exhibitionId) ?: return createNotFound("Exhibition $exhibitionId not found")

        val contentVersions = contentVersionController.listContentVersions(exhibition = exhibition, exhibitionRoom = exhibitionRoom, deviceGroup = null)
        if (contentVersions.isNotEmpty()) {
            val contentVersionIds = contentVersions.map { it.id }.joinToString()
            return createBadRequest("Cannot delete room $roomId because it's used in content versions $contentVersionIds")
        }

        exhibitionRoomController.deleteExhibitionRoom(exhibitionRoom)

        return createNoContent()
    }
}
