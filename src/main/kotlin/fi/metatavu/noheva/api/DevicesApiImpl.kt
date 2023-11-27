package fi.metatavu.noheva.api

import fi.metatavu.noheva.api.spec.DevicesApi
import fi.metatavu.noheva.api.spec.model.*
import fi.metatavu.noheva.api.translate.DeviceTranslator
import fi.metatavu.noheva.devices.DeviceModelController
import fi.metatavu.noheva.devices.ExhibitionDeviceController
import fi.metatavu.noheva.persistence.model.DeviceModel
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
    lateinit var deviceTranslator: DeviceTranslator

    @Inject
    lateinit var deviceModelController: DeviceModelController

    @Inject
    lateinit var exhibitionDeviceController: ExhibitionDeviceController

    override fun createDevice(deviceRequest: DeviceRequest): Response {
        val existingDevice = deviceController.findDevice(serialNumber = deviceRequest.serialNumber)


        existingDevice?.let {
            if (it.approvalStatus != DeviceApprovalStatus.PENDING_REAPPROVAL) {
                return createConflict("Device with serial number $deviceRequest.serialNumber already exists.")
            }

            if (it.approvalStatus == DeviceApprovalStatus.PENDING_REAPPROVAL) {
                val updatedDevice = deviceController.reInitiateDeviceApproval(
                    device = it,
                    name = deviceRequest.name,
                    description = deviceRequest.description,
                    version = deviceRequest.version
                )

                return createCreated(deviceTranslator.translate(updatedDevice))
            }
        }

        val createdDevice = deviceController.createDevice(
            serialNumber = deviceRequest.serialNumber,
            name = deviceRequest.name,
            deviceType = deviceRequest.deviceType,
            description = deviceRequest.description,
            version = deviceRequest.version
        )

        return createCreated(deviceTranslator.translate(createdDevice))
    }

    override fun deleteDevice(deviceId: UUID): Response {
        loggedUserId ?: return createUnauthorized("Unauthorized")

        val device = deviceController.findDevice(id = deviceId)
            ?: return createNotFound("Device $deviceId not found")

        val foundExhibitionDevices = exhibitionDeviceController.listByDevice(device = device)

        if (foundExhibitionDevices.isNotEmpty()) {
            val exhibitionDeviceIds = foundExhibitionDevices.map { it.id }.joinToString()
            return createBadRequest("Cannot delete device $deviceId because it's in use by exhibition devices $exhibitionDeviceIds")
        }

        deviceController.deleteDevice(device)

        return createNoContent()
    }

    override fun findDevice(deviceId: UUID): Response {
        loggedUserId ?: return createUnauthorized("Unauthorized")

        val foundDevice = deviceController.findDevice(id = deviceId)
            ?: return createNotFound("Device $deviceId not found")

        return createOk(deviceTranslator.translate(foundDevice))
    }

    override fun getDeviceKey(deviceId: UUID): Response {
        val foundDevice = deviceController.findDevice(id = deviceId)
            ?: return createNotFound("Device $deviceId not found")

        if (foundDevice.approvalStatus == DeviceApprovalStatus.APPROVED) {
            val keyPair = cryptoController.generateRsaKeyPair()
                ?: return createInternalServerError("Couldn't create keypair")

            deviceController.storeDeviceKey(device = foundDevice, key = keyPair.public)

            return createOk(DeviceKey(key = cryptoController.getPrivateKeyBase64(keyPair.private)))
        }

        return createForbidden(FORBIDDEN)
    }

    override fun listDevices(status: DeviceStatus?, approvalStatus: DeviceApprovalStatus?): Response {
        loggedUserId ?: return createUnauthorized("Unauthorized")

        val foundDevices = deviceController.listDevices(status = status, approvalStatus = approvalStatus)

        return createOk(deviceTranslator.translate(foundDevices))
    }

    override fun updateDevice(deviceId: UUID, device: Device): Response {
        val userId = loggedUserId ?: return createUnauthorized("Unauthorized")

        val foundDevice = deviceController.findDevice(id = deviceId)
            ?: return createNotFound("Device $deviceId not found")

        var foundDeviceModel: DeviceModel? = null

        if (device.deviceModelId != null) {
            foundDeviceModel = deviceModelController.findDeviceModelById(device.deviceModelId)
                ?: return createBadRequest("Device model ${device.deviceModelId} not found")
        }

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
                DeviceApprovalStatus.READY ->
                    if (device.approvalStatus != it && device.approvalStatus != DeviceApprovalStatus.PENDING_REAPPROVAL) {
                        return createBadRequest("Cannot change approval status")
                    }
                else -> if (device.approvalStatus != it) return createBadRequest("Cannot change approval status")
            }
        }

        val updatedDevice = deviceController.updateDevice(
            existingDevice = foundDevice,
            newDevice = device,
            deviceModel = foundDeviceModel,
            userId = userId
        )

        return createOk(deviceTranslator.translate(updatedDevice))
    }

}