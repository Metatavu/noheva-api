package fi.metatavu.muisti.api;

import fi.metatavu.muisti.api.spec.RfidAntennasApi
import fi.metatavu.muisti.api.spec.model.Error
import fi.metatavu.muisti.api.spec.model.RfidAntenna

import javax.ws.rs.*
import javax.ws.rs.core.Response


import java.io.InputStream
import java.util.*


class RfidAntennasApi: RfidAntennasApi, AbstractApi() {

    override fun listRfidAntennas(exhibitionId: UUID, roomId: UUID?, deviceGroupId: UUID?): Response {
        TODO("Not yet implemented")
    }

    override fun createRfidAntenna(exhibitionId: UUID, rfidAntenna: RfidAntenna): Response {
        TODO("Not yet implemented")
    }

    override fun findRfidAntenna(exhibitionId: UUID, rfidAntennaId: UUID): Response {
        TODO("Not yet implemented")
    }

    override fun updateRfidAntenna(exhibitionId: UUID, rfidAntennaId: UUID, rfidAntenna: RfidAntenna): Response {
        TODO("Not yet implemented")
    }

    override fun deleteRfidAntenna(exhibitionId: UUID, rfidAntennaId: UUID): Response {
        TODO("Not yet implemented")
    }
}
