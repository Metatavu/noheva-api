package fi.metatavu.noheva.api.translate

import fi.metatavu.noheva.api.spec.model.*
import fi.metatavu.noheva.contents.DataSerializationController
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Translator for translating JPA exhibition layout to device layout REST entity
 */
@ApplicationScoped
@Suppress ("unused")
class DeviceDataLayoutTranslator: AbstractTranslator<fi.metatavu.noheva.persistence.model.PageLayout, DeviceLayout>() {

    @Inject
    lateinit var dataSerializationController: DataSerializationController

    override fun translate(entity: fi.metatavu.noheva.persistence.model.PageLayout): DeviceLayout {
        return DeviceLayout(
            id = entity.id,
            data = dataSerializationController.getStringDataAsRestObject(entity.data, entity.layoutType) as PageLayoutViewHtml,
            screenOrientation = entity.screenOrientation,
            modifiedAt = entity.modifiedAt
        )
    }

}