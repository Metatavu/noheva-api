package fi.metatavu.muisti.api;

import fi.metatavu.muisti.api.spec.ExhibitionsApi
import fi.metatavu.muisti.api.spec.model.Exhibition
import java.util.*
import javax.ws.rs.core.Response


class ExhibitionsApiImpl: ExhibitionsApi, AbstractApi() {

    /* V1 */
    override fun listExhibitions(): Response {
        TODO("Not yet implemented")
    }

    override fun createExhibition(sourceExhibitionId: UUID?, exhibition: Exhibition?): Response {
        TODO("Not yet implemented")
    }

    override fun findExhibition(exhibitionId: UUID): Response {
        TODO("Not yet implemented")
    }

    override fun updateExhibition(exhibitionId: UUID, exhibition: Exhibition): Response {
        TODO("Not yet implemented")
    }

    override fun deleteExhibition(exhibitionId: UUID): Response {
        TODO("Not yet implemented")
    }

}
