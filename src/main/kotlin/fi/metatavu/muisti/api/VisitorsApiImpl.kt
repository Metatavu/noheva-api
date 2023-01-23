package fi.metatavu.muisti.api;

import fi.metatavu.muisti.api.spec.VisitorsApi
import fi.metatavu.muisti.api.spec.model.Visitor
import java.util.*
import javax.ws.rs.core.Response


class VisitorsApiImpl: VisitorsApi, AbstractApi() {
    /* V1 */
    override fun listVisitors(exhibitionId: UUID, tagId: String?, email: String?): Response {
        TODO("Not yet implemented")
    }

    override fun createVisitor(exhibitionId: UUID, visitor: Visitor): Response {
        TODO("Not yet implemented")
    }

    override fun findVisitor(exhibitionId: UUID, visitorId: UUID): Response {
        TODO("Not yet implemented")
    }

    override fun findVisitorTag(exhibitionId: UUID, tagId: String): Response {
        TODO("Not yet implemented")
    }

    override fun updateVisitor(exhibitionId: UUID, visitorId: UUID, visitor: Visitor): Response {
        TODO("Not yet implemented")
    }

    override fun deleteVisitor(exhibitionId: UUID, visitorId: UUID): Response {
        TODO("Not yet implemented")
    }
}
