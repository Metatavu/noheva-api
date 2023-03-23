package fi.metatavu.noheva.contents

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import fi.metatavu.noheva.api.spec.model.ExhibitionPageResource
import fi.metatavu.noheva.api.spec.model.LayoutType
import fi.metatavu.noheva.api.spec.model.PageLayoutView
import fi.metatavu.noheva.api.spec.model.PageLayoutViewHtml
import javax.enterprise.context.ApplicationScoped

/**
 * Controller for various serialization tasks that use default object mapper
 */
@ApplicationScoped
class DataSerializationController {

    private val objectMapper = ObjectMapper()

    /**
     * Serializes the object into JSON string
     *
     * @param data object
     * @return JSON string
     */
    fun <T> getDataAsString(data: T): String {
        return objectMapper.writeValueAsString(data)
    }

    /**
     * Translates stored page layout data string into REST resources based on layout type
     *
     * @param data stored string page layout data
     * @param layoutType layout type
     * @return translated page layout data (either PageLayoutView or PageLayoutViewHtml) or null if failed to translate
     */
    fun getStringDataAsRestObject(data: String?, layoutType: LayoutType): Any {
        return if (layoutType == LayoutType.ANDROID) {
            objectMapper.readValue(data, PageLayoutView::class.java)
        } else {
            objectMapper.readValue(data, PageLayoutViewHtml::class.java)
        }
    }

    /**
     * Checks if the REST page layout data is valid for the given layout type
     *
     * @param dataObject page layout data
     * @param layoutType layout type
     * @return true if the data is valid for the given layout type
     */
    fun isValidDataLayoutType(dataObject: Any, layoutType: LayoutType): Boolean {
        return try {
            getStringDataAsRestObject(getDataAsString(dataObject), layoutType)
            true
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Parses resosources from string into list of objects
     *
     * @param resources resources as list
     * @return list of page resource objects
     */
    fun parseStringToPageResources(resources: String?): List<ExhibitionPageResource> {
        val objectMapper = ObjectMapper()
        resources ?: return listOf()
        return objectMapper.readValue(resources)
    }
}