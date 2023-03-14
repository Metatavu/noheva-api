package fi.metatavu.noheva.api.test.functional.settings

/**
 * Test settings mqtt client
 *
 * @author Antti Leppä
 */
class MqttTestSettings {

    /**
     * Returns MQTT server URL
     */
    val mqttServerUrl: String
        get() = "localhost"

    /**
     * Returns MQTT topic
     */
    val mqttTopic: String
        get() = "test"

}