package fi.metatavu.muisti.api.test.functional.mqtt


import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fi.metatavu.muisti.api.client.models.StoredFile
import okio.Buffer
import org.awaitility.Awaitility.await
import java.util.concurrent.TimeUnit.MINUTES

/**
 * MQTT subscription class for functional tests
 *
 * @param targetClass message class
 * @param T message type
 */
class TestMqttSubscription <T>(private val targetClass: Class<T>) {

    private val messages: MutableList<T> = mutableListOf()

    /**
     * Adds received message
     *
     * @param bytes message bytes
     */
    fun addMessageBytes(bytes: ByteArray) {
        val buffer = Buffer()
        buffer.write(bytes)
        val message: T? = jacksonObjectMapper().readValue(buffer.readByteArray(), object: TypeReference<T>() {})
        if (message != null) {
            if (!messages.contains(message)) {
                messages.add(message)
            }
        }
    }

    /**
     * Returns received messages
     *
     * @return received messages
     */
    fun getMessages(waitCount: Int): List<T> {
        await().atMost(1, MINUTES).until{ messages.size >= waitCount}
        return messages.toList()
    }

}