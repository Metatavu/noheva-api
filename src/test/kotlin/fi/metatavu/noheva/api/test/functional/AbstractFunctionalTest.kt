package fi.metatavu.noheva.api.test.functional

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fi.metatavu.noheva.api.client.models.*
import fi.metatavu.noheva.api.test.functional.builder.AbstractResourceTest
import fi.metatavu.noheva.api.test.functional.builder.TestBuilder
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.apache.commons.codec.digest.DigestUtils
import org.json.JSONException
import org.junit.Assert
import org.skyscreamer.jsonassert.JSONCompare
import org.skyscreamer.jsonassert.JSONCompareMode
import org.skyscreamer.jsonassert.JSONCompareResult
import org.skyscreamer.jsonassert.comparator.CustomComparator
import java.io.IOException
import java.io.InputStream

/**
 * Abstract base class for functional tests
 *
 * @author Antti Leppä
 */
abstract class AbstractFunctionalTest: AbstractResourceTest() {

    /**
     * Creates a default room and all required resources into given exhibition
     *
     * @param testBuilder test builder instance
     * @param exhibition exhibition
     * @return created room
     */
    protected fun createDefaultRoom(testBuilder: TestBuilder, exhibition: Exhibition): ExhibitionRoom {
        val floor = testBuilder.admin.exhibitionFloors.create(exhibition)
        return testBuilder.admin.exhibitionRooms.create(exhibition = exhibition, floor = floor)
    }

    /**
     * Creates a default device group and all required resources into given exhibition
     *
     * @param testBuilder test builder instance
     * @param exhibition exhibition
     * @return created device group
     */
    protected fun createDefaultDeviceGroup(testBuilder: TestBuilder, exhibition: Exhibition): ExhibitionDeviceGroup {
        val room = createDefaultRoom(testBuilder, exhibition)
        return testBuilder.admin.exhibitionDeviceGroups.create(
            exhibition = exhibition,
            room = room,
            name = "Group 1"
        )
    }

    /**
     * Creates a default device and all required resources into given exhibition
     *
     * @param testBuilder test builder instance
     * @param exhibition exhibition
     * @param deviceGroup device group
     * @return created device
     */
    protected fun createDefaultDevice(testBuilder: TestBuilder, exhibition: Exhibition, deviceGroup: ExhibitionDeviceGroup): ExhibitionDevice {
        val model = testBuilder.admin.deviceModels.create()
        return testBuilder.admin.exhibitionDevices.create(exhibition = exhibition, model = model, group = deviceGroup)
    }

    /**
     * Calculates contents md5 from a resource
     *
     * @param resourceName resource name
     * @return resource contents md5
     * @throws IOException thrown when file reading fails
     */
    @Throws(IOException::class)
    protected open fun getResourceMd5(resourceName: String?): String? {
        val classLoader = javaClass.classLoader
        classLoader.getResourceAsStream(resourceName).use { fileStream -> return DigestUtils.md5Hex(fileStream) }
    }

    /**
     * Downloads URI contents as an input stream
     *
     * @param uri URI
     * @return content stream
     */
    protected fun download(uri: String): InputStream? {
        val request: Request = Request.Builder()
            .url(uri)
            .get()
            .build()

        val client = OkHttpClient.Builder().hostnameVerifier { _, _ -> true }.build()
        val response: Response = client.newCall(request).execute()
        Assert.assertTrue(response.isSuccessful)

        val body = response.body()
        Assert.assertNotNull(body)

        return body?.byteStream()
    }

    /**
     * Asserts that actual object equals expected object when both are serialized into JSON
     *
     * @param expected expected
     * @param actual actual
     * @throws JSONException thrown when JSON serialization error occurs
     * @throws IOException thrown when IO Exception occurs
     */
    @Throws(IOException::class, JSONException::class)
    fun assertJsonsEqual(expected: Any?, actual: Any?) {
        if (expected == null && actual == null) {
            return
        }

        val compareResult: JSONCompareResult? = jsonCompare(expected, actual)
        Assert.assertTrue(compareResult?.message, compareResult?.passed()?: false)
    }

    /**
     * Compares objects as serialized JSONs
     *
     * @param expected expected
     * @param actual actual
     * @return comparison result
     * @throws JSONException
     * @throws JsonProcessingException
     */
    @Throws(JSONException::class, JsonProcessingException::class)
    private fun jsonCompare(expected: Any?, actual: Any?): JSONCompareResult? {
        val customComparator = CustomComparator(JSONCompareMode.LENIENT)
        return JSONCompare.compareJSON(toJSONString(expected), toJSONString(actual), customComparator)
    }

    /**
     * Serializes an object into JSON
     *
     * @param object object
     * @return JSON string
     * @throws JsonProcessingException
     */
    @Throws(JsonProcessingException::class)
    private fun toJSONString(`object`: Any?): String? {
        return if (`object` == null) {
            null
        } else getObjectMapper().writeValueAsString(`object`)
    }

    /**
     * Returns object mapper with default modules and settings
     *
     * @return object mapper
     */
    private fun getObjectMapper(): ObjectMapper {
        val objectMapper = ObjectMapper()
        objectMapper.registerModule(JavaTimeModule())
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        return objectMapper
    }

    /**
     * Returns a page layout view data in android format
     *
     * @param data data
     * @return page layout view data android format
     */
    fun parsePageLayoutViewDataAndroid(data: Any): PageLayoutView? {
        return jacksonObjectMapper().readValue(
            jacksonObjectMapper().writeValueAsBytes(data),
            PageLayoutView::class.java
        )
    }

    /**
     * Returns a page layout view data in html format
     *
     * @param data data
     * @return page layout view data html format
     */
    fun parsePageLayoutViewDataHtml(data: Any): PageLayoutViewHtml? {
        return jacksonObjectMapper().readValue(
            jacksonObjectMapper().writeValueAsBytes(data),
            PageLayoutViewHtml::class.java
        )
    }
}
