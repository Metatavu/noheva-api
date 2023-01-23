package fi.metatavu.muisti.api;

import fi.metatavu.muisti.api.spec.StoredFilesApi
import fi.metatavu.muisti.api.spec.model.StoredFile
import javax.ws.rs.core.Response


class StoredFilesApiImpl : StoredFilesApi, AbstractApi() {
    override fun listStoredFiles(folder: String): Response {
        TODO("Not yet implemented")
    }

    override fun findStoredFile(storedFileId: String): Response {
        TODO("Not yet implemented")
    }

    override fun updateStoredFile(storedFileId: String, storedFile: StoredFile): Response {
        TODO("Not yet implemented")
    }

    override fun deleteStoredFile(storedFileId: String): Response {
        TODO("Not yet implemented")
    }

}
