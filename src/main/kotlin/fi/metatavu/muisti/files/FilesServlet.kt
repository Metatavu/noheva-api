package fi.metatavu.muisti.files

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.slf4j.Logger
import java.io.IOException
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.servlet.ServletException
import javax.servlet.annotation.MultipartConfig
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


/**
 * Servlet that handles file upload requests
 *
 * @author Antti Leppä
 */
@RequestScoped
@MultipartConfig
@WebServlet(urlPatterns = ["/files", "/files/*"])
class FilesServlet : HttpServlet() {

    @Inject
    private lateinit var logger: Logger

    @Inject
    private lateinit var fileController: FileController

    @Throws(ServletException::class, IOException::class)
    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        resp.status = HttpServletResponse.SC_NOT_IMPLEMENTED
    }

    @Throws(ServletException::class, IOException::class)
    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        try {
            val file = req.getPart("file")
            if (file == null) {
                resp.status = HttpServletResponse.SC_BAD_REQUEST
                return
            }

            val folder = req.getParameter("folder")
            setCorsHeaders(resp)
            val contentType = file.contentType
            val fileName = file.submittedFileName
            val inputStream = file.inputStream
            InputFile(folder, FileMeta(contentType, fileName), inputStream).use { inputFile ->
                val storedFile = fileController.storeFile(inputFile)
                resp.contentType = "application/json"
                val servletOutputStream = resp.outputStream
                try {
                    val objectMapper = ObjectMapper()
                    objectMapper.registerModule(KotlinModule())
                    objectMapper.writeValue(servletOutputStream, storedFile)
                } finally {
                    servletOutputStream.flush()
                }
            }
        } catch (e: Exception) {
            logger.error("Upload failed on internal server error", e)
            resp.status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR
        }
    }

    @Throws(ServletException::class, IOException::class)
    override fun doDelete(req: HttpServletRequest, resp: HttpServletResponse) {
        resp.status = HttpServletResponse.SC_NOT_IMPLEMENTED
    }

    /**
     * Sets CORS headers for the response
     *
     * @param response
     */
    private fun setCorsHeaders(response: HttpServletResponse) {
        response.setHeader("Access-Control-Allow-Origin", "*")
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Authorization")
        response.setHeader("Access-Control-Allow-Methods", "GET,PUT,POST,DELETE,OPTIONS")
    }

}