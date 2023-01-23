package fi.metatavu.muisti.api;

import fi.metatavu.muisti.api.spec.VisitorSessionsApi
import fi.metatavu.muisti.api.spec.model.VisitorSession
import fi.metatavu.muisti.api.spec.model.VisitorSessionV2
import java.util.*
import javax.ws.rs.core.Response


class VisitorSessionsApiImpl: VisitorSessionsApi, AbstractApi() {
    /* V1 */
    override fun listVisitorSessions(exhibitionId: UUID, tagId: String?): Response {
        TODO("Not yet implemented")
    }

    override fun createVisitorSession(exhibitionId: UUID, visitorSession: VisitorSession): Response {
        TODO("Not yet implemented")
    }

    override fun findVisitorSession(exhibitionId: UUID, visitorSessionId: UUID): Response {
        TODO("Not yet implemented")
    }

    override fun updateVisitorSession(
        exhibitionId: UUID,
        visitorSessionId: UUID,
        visitorSession: VisitorSession
    ): Response {
        TODO("Not yet implemented")
    }

    override fun deleteVisitorSession(exhibitionId: UUID, visitorSessionId: UUID): Response {
        TODO("Not yet implemented")
    }
    /* V2 */

    override fun listVisitorSessionsV2(exhibitionId: UUID, tagId: String?, modifiedAfter: String?): Response {
        TODO("Not yet implemented")
    }

    override fun createVisitorSessionV2(exhibitionId: UUID, visitorSessionV2: VisitorSessionV2): Response {
        TODO("Not yet implemented")
    }

    override fun findVisitorSessionV2(exhibitionId: UUID, visitorSessionId: UUID): Response {
        TODO("Not yet implemented")
    }

    override fun updateVisitorSessionV2(
        exhibitionId: UUID,
        visitorSessionId: UUID,
        visitorSessionV2: VisitorSessionV2
    ): Response {
        TODO("Not yet implemented")
    }

    override fun deleteVisitorSessionV2(exhibitionId: UUID, visitorSessionId: UUID): Response {
        TODO("Not yet implemented")
    }
}
