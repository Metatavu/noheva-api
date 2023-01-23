package fi.metatavu.muisti.api;

import fi.metatavu.muisti.api.spec.ExhibitionDevicesApi
import fi.metatavu.muisti.api.spec.model.ExhibitionDevice

import javax.ws.rs.core.Response


import java.util.*


class ExhibitionDeviceGroupsApiImpl: ExhibitionDevicesApi, AbstractApi() {
    /* V1 */
    override fun listExhibitionDevices(exhibitionId: UUID, exhibitionGroupId: UUID?, deviceModelId: UUID?): Response {
        TODO("Not yet implemented")
    }

    override fun createExhibitionDevice(exhibitionId: UUID, exhibitionDevice: ExhibitionDevice): Response {
        TODO("Not yet implemented")
    }

    override fun findExhibitionDevice(exhibitionId: UUID, deviceId: UUID): Response {
        TODO("Not yet implemented")
    }

    override fun updateExhibitionDevice(
        exhibitionId: UUID,
        deviceId: UUID,
        exhibitionDevice: ExhibitionDevice
    ): Response {
        TODO("Not yet implemented")
    }

    override fun deleteExhibitionDevice(exhibitionId: UUID, deviceId: UUID): Response {
        TODO("Not yet implemented")
    }
}
