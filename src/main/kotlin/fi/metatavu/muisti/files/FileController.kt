package fi.metatavu.muisti.files

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
open class FileController {

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
    open fun init() {
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
     * Stores file and returns reference id
     *
     * @param inputFile input file
     * @return output file
     */
    @Throws(FileStorageException::class)
    open fun storeFile(inputFile: InputFile): OutputFile {
        return fileStorageProvider.store(inputFile)
    }
}