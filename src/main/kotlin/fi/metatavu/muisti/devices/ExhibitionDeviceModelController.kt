package fi.metatavu.muisti.devices

import fi.metatavu.muisti.api.spec.model.ExhibitionDeviceModelDisplayMetrics
import fi.metatavu.muisti.persistence.dao.ExhibitionDeviceModelDAO
import fi.metatavu.muisti.persistence.model.Exhibition
import fi.metatavu.muisti.persistence.model.ExhibitionDeviceModel
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Controller for exhibition device models
 */
@ApplicationScoped
class ExhibitionDeviceModelController() {

    @Inject
    private lateinit var exhibitionDeviceModelDAO: ExhibitionDeviceModelDAO

    /**
     * Creates new exhibition device model
     *
     * @param manufacturer device manufacturer
     * @param model device model
     * @param dimensionWidth device physical width
     * @param dimensionHeight device physical height
     * @param displayMetrics display metrics
     * @param capabilityTouch whether device has touch capability
     * @param creatorId creating user id
     * @return created exhibition device model
     */
    fun createExhibitionDeviceModel(exhibition: Exhibition, manufacturer: String, model: String, dimensionWidth: Double?, dimensionHeight: Double?, displayMetrics: ExhibitionDeviceModelDisplayMetrics, capabilityTouch: Boolean, creatorId: UUID): ExhibitionDeviceModel {
        return exhibitionDeviceModelDAO.create(UUID.randomUUID(),
                exhibition =  exhibition,
                manufacturer = manufacturer,
                model = model,
                dimensionWidth = dimensionWidth,
                dimensionHeight = dimensionHeight,
                widthPixels = displayMetrics.widthPixels,
                heightPixels = displayMetrics.heightPixels,
                density = displayMetrics.density,
                xdpi = displayMetrics.xdpi,
                ydpi = displayMetrics.ydpi,
                capabilityTouch = capabilityTouch,
                creatorId = creatorId,
                lastModifierId = creatorId)
    }

    /**
     * Finds an exhibition device model by id
     *
     * @param id exhibition device model id
     * @return found exhibition device model or null if not found
     */
    fun findExhibitionDeviceModelById(id: UUID): ExhibitionDeviceModel? {
        return exhibitionDeviceModelDAO.findById(id)
    }

    /**
     * Lists device models in an exhibitions
     *
     * @returns all deviceModels in an exhibition
     */
    fun listExhibitionDeviceModels(exhibition: Exhibition): List<ExhibitionDeviceModel> {
        return exhibitionDeviceModelDAO.listByExhibition(exhibition)
    }

    /**
     * Updates an exhibition device model
     *
     * @param exhibitionDeviceModel exhibition device model to be updated
     * @param manufacturer device manufacturer
     * @param model device model
     * @param dimensionWidth device physical width
     * @param dimensionHeight device physical height
     * @param displayMetrics display metrics
     * @param capabilityTouch whether device has touch capability
     * @param modifierId modifying user id
     * @return updated exhibition
     */
    fun updateExhibitionDeviceModel(exhibitionDeviceModel: ExhibitionDeviceModel, manufacturer: String, model: String, dimensionWidth: Double?, dimensionHeight: Double?, displayMetrics: ExhibitionDeviceModelDisplayMetrics, capabilityTouch: Boolean, modifierId: UUID): ExhibitionDeviceModel {
        exhibitionDeviceModelDAO.updateManufacturer(exhibitionDeviceModel, manufacturer, modifierId)
        exhibitionDeviceModelDAO.updateModel(exhibitionDeviceModel, model, modifierId)
        exhibitionDeviceModelDAO.updateDimensionWidth(exhibitionDeviceModel, dimensionWidth, modifierId)
        exhibitionDeviceModelDAO.updateDimensionHeight(exhibitionDeviceModel, dimensionHeight, modifierId)
        exhibitionDeviceModelDAO.updateWidthPixels(exhibitionDeviceModel, displayMetrics.widthPixels, modifierId)
        exhibitionDeviceModelDAO.updateHeightPixels(exhibitionDeviceModel, displayMetrics.heightPixels, modifierId)
        exhibitionDeviceModelDAO.updateDensity(exhibitionDeviceModel, displayMetrics.density, modifierId)
        exhibitionDeviceModelDAO.updateXdpi(exhibitionDeviceModel, displayMetrics.xdpi, modifierId)
        exhibitionDeviceModelDAO.updateYdpi(exhibitionDeviceModel, displayMetrics.ydpi, modifierId)
        exhibitionDeviceModelDAO.updateCapabilityTouch(exhibitionDeviceModel, capabilityTouch, modifierId)
        return exhibitionDeviceModel
    }

    /**
     * Deletes an exhibition device model
     *
     * @param exhibitionDeviceModel exhibition device model to be deleted
     */
    fun deleteExhibitionDeviceModel(exhibitionDeviceModel: ExhibitionDeviceModel) {
        return exhibitionDeviceModelDAO.delete(exhibitionDeviceModel)
    }

}