package fi.metatavu.noheva.realtime.mqtt.listeners

import fi.metatavu.noheva.api.spec.model.MqttDeviceStatus
import fi.metatavu.noheva.devices.DeviceController
import javax.inject.Inject
import javax.inject.Singleton

/**
 * MQTT Listener for device status messages
 */
@Singleton
class StatusMessageListener: AbstractMqttListener<MqttDeviceStatus>(targetClass = MqttDeviceStatus::class.java) {

    override val topic: String
        get() = "$baseTopic/+/status"

    @Inject
    lateinit var deviceController: DeviceController

    /**
     * Updates device with incoming status message
     *
     * @message message
     */
    override fun onMessage(message: MqttDeviceStatus)  {
        val foundDevice = deviceController.findDevice(message.deviceId)
        if (foundDevice == null) {
            logger.warn("Received status message from unknown device: ${message.deviceId}")
            return
        }
        deviceController.handleDeviceStatusMessage(
            device = foundDevice,
            status = message.status,
            version = message.version
        )
    }
}