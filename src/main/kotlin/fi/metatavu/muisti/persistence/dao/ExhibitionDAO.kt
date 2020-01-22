package fi.metatavu.muisti.persistence.dao

import fi.metatavu.muisti.persistence.model.Exhibition
import java.util.*
import javax.enterprise.context.ApplicationScoped

/**
 * DAO class for Exhibition
 *
 * @author Antti Leppä
 */
@ApplicationScoped
open class ExhibitionDAO() : AbstractDAO<Exhibition>() {

    /**
     * Creates new Exhibition
     *
     * @param id id
     * @param name name
     * @param creatorId creator's id
     * @param lastModifierId last modifier's id
     * @return created exhibition
     */
    open fun create(id: UUID, name: String, creatorId: UUID, lastModifierId: UUID): Exhibition {
        val exhibition = Exhibition()
        exhibition.name = name
        exhibition.id = id
        exhibition.creatorId = creatorId
        exhibition.lastModifierId = lastModifierId
        return persist(exhibition)
    }

    /**
     * Updates name
     *
     * @param name name
     * @param lastModifierId last modifier's id
     * @return updated exhibition
     */
    open fun updateName(exhibition: Exhibition, name: String, lastModifierId: UUID): Exhibition {
        exhibition.lastModifierId = lastModifierId
        exhibition.name = name
        return persist(exhibition)
    }

}