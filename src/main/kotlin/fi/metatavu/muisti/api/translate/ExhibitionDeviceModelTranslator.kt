package fi.metatavu.muisti.api.translate

import fi.metatavu.muisti.api.spec.model.ExhibitionDeviceModelCapabilities
import fi.metatavu.muisti.api.spec.model.ExhibitionDeviceModelDimensions
import fi.metatavu.muisti.api.spec.model.ExhibitionDeviceModelDisplayMetrics
import javax.enterprise.context.ApplicationScoped

/**
 * Translator for translating JPA exhibition device model entities into REST resources
 */
@ApplicationScoped
class ExhibitionDeviceModelTranslator: AbstractTranslator<fi.metatavu.muisti.persistence.model.ExhibitionDeviceModel, fi.metatavu.muisti.api.spec.model.ExhibitionDeviceModel>() {

    override fun translate(entity: fi.metatavu.muisti.persistence.model.ExhibitionDeviceModel): fi.metatavu.muisti.api.spec.model.ExhibitionDeviceModel {
        val capabilities = ExhibitionDeviceModelCapabilities()
        capabilities.touch = entity.capabilityTouch

        val dimensions = ExhibitionDeviceModelDimensions()
        dimensions.height = entity.dimensionHeight
        dimensions.width = entity.dimensionWidth

        val displayMetrics = ExhibitionDeviceModelDisplayMetrics()
        displayMetrics.heightPixels = entity.heightPixels
        displayMetrics.widthPixels = entity.widthPixels
        displayMetrics.density = entity.density
        displayMetrics.xdpi = entity.xdpi
        displayMetrics.ydpi = entity.ydpi

        val result = fi.metatavu.muisti.api.spec.model.ExhibitionDeviceModel()
        result.id = entity.id
        result.exhibitionId = entity.exhibition?.id
        result.manufacturer = entity.manufacturer
        result.model = entity.model
        result.capabilities = capabilities
        result.dimensions = dimensions
        result.displayMetrics = displayMetrics
        result.creatorId = entity.creatorId
        result.lastModifierId = entity.lastModifierId
        result.createdAt = entity.createdAt
        result.modifiedAt = entity.modifiedAt

        return result
    }

}

