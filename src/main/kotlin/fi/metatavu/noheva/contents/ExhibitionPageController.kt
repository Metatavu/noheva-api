package fi.metatavu.noheva.contents

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import fi.metatavu.noheva.api.spec.model.*
import fi.metatavu.noheva.persistence.dao.ExhibitionPageDAO
import fi.metatavu.noheva.persistence.model.ContentVersion
import fi.metatavu.noheva.persistence.model.Exhibition
import fi.metatavu.noheva.persistence.model.ExhibitionDevice
import fi.metatavu.noheva.persistence.model.ExhibitionDeviceGroup
import fi.metatavu.noheva.persistence.model.ExhibitionPage
import fi.metatavu.noheva.persistence.model.PageLayout
import fi.metatavu.noheva.utils.CopyException
import fi.metatavu.noheva.utils.IdMapper
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Controller for exhibition pages
 */
@ApplicationScoped
class ExhibitionPageController {

    @Inject
    lateinit var exhibitionPageDAO: ExhibitionPageDAO

    /**
     * Creates new exhibition page
     *
     * @param exhibition exhibition
     * @param device device
     * @param layout layout
     * @param contentVersion content version
     * @param name name
     * @param orderNumber order number
     * @param resources resources
     * @param eventTriggers event triggers
     * @param enterTransitions page enter transitions
     * @param exitTransitions page exit transitions
     * @param creatorId creating user id
     * @return created exhibition page
     */
    fun createPage(
        exhibition: Exhibition,
        device: ExhibitionDevice,
        layout: PageLayout,
        contentVersion: ContentVersion,
        name: String,
        orderNumber: Int,
        resources: List<ExhibitionPageResource>,
        eventTriggers: List<ExhibitionPageEventTrigger>,
        enterTransitions: List<ExhibitionPageTransition>,
        exitTransitions: List<ExhibitionPageTransition>,
        creatorId: UUID
    ): ExhibitionPage {
        return exhibitionPageDAO.create(
            UUID.randomUUID(),
            exhibition = exhibition,
            device = device,
            layout = layout,
            contentVersion = contentVersion,
            name = name,
            orderNumber = orderNumber,
            resources = getDataAsString(resources),
            eventTriggers = getDataAsString(eventTriggers),
            enterTransitions = getDataAsString(enterTransitions),
            exitTransitions = getDataAsString(exitTransitions),
            creatorId = creatorId,
            lastModifierId = creatorId
        )
    }

    /**
     * Creates a copy of a page.
     *
     * Method remaps page navigate actions according to ids specified by the id mapper
     *
     * @param sourcePage source page
     * @param targetDevice target device for the copied page
     * @param targetContentVersion target content version for the copied page
     * @param idMapper id mapper
     * @param creatorId id of user that created the copy
     * @return copied page
     */
    fun copyPage(
        sourcePage: ExhibitionPage,
        targetDevice: ExhibitionDevice,
        targetContentVersion: ContentVersion,
        idMapper: IdMapper,
        creatorId: UUID
    ): ExhibitionPage {
        val id = idMapper.getNewId(sourcePage.id) ?: throw CopyException("Target page id not found")

        val eventTriggers = remapEventTriggers(
            eventTriggers = parseEventTriggers(eventTriggers = sourcePage.eventTriggers),
            idMapper = idMapper
        )

        val targetExhibition = targetDevice.exhibition ?: throw CopyException("Target exhibition not found")
        if (targetContentVersion.exhibition?.id != targetExhibition.id) {
            throw CopyException("Target exhibition does not match source exhibition")
        }

        return exhibitionPageDAO.create(
            id = id,
            exhibition = targetExhibition,
            device = targetDevice,
            layout = sourcePage.layout ?: throw CopyException("Source page layout not found"),
            contentVersion = targetContentVersion,
            name = sourcePage.name ?: throw CopyException("Source page name not found"),
            orderNumber = sourcePage.orderNumber ?: throw CopyException("Source page orderNumber not found"),
            resources = sourcePage.resources ?: throw CopyException("Source page resources not found"),
            eventTriggers = getDataAsString(eventTriggers),
            enterTransitions = sourcePage.enterTransitions,
            exitTransitions = sourcePage.exitTransitions,
            creatorId = creatorId,
            lastModifierId = creatorId
        )
    }

    /**
     * Finds an exhibition page by id
     *
     * @param id exhibition page id
     * @return found exhibition page or null if not found
     */
    fun findExhibitionPageById(id: UUID): ExhibitionPage? {
        return exhibitionPageDAO.findById(id)
    }

    /**
     * Lists exhibition pages
     *
     * @param exhibition exhibition
     * @param exhibitionDevice filter by exhibition device. Ignored if null is passed
     * @param contentVersion filter by exhibition content version. Ignored if null is passed
     * @param pageLayout filter by page layout. Ignored if null is passed
     * @return List of exhibition pages
     */
    fun listExhibitionPages(
        exhibition: Exhibition,
        exhibitionDevice: ExhibitionDevice?,
        contentVersion: ContentVersion?,
        pageLayout: PageLayout?
    ): List<ExhibitionPage> {
        return exhibitionPageDAO.list(
            exhibition = exhibition,
            exhibitionDevice = exhibitionDevice,
            contentVersion = contentVersion,
            pageLayout = pageLayout
        )
    }

    /**
     * Lists pages by layout
     *
     * @returns all pages with given layout
     */
    fun listExhibitionLayoutPages(pageLayout: PageLayout): List<ExhibitionPage> {
        return exhibitionPageDAO.listByLayout(pageLayout)
    }

    /**
     * Lists pages by device group
     *
     * @param deviceGroup device group
     * @return List of pages in device group
     */
    fun listDeviceGroupPages(deviceGroup: ExhibitionDeviceGroup): List<ExhibitionPage> {
        return exhibitionPageDAO.listByDeviceGroup(deviceGroup = deviceGroup)
    }

    /**
     * Updates an exhibition page
     *
     * @param exhibitionPage exhibition page  to be updated
     * @param device device
     * @param layout layout
     * @param contentVersion content version
     * @param name name
     * @param resources resources
     * @param eventTriggers event triggers
     * @param enterTransitions page enter transitions
     * @param exitTransitions page exit transitions
     * @param modifierId modifying user id
     * @param orderNumber order number
     * @return updated exhibition page
     */
    fun updateExhibitionPage(
        exhibitionPage: ExhibitionPage,
        device: ExhibitionDevice,
        layout: PageLayout,
        contentVersion: ContentVersion,
        name: String,
        resources: List<ExhibitionPageResource>,
        eventTriggers: List<ExhibitionPageEventTrigger>,
        enterTransitions: List<ExhibitionPageTransition>,
        exitTransitions: List<ExhibitionPageTransition>,
        orderNumber: Int,
        modifierId: UUID
    )
            : ExhibitionPage {
        var result = exhibitionPageDAO.updateName(exhibitionPage, name, modifierId)
        result = exhibitionPageDAO.updateLayout(result, layout, modifierId)
        result = exhibitionPageDAO.updateDevice(result, device, modifierId)
        result = exhibitionPageDAO.updateContentVersion(result, contentVersion, modifierId)
        result = exhibitionPageDAO.updateResources(result, getDataAsString(resources), modifierId)
        result = exhibitionPageDAO.updateEventTriggers(result, getDataAsString(eventTriggers), modifierId)
        result = exhibitionPageDAO.updateEnterTransitions(result, getDataAsString(enterTransitions), modifierId)
        result = exhibitionPageDAO.updateExitTransitions(result, getDataAsString(exitTransitions), modifierId)
        result = exhibitionPageDAO.updateOrderNumber(result, orderNumber, modifierId)
        return result
    }

    /**
     * Deletes an exhibition page
     *
     * @param exhibitionPage exhibition page to be deleted
     */
    fun deleteExhibitionPage(exhibitionPage: ExhibitionPage) {
        return exhibitionPageDAO.delete(exhibitionPage)
    }

    /**
     * Parses event triggers string as list of event triggers objects
     *
     * @param eventTriggers event triggers string
     * @return list of event triggers objects
     */
    fun parseEventTriggers(eventTriggers: String?): List<ExhibitionPageEventTrigger> {
        eventTriggers ?: return listOf()
        val objectMapper = ObjectMapper()
        return objectMapper.readValue(eventTriggers)
    }

    /**
     * Remaps event triggers when copying the page
     *
     * @param eventTriggers event triggers to be remapped
     * @param idMapper id mapper
     * @return remapped event triggers
     */
    private fun remapEventTriggers(
        eventTriggers: List<ExhibitionPageEventTrigger>,
        idMapper: IdMapper
    ): List<ExhibitionPageEventTrigger> {
        return eventTriggers.map { remapEventTrigger(it, idMapper) }
    }

    /**
     * Remaps event trigger when copying the page
     *
     * @param eventTrigger event trigger to be remapped
     * @param idMapper id mapper
     * @return remapped event trigger
     */
    private fun remapEventTrigger(
        eventTrigger: ExhibitionPageEventTrigger,
        idMapper: IdMapper
    ): ExhibitionPageEventTrigger {
        return eventTrigger.copy(events = eventTrigger.events?.map { remapEvent(it, idMapper) })
    }

    /**
     * Remaps event when copying the page
     *
     * @param event event to be remapped
     * @param idMapper id mapper
     * @return remapped event
     */
    private fun remapEvent(event: ExhibitionPageEvent, idMapper: IdMapper): ExhibitionPageEvent {
        return event.copy(properties = event.properties.map { remapEventProperty(it, idMapper) })
    }

    /**
     * Remaps event properties when copying the page
     *
     * @param property property to be remapped
     * @param idMapper id mapper
     * @return remapped property
     */
    private fun remapEventProperty(
        property: ExhibitionPageEventProperty,
        idMapper: IdMapper
    ): ExhibitionPageEventProperty {
        return if (property.name == "pageId") {
            val oldId = UUID.fromString(property.value)
            property.copy(value = idMapper.getNewId(oldId).toString())
        } else {
            property.copy()
        }
    }

    /**
     * Serializes the object into JSON string
     *
     * @param data object
     * @return JSON string
     */
    private fun <T> getDataAsString(data: T): String {
        val objectMapper = ObjectMapper()
        return objectMapper.writeValueAsString(data)
    }

}
