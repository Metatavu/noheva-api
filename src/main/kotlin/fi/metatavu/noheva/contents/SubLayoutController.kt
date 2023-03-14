package fi.metatavu.noheva.contents

import com.fasterxml.jackson.databind.ObjectMapper
import fi.metatavu.noheva.api.spec.model.PageLayoutView
import fi.metatavu.noheva.persistence.dao.SubLayoutDAO
import fi.metatavu.noheva.persistence.model.SubLayout
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Controller for exhibition sub layouts
 *
 * @author Jari Nykänen
 */
@ApplicationScoped
class SubLayoutController {

    @Inject
    lateinit var subLayoutDAO: SubLayoutDAO

    /**
     * Creates new sub layout
     *
     * @param name name
     * @param data data
     * @param creatorId creating user id
     * @return created sub layout
     */
    fun createSubLayout(name: String, data: PageLayoutView, creatorId: UUID): SubLayout {
        return subLayoutDAO.create(UUID.randomUUID(), name, getDataAsString(data), creatorId, creatorId)
    }

    /**
     * Finds a sub layout by id
     *
     * @param id sub layout id
     * @return found sub layout or null if not found
     */
    fun findSubLayoutById(id: UUID): SubLayout? {
        return subLayoutDAO.findById(id)
    }

    /**
     * List of sub layouts
     *
     * @return list of sub layouts
     */
    fun listSubLayouts(): List<SubLayout> {
        return subLayoutDAO.listAll();
    }

    /**
     * Updates a sub layout
     *
     * @param subLayout sub layout to be updated
     * @param name name
     * @param data data
     * @param modifierId modifying user id
     * @return updated exhibition
     */
    fun updateSubLayout(subLayout: SubLayout, name: String, data: PageLayoutView, modifierId: UUID): SubLayout {
        subLayoutDAO.updateName(subLayout, name, modifierId)
        subLayoutDAO.updateData(subLayout, getDataAsString(data), modifierId)
        return subLayout
    }

    /**
     * Deletes a sub layout
     *
     * @param subLayout sub layout to be deleted
     */
    fun deleteSubLayout(subLayout: SubLayout) {
        return subLayoutDAO.delete(subLayout)
    }

    /**
     * Serializes the view into JSON string
     *
     * @param data view
     * @return JSON string
     */
    private fun getDataAsString(data: PageLayoutView): String {
        val objectMapper = ObjectMapper()
        return objectMapper.writeValueAsString(data)
    }

}