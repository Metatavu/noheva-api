package fi.metatavu.noheva.realtime.mqtt.listeners

import fi.metatavu.noheva.api.spec.model.MqttDeviceStatus
import fi.metatavu.noheva.devices.DeviceController
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * MQTT Listener for device status messages
 */
@Suppress("unused")
@ApplicationScoped
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