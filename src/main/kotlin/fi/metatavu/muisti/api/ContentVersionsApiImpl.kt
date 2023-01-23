package fi.metatavu.muisti.api;

import fi.metatavu.muisti.api.spec.ContentVersionsApi
import fi.metatavu.muisti.api.spec.model.ContentVersion
import java.util.*
import javax.ws.rs.core.Response


class ContentVersionsApiImpl: ContentVersionsApi, AbstractApi() {

    //V1
    override fun listContentVersions(exhibitionId: UUID, roomId: UUID?): Response {
        TODO("Not yet implemented")
    }

    override fun createContentVersion(exhibitionId: UUID, contentVersion: ContentVersion): Response {
        TODO("Not yet implemented")
    }

    override fun findContentVersion(exhibitionId: UUID, contentVersionId: UUID): Response {
        TODO("Not yet implemented")
    }

    override fun updateContentVersion(
        exhibitionId: UUID,
        contentVersionId: UUID,
        contentVersion: ContentVersion
    ): Response {
        TODO("Not yet implemented")
    }

    override fun deleteContentVersion(exhibitionId: UUID, contentVersionId: UUID): Response {
        TODO("Not yet implemented")
    }
}
