package fi.metatavu.muisti.files.storage

import fi.metatavu.muisti.files.InputFile
import fi.metatavu.muisti.files.OutputFile
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

/**
 * File storage provider for storing files locally.
 *
 * Mainly used in tests
 *
 * @author Antti Leppä
 */
open class LocalFileStorageProvider : FileStorageProvider {
    private var folder: File? = null
    @Throws(FileStorageException::class)
    override fun init() {
        val path = System.getenv("LOCAL_FILE_STORAGE_PATH")
        if (StringUtils.isBlank(path)) {
            throw FileStorageException("LOCAL_FILE_STORAGE_PATH is not set")
        }
        folder = File(path)
        if (!folder!!.exists() || !folder!!.isDirectory) {
            throw FileStorageException("LOCAL_FILE_STORAGE_PATH is not folder")
        }
        if (!folder!!.canRead() || !folder!!.canWrite()) {
            throw FileStorageException("LOCAL_FILE_STORAGE_PATH is not writeable")
        }
    }

    @Throws(FileStorageException::class)
    override fun store(inputFile: InputFile): OutputFile {
        val parent = File(folder, inputFile.folder)
        if (!parent.exists()) {
            parent.mkdirs()
        }
        val file = File(parent, UUID.randomUUID().toString())
        try {
            FileOutputStream(file).use { fileStream -> IOUtils.copy(inputFile.data, fileStream) }
        } catch (e: IOException) {
            throw FileStorageException(e)
        }
        return OutputFile(inputFile.meta, file.toURI())
    }

    override val id: String
        get() = "LOCAL"
}
