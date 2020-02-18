package fi.metatavu.muisti.api.test.functional.builder.impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import fi.metatavu.jaxrs.test.functional.builder.AbstractTestBuilder
import fi.metatavu.jaxrs.test.functional.builder.CloseableResource
import fi.metatavu.jaxrs.test.functional.builder.TestBuilderResource
import fi.metatavu.muisti.api.client.infrastructure.ApiClient
import fi.metatavu.muisti.api.test.functional.settings.TestSettings
import fi.metatavu.muisti.files.OutputFile
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import java.io.File
import java.io.IOException
import java.nio.file.Files


/**
 * Test builder resource for uploaded files
 *
 * @author Antti Leppä
 * @author Heikki Kurhinen
 */
class FileTestBuilderResource(private val testBuilder: AbstractTestBuilder<ApiClient?>) : TestBuilderResource<OutputFile> {
    /**
     * Uploads resource into file store
     *
     * @param folder folder
     * @param resourceName resource name
     * @return upload response
     * @throws IOException thrown on upload failure
     */
    @Throws(IOException::class)
    fun upload(folder: String, resourceName: String, contentType: String): OutputFile {
        val classLoader = javaClass.classLoader
        classLoader.getResourceAsStream(resourceName).use { fileStream ->
            val fileData: ByteArray = fileStream!!.readAllBytes()
            val fileBody: RequestBody = fileData.toRequestBody(contentType.toMediaType(), 0, fileData.size)

           val requestBody: MultipartBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "file", fileBody)
                .addFormDataPart("folder", folder)
                .build()

            val request: Request = Request.Builder()
                .url(TestSettings.filesBasePath)
                .post(requestBody)
                .build()

            val response: Response = OkHttpClient().newCall(request).execute()

            assertTrue(response.isSuccessful)
            val objectMapper = ObjectMapper()
            objectMapper.registerModule(KotlinModule())
            val result = objectMapper.readValue(response.body?.charStream(), OutputFile::class.java)
            assertNotNull(result)
            assertNotNull(result.uri)
            return result
        }
    }

    /**
     * Adds file as closeable into test builder
     *
     * @param file file
     * @return file
     */
    override fun addClosable(file: OutputFile): OutputFile {
        testBuilder.addClosable<CloseableResource<*>>(CloseableFileResource(file))
        return file
    }

    @Throws(Exception::class)
    override fun clean(t: OutputFile) { // File is cleaned in closeable file resource
    }

    /**
     * Describes closeable file resource
     *
     * @author Antti Leppä
     */
    private class CloseableFileResource
    /**
     * Constructor
     *
     * @param resource file resource
     */(resource: OutputFile?) : CloseableResource<OutputFile?>(resource) {
        @Throws(java.lang.Exception::class)
        override fun close() {
            val file = File(resource!!.uri)
            if (file.exists()) {
                Files.delete(file.toPath())
            }
        }
    }

}