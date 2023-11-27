package fi.metatavu.noheva.contents

import fi.metatavu.noheva.api.spec.model.ExhibitionPageResource
import fi.metatavu.noheva.api.spec.model.LayoutType
import fi.metatavu.noheva.api.spec.model.PageLayoutViewHtml
import fi.metatavu.noheva.api.spec.model.ScreenOrientation
import fi.metatavu.noheva.persistence.dao.PageLayoutDAO
import fi.metatavu.noheva.persistence.model.DeviceModel
import fi.metatavu.noheva.persistence.model.ExhibitionDevice
import fi.metatavu.noheva.persistence.model.PageLayout
import org.jsoup.Jsoup
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Controller for exhibition page layouts
 */
@ApplicationScoped
class PageLayoutController {

    @Inject
    lateinit var pageLayoutDAO: PageLayoutDAO

    @Inject
    lateinit var dataSerializationController: DataSerializationController

    /**
     * Creates new exhibition page layout
     *
     * @param name name
     * @param data data
     * @param layoutType layout type of the data
     * @param defaultResources default page resources
     * @param thumbnailUrl thumbnail URL
     * @param deviceModel device model
     * @param screenOrientation screen orientation
     * @param creatorId creating user id
     * @return created exhibition page layout
     */
    fun createPageLayout(
        name: String,
        data: Any,
        layoutType: LayoutType,
        defaultResources: List<ExhibitionPageResource>?,
        thumbnailUrl: String?,
        deviceModel: DeviceModel,
        screenOrientation: ScreenOrientation,
        creatorId: UUID
    ): PageLayout {
        return pageLayoutDAO.create(
            id = UUID.randomUUID(),
            name = name,
            data = dataSerializationController.getDataAsString(data),
            defaultResources = dataSerializationController.getDataAsString(defaultResources),
            layoutType = layoutType,
            thumbnailUrl = thumbnailUrl,
            deviceModel = deviceModel,
            screenOrientation = screenOrientation,
            creatorId = creatorId,
            lastModifierId = creatorId
        )
    }

    /**
     * Finds an exhibition page layout by id
     *
     * @param id exhibition page layout id
     * @return found exhibition page layout or null if not found
     */
    fun findPageLayoutById(id: UUID): PageLayout? {
        return pageLayoutDAO.findById(id)
    }

    /**
     * List of exhibition page layouts by device model id and screen orientation
     *
     * @param deviceModel device model
     * @param screenOrientation screen orientation
     * @return list of exhibition page layouts
     */
    fun listPageLayouts(
        deviceModel: DeviceModel?,
        screenOrientation: ScreenOrientation?
    ): List<PageLayout> {
        return pageLayoutDAO.list(deviceModel, screenOrientation)
    }

    /**
     * Lists layouts used in given device
     *
     * @param exhibitionDevice device
     * @return layouts used in the device
     */
    fun listPageLayoutsForDevice(
        exhibitionDevice: ExhibitionDevice
    ): List<PageLayout> {
        return pageLayoutDAO.listByDevice(
            exhibitionDevice = exhibitionDevice
        )
    }

    /**
     * Updates an exhibition page layout
     *
     * @param pageLayout exhibition page layout to be updated
     * @param name name
     * @param data data
     * @param defaultResources default page resources
     * @param thumbnailUrl thumbnail URL
     * @param screenOrientation screen orientation
     * @param modifierId modifying user id
     * @return updated exhibition
     */
    fun updatePageLayout(
        pageLayout: PageLayout,
        name: String,
        data: Any,
        defaultResources: List<ExhibitionPageResource>?,
        thumbnailUrl: String?,
        deviceModel: DeviceModel,
        screenOrientation: ScreenOrientation,
        modifierId: UUID
    ): PageLayout {
        pageLayoutDAO.updateName(pageLayout, name, modifierId)
        pageLayoutDAO.updateData(pageLayout, dataSerializationController.getDataAsString(data), modifierId)
        pageLayoutDAO.updateDefaultResources(pageLayout, dataSerializationController.getDataAsString(defaultResources), modifierId)
        pageLayoutDAO.updateThumbnailUrl(pageLayout, thumbnailUrl, modifierId)
        pageLayoutDAO.updateDeviceModel(pageLayout, deviceModel, modifierId)
        pageLayoutDAO.updateScreenOrientation(pageLayout, screenOrientation, modifierId)
        return pageLayout
    }

    /**
     * Deletes an exhibition page layout
     *
     * @param pageLayout exhibition page layout to be deleted
     */
    fun deletePageLayout(pageLayout: PageLayout) {
        return pageLayoutDAO.delete(pageLayout)
    }

    /**
     * Returns HTML layout resource name map.
     *
     * Map contains resource id as key and component name and property pair as value.
     *
     * Property can be one of the following:
     * - #text (element text content)
     * - style-<style name> (element style)
     *
     * @param layout layout
     * @return HTML layout resource name map
     */
    fun getHtmlLayoutResourceNameMap(layout: PageLayout?): Map<String, Pair<String, String>> {
        val result = mutableMapOf<String, Pair<String, String>>()
        val layoutDocument = getParsedHtmlLayoutDocument(layout) ?: return result

        layoutDocument.getElementsByAttribute("data-component-type").forEach { componentElement ->
            val componentName = componentElement.attr("name")

            val styles = componentElement.attr("style")
                .split(";")
                .associate { style ->
                    val styleParts = style.split(":")
                    styleParts[0].trim() to styleParts[1].trim()
                }

            styles.forEach() { (key, value) ->
                if (value.startsWith("@resources/")) {
                    val resourceId = value.substringAfter("@resources/").trim()
                    result[resourceId] = Pair(componentName, "style-$key")
                }
            }

            val text = componentElement.text()
            if (text.startsWith("@resources/")) {
                val resourceId = text.substringAfter("@resources/").trim()
                result[resourceId] = Pair(componentName, "#text")
            }
        }

        return result
    }

    /**
     * Returns parsed HTML layout document
     *
     * @param layout layout
     * @return parsed HTML layout document
     */
    private fun getParsedHtmlLayoutDocument(layout: PageLayout?): org.jsoup.nodes.Document? {
        layout ?: return null
        if (layout.layoutType != LayoutType.HTML) return null

        val htmlLayout = dataSerializationController.getStringDataAsRestObject(layout.data, layout.layoutType) as PageLayoutViewHtml
        val html = htmlLayout.html

        return Jsoup.parse(html)
    }

}