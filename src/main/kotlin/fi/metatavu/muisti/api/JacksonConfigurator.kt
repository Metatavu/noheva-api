package fi.metatavu.muisti.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import javax.ws.rs.ext.ContextResolver
import javax.ws.rs.ext.Provider

/**
 * Jackson configurator for RESTEasy
 *
 * @author Antti Leppä
 */
@Provider
class JacksonConfigurator : ContextResolver<ObjectMapper> {

    override fun getContext(type: Class<*>?): ObjectMapper {
        val objectMapper = ObjectMapper()
        objectMapper.registerModule(JavaTimeModule())
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        return objectMapper
    }

}