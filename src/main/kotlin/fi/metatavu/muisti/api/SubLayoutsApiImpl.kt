package fi.metatavu.muisti.api;

import fi.metatavu.muisti.api.spec.SubLayoutsApi
import fi.metatavu.muisti.api.spec.model.SubLayout
import java.util.*
import javax.ws.rs.core.Response


class SubLayoutsApiImpl: SubLayoutsApi, AbstractApi() {

    /* V1 */

    override fun listSubLayouts(): Response {
        TODO("Not yet implemented")
    }

    override fun createSubLayout(subLayout: SubLayout): Response {
        TODO("Not yet implemented")
    }

    override fun findSubLayout(subLayoutId: UUID): Response {
        TODO("Not yet implemented")
    }

    override fun updateSubLayout(subLayoutId: UUID, subLayout: SubLayout): Response {
        TODO("Not yet implemented")
    }

    override fun deleteSubLayout(subLayoutId: UUID): Response {
        TODO("Not yet implemented")
    }
}
