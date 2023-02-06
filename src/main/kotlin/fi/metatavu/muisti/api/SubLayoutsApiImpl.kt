package fi.metatavu.muisti.api

import fi.metatavu.muisti.api.spec.SubLayoutsApi
import fi.metatavu.muisti.api.spec.model.SubLayout
import fi.metatavu.muisti.api.translate.SubLayoutTranslator
import fi.metatavu.muisti.contents.SubLayoutController
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

    override fun listSubLayouts(): Response {
        val result = subLayoutController.listSubLayouts()
        return createOk(result.map(subLayoutTranslator::translate))
    }

    override fun createSubLayout(subLayout: SubLayout): Response {
        val userId = loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        val name = subLayout.name
        val data = subLayout.data

        val created = subLayoutController.createSubLayout(name, data, userId)
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

        val subLayoutFound = subLayoutController.findSubLayoutById(subLayoutId)
            ?: return createNotFound("Sub layout $subLayoutId not found")
        val result = subLayoutController.updateSubLayout(subLayoutFound, name, data, userId)

        return createOk(subLayoutTranslator.translate(result))
    }

    override fun deleteSubLayout(subLayoutId: UUID): Response {
        loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        val subLayout = subLayoutController.findSubLayoutById(subLayoutId) ?: return createNotFound("Layout $subLayoutId not found")

        subLayoutController.deleteSubLayout(subLayout)

        return createNoContent()
    }
}
