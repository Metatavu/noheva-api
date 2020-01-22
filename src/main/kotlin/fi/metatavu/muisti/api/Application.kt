package fi.metatavu.muisti.api

import javax.ws.rs.ApplicationPath
import javax.ws.rs.core.Application

@ApplicationPath("/v1")
open class JaxRsActivator : Application() {
}