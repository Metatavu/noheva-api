package fi.metatavu.muisti.api.translate

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import fi.metatavu.muisti.api.spec.model.ExhibitionPage
import fi.metatavu.muisti.api.spec.model.ExhibitionPageResource
import fi.metatavu.muisti.api.spec.model.ExhibitionPageTransition
import fi.metatavu.muisti.contents.ExhibitionPageController
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Translator for translating JPA exhibition page  entities into REST resources
 */
@ApplicationScoped
class ExhibitionPageTranslator: AbstractTranslator<fi.metatavu.muisti.persistence.model.ExhibitionPage, fi.metatavu.muisti.api.spec.model.ExhibitionPage>() {

    @Inject
    lateinit var exhibitionPageController: ExhibitionPageController

    override fun translate(entity: fi.metatavu.muisti.persistence.model.ExhibitionPage): ExhibitionPage {
        val objectMapper = ObjectMapper()
        return ExhibitionPage(
            id = entity.id,
            exhibitionId = entity.exhibition?.id,
            deviceId = entity.device!!.id!!,
            layoutId = entity.layout!!.id!!,
            contentVersionId = entity.contentVersion!!.id!!,
            name = entity.name!!,
            resources = getResources(objectMapper, entity.resources),
            eventTriggers = exhibitionPageController.parseEventTriggers(entity.eventTriggers),
            enterTransitions = getTransitions(objectMapper, entity.enterTransitions),
            exitTransitions = getTransitions(objectMapper, entity.exitTransitions),
            orderNumber = entity.orderNumber!!,
            creatorId = entity.creatorId,
            lastModifierId = entity.lastModifierId,
            createdAt = entity.createdAt,
            modifiedAt = entity.modifiedAt
        )
    }

    /**
     * Reads resources string as list of page resources
     *
     * @param objectMapper objectMapper
     * @param resources resources string
     * @return JSON list of page resources
     */
    private fun getResources(objectMapper: ObjectMapper, resources: String?): List<ExhibitionPageResource> {
        resources ?: return listOf()
        return objectMapper.readValue(resources)
    }

    /**
     * Reads transitions string as list of transitions
     *
     * @param objectMapper objectMapper
     * @param transitions transitions string
     * @return JSON list of transitions
     */
    private fun getTransitions(objectMapper: ObjectMapper, transitions: String?): List<ExhibitionPageTransition> {
        transitions ?: return listOf()
        return objectMapper.readValue(transitions)
    }

}