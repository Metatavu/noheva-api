package fi.metatavu.noheva.settings

/**
 * MQTT settings
 *
 * @param serverUrls MQTT server URLs
 * @param topic MQTT server main topic
 * @param username optional username for MQTT server
 * @param password optional password for MQTT server
 * @author Antti Leppä
 */
data class MqttSettings (
    var publisherId: String,
    var serverUrls: List<String>,
    var topic: String,
    var username: String?,
    var password: String?,
)