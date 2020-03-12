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
     * @param widthPixels device x-resolution
     * @param heightPixels device y-resolution
     * @param density density
     * @param xdpi xdpi
     * @param ydpi ydpi
     * @param capabilityTouch whether device has touch capability
     * @param creatorId creator's id
     * @param lastModifierId last modifier's id
     * @return created exhibitionDeviceModel
     */
    fun create(id: UUID, exhibition: Exhibition, manufacturer: String, model: String, dimensionWidth: Double?, dimensionHeight: Double?, widthPixels: Int?, heightPixels: Int?, density: Double?, xdpi: Double?, ydpi: Double?, capabilityTouch: Boolean, creatorId: UUID, lastModifierId: UUID): ExhibitionDeviceModel {
        val exhibitionDeviceModel = ExhibitionDeviceModel()
        exhibitionDeviceModel.id = id
        exhibitionDeviceModel.manufacturer = manufacturer
        exhibitionDeviceModel.model = model
        exhibitionDeviceModel.dimensionWidth = dimensionWidth
        exhibitionDeviceModel.dimensionHeight = dimensionHeight
        exhibitionDeviceModel.heightPixels = heightPixels
        exhibitionDeviceModel.widthPixels = widthPixels
        exhibitionDeviceModel.density = density
        exhibitionDeviceModel.xdpi = xdpi
        exhibitionDeviceModel.ydpi = ydpi
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
     * Updates height pixels
     *
     * @param heightPixels heightPixels
     * @param lastModifierId last modifier's id
     * @return updated exhibitionDeviceModel
     */
    fun updateHeightPixels(exhibitionDeviceModel: ExhibitionDeviceModel, heightPixels: Int?, lastModifierId: UUID): ExhibitionDeviceModel {
        exhibitionDeviceModel.lastModifierId = lastModifierId
        exhibitionDeviceModel.heightPixels = heightPixels
        return persist(exhibitionDeviceModel)
    }

    /**
     * Updates width pixels
     *
     * @param widthPixels widthPixels
     * @param lastModifierId last modifier's id
     * @return updated exhibitionDeviceModel
     */
    fun updateWidthPixels(exhibitionDeviceModel: ExhibitionDeviceModel, widthPixels: Int?, lastModifierId: UUID): ExhibitionDeviceModel {
        exhibitionDeviceModel.lastModifierId = lastModifierId
        exhibitionDeviceModel.widthPixels = widthPixels
        return persist(exhibitionDeviceModel)
    }

    /**
     * Updates density
     *
     * @param density density
     * @param lastModifierId last modifier's id
     * @return updated exhibitionDeviceModel
     */
    fun updateDensity(exhibitionDeviceModel: ExhibitionDeviceModel, density: Double?, lastModifierId: UUID): ExhibitionDeviceModel {
        exhibitionDeviceModel.lastModifierId = lastModifierId
        exhibitionDeviceModel.density = density
        return persist(exhibitionDeviceModel)
    }

    /**
     * Updates xdpi
     *
     * @param xdpi xdpi
     * @param lastModifierId last modifier's id
     * @return updated exhibitionDeviceModel
     */
    fun updateXdpi(exhibitionDeviceModel: ExhibitionDeviceModel, xdpi: Double?, lastModifierId: UUID): ExhibitionDeviceModel {
        exhibitionDeviceModel.lastModifierId = lastModifierId
        exhibitionDeviceModel.xdpi = xdpi
        return persist(exhibitionDeviceModel)
    }

    /**
     * Updates ydpi
     *
     * @param ydpi ydpi
     * @param lastModifierId last modifier's id
     * @return updated exhibitionDeviceModel
     */
    fun updateYdpi(exhibitionDeviceModel: ExhibitionDeviceModel, ydpi: Double?, lastModifierId: UUID): ExhibitionDeviceModel {
        exhibitionDeviceModel.lastModifierId = lastModifierId
        exhibitionDeviceModel.ydpi = ydpi
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