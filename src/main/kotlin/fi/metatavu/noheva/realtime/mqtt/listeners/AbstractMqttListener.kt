package fi.metatavu.noheva.realtime.mqtt.listeners

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.slf4j.Logger
import javax.inject.Inject
import javax.transaction.Transactional

/**
 * Abstract base class for MQTT Listeners
 *
 * @param T type of the message
 * @param targetClass target class for deserialization
 */
abstract class AbstractMqttListener<T>(private val targetClass: Class<T>) {

    abstract val topic: String

    @Inject
    open lateinit var logger: Logger

    @ConfigProperty(name = "mqtt.topic")
    lateinit var baseTopic: String

    /**
     * Message handler
     *
     * @param message message
     */
    abstract fun onMessage(message: T)

    /**
     * Deserializes and processes MQTT message
     *
     * @param message message
     */
    @Transactional
    fun handleMessage(message: MqttMessage?) {
        if (message?.payload == null) return
        try {
            onMessage(jacksonObjectMapper().readValue(message.payload, targetClass))
        } catch (e: Exception) {
            logger.error("Failed to handle MQTT message", e)
        }
    }

    /**
     * Sets listener to MQTT client
     */
    fun setListener(addListener: (key: String, value: AbstractMqttListener<T>) -> Unit) {
        addListener(topic, this)
    }
}