package fi.metatavu.muisti.api.translate

import com.fasterxml.jackson.databind.ObjectMapper
import fi.metatavu.muisti.api.spec.model.PageLayout
import fi.metatavu.muisti.api.spec.model.PageLayoutView
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Translator for translating JPA exhibition page layout entities into REST resources
 */
@ApplicationScoped
class PageLayoutTranslator : AbstractTranslator<fi.metatavu.muisti.persistence.model.PageLayout, PageLayout>() {

    @Inject
    lateinit var objectMapper: ObjectMapper
    override fun translate(entity: fi.metatavu.muisti.persistence.model.PageLayout): PageLayout {
        return PageLayout(
            id = entity.id,
            name = entity.name!!,
            data = getData(entity.data),
            thumbnailUrl = entity.thumbnailUrl,
            modelId = entity.deviceModel?.id,
            screenOrientation = entity.screenOrientation!!,
            creatorId = entity.creatorId,
            lastModifierId = entity.lastModifierId,
            createdAt = entity.createdAt,
            modifiedAt = entity.modifiedAt
        )
    }

    /**
     * Serializes the view into JSON string
     *
     * @param data view
     * @return JSON string
     */
    private fun getData(data: String?): PageLayoutView {
        return objectMapper.readValue(data, PageLayoutView::class.java)
    }

}

