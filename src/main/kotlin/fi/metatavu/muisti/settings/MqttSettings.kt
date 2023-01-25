package fi.metatavu.muisti.settings

/**
 * MQTT settings
 *
 * @param serverUrl MQTT server URL
 * @param topic MQTT server main topic
 * @param username optional username for MQTT server
 * @param password optional password for MQTT server
 * @author Antti Leppä
 */
data class MqttSettings (
    var publisherId: String,
    var serverUrl: String,
    var topic: String,
    var username: String?,
    var password: String?,
)