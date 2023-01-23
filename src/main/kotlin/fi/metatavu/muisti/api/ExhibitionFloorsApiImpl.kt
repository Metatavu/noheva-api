package fi.metatavu.muisti.api;

import fi.metatavu.muisti.api.spec.ExhibitionFloorsApi
import fi.metatavu.muisti.api.spec.model.ExhibitionFloor
import java.util.*

import javax.ws.rs.*
import javax.ws.rs.core.Response


class ExhibitionFloorsApiImpl: ExhibitionFloorsApi, AbstractApi() {

    /* V1 */
    override fun listExhibitionFloors(exhibitionId: UUID): Response {
        TODO("Not yet implemented")
    }

    override fun createExhibitionFloor(exhibitionId: UUID, exhibitionFloor: ExhibitionFloor): Response {
        TODO("Not yet implemented")
    }

    override fun findExhibitionFloor(exhibitionId: UUID, floorId: UUID): Response {
        TODO("Not yet implemented")
    }

    override fun updateExhibitionFloor(exhibitionId: UUID, floorId: UUID, exhibitionFloor: ExhibitionFloor): Response {
        TODO("Not yet implemented")
    }

    override fun deleteExhibitionFloor(exhibitionId: UUID, floorId: UUID): Response {
        TODO("Not yet implemented")
    }
}
