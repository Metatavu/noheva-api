package fi.metatavu.muisti.persistence.dao

import fi.metatavu.muisti.persistence.model.Exhibition
import fi.metatavu.muisti.persistence.model.ExhibitionContentVersion
import fi.metatavu.muisti.persistence.model.ExhibitionContentVersion_
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.persistence.TypedQuery
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Root

/**
 * DAO class for ExhibitionContentVersion
 *
 * @author Antti Leppä
 */
@ApplicationScoped
class ExhibitionContentVersionDAO() : AbstractDAO<ExhibitionContentVersion>() {

    /**
     * Creates new ExhibitionContentVersion
     *
     * @param id id
     * @param exhibition exhibition
     * @param name name
     * @param creatorId creator's id
     * @param lastModifierId last modifier's id
     * @return created exhibitionContentVersion
     */
    fun create(id: UUID, exhibition: Exhibition, name: String, creatorId: UUID, lastModifierId: UUID): ExhibitionContentVersion {
        val exhibitionContentVersion = ExhibitionContentVersion()
        exhibitionContentVersion.id = id
        exhibitionContentVersion.name = name
        exhibitionContentVersion.exhibition = exhibition
        exhibitionContentVersion.creatorId = creatorId
        exhibitionContentVersion.lastModifierId = lastModifierId
        return persist(exhibitionContentVersion)
    }

    /**
     * Lists ExhibitionContentVersions by exhibition
     *
     * @param exhibition exhibition
     * @return List of ExhibitionContentVersions
     */
    fun listByExhibition(exhibition: Exhibition): List<ExhibitionContentVersion> {
        val entityManager = getEntityManager()
        val criteriaBuilder = entityManager.criteriaBuilder
        val criteria: CriteriaQuery<ExhibitionContentVersion> = criteriaBuilder.createQuery(ExhibitionContentVersion::class.java)
        val root: Root<ExhibitionContentVersion> = criteria.from(ExhibitionContentVersion::class.java)
        criteria.select(root)
        criteria.where(criteriaBuilder.equal(root.get(ExhibitionContentVersion_.exhibition), exhibition))
        val query: TypedQuery<ExhibitionContentVersion> = entityManager.createQuery<ExhibitionContentVersion>(criteria)
        return query.getResultList()
    }

    /**
     * Updates name
     *
     * @param name name
     * @param lastModifierId last modifier's id
     * @return updated exhibitionContentVersion
     */
    fun updateName(exhibitionContentVersion: ExhibitionContentVersion, name: String, lastModifierId: UUID): ExhibitionContentVersion {
        exhibitionContentVersion.lastModifierId = lastModifierId
        exhibitionContentVersion.name = name
        return persist(exhibitionContentVersion)
    }

}