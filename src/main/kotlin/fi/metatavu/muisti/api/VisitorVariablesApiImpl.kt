package fi.metatavu.muisti.api;

import fi.metatavu.muisti.api.spec.VisitorVariablesApi
import fi.metatavu.muisti.api.spec.model.VisitorVariable
import java.util.*
import javax.ws.rs.core.Response


class VisitorVariablesApiImpl: VisitorVariablesApi, AbstractApi() {
    /* V1 */
    override fun listVisitorVariables(exhibitionId: UUID, name: String?): Response {
        TODO("Not yet implemented")
    }

    override fun createVisitorVariable(exhibitionId: UUID, visitorVariable: VisitorVariable): Response {
        TODO("Not yet implemented")
    }

    override fun findVisitorVariable(exhibitionId: UUID, visitorVariableId: UUID): Response {
        TODO("Not yet implemented")
    }

    override fun updateVisitorVariable(
        exhibitionId: UUID,
        visitorVariableId: UUID,
        visitorVariable: VisitorVariable
    ): Response {
        TODO("Not yet implemented")
    }

    override fun deleteVisitorVariable(exhibitionId: UUID, visitorVariableId: UUID): Response {
        TODO("Not yet implemented")
    }
}
