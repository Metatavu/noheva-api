package fi.metatavu.muisti.api

import fi.metatavu.muisti.api.spec.StoredFilesApi
import fi.metatavu.muisti.api.spec.model.StoredFile
import fi.metatavu.muisti.files.FileController
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.transaction.Transactional
import javax.ws.rs.core.Response

/**
 * Stored files API REST endpoints
 *
 * @author Antti Leppä
 */
@RequestScoped
@Transactional
class StoredFilesApiImpl: StoredFilesApi, AbstractApi() {

    @Inject
    private lateinit var fileController: FileController

    override fun findStoredFile(storedFileId: String?): Response {
        storedFileId ?: return createNotFound(STORED_FILE_NOT_FOUND)
        val storedFile = fileController.findStoredFile(storedFileId) ?: return createNotFound("Stored file $storedFileId not found")
        return createOk(storedFile)
    }

    override fun listStoredFiles(path: String?): Response {
        if (path.isNullOrBlank()) {
            return createBadRequest("Path is required")
        }

        return createOk(fileController.listStoredFiles(path))
    }

    override fun updateStoredFile(storedFileId: String?, storedFile: StoredFile?): Response {
        storedFile ?: return createBadRequest("Payload is required")
        storedFileId ?: return createNotFound(STORED_FILE_NOT_FOUND)
        fileController.findStoredFile(storedFileId) ?: return createNotFound("Stored file $storedFileId not found")
        return createOk(fileController.updateStoredFile(storedFile))
    }

    override fun deleteStoredFile(storedFileId: String?): Response {
        storedFileId ?: return createNotFound(STORED_FILE_NOT_FOUND)
        val storedFile = fileController.findStoredFile(storedFileId) ?: return createNotFound(STORED_FILE_NOT_FOUND)
        fileController.deleteStoredFile(storedFile.id)
        return createNoContent()
    }

}