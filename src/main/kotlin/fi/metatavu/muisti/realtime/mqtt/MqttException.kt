package fi.metatavu.muisti.realtime.mqtt

/**
 * Exception for MQTT operations
 *
 * @author Antti Leppä
 */
class MqttException : Exception {

    constructor(cause: Exception?) : super(cause) {}
    constructor(message: String?) : super(message) {}

}