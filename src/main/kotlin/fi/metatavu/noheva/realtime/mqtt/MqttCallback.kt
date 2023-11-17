package fi.metatavu.noheva.realtime.mqtt

import fi.metatavu.noheva.realtime.mqtt.listeners.AbstractMqttListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttTopic
import org.slf4j.Logger
import javax.enterprise.inject.Instance
import javax.inject.Inject
import javax.inject.Singleton

/**
 * MQTT Client callback
 */
@Singleton
class MqttCallback: MqttCallback {

    @Inject
    lateinit var listeners: Instance<AbstractMqttListener<*>>

    @Inject
    lateinit var logger: Logger

    override fun connectionLost(cause: Throwable?) {
        logger.warn("MQTT connection lost", cause)
    }

    override fun messageArrived(topic: String?, message: org.eclipse.paho.client.mqttv3.MqttMessage?) {
        topic?.let {
            listeners.forEach {
                if (MqttTopic.isMatched(it.topic, topic)) {
                    it.handleMessage(message)
                }
            }
        }
    }

    override fun deliveryComplete(token: IMqttDeliveryToken?) {
    }

}