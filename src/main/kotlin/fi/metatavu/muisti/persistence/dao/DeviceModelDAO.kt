package fi.metatavu.muisti.persistence.dao

import fi.metatavu.muisti.persistence.model.DeviceModel
import java.util.*
import javax.enterprise.context.ApplicationScoped

/**
 * DAO class for DeviceModel
 *
 * @author Antti Leppä
 */
@ApplicationScoped
class DeviceModelDAO() : AbstractDAO<DeviceModel>() {

    /**
     * Creates new DeviceModel
     *
     * @param id id
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
     * @return created deviceModel
     */
    fun create(id: UUID, manufacturer: String, model: String, dimensionWidth: Double?, dimensionHeight: Double?, widthPixels: Int?, heightPixels: Int?, density: Double?, xdpi: Double?, ydpi: Double?, capabilityTouch: Boolean, creatorId: UUID, lastModifierId: UUID): DeviceModel {
        val deviceModel = DeviceModel()
        deviceModel.id = id
        deviceModel.manufacturer = manufacturer
        deviceModel.model = model
        deviceModel.dimensionWidth = dimensionWidth
        deviceModel.dimensionHeight = dimensionHeight
        deviceModel.heightPixels = heightPixels
        deviceModel.widthPixels = widthPixels
        deviceModel.density = density
        deviceModel.xdpi = xdpi
        deviceModel.ydpi = ydpi
        deviceModel.capabilityTouch = capabilityTouch
        deviceModel.creatorId = creatorId
        deviceModel.lastModifierId = lastModifierId
        return persist(deviceModel)
    }

    /**
     * Updates manufacturer
     *
     * @param deviceModel exhibition device model
     * @param manufacturer manufacturer
     * @param lastModifierId last modifier's id
     * @return updated deviceModel
     */
    fun updateManufacturer(deviceModel: DeviceModel, manufacturer: String, lastModifierId: UUID): DeviceModel {
        deviceModel.lastModifierId = lastModifierId
        deviceModel.manufacturer = manufacturer
        return persist(deviceModel)
    }

    /**
     * Updates model
     *
     * @param deviceModel exhibition device model
     * @param lastModifierId last modifier's id
     * @return updated deviceModel
     */
    fun updateModel(deviceModel: DeviceModel, model: String, lastModifierId: UUID): DeviceModel {
        deviceModel.lastModifierId = lastModifierId
        deviceModel.model = model
        return persist(deviceModel)
    }

    /**
     * Updates dimension width
     *
     * @param deviceModel exhibition device model
     * @param dimensionWidth dimensionWidth
     * @param lastModifierId last modifier's id
     * @return updated deviceModel
     */
    fun updateDimensionWidth(deviceModel: DeviceModel, dimensionWidth: Double?, lastModifierId: UUID): DeviceModel {
        deviceModel.lastModifierId = lastModifierId
        deviceModel.dimensionWidth = dimensionWidth
        return persist(deviceModel)
    }

    /**
     * Updates dimension height
     *
     * @param deviceModel exhibition device model
     * @param dimensionHeight dimensionHeight
     * @param lastModifierId last modifier's id
     * @return updated deviceModel
     */
    fun updateDimensionHeight(deviceModel: DeviceModel, dimensionHeight: Double?, lastModifierId: UUID): DeviceModel {
        deviceModel.lastModifierId = lastModifierId
        deviceModel.dimensionHeight = dimensionHeight
        return persist(deviceModel)
    }

    /**
     * Updates height pixels
     *
     * @param deviceModel exhibition device model
     * @param heightPixels heightPixels
     * @param lastModifierId last modifier's id
     * @return updated deviceModel
     */
    fun updateHeightPixels(deviceModel: DeviceModel, heightPixels: Int?, lastModifierId: UUID): DeviceModel {
        deviceModel.lastModifierId = lastModifierId
        deviceModel.heightPixels = heightPixels
        return persist(deviceModel)
    }

    /**
     * Updates width pixels
     *
     * @param deviceModel exhibition device model
     * @param widthPixels widthPixels
     * @param lastModifierId last modifier's id
     * @return updated deviceModel
     */
    fun updateWidthPixels(deviceModel: DeviceModel, widthPixels: Int?, lastModifierId: UUID): DeviceModel {
        deviceModel.lastModifierId = lastModifierId
        deviceModel.widthPixels = widthPixels
        return persist(deviceModel)
    }

    /**
     * Updates density
     *
     * @param deviceModel exhibition device model
     * @param density density
     * @param lastModifierId last modifier's id
     * @return updated deviceModel
     */
    fun updateDensity(deviceModel: DeviceModel, density: Double?, lastModifierId: UUID): DeviceModel {
        deviceModel.lastModifierId = lastModifierId
        deviceModel.density = density
        return persist(deviceModel)
    }

    /**
     * Updates xdpi
     *
     * @param deviceModel exhibition device model
     * @param xdpi xdpi
     * @param lastModifierId last modifier's id
     * @return updated deviceModel
     */
    fun updateXdpi(deviceModel: DeviceModel, xdpi: Double?, lastModifierId: UUID): DeviceModel {
        deviceModel.lastModifierId = lastModifierId
        deviceModel.xdpi = xdpi
        return persist(deviceModel)
    }

    /**
     * Updates ydpi
     *
     * @param deviceModel exhibition device model
     * @param ydpi ydpi
     * @param lastModifierId last modifier's id
     * @return updated deviceModel
     */
    fun updateYdpi(deviceModel: DeviceModel, ydpi: Double?, lastModifierId: UUID): DeviceModel {
        deviceModel.lastModifierId = lastModifierId
        deviceModel.ydpi = ydpi
        return persist(deviceModel)
    }

    /**
     * Updates capability touch
     *
     * @param deviceModel exhibition device model
     * @param capabilityTouch capabilityTouch
     * @param lastModifierId last modifier's id
     * @return updated deviceModel
     */
    fun updateCapabilityTouch(deviceModel: DeviceModel, capabilityTouch: Boolean, lastModifierId: UUID): DeviceModel {
        deviceModel.lastModifierId = lastModifierId
        deviceModel.capabilityTouch = capabilityTouch
        return persist(deviceModel)
    }

}