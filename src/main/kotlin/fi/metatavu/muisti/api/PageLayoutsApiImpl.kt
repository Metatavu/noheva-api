package fi.metatavu.muisti.api;

import fi.metatavu.muisti.api.spec.PageLayoutsApi
import fi.metatavu.muisti.api.spec.model.PageLayout
import java.util.*
import javax.ws.rs.core.Response


class PageLayoutsApiImpl: PageLayoutsApi, AbstractApi() {
    /* V1 */
    override fun listPageLayouts(deviceModelId: UUID?, screenOrientation: String?): Response {
        TODO("Not yet implemented")
    }

    override fun createPageLayout(pageLayout: PageLayout): Response {
        TODO("Not yet implemented")
    }

    override fun findPageLayout(pageLayoutId: UUID): Response {
        TODO("Not yet implemented")
    }

    override fun updatePageLayout(pageLayoutId: UUID, pageLayout: PageLayout): Response {
        TODO("Not yet implemented")
    }

    override fun deletePageLayout(pageLayoutId: UUID): Response {
        TODO("Not yet implemented")
    }

}
