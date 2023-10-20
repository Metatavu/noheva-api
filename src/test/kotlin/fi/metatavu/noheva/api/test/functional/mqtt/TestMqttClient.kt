package fi.metatavu.noheva.api.test.functional.mqtt

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fi.metatavu.noheva.realtime.mqtt.MqttConnection
import fi.metatavu.noheva.settings.MqttSettings
import org.eclipse.microprofile.config.ConfigProvider
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.util.*
import javax.enterprise.event.TransactionPhase

/**
 * MQTT client for functional tests
 */
class TestMqttClient : MqttCallback, AutoCloseable {

    private val subscriptions = mutableMapOf<String, MutableList<TestMqttSubscription<*>>>()

    /**
     * Constructor. Connects to MQTT server
     */
    init {
        MqttConnection.connect(MqttSettings(
            publisherId = UUID.randomUUID().toString(),
            serverUrl = ConfigProvider.getConfig().getValue("mqtt.server.url", String::class.java),
            topic = ConfigProvider.getConfig().getValue("mqtt.topic", String::class.java),
            username = null,
            password = null
        ))

        MqttConnection.setSubscriber(this)
    }

    /**
     * Publishes given message to given topic
     *
     * @param message message
     * @param subTopic topic
     */
    fun <T> publish(message: T, subTopic: String) {
        MqttConnection.publish(
            fi.metatavu.noheva.realtime.mqtt.MqttMessage(
                subtopic = subTopic,
                data = jacksonObjectMapper().writeValueAsBytes(message),
                transactionPhase = TransactionPhase.AFTER_COMPLETION
            )
        )
    }

    /**
     * Subscribes to a topic
     *
     * @param targetClass message class
     * @param subtopic subtopic to subscribe
     */
    fun <T> subscribe(targetClass: Class<T>, subtopic: String): TestMqttSubscription<T> {
        val mqttTopic = ConfigProvider.getConfig().getValue("mqtt.topic", String::class.java)
        val topic = "${mqttTopic}/$subtopic"
        var topicSubscriptions = subscriptions.get(topic)
        if (topicSubscriptions == null) {
            topicSubscriptions = mutableListOf()
            subscriptions[topic] = topicSubscriptions
        }

        val subscription = TestMqttSubscription(targetClass)
        topicSubscriptions.add(subscription)

        return subscription
    }

    override fun messageArrived(topic: String?, message: MqttMessage?) {
        message ?: return

        val messageBytes = message.payload
        subscriptions[topic]?.forEach{ it.addMessageBytes(messageBytes) }
    }

    override fun connectionLost(cause: Throwable?) {
        println("Connection lost")
    }

    override fun deliveryComplete(token: IMqttDeliveryToken?) {
        println("deliveryComplete")
    }

    override fun close() {
        MqttConnection.disconnect()
    }

}