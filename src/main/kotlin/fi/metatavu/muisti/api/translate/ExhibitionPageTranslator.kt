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
class ExhibitionPageTranslator :
    AbstractTranslator<fi.metatavu.muisti.persistence.model.ExhibitionPage, ExhibitionPage>() {

    @Inject
    lateinit var exhibitionPageController: ExhibitionPageController

    override fun translate(entity: fi.metatavu.muisti.persistence.model.ExhibitionPage): ExhibitionPage {
        return ExhibitionPage(
            id = entity.id,
            exhibitionId = entity.exhibition?.id,
            deviceId = entity.device!!.id!!,
            layoutId = entity.layout!!.id!!,
            contentVersionId = entity.contentVersion!!.id!!,
            name = entity.name!!,
            resources = getResources(entity.resources),
            eventTriggers = exhibitionPageController.parseEventTriggers(entity.eventTriggers),
            enterTransitions = getTransitions(entity.enterTransitions),
            exitTransitions = getTransitions(entity.exitTransitions),
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
     * @param resources resources string
     * @return JSON list of page resources
     */
    private fun getResources(resources: String?): List<ExhibitionPageResource> {
        resources ?: return listOf()
        val objectMapper = ObjectMapper()
        return objectMapper.readValue(resources)
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