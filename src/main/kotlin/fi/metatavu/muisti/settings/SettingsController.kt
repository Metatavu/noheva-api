package fi.metatavu.muisti.settings

import javax.enterprise.context.ApplicationScoped

/**
 * Controller for application settings
 */
@ApplicationScoped
class SettingsController {

    /**
     * Returns MQTT server settings
     *
     * @return MQTT server settings
     */
    fun getMqttSettings(): MqttSettings {
        return MqttSettings(
            serverUrl = System.getenv("MQTT_SERVER_URL"),
            topic = System.getenv("MQTT_TOPIC"),
            username = System.getenv("MQTT_USERNAME"),
            password = System.getenv("MQTT_PASSWORD")
        )
    }

}