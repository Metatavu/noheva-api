package fi.metatavu.muisti.api;

import fi.metatavu.muisti.api.spec.ExhibitionRoomsApi
import fi.metatavu.muisti.api.spec.model.ExhibitionRoom
import java.util.*

import javax.ws.rs.*
import javax.ws.rs.core.Response


class ExhibitionRoomsApiImpl: ExhibitionRoomsApi, AbstractApi() {

    /* V1 */
    override fun listExhibitionRooms(exhibitionId: UUID, floorId: UUID?): Response {
        TODO("Not yet implemented")
    }

    override fun createExhibitionRoom(exhibitionId: UUID, exhibitionRoom: ExhibitionRoom): Response {
        TODO("Not yet implemented")
    }

    override fun findExhibitionRoom(exhibitionId: UUID, roomId: UUID): Response {
        TODO("Not yet implemented")
    }

    override fun updateExhibitionRoom(exhibitionId: UUID, roomId: UUID, exhibitionRoom: ExhibitionRoom): Response {
        TODO("Not yet implemented")
    }

    override fun deleteExhibitionRoom(exhibitionId: UUID, roomId: UUID): Response {
        TODO("Not yet implemented")
    }
}
