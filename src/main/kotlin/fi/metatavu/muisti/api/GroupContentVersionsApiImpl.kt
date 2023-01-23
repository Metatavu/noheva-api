package fi.metatavu.muisti.api;

import fi.metatavu.muisti.api.spec.GroupContentVersionsApi
import fi.metatavu.muisti.api.spec.model.GroupContentVersion
import java.util.*

import javax.ws.rs.*
import javax.ws.rs.core.Response


class GroupContentVersionsApiImpl: GroupContentVersionsApi, AbstractApi() {

    /* V1 */
    override fun listGroupContentVersions(exhibitionId: UUID, contentVersionId: UUID?, deviceGroupId: UUID?): Response {
        TODO("Not yet implemented")
    }

    override fun createGroupContentVersion(exhibitionId: UUID, groupContentVersion: GroupContentVersion): Response {
        TODO("Not yet implemented")
    }

    override fun findGroupContentVersion(exhibitionId: UUID, groupContentVersionId: UUID): Response {
        TODO("Not yet implemented")
    }

    override fun updateGroupContentVersion(
        exhibitionId: UUID,
        groupContentVersionId: UUID,
        groupContentVersion: GroupContentVersion
    ): Response {
        TODO("Not yet implemented")
    }

    override fun deleteGroupContentVersion(exhibitionId: UUID, groupContentVersionId: UUID): Response {
        TODO("Not yet implemented")
    }
}
