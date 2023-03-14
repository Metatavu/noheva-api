package fi.metatavu.noheva.api

import fi.metatavu.noheva.api.spec.StoredFilesApi
import fi.metatavu.noheva.api.spec.model.StoredFile
import fi.metatavu.noheva.files.FileController
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.transaction.Transactional
import javax.ws.rs.core.Response

/**
 * Stored files api implementation
 */
@RequestScoped
@Transactional
class StoredFilesApiImpl : StoredFilesApi, AbstractApi() {

    @Inject
    lateinit var fileController: FileController

    override fun listStoredFiles(folder: String): Response {
        if (folder.isBlank()) {
            return createBadRequest("Path is required")
        }

        return createOk(fileController.listStoredFiles(folder))
    }

    override fun findStoredFile(storedFileId: String): Response {
        val storedFile =
            fileController.findStoredFile(storedFileId) ?: return createNotFound("Stored file $storedFileId not found")
        return createOk(storedFile)
    }

    override fun updateStoredFile(storedFileId: String, storedFile: StoredFile): Response {
        fileController.findStoredFile(storedFileId) ?: return createNotFound("Stored file $storedFileId not found")
        return createOk(fileController.updateStoredFile(storedFile))
    }

    override fun deleteStoredFile(storedFileId: String): Response {
        val storedFile = fileController.findStoredFile(storedFileId) ?: return createNotFound(STORED_FILE_NOT_FOUND)
        fileController.deleteStoredFile(storedFile.id!!)
        return createNoContent()
    }

}
