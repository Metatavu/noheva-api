package fi.metatavu.muisti.api.test.functional.impl

import fi.metatavu.jaxrs.test.functional.builder.AbstractTestBuilder
import fi.metatavu.jaxrs.test.functional.builder.AbstractApiTestBuilderResource

import fi.metatavu.muisti.api.client.infrastructure.ApiClient

import fi.metatavu.muisti.api.client.models.Exhibition
import fi.metatavu.muisti.api.client.apis.ExhibitionsApi

import fi.metatavu.muisti.api.test.functional.impl.ApiTestBuilderResource

import fi.metatavu.muisti.api.test.functional.settings.TestSettings

public class ExhibitionsTestBuilderResource(testBuilder: AbstractTestBuilder<ApiClient?>?, apiClient: ApiClient) : ApiTestBuilderResource<Exhibition, ApiClient?>(testBuilder, apiClient) {

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
        val payload: Exhibition = Exhibition(name)
        val result: Exhibition = this.getApi().createExhibition(payload)
        addClosable(result)
        return result
    }

    override protected fun getApi(): ExhibitionsApi {
        val apiClient = apiClient()
        // return ExhibitionsApi(TestSettings.apiBasePath)
    }

    override fun clean(exhibition: Exhibition) {

    }

}