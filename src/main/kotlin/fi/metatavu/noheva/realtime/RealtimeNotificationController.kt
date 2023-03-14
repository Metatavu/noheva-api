package fi.metatavu.noheva.realtime

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fi.metatavu.noheva.api.spec.model.*
import fi.metatavu.noheva.realtime.mqtt.MqttController
import fi.metatavu.noheva.realtime.mqtt.MqttMessage
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
    lateinit var mqttController: MqttController

    /**
     * Notify subscribers about new page creation
     *
     * @param exhibitionId exhibition id
     * @param id page id
     */
    fun notifyExhibitionPageCreate(exhibitionId: UUID, id: UUID) {
        val mqttMessage = MqttExhibitionPageCreate(
            exhibitionId = exhibitionId,
            id = id
        )
        publishMqttTransactionSuccess("pages/create", mqttMessage)
    }

    /**
     * Notify subscribers about page update
     *
     * @param exhibitionId exhibition id
     * @param id page id
     */
    fun notifyExhibitionPageUpdate(exhibitionId: UUID, id: UUID) {
        val mqttMessage = MqttExhibitionPageUpdate(
            exhibitionId = exhibitionId,
            id = id
        )
        publishMqttTransactionSuccess("pages/update", mqttMessage)
    }

    /**
     * Notify subscribers about page deletion
     *
     * @param exhibitionId exhibition id
     * @param id page id
     */
    fun notifyExhibitionPageDelete(exhibitionId: UUID, id: UUID) {
        val mqttMessage = MqttExhibitionPageDelete(
            exhibitionId = exhibitionId,
            id = id
        )
        publishMqttTransactionSuccess("pages/delete", mqttMessage)
    }

    /**
     * Notify subscribers about new visitor session creation
     *
     * @param exhibitionId exhibition id
     * @param id page id
     */
    fun notifyExhibitionVisitorSessionCreate(exhibitionId: UUID, id: UUID) {
        val mqttMessage = MqttExhibitionVisitorSessionCreate(
            exhibitionId = exhibitionId,
            id = id
        )
        publishMqttTransactionSuccess("visitorsessions/create", mqttMessage)
    }

    /**
     * Notify subscribers about visitor session update
     *
     * @param exhibitionId exhibition id
     * @param id page id
     * @param variablesChanged variables changed
     * @param visitorsChanged visitors changed
     */
    fun notifyExhibitionVisitorSessionUpdate(
        exhibitionId: UUID,
        id: UUID,
        variablesChanged: Boolean,
        visitorsChanged: Boolean
    ) {
        val mqttMessage = MqttExhibitionVisitorSessionUpdate(
            exhibitionId = exhibitionId,
            id = id,
            variablesChanged = variablesChanged,
            visitorsChanged = visitorsChanged
        )
        publishMqttTransactionSuccess("visitorsessions/update", mqttMessage)
    }

    /**
     * Notify subscribers about visitor session deletion
     *
     * @param exhibitionId exhibition id
     * @param id page id
     */
    fun notifyExhibitionVisitorSessionDelete(exhibitionId: UUID, id: UUID) {
        val mqttMessage = MqttExhibitionVisitorSessionDelete(
            exhibitionId = exhibitionId,
            id = id
        )
        publishMqttTransactionSuccess("visitorsessions/delete", mqttMessage)
    }

    /**
     * Notify subscribers about new visitor creation
     *
     * @param exhibitionId exhibition id
     * @param id page id
     */
    fun notifyVisitorCreate(exhibitionId: UUID, id: UUID) {
        val mqttMessage = MqttVisitorCreate(
            exhibitionId = exhibitionId,
            id = id
        )
        publishMqttTransactionSuccess("visitors/create", mqttMessage)
    }

    /**
     * Notify subscribers about visitor update
     *
     * @param exhibitionId exhibition id
     * @param id page id
     */
    fun notifyVisitorUpdate(exhibitionId: UUID, id: UUID) {
        val mqttMessage = MqttVisitorUpdate(
            exhibitionId = exhibitionId,
            id = id
        )
        publishMqttTransactionSuccess("visitors/update", mqttMessage)
    }

    /**
     * Notify subscribers about visitor deletion
     *
     * @param exhibitionId exhibition id
     * @param id page id
     */
    fun notifyVisitorDelete(exhibitionId: UUID, id: UUID) {
        val mqttMessage = MqttVisitorDelete(
            exhibitionId = exhibitionId,
            id = id
        )
        publishMqttTransactionSuccess("visitors/delete", mqttMessage)
    }

    /**
     * Notify subscribers about new device group creation
     *
     * @param exhibitionId exhibition id
     * @param id device group id
     */
    fun notifyDeviceGroupCreate(exhibitionId: UUID, id: UUID) {
        val mqttMessage = MqttDeviceGroupCreate(
            exhibitionId = exhibitionId,
            id = id
        )
        publishMqttTransactionSuccess("devicegroups/create", mqttMessage)
    }

    /**
     * Notify subscribers about device group update
     *
     * @param exhibitionId exhibition id
     * @param id device group id
     */
    fun notifyDeviceGroupUpdate(exhibitionId: UUID, id: UUID) {
        val mqttMessage = MqttDeviceGroupUpdate(
            exhibitionId = exhibitionId,
            id = id
        )
        publishMqttTransactionSuccess("devicegroups/update", mqttMessage)
    }

    /**
     * Notify subscribers about device group deletion
     *
     * @param exhibitionId exhibition id
     * @param id device group id
     */
    fun notifyDeviceGroupDelete(exhibitionId: UUID, id: UUID) {
        val mqttMessage = MqttDeviceGroupDelete(
            exhibitionId = exhibitionId,
            id = id
        )
        publishMqttTransactionSuccess("devicegroups/delete", mqttMessage)
    }

    /**
     * Notify subscribers about new RFID antenna creation
     *
     * @param exhibitionId exhibition id
     * @param id antenna id
     */
    fun notifyRfidAntennaCreate(exhibitionId: UUID, id: UUID) {
        val mqttMessage = MqttRfidAntennaCreate(
            exhibitionId = exhibitionId,
            id = id
        )
        publishMqttTransactionSuccess("rfidantennas/create", mqttMessage)
    }

    /**
     * Notify subscribers about RFID antenna update
     *
     * @param exhibitionId exhibition id
     * @param id antenna id
     * @param groupChanged whether antenna device group has changed
     */
    fun notifyRfidAntennaUpdate(exhibitionId: UUID, id: UUID, groupChanged: Boolean) {
        val mqttMessage = MqttRfidAntennaUpdate(
            exhibitionId = exhibitionId,
            id = id,
            groupChanged = groupChanged
        )
        publishMqttTransactionSuccess("rfidantennas/update", mqttMessage)
    }

    /**
     * Notify subscribers about RFID antenna deletion
     *
     * @param exhibitionId exhibition id
     * @param id antenna id
     */
    fun notifyRfidAntennaDelete(exhibitionId: UUID, id: UUID) {
        val mqttMessage = MqttRfidAntennaDelete(
            exhibitionId = exhibitionId,
            id = id
        )
        publishMqttTransactionSuccess("rfidantennas/delete", mqttMessage)
    }

    /**
     * Notify subscribers about new device creation
     *
     * @param exhibitionId exhibition id
     * @param id device id
     */
    fun notifyDeviceCreate(exhibitionId: UUID, id: UUID) {
        val mqttMessage = MqttDeviceCreate(
            exhibitionId = exhibitionId,
            id = id
        )
        publishMqttTransactionSuccess("devices/create", mqttMessage)
    }

    /**
     * Notify subscribers about device update
     *
     * @param exhibitionId exhibition id
     * @param id device id
     * @param groupChanged whether antenna device group has changed
     */
    fun notifyDeviceUpdate(exhibitionId: UUID, id: UUID, groupChanged: Boolean) {
        val mqttMessage = MqttDeviceUpdate(
            exhibitionId = exhibitionId,
            id = id,
            groupChanged = groupChanged
        )
        publishMqttTransactionSuccess("devices/update", mqttMessage)
    }

    /**
     * Notify subscribers about device deletion
     *
     * @param exhibitionId exhibition id
     * @param id device id
     */
    fun notifyDeviceDelete(exhibitionId: UUID, id: UUID) {
        val mqttMessage = MqttDeviceDelete(
            exhibitionId = exhibitionId,
            id = id
        )
        publishMqttTransactionSuccess("devices/delete", mqttMessage)
    }

    /**
     * Enqueues MQTT message to be published when transaction ends successfully
     *
     * @param subtopic message subtopic
     * @param message message
     */
    private fun publishMqttTransactionSuccess(subtopic: String, message: Any) {
        mqttController.publish(
            MqttMessage(
                subtopic = subtopic,
                data = jacksonObjectMapper().writeValueAsBytes(message),
                transactionPhase = TransactionPhase.AFTER_SUCCESS
            )
        )
    }

}
