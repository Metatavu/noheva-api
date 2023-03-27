package fi.metatavu.noheva.api.translate

import fi.metatavu.noheva.api.spec.model.SubLayout
import fi.metatavu.noheva.contents.DataSerializationController
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Translator for translating JPA exhibition sub layout entities into REST resources
 *
 * @author Jari Nykänen
 */
@ApplicationScoped
class SubLayoutTranslator : AbstractTranslator<fi.metatavu.noheva.persistence.model.SubLayout, SubLayout>() {

    @Inject
    lateinit var dataSerializationController: DataSerializationController

    override fun translate(entity: fi.metatavu.noheva.persistence.model.SubLayout): SubLayout {
        return SubLayout(
            id = entity.id,
            name = entity.name,
            data = dataSerializationController.getStringDataAsRestObject(entity.data, entity.layoutType),
            layoutType = entity.layoutType,
            defaultResources = dataSerializationController.parseStringToPageResources(entity.defaultResources),
            creatorId = entity.creatorId,
            lastModifierId = entity.lastModifierId,
            createdAt = entity.createdAt,
            modifiedAt = entity.modifiedAt
        )
    }

}

