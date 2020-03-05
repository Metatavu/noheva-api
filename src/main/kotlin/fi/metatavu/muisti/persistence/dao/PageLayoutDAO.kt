package fi.metatavu.muisti.persistence.dao

import fi.metatavu.muisti.persistence.model.PageLayout
import java.util.*
import javax.enterprise.context.ApplicationScoped

/**
 * DAO class for page layout
 *
 * @author Antti Leppä
 */
@ApplicationScoped
class PageLayoutDAO() : AbstractDAO<PageLayout>() {

    /**
     * Creates new PageLayout
     *
     * @param id id
     * @param name name
     * @param data data
     * @param thumbnailUrl thumbnail URL
     * @param creatorId creator's id
     * @param lastModifierId last modifier's id
     * @return created pageLayout
     */
    fun create(id: UUID, name: String, data: String, thumbnailUrl: String?, creatorId: UUID, lastModifierId: UUID): PageLayout {
        val pageLayout = PageLayout()
        pageLayout.id = id
        pageLayout.name = name
        pageLayout.data = data
        pageLayout.thumbnailUrl = thumbnailUrl
        pageLayout.creatorId = creatorId
        pageLayout.lastModifierId = lastModifierId
        return persist(pageLayout)
    }

    /**
     * Updates name
     *
     * @param name name
     * @param lastModifierId last modifier's id
     * @return updated pageLayout
     */
    fun updateName(pageLayout: PageLayout, name: String, lastModifierId: UUID): PageLayout {
        pageLayout.lastModifierId = lastModifierId
        pageLayout.name = name
        return persist(pageLayout)
    }

    /**
     * Updates data
     *
     * @param data data
     * @param lastModifierId last modifier's id
     * @return updated pageLayout
     */
    fun updateData(pageLayout: PageLayout, data: String, lastModifierId: UUID): PageLayout {
        pageLayout.lastModifierId = lastModifierId
        pageLayout.data = data
        return persist(pageLayout)
    }

    /**
     * Updates thumbnailUrl
     *
     * @param thumbnailUrl thumbnail URL
     * @param lastModifierId last modifier's id
     * @return updated pageLayout
     */
    fun updateThumbnailUrl(pageLayout: PageLayout, thumbnailUrl: String?, lastModifierId: UUID): PageLayout {
        pageLayout.lastModifierId = lastModifierId
        pageLayout.thumbnailUrl = thumbnailUrl
        return persist(pageLayout)
    }

}