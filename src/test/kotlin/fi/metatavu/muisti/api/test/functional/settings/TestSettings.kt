package fi.metatavu.muisti.api.test.functional.settings

/**
 * Utility class for retrieving functional test settings
 *
 * @author Antti Leppä
 */
object TestSettings {


    val mqttServerUrl: String
        get() = "localhost"

    val mqttTopic: String
        get() = "test"

}