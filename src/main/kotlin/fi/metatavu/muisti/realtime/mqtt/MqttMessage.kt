package fi.metatavu.muisti.realtime.mqtt

import javax.enterprise.event.TransactionPhase

/**
 * Data class used for contain MQTT message data
 *
 * @param subtopic message target subtopic
 * @param data message data as byte array
 * @param transactionPhase transaction phase when the message should be sent
 */
data class MqttMessage (
    val subtopic: String,
    val data: ByteArray,
    val transactionPhase: TransactionPhase
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MqttMessage

        if (subtopic != other.subtopic) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = subtopic.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }

}