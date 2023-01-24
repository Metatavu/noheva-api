package fi.metatavu.muisti.realtime.mqtt

import fi.metatavu.muisti.settings.SettingsController
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
    lateinit var settingsController: SettingsController

    @Inject
    lateinit var messageEvent: Event<MqttMessage>

    @PostConstruct
    @Suppress("unused")
    fun postConstruct() {
        MqttConnection.connect(settingsController.getMqttSettings())
    }

    /**
     * Schedules a message to be published
     *
     * @param message message
     */
    fun publish(message: MqttMessage) {
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