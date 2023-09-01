package fi.metatavu.noheva.api.translate

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import fi.metatavu.noheva.api.spec.model.*
import fi.metatavu.noheva.contents.ExhibitionPageController
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Translator for translating JPA exhibition page to device page REST entity
 */
@ApplicationScoped
@Suppress ("unused")
class DeviceDataPageTranslator: AbstractTranslator<fi.metatavu.noheva.persistence.model.ExhibitionPage, DevicePage>() {

    @Inject
    lateinit var exhibitionPageController: ExhibitionPageController

    override fun translate(entity: fi.metatavu.noheva.persistence.model.ExhibitionPage): DevicePage {
        val contentVersion = entity.contentVersion!!

        return DevicePage(
            id = entity.id!!,
            layoutId = entity.layout!!.id,
            exhibitionId = entity.exhibition?.id!!,
            resources = getResources(entity.resources),
            orderNumber = entity.orderNumber!!,
            language = contentVersion.language!!,
            modifiedAt = entity.modifiedAt!!,
            name = entity.name!!,
            eventTriggers = exhibitionPageController.parseEventTriggers(entity.eventTriggers),
            enterTransitions = getTransitions(entity.enterTransitions),
            exitTransitions = getTransitions(entity.exitTransitions),
            activeConditionUserVariable = contentVersion.activeConditionUserVariable,
            activeConditionEquals = contentVersion.activeConditionEquals
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
        return jacksonObjectMapper().readValue(resources)
    }

    /**
     * Reads transitions string as list of transitions
     *
     * @param transitions transitions string
     * @return JSON list of transitions
     */
    private fun getTransitions(transitions: String?): List<ExhibitionPageTransition> {
        transitions ?: return listOf()
        return jacksonObjectMapper().readValue(transitions)
    }

}