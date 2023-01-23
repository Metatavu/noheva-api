package fi.metatavu.muisti.api;

import fi.metatavu.muisti.api.spec.DeviceModelsApi
import fi.metatavu.muisti.api.spec.model.DeviceModel
import java.util.*

import javax.ws.rs.*
import javax.ws.rs.core.Response


class DeviceModelsApiImpl: DeviceModelsApi, AbstractApi() {

    /* V1 */
    override fun listDeviceModels(): Response {
        TODO("Not yet implemented")
    }

    override fun createDeviceModel(deviceModel: DeviceModel): Response {
        TODO("Not yet implemented")
    }

    override fun findDeviceModel(deviceModelId: UUID): Response {
        TODO("Not yet implemented")
    }

    override fun updateDeviceModel(deviceModelId: UUID, deviceModel: DeviceModel): Response {
        TODO("Not yet implemented")
    }

    override fun deleteDeviceModel(deviceModelId: UUID): Response {
        TODO("Not yet implemented")
    }
}
