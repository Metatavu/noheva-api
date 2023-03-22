package fi.metatavu.noheva.api

import fi.metatavu.noheva.api.spec.SubLayoutsApi
import fi.metatavu.noheva.api.spec.model.SubLayout
import fi.metatavu.noheva.contents.PageLayoutDataController
import fi.metatavu.noheva.api.translate.SubLayoutTranslator
import fi.metatavu.noheva.contents.SubLayoutController
import java.util.*
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.transaction.Transactional
import javax.ws.rs.core.Response

/**
 * Sub layouts api implementation
 */
@RequestScoped
@Transactional
class SubLayoutsApiImpl : SubLayoutsApi, AbstractApi() {

    @Inject
    lateinit var subLayoutController: SubLayoutController

    @Inject
    lateinit var subLayoutTranslator: SubLayoutTranslator

    @Inject
    lateinit var pageLayoutDataController: PageLayoutDataController

    override fun listSubLayouts(): Response {
        val result = subLayoutController.listSubLayouts()
        return createOk(result.map(subLayoutTranslator::translate))
    }

    override fun createSubLayout(subLayout: SubLayout): Response {
        val userId = loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        val name = subLayout.name
        val data = subLayout.data
        val layoutType = subLayout.layoutType

        if (pageLayoutDataController.isValidLayoutType(data, layoutType).not()) return createBadRequest(INVALID_LAYOUT_TYPE)

        val created = subLayoutController.createSubLayout(
            name = name,
            data = data,
            layoutType = layoutType,
            creatorId =  userId
        )
        return createOk(subLayoutTranslator.translate(created))
    }

    override fun findSubLayout(subLayoutId: UUID): Response {
        loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        val subLayout = subLayoutController.findSubLayoutById(subLayoutId)
            ?: return createNotFound("Sub layout $subLayoutId not found")
        return createOk(subLayoutTranslator.translate(subLayout))
    }

    override fun updateSubLayout(subLayoutId: UUID, subLayout: SubLayout): Response {
        val userId = loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        val name = subLayout.name
        val data = subLayout.data
        val layoutType = subLayout.layoutType

        if (pageLayoutDataController.isValidLayoutType(data, layoutType).not()) return createBadRequest(INVALID_LAYOUT_TYPE)

        val subLayoutFound = subLayoutController.findSubLayoutById(subLayoutId)
            ?: return createNotFound("Sub layout $subLayoutId not found")
        if (layoutType != subLayoutFound.layoutType) return createBadRequest("Layout type cannot be changed")
        val result = subLayoutController.updateSubLayout(
            subLayout = subLayoutFound,
            name = name,
            data = data,
            modifierId = userId
        )

        return createOk(subLayoutTranslator.translate(result))
    }

    override fun deleteSubLayout(subLayoutId: UUID): Response {
        loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        val subLayout = subLayoutController.findSubLayoutById(subLayoutId) ?: return createNotFound("Layout $subLayoutId not found")

        subLayoutController.deleteSubLayout(subLayout)

        return createNoContent()
    }
}
