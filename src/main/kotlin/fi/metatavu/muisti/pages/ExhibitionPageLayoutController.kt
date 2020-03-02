package fi.metatavu.muisti.pages

import com.fasterxml.jackson.databind.ObjectMapper
import fi.metatavu.muisti.api.spec.model.ExhibitionPageLayoutView
import fi.metatavu.muisti.persistence.dao.ExhibitionPageLayoutDAO
import fi.metatavu.muisti.persistence.model.Exhibition
import fi.metatavu.muisti.persistence.model.ExhibitionPageLayout
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Controller for exhibition page layouts
 */
@ApplicationScoped
class ExhibitionPageLayoutController() {

    @Inject
    private lateinit var exhibitionPageController: ExhibitionPageController

    @Inject
    private lateinit var exhibitionPageLayoutDAO: ExhibitionPageLayoutDAO

    /**
     * Creates new exhibition page layout
     *
     * @param name name
     * @param data data
     * @param creatorId creating user id
     * @return created exhibition page layout
     */
    fun createExhibitionPageLayout(exhibition: Exhibition, name: String, data: ExhibitionPageLayoutView, creatorId: UUID): ExhibitionPageLayout {
        return exhibitionPageLayoutDAO.create(UUID.randomUUID(), exhibition, name, getDataAsString(data), creatorId, creatorId)
    }

    /**
     * Finds an exhibition page layout by id
     *
     * @param id exhibition page layout id
     * @return found exhibition page layout or null if not found
     */
    fun findExhibitionPageLayoutById(id: UUID): ExhibitionPageLayout? {
        return exhibitionPageLayoutDAO.findById(id)
    }

    /**
     * Lists page layouts in an exhibitions
     *
     * @returns all pageLayouts in an exhibition
     */
    fun listExhibitionPageLayouts(exhibition: Exhibition): List<ExhibitionPageLayout> {
        return exhibitionPageLayoutDAO.listByExhibition(exhibition)
    }

    /**
     * Updates an exhibition page layout
     *
     * @param exhibitionPageLayout exhibition page layout to be updated
     * @param name name
     * @param data data
     * @param modifierId modifying user id
     * @return updated exhibition
     */
    fun updateExhibitionPageLayout(exhibitionPageLayout: ExhibitionPageLayout, name: String, data: ExhibitionPageLayoutView, modifierId: UUID): ExhibitionPageLayout {
        exhibitionPageLayoutDAO.updateName(exhibitionPageLayout, name, modifierId)
        exhibitionPageLayoutDAO.updateData(exhibitionPageLayout, getDataAsString(data), modifierId)
        return exhibitionPageLayout
    }

    /**
     * Deletes an exhibition page layout
     *
     * @param exhibitionPageLayout exhibition page layout to be deleted
     */
    fun deleteExhibitionPageLayout(exhibitionPageLayout: ExhibitionPageLayout) {
        exhibitionPageController.listExhibitionLayoutPages(exhibitionPageLayout)
            .forEach(exhibitionPageController::deleteExhibitionPage)

        return exhibitionPageLayoutDAO.delete(exhibitionPageLayout)
    }

    /**
     * Serializes the view into JSON string
     *
     * @param data view
     * @return JSON string
     */
    private fun getDataAsString(data: ExhibitionPageLayoutView): String {
        val objectMapper = ObjectMapper()
        return objectMapper.writeValueAsString(data)
    }

}