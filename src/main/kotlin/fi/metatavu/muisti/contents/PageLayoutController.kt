package fi.metatavu.muisti.contents

import com.fasterxml.jackson.databind.ObjectMapper
import fi.metatavu.muisti.api.spec.model.PageLayoutView
import fi.metatavu.muisti.api.spec.model.ScreenOrientation
import fi.metatavu.muisti.persistence.dao.PageLayoutDAO
import fi.metatavu.muisti.persistence.model.DeviceModel
import fi.metatavu.muisti.persistence.model.PageLayout
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Controller for exhibition page layouts
 */
@ApplicationScoped
class PageLayoutController() {

    @Inject
    private lateinit var exhibitionPageController: ExhibitionPageController

    @Inject
    private lateinit var pageLayoutDAO: PageLayoutDAO

    /**
     * Creates new exhibition page layout
     *
     * @param name name
     * @param data data
     * @param thumbnailUrl thumbnail URL
     * @param deviceModel device model
     * @param screenOrientation screen orientation
     * @param creatorId creating user id
     * @return created exhibition page layout
     */
    fun createPageLayout(name: String, data: PageLayoutView, thumbnailUrl: String?, deviceModel: DeviceModel, screenOrientation: ScreenOrientation, creatorId: UUID): PageLayout {
        return pageLayoutDAO.create(UUID.randomUUID(), name, getDataAsString(data), thumbnailUrl, deviceModel, screenOrientation, creatorId, creatorId)
    }

    /**
     * Finds an exhibition page layout by id
     *
     * @param id exhibition page layout id
     * @return found exhibition page layout or null if not found
     */
    fun findPageLayoutById(id: UUID): PageLayout? {
        return pageLayoutDAO.findById(id)
    }

    /**
     * List of exhibition page layouts by device model id and screen orientation
     *
     * @param deviceModel device model
     * @param screenOrientation screen orientation
     * @return list of exhibition page layouts
     */
    fun listPageLayouts(deviceModel: DeviceModel?, screenOrientation: ScreenOrientation?): List<PageLayout> {
        return pageLayoutDAO.list(deviceModel, screenOrientation)
    }

    /**
     * Updates an exhibition page layout
     *
     * @param pageLayout exhibition page layout to be updated
     * @param name name
     * @param data data
     * @param thumbnailUrl thumbnail URL
     * @param screenOrientation screen orientation
     * @param modifierId modifying user id
     * @return updated exhibition
     */
    fun updatePageLayout(pageLayout: PageLayout, name: String, data: PageLayoutView, thumbnailUrl: String?, deviceModel: DeviceModel, screenOrientation: ScreenOrientation, modifierId: UUID): PageLayout {
        pageLayoutDAO.updateName(pageLayout, name, modifierId)
        pageLayoutDAO.updateData(pageLayout, getDataAsString(data), modifierId)
        pageLayoutDAO.updateThumbnailUrl(pageLayout, thumbnailUrl, modifierId)
        pageLayoutDAO.updateDeviceModel(pageLayout, deviceModel, modifierId)
        pageLayoutDAO.updateScreenOrientation(pageLayout, screenOrientation, modifierId)
        return pageLayout
    }

    /**
     * Deletes an exhibition page layout
     *
     * @param pageLayout exhibition page layout to be deleted
     */
    fun deletePageLayout(pageLayout: PageLayout) {
        exhibitionPageController.listExhibitionLayoutPages(pageLayout)
            .forEach(exhibitionPageController::deleteExhibitionPage)

        return pageLayoutDAO.delete(pageLayout)
    }

    /**
     * Serializes the view into JSON string
     *
     * @param data view
     * @return JSON string
     */
    private fun getDataAsString(data: PageLayoutView): String {
        val objectMapper = ObjectMapper()
        return objectMapper.writeValueAsString(data)
    }

}