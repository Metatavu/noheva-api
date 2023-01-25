package fi.metatavu.muisti.api.test.functional.mqtt

import fi.metatavu.muisti.api.test.functional.settings.MqttTestSettings
import fi.metatavu.muisti.realtime.mqtt.MqttConnection
import fi.metatavu.muisti.settings.MqttSettings
import org.eclipse.microprofile.config.ConfigProvider
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.util.UUID

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

        println("test cluient subscribed to $topic")
        return subscription
    }

    override fun messageArrived(topic: String?, message: MqttMessage?) {
        println("Message callback on arrival $topic ${message?.payload}")
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