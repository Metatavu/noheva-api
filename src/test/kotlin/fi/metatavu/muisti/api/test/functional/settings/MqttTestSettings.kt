package fi.metatavu.muisti.api.test.functional.settings

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