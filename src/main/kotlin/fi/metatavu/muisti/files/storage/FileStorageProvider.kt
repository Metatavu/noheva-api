package fi.metatavu.muisti.files.storage

import fi.metatavu.muisti.api.spec.model.StoredFile
import fi.metatavu.muisti.files.InputFile

/**
 * Interface for describing a single file storage
 *
 * @author Antti Leppä
 */
interface FileStorageProvider {

    /**
     * Returns file storage id
     *
     * @return file storage id
     */
    val id: String

    /**
     * Initializes the file storage provider
     *
     * @throws FileStorageException thrown when initialization fails
     */
    @Throws(FileStorageException::class)
    fun init()

    /**
     * Stores a file
     *
     * @param inputFile input file data
     * @return stored file
     * @throws FileStorageException thrown when file storing fails
     */
    @Throws(FileStorageException::class)
    fun store(inputFile: InputFile): StoredFile

    /**
     * Finds single file
     *
     * @param storedFileId file id
     * @return file or null if not found
     */
    fun find(storedFileId: String): StoredFile?

    /**
     * Lists files in a folder
     *
     * @param folder parent folder
     * @return list of files in given folder
     */
    fun list(folder: String): List<StoredFile>

    /**
     * Updates stored file metadata
     *
     * @param storedFile
     * @return updated file
     */
    fun update(storedFile: StoredFile): StoredFile

    /**
     * Deletes single file
     *
     * @param storedFileId file id
     */
    fun delete(storedFileId: String)

}
