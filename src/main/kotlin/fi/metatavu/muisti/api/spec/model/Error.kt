package fi.metatavu.muisti.api.spec.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import java.util.*
import javax.validation.Valid
import javax.validation.constraints.*

class Error {
    @get:NotNull
    @get:JsonProperty("code")
    @Valid
    var code: Integer? = null
    @get:NotNull
    @get:JsonProperty("message")
    @Valid
    var message: String? = null

    /**
     */
    fun code(code: Integer?): fi.metatavu.muisti.api.spec.model.Error {
        this.code = code
        return this
    }

    /**
     */
    fun message(message: String?): fi.metatavu.muisti.api.spec.model.Error {
        this.message = message
        return this
    }

    @Override
    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("class Error {\n")
        sb.append("    code: ").append(toIndentedString(code)).append("\n")
        sb.append("    message: ").append(toIndentedString(message)).append("\n")
        sb.append("}")
        return sb.toString()
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private fun toIndentedString(o: Any?): String {
        return o?.toString()?.replace("\n", "\n    ") ?: "null"
    }
}