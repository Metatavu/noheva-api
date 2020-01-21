package fi.metatavu.muisti.api.test.functional.impl

import fi.metatavu.jaxrs.test.functional.builder.AbstractTestBuilder

import fi.metatavu.muisti.api.client.infrastructure.ApiClient

abstract class ApiTestBuilderResource<T, A>(testBuilder: AbstractTestBuilder<ApiClient?>?, apiClient: ApiClient) : fi.metatavu.jaxrs.test.functional.builder.AbstractApiTestBuilderResource<T, A, ApiClient?>(testBuilder) {

    private val apiClient: ApiClient

    override protected fun getApiClient(): ApiClient {
        return apiClient
    }

    init {
        this.apiClient = apiClient
    }
}