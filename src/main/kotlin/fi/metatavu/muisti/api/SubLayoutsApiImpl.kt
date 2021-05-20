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
 * Sub layouts API REST endpoints
 *
 * @author Jari Nykänen
 */
@RequestScoped
@Transactional
class SubLayoutsApiImpl: SubLayoutsApi, AbstractApi() {

    @Inject
    private lateinit var subLayoutController: SubLayoutController

    @Inject
    private lateinit var subLayoutTranslator: SubLayoutTranslator

    /* Sub layouts */

    override fun createSubLayout(payload: SubLayout?): Response {
        payload ?: return createBadRequest("Missing request body")
        val userId = loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        val name = payload.name
        val data = payload.data

        val subLayout = subLayoutController.createSubLayout(name, data, userId)
        return createOk(subLayoutTranslator.translate(subLayout))
    }

    override fun findSubLayout(subLayoutId: UUID?): Response {
        subLayoutId ?: return createBadRequest("Missing sub layout id!")
        loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        val subLayout = subLayoutController.findSubLayoutById(subLayoutId) ?: return createNotFound("Sub layout $subLayoutId not found")
        return createOk(subLayoutTranslator.translate(subLayout))
    }

    override fun listSubLayouts(): Response? {
        val result = subLayoutController.listSubLayouts()
        return createOk(result.map (subLayoutTranslator::translate))
    }

    override fun updateSubLayout(subLayoutId: UUID?, payload: SubLayout?): Response {
        payload ?: return createBadRequest("Missing request body")
        subLayoutId ?: return createBadRequest("Missing sub layout id!")

        val userId = loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        val name = payload.name
        val data = payload.data

        val subLayout = subLayoutController.findSubLayoutById(subLayoutId) ?: return createNotFound("Sub layout $subLayoutId not found")
        val result = subLayoutController.updateSubLayout(subLayout, name, data, userId)

        return createOk(subLayoutTranslator.translate(result))
    }

    override fun deleteSubLayout(subLayoutId: UUID?): Response {
        subLayoutId ?: return createBadRequest("Missing sub layout id!")
        loggerUserId ?: return createUnauthorized(UNAUTHORIZED)
        val subLayout = subLayoutController.findSubLayoutById(subLayoutId) ?: return createNotFound("Layout $subLayoutId not found")

        subLayoutController.deleteSubLayout(subLayout)

        return createNoContent()
    }

}