package co.statu.parsek.util


import com.github.jknack.handlebars.Handlebars

object TextUtil {
    private val handlebars by lazy {
        Handlebars()
    }

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
        return uncleanJson
            .replace("\"{\"", "{\"")
            .replace("\"}\"", "\"}")
//            .replace("}\",", "},")
    }

    fun String.compileInline() = handlebars.compileInline(this)
}