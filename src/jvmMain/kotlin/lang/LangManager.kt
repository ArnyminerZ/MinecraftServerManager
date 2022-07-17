package lang

import org.json.JSONObject
import utils.getResourceAsText
import java.util.Locale

object LangManager {
    private lateinit var fallbackLanguage: Locale
    private lateinit var language: Locale

    var localization: Map<String, String> = mapOf()
        private set

    @Throws(IllegalStateException::class)
    private fun loadStrings() {
        if (!this::language.isInitialized)
            throw IllegalStateException("Currently selected language is not initialized.")
        val langCode = "${language.toLanguageTag()}.json"
        println("Loading localization strings from \"$langCode\"...")
        val langFileContents = getResourceAsText("/lang/$langCode")
        val langJson = JSONObject(langFileContents)
        localization = langJson.toMap().mapValues { it.value.toString() }
    }

    fun initialize(fallbackLanguage: Locale, language: Locale = fallbackLanguage) {
        this.fallbackLanguage = fallbackLanguage
        setLanguage(language)
        try {
            loadStrings()
        } catch (e: NullPointerException) {
            setLanguage(fallbackLanguage)
            loadStrings()
        }
    }

    fun setLanguage(lang: Locale) {
        Locale.setDefault(lang)
        language = lang
    }
}