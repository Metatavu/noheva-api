package fi.metatavu.noheva.api.translate

import fi.metatavu.noheva.api.spec.model.PageLayout
import fi.metatavu.noheva.contents.DataSerializationController
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Translator for translating JPA exhibition page layout entities into REST resources
 */
@ApplicationScoped
class PageLayoutTranslator : AbstractTranslator<fi.metatavu.noheva.persistence.model.PageLayout, PageLayout>() {

    @Inject
    lateinit var dataSerializationController: DataSerializationController

    override fun translate(entity: fi.metatavu.noheva.persistence.model.PageLayout): PageLayout {
        return PageLayout(
            id = entity.id,
            name = entity.name,
            data = dataSerializationController.getStringDataAsRestObject(entity.data, entity.layoutType),
            layoutType = entity.layoutType,
            defaultResources = dataSerializationController.parseStringToPageResources(entity.defaultResources),
            thumbnailUrl = entity.thumbnailUrl,
            modelId = entity.deviceModel?.id,
            screenOrientation = entity.screenOrientation,
            creatorId = entity.creatorId,
            lastModifierId = entity.lastModifierId,
            createdAt = entity.createdAt,
            modifiedAt = entity.modifiedAt
        )
    }

}

