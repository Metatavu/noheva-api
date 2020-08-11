package fi.metatavu.muisti.api.translate

import com.fasterxml.jackson.databind.ObjectMapper
import fi.metatavu.muisti.api.spec.model.PageLayoutView
import javax.enterprise.context.ApplicationScoped

/**
 * Translator for translating JPA exhibition sub layout entities into REST resources
 *
 * @author Jari Nykänen
 */
@ApplicationScoped
class SubLayoutTranslator: AbstractTranslator<fi.metatavu.muisti.persistence.model.SubLayout, fi.metatavu.muisti.api.spec.model.SubLayout>() {

    override fun translate(entity: fi.metatavu.muisti.persistence.model.SubLayout): fi.metatavu.muisti.api.spec.model.SubLayout {
        val result = fi.metatavu.muisti.api.spec.model.SubLayout()
        result.id = entity.id
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
    private fun getData(data: String?): PageLayoutView {
        val objectMapper = ObjectMapper()
        return objectMapper.readValue(data, PageLayoutView::class.java)
    }

}

