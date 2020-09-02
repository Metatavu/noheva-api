package fi.metatavu.muisti.api.translate

import fi.metatavu.muisti.api.spec.model.Point
import fi.metatavu.muisti.contents.ExhibitionPageController
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Translator for translating JPA exhibition device entities into REST resources
 */
@ApplicationScoped
class ExhibitionDeviceTranslator: AbstractTranslator<fi.metatavu.muisti.persistence.model.ExhibitionDevice, fi.metatavu.muisti.api.spec.model.ExhibitionDevice>() {

    @Inject
    private lateinit var exhibitionPageController: ExhibitionPageController

    override fun translate(entity: fi.metatavu.muisti.persistence.model.ExhibitionDevice): fi.metatavu.muisti.api.spec.model.ExhibitionDevice {
        val location = Point()
        location.x = entity.locationX
        location.y = entity.locationY

        val result: fi.metatavu.muisti.api.spec.model.ExhibitionDevice = fi.metatavu.muisti.api.spec.model.ExhibitionDevice()
        result.id = entity.id
        result.exhibitionId = entity.exhibition?.id
        result.name = entity.name
        result.groupId = entity.exhibitionDeviceGroup?.id
        result.modelId = entity.deviceModel?.id
        result.indexPageId = entity.indexPage?.id
        result.creatorId = entity.creatorId
        result.lastModifierId = entity.lastModifierId
        result.createdAt = entity.createdAt
        result.modifiedAt = entity.modifiedAt
        result.location = location
        result.screenOrientation = entity.screenOrientation

        return result
    }

}

