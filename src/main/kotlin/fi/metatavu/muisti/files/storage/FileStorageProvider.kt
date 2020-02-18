package fi.metatavu.muisti.files.storage

import fi.metatavu.muisti.files.InputFile
import fi.metatavu.muisti.files.OutputFile

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
     * @throws FileStorageException thrown when storaging the file fails
     */
    @Throws(FileStorageException::class)
    fun store(inputFile: InputFile): OutputFile
}
