package fi.metatavu.muisti.api.translate

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import fi.metatavu.muisti.api.spec.model.ExhibitionPageResource
import java.util.*
import javax.enterprise.context.ApplicationScoped

/**
 * Translator for translating JPA content version entities into REST resources
 */
@ApplicationScoped
class ContentVersionTranslator: AbstractTranslator<fi.metatavu.muisti.persistence.model.ContentVersion, fi.metatavu.muisti.api.spec.model.ContentVersion>() {

    override fun translate(entity: fi.metatavu.muisti.persistence.model.ContentVersion): fi.metatavu.muisti.api.spec.model.ContentVersion {
        val result: fi.metatavu.muisti.api.spec.model.ContentVersion = fi.metatavu.muisti.api.spec.model.ContentVersion()
        result.id = entity.id
        result.exhibitionId = entity.exhibition?.id
        result.name = entity.name
        result.language = entity.language
        result.rooms = getResources(entity.rooms)
        result.creatorId = entity.creatorId
        result.lastModifierId = entity.lastModifierId
        result.createdAt = entity.createdAt
        result.modifiedAt = entity.modifiedAt
        return result
    }

    /**
     * Reads resources string as list of page resources
     *
     * @param resources resources string
     * @return JSON list of page resources
     */
    private fun getResources(resources: String?): List<UUID> {
        resources ?: return mutableListOf()
        val objectMapper = ObjectMapper()
        return objectMapper.readValue(resources)
    }

}

