package fi.metatavu.noheva.realtime.mqtt

import fi.metatavu.noheva.realtime.mqtt.listeners.AbstractMqttListener
import fi.metatavu.noheva.realtime.mqtt.listeners.StatusMessageListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttTopic
import org.slf4j.Logger
import javax.annotation.PostConstruct
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MqttCallback: MqttCallback {

    @Inject
    lateinit var statusMessageListener: StatusMessageListener

    @Inject
    lateinit var logger: Logger

    private val listeners = mutableMapOf<String, AbstractMqttListener<*>>()

    @Suppress("unused")
    @PostConstruct
    fun postConstruct() {
        statusMessageListener.setListener { key, value ->
            listeners[key] = value
        }
    }

    override fun connectionLost(cause: Throwable?) {
        logger.warn("MQTT connection lost", cause)
    }

    override fun messageArrived(topic: String?, message: org.eclipse.paho.client.mqttv3.MqttMessage?) {
        topic?.let {
            listeners.forEach {
                if (MqttTopic.isMatched(it.key, topic)) {
                    it.value.handleMessage(message)
                }
            }
        }
    }

    override fun deliveryComplete(token: IMqttDeliveryToken?) {
    }

}