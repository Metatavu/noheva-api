package fi.metatavu.muisti.api.translate

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import fi.metatavu.muisti.api.spec.model.ExhibitionPageEventTrigger
import fi.metatavu.muisti.api.spec.model.ExhibitionPageResource
import fi.metatavu.muisti.api.spec.model.ExhibitionPageTransition
import fi.metatavu.muisti.api.spec.model.Transition
import javax.enterprise.context.ApplicationScoped

/**
 * Translator for translating JPA exhibition page  entities into REST resources
 */
@ApplicationScoped
class ExhibitionPageTranslator: AbstractTranslator<fi.metatavu.muisti.persistence.model.ExhibitionPage, fi.metatavu.muisti.api.spec.model.ExhibitionPage>() {

    override fun translate(entity: fi.metatavu.muisti.persistence.model.ExhibitionPage): fi.metatavu.muisti.api.spec.model.ExhibitionPage {
        val result = fi.metatavu.muisti.api.spec.model.ExhibitionPage()
        result.id = entity.id
        result.exhibitionId = entity.exhibition?.id
        result.deviceId = entity.device?.id
        result.layoutId = entity.layout?.id
        result.contentVersionId = entity.contentVersion?.id
        result.name = entity.name
        result.resources = getResources(entity.resources)
        result.eventTriggers = getEventTriggers(entity.eventTriggers)
        result.enterTransitions = getTransitions(entity.enterTransitions)
        result.exitTransitions = getTransitions(entity.exitTransitions)
        result.orderNumber = entity.orderNumber
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
    private fun getResources(resources: String?): List<ExhibitionPageResource> {
        resources ?: return listOf()
        val objectMapper = ObjectMapper()
        return objectMapper.readValue(resources)
    }

    /**
     * Reads event triggers string as event triggers object
     *
     * @param eventTriggers event triggers string
     * @return event triggers object
     */
    private fun getEventTriggers(eventTriggers: String?): List<ExhibitionPageEventTrigger> {
        eventTriggers ?: return listOf()
        val objectMapper = ObjectMapper()
        return objectMapper.readValue(eventTriggers)
    }

    /**
     * Reads transitions string as list of transitions
     *
     * @param transitions transitions string
     * @return JSON list of transitions
     */
    private fun getTransitions(transitions: String?): List<ExhibitionPageTransition> {
        transitions ?: return listOf()
        val objectMapper = ObjectMapper()
        return objectMapper.readValue(transitions)
    }

}