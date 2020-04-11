package fi.metatavu.muisti.api.translate

import com.fasterxml.jackson.databind.ObjectMapper
import fi.metatavu.muisti.api.spec.model.PageLayoutView
import javax.enterprise.context.ApplicationScoped

/**
 * Translator for translating JPA exhibition page layout entities into REST resources
 */
@ApplicationScoped
class PageLayoutTranslator: AbstractTranslator<fi.metatavu.muisti.persistence.model.PageLayout, fi.metatavu.muisti.api.spec.model.PageLayout>() {

    override fun translate(entity: fi.metatavu.muisti.persistence.model.PageLayout): fi.metatavu.muisti.api.spec.model.PageLayout {
        val result = fi.metatavu.muisti.api.spec.model.PageLayout()
        result.id = entity.id
        result.name = entity.name
        result.data = getData(entity.data)
        result.thumbnailUrl = entity.thumbnailUrl
        result.modelId = entity.deviceModel?.id
        result.screenOrientation = entity.screenOrientation
        result.creatorId = entity.creatorId
        result.lastModifierId = entity.lastModifierId
        result.createdAt = entity.createdAt
        result.modifiedAt = entity.modifiedAt

        return result
    }

    /**
     * Serializes the view into JSON string
     *
     * @param data view
     * @return JSON string
     */
    private fun getData(data: String?): PageLayoutView {
        val objectMapper = ObjectMapper()
        return objectMapper.readValue(data, PageLayoutView::class.java)
    }

}

