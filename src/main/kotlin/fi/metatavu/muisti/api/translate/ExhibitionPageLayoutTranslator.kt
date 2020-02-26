package fi.metatavu.muisti.api.translate

import com.fasterxml.jackson.databind.ObjectMapper
import fi.metatavu.muisti.api.spec.model.ExhibitionPageLayoutView
import javax.enterprise.context.ApplicationScoped

/**
 * Translator for translating JPA exhibition page layout entities into REST resources
 */
@ApplicationScoped
class ExhibitionPageLayoutTranslator: AbstractTranslator<fi.metatavu.muisti.persistence.model.ExhibitionPageLayout, fi.metatavu.muisti.api.spec.model.ExhibitionPageLayout>() {

    override fun translate(entity: fi.metatavu.muisti.persistence.model.ExhibitionPageLayout?): fi.metatavu.muisti.api.spec.model.ExhibitionPageLayout? {
        if (entity == null) {
            return null
        }

        val result = fi.metatavu.muisti.api.spec.model.ExhibitionPageLayout()
        result.id = entity.id
        result.exhibitionId = entity.exhibition?.id
        result.name = entity.name
        result.data = getData(entity.data)
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
    private fun getData(data: String?): ExhibitionPageLayoutView {
        val objectMapper = ObjectMapper()
        return objectMapper.readValue(data, ExhibitionPageLayoutView::class.java)
    }

}

