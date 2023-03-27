package fi.metatavu.noheva.persistence.dao

import fi.metatavu.noheva.api.spec.model.LayoutType
import fi.metatavu.noheva.persistence.model.*
import java.util.*
import javax.enterprise.context.ApplicationScoped

/**
 * DAO class for sub layout
 *
 * @author Jari Nykänen
 */
@ApplicationScoped
class SubLayoutDAO : AbstractDAO<SubLayout>() {

    /**
     * Creates new sub layout
     *
     * @param id id
     * @param name name
     * @param data data
     * @param layoutType layout type of data
     * @param defaultResources resources as string
     * @param creatorId creator's id
     * @param lastModifierId last modifier's id
     * @return created sub layout
     */
    fun create(
        id: UUID,
        name: String,
        data: String,
        layoutType: LayoutType,
        defaultResources: String?,
        creatorId: UUID,
        lastModifierId: UUID
    ): SubLayout {
        val subLayout = SubLayout()
        subLayout.id = id
        subLayout.name = name
        subLayout.data = data
        subLayout.layoutType = layoutType
        subLayout.defaultResources = defaultResources
        subLayout.creatorId = creatorId
        subLayout.lastModifierId = lastModifierId
        return persist(subLayout)
    }

    /**
     * Updates name
     *
     * @param name name
     * @param lastModifierId last modifier's id
     * @return updated sub layout
     */
    fun updateName(subLayout: SubLayout, name: String, lastModifierId: UUID): SubLayout {
        subLayout.lastModifierId = lastModifierId
        subLayout.name = name
        return persist(subLayout)
    }

    /**
     * Updates data
     *
     * @param data data
     * @param lastModifierId last modifier's id
     * @return updated sub layout
     */
    fun updateData(subLayout: SubLayout, data: String, lastModifierId: UUID): SubLayout {
        subLayout.lastModifierId = lastModifierId
        subLayout.data = data
        return persist(subLayout)
    }

    /**
     * Updates default resources
     *
     * @param subLayout layout to update
     * @param defaultResources default resources
     * @param lastModifierId last modifier's id
     * @return updated sub layout
     */
    fun updateDefaultResources(subLayout: SubLayout, defaultResources: String?, lastModifierId: UUID): SubLayout {
        subLayout.lastModifierId = lastModifierId
        subLayout.defaultResources = defaultResources
        return persist(subLayout)
    }
}