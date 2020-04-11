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
     * @param deviceWidth device physical width
     * @param deviceHeight device physical height
     * @param deviceDepth device physical depth
     * @param screenWidth device screen physical height
     * @param screenHeight device screen physical height
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
    fun create(id: UUID, manufacturer: String, model: String, deviceWidth: Double?, deviceHeight: Double?, deviceDepth: Double?, screenWidth: Double?, screenHeight: Double?, widthPixels: Int?, heightPixels: Int?, density: Double?, xdpi: Double?, ydpi: Double?, capabilityTouch: Boolean, creatorId: UUID, lastModifierId: UUID): DeviceModel {
        val deviceModel = DeviceModel()
        deviceModel.id = id
        deviceModel.manufacturer = manufacturer
        deviceModel.model = model
        deviceModel.deviceWidth = deviceWidth
        deviceModel.deviceHeight = deviceHeight
        deviceModel.deviceDepth = deviceDepth
        deviceModel.screenWidth = screenWidth
        deviceModel.screenHeight = screenHeight
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
     * Updates device width
     *
     * @param deviceModel exhibition device model
     * @param deviceWidth device width
     * @param lastModifierId last modifier's id
     * @return updated deviceModel
     */
    fun updateDeviceWidth(deviceModel: DeviceModel, deviceWidth: Double?, lastModifierId: UUID): DeviceModel {
        deviceModel.lastModifierId = lastModifierId
        deviceModel.deviceWidth = deviceWidth
        return persist(deviceModel)
    }

    /**
     * Updates device height
     *
     * @param deviceModel exhibition device model
     * @param deviceHeight device height
     * @param lastModifierId last modifier's id
     * @return updated deviceModel
     */
    fun updateDeviceHeight(deviceModel: DeviceModel, deviceHeight: Double?, lastModifierId: UUID): DeviceModel {
        deviceModel.lastModifierId = lastModifierId
        deviceModel.deviceHeight = deviceHeight
        return persist(deviceModel)
    }

    /**
     * Updates device depth
     *
     * @param deviceModel exhibition device model
     * @param deviceDepth device physical depth
     * @param lastModifierId last modifier's id
     * @return updated deviceModel
     */
    fun updateDeviceDepth(deviceModel: DeviceModel, deviceDepth: Double?, lastModifierId: UUID): DeviceModel {
        deviceModel.lastModifierId = lastModifierId
        deviceModel.deviceDepth = deviceDepth
        return persist(deviceModel)
    }

    /**
     * Updates screen physical width
     *
     * @param deviceModel exhibition device model
     * @param screenWidth screen physical width
     * @param lastModifierId last modifier's id
     * @return updated deviceModel
     */
    fun updateScreenWidth(deviceModel: DeviceModel, screenWidth: Double?, lastModifierId: UUID): DeviceModel {
        deviceModel.lastModifierId = lastModifierId
        deviceModel.screenWidth = screenWidth
        return persist(deviceModel)
    }

    /**
     * Updates screen physical height
     *
     * @param deviceModel exhibition device model
     * @param screenHeight screen physical height
     * @param lastModifierId last modifier's id
     * @return updated deviceModel
     */
    fun updateScreenHeight(deviceModel: DeviceModel, screenHeight: Double?, lastModifierId: UUID): DeviceModel {
        deviceModel.lastModifierId = lastModifierId
        deviceModel.screenHeight = screenHeight
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