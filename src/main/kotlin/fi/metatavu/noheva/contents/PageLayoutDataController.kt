package fi.metatavu.noheva.contents

import com.fasterxml.jackson.databind.ObjectMapper
import fi.metatavu.noheva.api.spec.model.LayoutType
import fi.metatavu.noheva.api.spec.model.PageLayoutView
import fi.metatavu.noheva.api.spec.model.PageLayoutViewHtml
import javax.enterprise.context.ApplicationScoped

/**
 * Translates and verifies layout data
 */
@ApplicationScoped
class PageLayoutDataController {

    val objectMapper = ObjectMapper()

    /**
     * Translates stored page layout data string into REST resources based on layout type
     *
     * @param data stored string page layout data
     * @param layoutType layout type
     * @return translated page layout data (either PageLayoutView or PageLayoutViewHtml) or null if failed to translate
     */
    fun getStringDataAsRestObject(data: String?, layoutType: LayoutType): Any? {
        return try {
            if (layoutType == LayoutType.ANDROID) {
                objectMapper.readValue(data, PageLayoutView::class.java)
            } else {
                objectMapper.readValue(data, PageLayoutViewHtml::class.java)
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Checks if the REST page layout data is valid for the given layout type
     *
     * @param dataObject page layout data
     * @param layoutType layout type
     * @return true if the data is valid for the given layout type
     */
    fun isValidLayoutType(dataObject: Any, layoutType: LayoutType): Boolean {
        getStringDataAsRestObject(getRestObjectAsString(dataObject), layoutType) ?: return false
        return true
    }

    /**
     * Translates page layout data into a string
     *
     * @param data page layout data
     * @return string representation of the page layout data
     */
    fun getRestObjectAsString(data: Any): String {
        return objectMapper.writeValueAsString(data)
    }
}