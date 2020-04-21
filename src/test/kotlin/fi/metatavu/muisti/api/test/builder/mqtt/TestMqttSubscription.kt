package fi.metatavu.muisti.api.test.functional.mqtt

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import fi.metatavu.muisti.api.client.infrastructure.UUIDAdapter
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

    private val moshi = Moshi.Builder().add(UUIDAdapter()).add(KotlinJsonAdapterFactory()).build()
    private val jsonAdapter: JsonAdapter<T> = moshi.adapter<T>(targetClass)
    private val messages: MutableList<T> = mutableListOf()

    /**
     * Adds received message
     *
     * @param bytes message bytes
     */
    fun addMessageBytes(bytes: ByteArray) {
        val buffer = Buffer()
        buffer.write(bytes)
        val message: T? = jsonAdapter.fromJson(buffer)
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