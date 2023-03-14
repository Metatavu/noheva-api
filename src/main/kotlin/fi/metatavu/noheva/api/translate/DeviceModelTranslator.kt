package fi.metatavu.noheva.api.translate

import fi.metatavu.noheva.api.spec.model.DeviceModel
import fi.metatavu.noheva.api.spec.model.DeviceModelCapabilities
import fi.metatavu.noheva.api.spec.model.DeviceModelDimensions
import fi.metatavu.noheva.api.spec.model.DeviceModelDisplayMetrics
import javax.enterprise.context.ApplicationScoped

/**
 * Translator for translating JPA exhibition device model entities into REST resources
 */
@ApplicationScoped
class DeviceModelTranslator :
    AbstractTranslator<fi.metatavu.noheva.persistence.model.DeviceModel, DeviceModel>() {

    override fun translate(entity: fi.metatavu.noheva.persistence.model.DeviceModel): DeviceModel {
        val capabilities = DeviceModelCapabilities(entity.capabilityTouch)

        val dimensions = DeviceModelDimensions(
            deviceWidth = entity.deviceWidth,
            deviceHeight = entity.deviceHeight,
            deviceDepth = entity.deviceDepth,
            screenWidth = entity.screenWidth,
            screenHeight = entity.screenHeight
        )

        val displayMetrics = DeviceModelDisplayMetrics(
            heightPixels = entity.heightPixels,
            widthPixels = entity.widthPixels,
            density = entity.density,
            xdpi = entity.xdpi,
            ydpi = entity.ydpi
        )

        return DeviceModel(
            id = entity.id,
            manufacturer = entity.manufacturer!!,
            model = entity.model!!,
            capabilities = capabilities,
            dimensions = dimensions,
            displayMetrics = displayMetrics,
            screenOrientation = entity.screenOrientation!!,
            creatorId = entity.creatorId,
            lastModifierId = entity.lastModifierId,
            createdAt = entity.createdAt,
            modifiedAt = entity.modifiedAt
        )
    }

}

