package fi.metatavu.muisti.api.test.functional.builder.impl

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.muisti.api.client.apis.StoredFilesApi
import fi.metatavu.muisti.api.client.infrastructure.ApiClient
import fi.metatavu.muisti.api.client.infrastructure.ClientException
import fi.metatavu.muisti.api.client.models.StoredFile
import fi.metatavu.muisti.api.test.builder.impl.ApiTestBuilderResource
import fi.metatavu.muisti.api.test.functional.builder.TestBuilder
import okhttp3.*
import org.junit.Assert
import org.junit.Assert.fail
import java.io.IOException

/**
 * Test builder resource for stored files
 */
class StoredFilesTestBuilderResource(testBuilder: TestBuilder, val accessTokenProvider: AccessTokenProvider?, apiClient: ApiClient) : ApiTestBuilderResource<StoredFile, ApiClient?>(testBuilder, apiClient) {

    /**
     * Uploads resource into file store
     *
     * @param folder folder
     * @param resourceName resource name
     * @param contentType content type
     * @param filename filename
     * @return upload response
     * @throws IOException thrown on upload failure
     */
    @Throws(IOException::class)
    fun upload(folder: String, resourceName: String, contentType: String, filename: String?): StoredFile {
        val classLoader = javaClass.classLoader
        classLoader.getResourceAsStream(resourceName).use { fileStream ->
            val fileData: ByteArray = fileStream!!.readBytes()
            val fileBody: RequestBody = RequestBody.create(MediaType.parse(contentType), fileData)

            val requestBody: MultipartBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", filename ?: resourceName, fileBody)
                    .addFormDataPart("folder", folder)
                    .build()

            val request: Request = Request.Builder()
                    .url(testBuilder.settings.filesBasePath)
                    .post(requestBody)
                    .build()

            val response: Response = OkHttpClient().newCall(request).execute()

            Assert.assertTrue(response.isSuccessful)
            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            val adapter = moshi.adapter(StoredFile::class.java)
            val result = adapter.fromJson(response.body()?.source()!!)

            Assert.assertNotNull(result)
            Assert.assertNotNull(result?.uri)

            addClosable(result)

            return result!!
        }
    }

    /**
     * Finds a stored file by id
     *
     * @param storedFileId stored file id
     * @return stored file or null if not found
     */
    fun findStoredFile(storedFileId: String): StoredFile {
        return api.findStoredFile(storedFileId)
    }

    /**
     * Lists stored files
     *
     * @param folder folder
     * @return stored files
     */
    fun listStoredFiles(folder: String): Array<StoredFile> {
        return api.listStoredFiles(
            folder = folder
        )
    }

    /**
     * Updates stored file metadata
     *
     * @param storedFile stored file
     * @return updated stored file
     */
    fun updateStoredFile(storedFile: StoredFile): StoredFile {
        return api.updateStoredFile(storedFileId = storedFile.id!!, storedFile = storedFile)
    }

    /**
     * Asserts find fails with given status
     *
     * @param expectedStatus expected status code
     * @param storedFileId stored file id
     */
    fun assertFindFailStatus(expectedStatus: Int, storedFileId: String) {
        try {
            api.findStoredFile(storedFileId)
            fail(String.format("Expected find to fail with message %d", expectedStatus))
        } catch (e: ClientException) {
            assertClientExceptionStatus(expectedStatus, e)
        }
    }

    override fun clean(storedFile: StoredFile) {
        this.api.deleteStoredFile(storedFile.id!!)
    }

    override fun getApi(): StoredFilesApi {
        ApiClient.accessToken = accessTokenProvider?.accessToken
        return StoredFilesApi(testBuilder.settings.apiBasePath)
    }

}
