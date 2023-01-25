package fi.metatavu.muisti.realtime.mqtt

import fi.metatavu.muisti.settings.MqttSettings
import fi.metatavu.muisti.settings.SettingsController
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.util.*
import javax.annotation.PostConstruct
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.event.Event
import javax.enterprise.event.Observes
import javax.enterprise.event.TransactionPhase
import javax.inject.Inject

/**
 * Controller for MQTT
 *
 * @author Antti Leppä
 */
@ApplicationScoped
class MqttController {

    @Inject
    lateinit var messageEvent: Event<MqttMessage>

    @ConfigProperty(name = "mqtt.topic")
    private lateinit var mqttTopic: String

    @ConfigProperty(name = "mqtt.server.url")
    private lateinit var serverUrl: String

    @ConfigProperty(name = "mqtt.username")
    private lateinit var username: Optional<String>

    @ConfigProperty(name = "mqtt.password")
    private lateinit var password: Optional<String>

    @PostConstruct
    @Suppress("unused")
    fun postConstruct() {
        MqttConnection.connect(MqttSettings(
            publisherId = UUID.randomUUID().toString(),
            serverUrl = serverUrl,
            topic = mqttTopic,
            username = if (username.isPresent) username.get() else null,
            password = if (password.isPresent) password.get() else null,
        ))
    }

    /**
     * Schedules a message to be published
     *
     * @param message message
     */
    fun publish(message: MqttMessage) {
        println("Firing event ${message.subtopic}")
        messageEvent.fire(message)
    }

    /**
     * Event handler for publishing mqtt messages on transaction end
     *
     * @param event event
     */
    @Suppress("unused")
    fun onMessageEvent(@Observes(during = TransactionPhase.AFTER_SUCCESS) event: MqttMessage) {
        if (event.transactionPhase == TransactionPhase.AFTER_SUCCESS) {

            MqttConnection.publish(event)
        }
    }

}