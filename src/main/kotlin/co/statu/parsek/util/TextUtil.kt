package co.statu.parsek.util


import org.apache.commons.text.StringEscapeUtils

object TextUtil {
    fun convertStringToUrl(string: String, limit: Int = 200) =
        string
            .replace("\\s+".toRegex(), "-")
            .replace("[^\\dA-Za-z-]+".toRegex(), "")
            .lowercase()
            .take(limit)

    fun String.convertToSnakeCase(): String {
        val regex = Regex("([a-z])([A-Z])")
        val result = regex.replace(this) { matchResult ->
            "${matchResult.groupValues[1]}_${matchResult.groupValues[2].lowercase()}"
        }
        return result
    }

    fun unescapeJson(uncleanJson: String): String {
        return StringEscapeUtils.unescapeJson(uncleanJson)
            .replace("\"{\"", "{\"")
            .replace("\"}\"", "\"}")
    }
}