package fi.metatavu.muisti.files

import fi.metatavu.muisti.api.spec.model.StoredFile
import fi.metatavu.muisti.files.storage.FileStorageException
import fi.metatavu.muisti.files.storage.FileStorageProvider
import org.apache.commons.lang3.StringUtils
import javax.annotation.PostConstruct
import javax.ejb.Singleton
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.inject.Any
import javax.enterprise.inject.Instance
import javax.inject.Inject

/**
 * Controller for file functions
 *
 * @author Antti Leppä
 */
@ApplicationScoped
@Singleton
class FileController {

    @Inject
    @Any
    private lateinit var fileStorageProviders: Instance<FileStorageProvider>

    private lateinit var fileStorageProvider: FileStorageProvider

    /**
     * Bean post construct method
     *
     * @throws FileStorageException thrown on file storage configuration issues
     */
    @PostConstruct
    @Throws(FileStorageException::class)
    fun init() {
        val fileStorageProviderId = System.getenv("FILE_STORAGE_PROVIDER")
        if (StringUtils.isEmpty(fileStorageProviderId)) {
            throw FileStorageException("FILE_STORAGE_PROVIDER is not defined")
        }

        fileStorageProvider = fileStorageProviders.stream()
            .filter { fileStorageProvider: FileStorageProvider -> fileStorageProviderId == fileStorageProvider.id }
            .findFirst()
            .orElseThrow<FileStorageException> { FileStorageException("Invalid file storage provider configured") }

        fileStorageProvider.init()
    }

    /**
     * Stores file
     *
     * @param inputFile input file
     * @return stored file
     */
    @Throws(FileStorageException::class)
    fun storeFile(inputFile: InputFile): StoredFile {
        return fileStorageProvider.store(inputFile)
    }

    /**
     * Finds a stored file
     *
     * @param storedFileId stored file id
     * @return stored file or null if not found
     */
    fun findStoredFile(storedFileId: String): StoredFile? {
        return fileStorageProvider.find(storedFileId)
    }

    /**
     * Lists a stored files
     *
     * @param folder folder
     * @return stored files
     */
    fun listStoredFiles(folder: String): List<StoredFile> {
        return fileStorageProvider.list(folder)
    }

    /**
     * Updates stored file
     *
     * @param storedFile stored file
     * @return updated stored file
     */
    fun updateStoredFile(storedFile: StoredFile): StoredFile {
        return fileStorageProvider.update(storedFile)
    }

    /**
     * Deletes stored file
     *
     * @param storedFileId stored file id
     */
    fun deleteStoredFile(storedFileId: String) {
        fileStorageProvider.delete(storedFileId)
    }
}