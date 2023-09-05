package fi.metatavu.noheva.contents

import fi.metatavu.noheva.api.spec.model.ExhibitionPageResource
import fi.metatavu.noheva.api.spec.model.LayoutType
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

    @Inject
    lateinit var dataSerializationController: DataSerializationController

    /**
     * Creates new sub layout
     *
     * @param name name
     * @param data data
     * @param layoutType layout type
     * @param defaultResources default resources
     * @param creatorId creating user id
     * @return created sub layout
     */
    fun createSubLayout(
        name: String,
        data: Any,
        layoutType: LayoutType,
        defaultResources: List<ExhibitionPageResource>?,
        creatorId: UUID
    ): SubLayout {
        return subLayoutDAO.create(
            id = UUID.randomUUID(),
            name = name,
            data = dataSerializationController.getDataAsString(data),
            layoutType = layoutType,
            defaultResources = dataSerializationController.getDataAsString(defaultResources),
            creatorId = creatorId,
            lastModifierId = creatorId
        )
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
        return subLayoutDAO.listAll()
    }

    /**
     * Updates a sub layout
     *
     * @param subLayout sub layout to be updated
     * @param name name
     * @param data data
     * @param defaultResources default resources
     * @param modifierId modifying user id
     * @return updated exhibition
     */
    fun updateSubLayout(
        subLayout: SubLayout,
        name: String,
        data: Any,
        defaultResources: List<ExhibitionPageResource>?,
        modifierId: UUID
    ): SubLayout {
        subLayoutDAO.updateName(subLayout, name, modifierId)
        subLayoutDAO.updateData(subLayout, dataSerializationController.getDataAsString(data), modifierId)
        subLayoutDAO.updateDefaultResources(subLayout, dataSerializationController.getDataAsString(defaultResources), modifierId)
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

}