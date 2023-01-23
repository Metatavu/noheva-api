package fi.metatavu.muisti.api.translate

import com.fasterxml.jackson.databind.ObjectMapper
import fi.metatavu.muisti.api.spec.model.PageLayoutView
import fi.metatavu.muisti.api.spec.model.SubLayout
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Translator for translating JPA exhibition sub layout entities into REST resources
 *
 * @author Jari Nykänen
 */
@ApplicationScoped
class SubLayoutTranslator : AbstractTranslator<fi.metatavu.muisti.persistence.model.SubLayout, SubLayout>() {

    @Inject
    lateinit var objectMapper: ObjectMapper
    override fun translate(entity: fi.metatavu.muisti.persistence.model.SubLayout): SubLayout {
        return SubLayout(
            id = entity.id,
            name = entity.name!!,
            data = getData(entity.data),
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

