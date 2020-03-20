package fi.metatavu.muisti.api.translate

import fi.metatavu.muisti.api.spec.model.DeviceModelCapabilities
import fi.metatavu.muisti.api.spec.model.DeviceModelDimensions
import fi.metatavu.muisti.api.spec.model.DeviceModelDisplayMetrics
import javax.enterprise.context.ApplicationScoped

/**
 * Translator for translating JPA exhibition device model entities into REST resources
 */
@ApplicationScoped
class DeviceModelTranslator: AbstractTranslator<fi.metatavu.muisti.persistence.model.DeviceModel, fi.metatavu.muisti.api.spec.model.DeviceModel>() {

    override fun translate(entity: fi.metatavu.muisti.persistence.model.DeviceModel): fi.metatavu.muisti.api.spec.model.DeviceModel {
        val capabilities = DeviceModelCapabilities()
        capabilities.touch = entity.capabilityTouch

        val dimensions = DeviceModelDimensions()
        dimensions.height = entity.dimensionHeight
        dimensions.width = entity.dimensionWidth

        val displayMetrics = DeviceModelDisplayMetrics()
        displayMetrics.heightPixels = entity.heightPixels
        displayMetrics.widthPixels = entity.widthPixels
        displayMetrics.density = entity.density
        displayMetrics.xdpi = entity.xdpi
        displayMetrics.ydpi = entity.ydpi

        val result = fi.metatavu.muisti.api.spec.model.DeviceModel()
        result.id = entity.id
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

