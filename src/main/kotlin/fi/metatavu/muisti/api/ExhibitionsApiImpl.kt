package fi.metatavu.muisti.api

import fi.metatavu.muisti.api.spec.ExhibitionsApi
import fi.metatavu.muisti.api.spec.model.Exhibition

import java.util.UUID
import java.util.stream.Collectors
import javax.ejb.Stateful
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.validation.Valid
import javax.ws.rs.Consumes
import javax.ws.rs.Produces
import javax.ws.rs.core.Response

/**
 * Exhibitions API REST endpoints
 *
 * @author Antti Leppä
 */
@RequestScoped
@Stateful
open class ExhibitionsApiImpl(): ExhibitionsApi {

    override fun createExhibition(exhibition: Exhibition?): Response? {
        return Response.ok("impl").build()
    }

    override fun findExhibition(exhibitionId: UUID?): Response? {
        return Response.ok("impl").build()
    }

    override fun listExhibitions(): Response? {
        return Response.ok("impl").build()
    }

    override fun updateExhibition(exhibitionId: UUID?, exhibition: Exhibition?): Response? {
        return Response.ok("impl").build()
    }

    override fun deleteExhibition(exhibitionId: UUID?): Response? {
        return Response.ok("impl").build()
    }

}