package fi.metatavu.muisti.api.test.functional.impl

import fi.metatavu.jaxrs.test.functional.builder.AbstractTestBuilder
import fi.metatavu.jaxrs.test.functional.builder.AbstractApiTestBuilderResource
import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider

import fi.metatavu.muisti.api.client.infrastructure.ApiClient

import fi.metatavu.muisti.api.client.models.Exhibition
import fi.metatavu.muisti.api.client.apis.ExhibitionsApi

import fi.metatavu.muisti.api.test.functional.impl.ApiTestBuilderResource

import fi.metatavu.muisti.api.test.functional.settings.TestSettings
import org.slf4j.LoggerFactory

/**
 * Test builder resource for handling exhibitions
 */
class ExhibitionsTestBuilderResource(testBuilder: AbstractTestBuilder<ApiClient?>?, accessTokenProvider: AccessTokenProvider?, apiClient: ApiClient) : ApiTestBuilderResource<Exhibition, ApiClient?>(testBuilder, apiClient) {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val accessTokenProvider: AccessTokenProvider? = accessTokenProvider

    /**
     * Creates new exhibition with default values
     *
     * @return created exhibition
     */
    fun create(): Exhibition {
        return create("default exhibition")
    }

    /**
     * Creates exhibition
     *
     * @param name exhibition name
     * @return created exhibition
     */
    fun create(name: String): Exhibition {
        val payload = Exhibition(name)
        val result: Exhibition = this.getApi().createExhibition(payload)
        addClosable(result)
        return result
    }

    override fun getApi(): ExhibitionsApi {
        ApiClient.accessToken = accessTokenProvider?.accessToken
        return ExhibitionsApi(TestSettings.apiBasePath)
    }

    override fun clean(exhibition: Exhibition) {
        this.getApi().deleteExhibition(exhibition.id!!)
    }

}