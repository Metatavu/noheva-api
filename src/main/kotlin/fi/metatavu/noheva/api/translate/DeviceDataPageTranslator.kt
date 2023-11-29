package fi.metatavu.noheva.api.translate

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import fi.metatavu.noheva.api.spec.model.*
import fi.metatavu.noheva.contents.DataSerializationController
import fi.metatavu.noheva.contents.ExhibitionPageController
import fi.metatavu.noheva.contents.PageLayoutController
import fi.metatavu.noheva.persistence.model.PageLayout
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

    @Inject
    lateinit var pageLayoutController: PageLayoutController

    @Inject
    lateinit var dataSerializationController: DataSerializationController

    override fun translate(entity: fi.metatavu.noheva.persistence.model.ExhibitionPage): DevicePage {
        val contentVersion = entity.contentVersion!!

        return DevicePage(
            id = entity.id!!,
            layoutId = entity.layout!!.id,
            exhibitionId = entity.exhibition?.id!!,
            resources = getResources(resources = entity.resources, layout = entity.layout),
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
    private fun getResources(resources: String?, layout: PageLayout?): List<DevicePageResource> {
        resources ?: return listOf()
        val resourceNameMap = pageLayoutController.getHtmlLayoutResourceNameMap(layout)

        return jacksonObjectMapper().readValue<List<ExhibitionPageResource>>(resources)
            .map { resource -> DevicePageResource(
                id = resource.id,
                data = resource.data,
                type = resource.type,
                mode = resource.mode,
                component = resourceNameMap[resource.id]?.first,
                property = resourceNameMap[resource.id]?.second
            )}
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