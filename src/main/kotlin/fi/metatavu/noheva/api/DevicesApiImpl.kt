package fi.metatavu.noheva.api

import fi.metatavu.noheva.api.spec.DevicesApi
import fi.metatavu.noheva.api.spec.model.Device
import fi.metatavu.noheva.api.spec.model.DeviceApprovalStatus
import fi.metatavu.noheva.api.spec.model.DeviceRequest
import fi.metatavu.noheva.api.spec.model.DeviceStatus
import fi.metatavu.noheva.api.translate.DeviceTranslator
import fi.metatavu.noheva.devices.DeviceController
import java.util.*
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.transaction.Transactional
import javax.ws.rs.core.Response

/**
 * Devices API implementation
 */
@RequestScoped
@Transactional
@Suppress("UNUSED")
class DevicesApiImpl: DevicesApi, AbstractApi() {

    @Inject
    lateinit var deviceController: DeviceController

    @Inject
    lateinit var deviceTranslator: DeviceTranslator

    override fun createDevice(deviceRequest: DeviceRequest): Response {
        val existingDevice = deviceController.findDevice(serialNumber = deviceRequest.serialNumber)

        if (existingDevice != null && existingDevice.approvalStatus != DeviceApprovalStatus.PENDING_REAPPROVAL) {
            return createConflict("Device with serial number $deviceRequest.serialNumber already exists.")
        }

        val createdDevice = deviceController.createDevice(
            serialNumber = deviceRequest.serialNumber,
            name = deviceRequest.name,
            description = deviceRequest.description,
            version = deviceRequest.version
        )

        return createCreated(deviceTranslator.translate(createdDevice))
    }

    override fun deleteDevice(deviceId: UUID): Response {
        loggedUserId ?: return createUnauthorized("Unauthorized")

        val device = deviceController.findDevice(deviceId) ?: return createNotFound("Device $deviceId not found")

        deviceController.deleteDevice(device)

        return createNoContent()
    }

    override fun findDevice(deviceId: UUID): Response {
        loggedUserId ?: return createUnauthorized("Unauthorized")

        val foundDevice = deviceController.findDevice(deviceId) ?: return createNotFound("Device $deviceId not found")

        return createOk(deviceTranslator.translate(foundDevice))
    }

    override fun getDeviceKey(deviceId: UUID): Response {
        TODO("Not yet implemented")
    }

    override fun listDevices(
        status: DeviceStatus?,
        approvalStatus: DeviceApprovalStatus?
    ): Response {
        loggedUserId ?: return createUnauthorized("Unauthorized")

        val foundDevices = deviceController.listDevices(
            status = status,
            approvalStatus = approvalStatus
        )

        return createOk(deviceTranslator.translate(foundDevices))
    }

    override fun updateDevice(deviceId: UUID, device: Device): Response {
        val userId = loggedUserId ?: return createUnauthorized("Unauthorized")

        val foundDevice = deviceController.findDevice(deviceId) ?: return createNotFound("Device $deviceId not found")

        foundDevice.approvalStatus.let {
            when (it) {
                DeviceApprovalStatus.PENDING ->
                    if (device.approvalStatus != it && device.approvalStatus != DeviceApprovalStatus.APPROVED) {
                        return createBadRequest("Cannot change approval status")
                    }
                DeviceApprovalStatus.APPROVED ->
                    if (device.approvalStatus != it && device.approvalStatus != DeviceApprovalStatus.PENDING) {
                        return createBadRequest("Cannot change approval status")
                    }
                else -> if (device.approvalStatus != it) return createBadRequest("Cannot change approval status")
            }
        }

        val updatedDevice = deviceController.updateDevice(
            existingDevice = foundDevice,
            newDevice = device,
            userId = userId
        )

        return createOk(deviceTranslator.translate(updatedDevice))
    }

}