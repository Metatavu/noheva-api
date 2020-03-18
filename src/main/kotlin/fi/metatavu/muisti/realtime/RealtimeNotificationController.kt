package fi.metatavu.muisti.realtime

import com.fasterxml.jackson.databind.ObjectMapper
import fi.metatavu.muisti.api.spec.model.MqttExhibitionPageCreate
import fi.metatavu.muisti.api.spec.model.MqttExhibitionPageDelete
import fi.metatavu.muisti.api.spec.model.MqttExhibitionPageUpdate
import fi.metatavu.muisti.realtime.mqtt.MqttController
import fi.metatavu.muisti.realtime.mqtt.MqttMessage
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.event.TransactionPhase
import javax.inject.Inject

/**
 * Realtime notification controller
 */
@ApplicationScoped
class RealtimeNotificationController {

    @Inject
    private lateinit var mqttController: MqttController

    /**
     * Notify subscribers about new page creation
     *
     * @param exhibitionId exhibition id
     * @param id page id
     */
    fun notifyExhibitionPageCreate(exhibitionId: UUID, id: UUID) {
        val mqttMessage = MqttExhibitionPageCreate()
        mqttMessage.exhibitionId = exhibitionId
        mqttMessage.id = id
        publishMqttTransactionSuccess("pages/create", mqttMessage)
    }

    /**
     * Notify subscribers about page update
     *
     * @param exhibitionId exhibition id
     * @param id page id
     */
    fun notifyExhibitionPageUpdate(exhibitionId: UUID, id: UUID) {
        val mqttMessage = MqttExhibitionPageUpdate()
        mqttMessage.exhibitionId = exhibitionId
        mqttMessage.id = id
        publishMqttTransactionSuccess("pages/update", mqttMessage)
    }

    /**
     * Notify subscribers about page deletion
     *
     * @param exhibitionId exhibition id
     * @param id page id
     */
    fun notifyExhibitionPageDelete(exhibitionId: UUID, id: UUID) {
        val mqttMessage = MqttExhibitionPageDelete()
        mqttMessage.exhibitionId = exhibitionId
        mqttMessage.id = id
        publishMqttTransactionSuccess("pages/delete", mqttMessage)
    }

    /**
     * Enqueues MQTT message to be published when transaction ends successfully
     *
     * @param subtopic message subtopic
     * @param message message
     */
    private fun publishMqttTransactionSuccess(subtopic: String, message: Any) {
        val objectMapper = ObjectMapper()
        mqttController.publish(MqttMessage(subtopic = subtopic, data = objectMapper.writeValueAsBytes(message), transactionPhase = TransactionPhase.AFTER_SUCCESS))
    }

}