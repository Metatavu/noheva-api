package fi.metatavu.muisti.persistence.dao

import fi.metatavu.muisti.persistence.model.Exhibition
import fi.metatavu.muisti.persistence.model.ExhibitionDeviceModel
import fi.metatavu.muisti.persistence.model.ExhibitionDeviceModel_
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.persistence.TypedQuery
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Root

/**
 * DAO class for ExhibitionDeviceModel
 *
 * @author Antti Leppä
 */
@ApplicationScoped
class ExhibitionDeviceModelDAO() : AbstractDAO<ExhibitionDeviceModel>() {

    /**
     * Creates new ExhibitionDeviceModel
     *
     * @param id id
     * @param exhibition exhibition
     * @param manufacturer device manufacturer
     * @param model device model
     * @param dimensionWidth device physical width
     * @param dimensionHeight device physical height
     * @param resolutionX device x-resolution
     * @param resolutionY device y-resolution
     * @param capabilityTouch whether device has touch capability
     * @param creatorId creator's id
     * @param lastModifierId last modifier's id
     * @return created exhibitionDeviceModel
     */
    fun create(id: UUID, exhibition: Exhibition, manufacturer: String, model: String, dimensionWidth: Double?, dimensionHeight: Double?, resolutionX: Double?, resolutionY: Double?, capabilityTouch: Boolean, creatorId: UUID, lastModifierId: UUID): ExhibitionDeviceModel {
        val exhibitionDeviceModel = ExhibitionDeviceModel()
        exhibitionDeviceModel.id = id
        exhibitionDeviceModel.manufacturer = manufacturer
        exhibitionDeviceModel.model = model
        exhibitionDeviceModel.dimensionWidth = dimensionWidth
        exhibitionDeviceModel.dimensionHeight = dimensionHeight
        exhibitionDeviceModel.resolutionX = resolutionX
        exhibitionDeviceModel.resolutionY = resolutionY
        exhibitionDeviceModel.capabilityTouch = capabilityTouch
        exhibitionDeviceModel.exhibition = exhibition
        exhibitionDeviceModel.creatorId = creatorId
        exhibitionDeviceModel.lastModifierId = lastModifierId
        return persist(exhibitionDeviceModel)
    }

    /**
     * Lists ExhibitionDeviceModels by exhibition
     *
     * @param exhibition exhibition
     * @return List of ExhibitionDeviceModels
     */
    fun listByExhibition(exhibition: Exhibition): List<ExhibitionDeviceModel> {
        val entityManager = getEntityManager()
        val criteriaBuilder = entityManager.criteriaBuilder
        val criteria: CriteriaQuery<ExhibitionDeviceModel> = criteriaBuilder.createQuery(ExhibitionDeviceModel::class.java)
        val root: Root<ExhibitionDeviceModel> = criteria.from(ExhibitionDeviceModel::class.java)
        criteria.select(root)
        criteria.where(criteriaBuilder.equal(root.get(ExhibitionDeviceModel_.exhibition), exhibition))
        val query: TypedQuery<ExhibitionDeviceModel> = entityManager.createQuery<ExhibitionDeviceModel>(criteria)
        return query.getResultList()
    }

    /**
     * Updates manufacturer
     *
     * @param manufacturer manufacturer
     * @param lastModifierId last modifier's id
     * @return updated exhibitionDeviceModel
     */
    fun updateManufacturer(exhibitionDeviceModel: ExhibitionDeviceModel, manufacturer: String, lastModifierId: UUID): ExhibitionDeviceModel {
        exhibitionDeviceModel.lastModifierId = lastModifierId
        exhibitionDeviceModel.manufacturer = manufacturer
        return persist(exhibitionDeviceModel)
    }

    /**
     * Updates model
     *
     * @param model model
     * @param lastModifierId last modifier's id
     * @return updated exhibitionDeviceModel
     */
    fun updateModel(exhibitionDeviceModel: ExhibitionDeviceModel, model: String, lastModifierId: UUID): ExhibitionDeviceModel {
        exhibitionDeviceModel.lastModifierId = lastModifierId
        exhibitionDeviceModel.model = model
        return persist(exhibitionDeviceModel)
    }

    /**
     * Updates dimension width
     *
     * @param dimensionWidth dimensionWidth
     * @param lastModifierId last modifier's id
     * @return updated exhibitionDeviceModel
     */
    fun updateDimensionWidth(exhibitionDeviceModel: ExhibitionDeviceModel, dimensionWidth: Double?, lastModifierId: UUID): ExhibitionDeviceModel {
        exhibitionDeviceModel.lastModifierId = lastModifierId
        exhibitionDeviceModel.dimensionWidth = dimensionWidth
        return persist(exhibitionDeviceModel)
    }

    /**
     * Updates dimension height
     *
     * @param dimensionHeight dimensionHeight
     * @param lastModifierId last modifier's id
     * @return updated exhibitionDeviceModel
     */
    fun updateDimensionHeight(exhibitionDeviceModel: ExhibitionDeviceModel, dimensionHeight: Double?, lastModifierId: UUID): ExhibitionDeviceModel {
        exhibitionDeviceModel.lastModifierId = lastModifierId
        exhibitionDeviceModel.dimensionHeight = dimensionHeight
        return persist(exhibitionDeviceModel)
    }

    /**
     * Updates resolution x
     *
     * @param resolutionX resolutionX
     * @param lastModifierId last modifier's id
     * @return updated exhibitionDeviceModel
     */
    fun updateResolutionX(exhibitionDeviceModel: ExhibitionDeviceModel, resolutionX: Double?, lastModifierId: UUID): ExhibitionDeviceModel {
        exhibitionDeviceModel.lastModifierId = lastModifierId
        exhibitionDeviceModel.resolutionX = resolutionX
        return persist(exhibitionDeviceModel)
    }

    /**
     * Updates resolution y
     *
     * @param resolutionY resolutionY
     * @param lastModifierId last modifier's id
     * @return updated exhibitionDeviceModel
     */
    fun updateResolutionY(exhibitionDeviceModel: ExhibitionDeviceModel, resolutionY: Double?, lastModifierId: UUID): ExhibitionDeviceModel {
        exhibitionDeviceModel.lastModifierId = lastModifierId
        exhibitionDeviceModel.resolutionY = resolutionY
        return persist(exhibitionDeviceModel)
    }

    /**
     * Updates capability touch
     *
     * @param capabilityTouch capabilityTouch
     * @param lastModifierId last modifier's id
     * @return updated exhibitionDeviceModel
     */
    fun updateCapabilityTouch(exhibitionDeviceModel: ExhibitionDeviceModel, capabilityTouch: Boolean, lastModifierId: UUID): ExhibitionDeviceModel {
        exhibitionDeviceModel.lastModifierId = lastModifierId
        exhibitionDeviceModel.capabilityTouch = capabilityTouch
        return persist(exhibitionDeviceModel)
    }

}