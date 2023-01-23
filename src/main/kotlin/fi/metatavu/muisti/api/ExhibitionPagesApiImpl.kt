package fi.metatavu.muisti.api;

import fi.metatavu.muisti.api.spec.ExhibitionPagesApi
import fi.metatavu.muisti.api.spec.model.ExhibitionPage
import java.util.*

import javax.ws.rs.*
import javax.ws.rs.core.Response


class ExhibitionPagesApiImpl: ExhibitionPagesApi, AbstractApi() {

    /* V1 */
    override fun listExhibitionPages(
        exhibitionId: UUID,
        contentVersionId: UUID?,
        exhibitionDeviceId: UUID?,
        pageLayoutId: UUID?
    ): Response {
        TODO("Not yet implemented")
    }

    override fun createExhibitionPage(exhibitionId: UUID, exhibitionPage: ExhibitionPage): Response {
        TODO("Not yet implemented")
    }

    override fun findExhibitionPage(exhibitionId: UUID, pageId: UUID): Response {
        TODO("Not yet implemented")
    }

    override fun updateExhibitionPage(exhibitionId: UUID, pageId: UUID, exhibitionPage: ExhibitionPage): Response {
        TODO("Not yet implemented")
    }

    override fun deleteExhibitionPage(exhibitionId: UUID, pageId: UUID): Response {
        TODO("Not yet implemented")
    }
}