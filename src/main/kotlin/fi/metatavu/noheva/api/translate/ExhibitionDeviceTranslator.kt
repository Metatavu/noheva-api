package fi.metatavu.noheva.api.translate

import fi.metatavu.noheva.api.spec.model.ExhibitionDevice
import fi.metatavu.noheva.api.spec.model.Point
import javax.enterprise.context.ApplicationScoped

/**
 * Translator for translating JPA exhibition device entities into REST resources
 */
@ApplicationScoped
class ExhibitionDeviceTranslator :
    AbstractTranslator<fi.metatavu.noheva.persistence.model.ExhibitionDevice, ExhibitionDevice>() {

    override fun translate(entity: fi.metatavu.noheva.persistence.model.ExhibitionDevice): ExhibitionDevice {
        val location = Point(
            x = entity.locationX, y = entity.locationY
        )

        return ExhibitionDevice(
            id = entity.id,
            exhibitionId = entity.exhibition?.id,
            name = entity.name!!,
            groupId = entity.exhibitionDeviceGroup!!.id!!,
            modelId = entity.deviceModel!!.id!!,
            creatorId = entity.creatorId,
            lastModifierId = entity.lastModifierId,
            createdAt = entity.createdAt,
            modifiedAt = entity.modifiedAt,
            location = location,
            screenOrientation = entity.screenOrientation!!,
            imageLoadStrategy = entity.imageLoadStrategy!!,
            idlePageId = entity.idlePage?.id
        )
    }

}

