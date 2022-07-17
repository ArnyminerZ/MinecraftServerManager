package manager

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import utils.add
import java.util.prefs.Preferences

object PreferencesManager {
    private val pref = Preferences.userRoot().node("ServerCreator")

    private val observingBooleans = mutableMapOf<String, List<MutableState<Boolean>>>()

    fun clear() = pref.clear()

    fun getString(key: String): String? =
        pref.get(key, "\u0000")
            .takeIf { it != "\u0000" }

    fun getString(key: String, default: String): String = pref.get(key, default)

    fun getBoolean(key: String, default: Boolean): Boolean = pref.getBoolean(key, default)

    fun getBoolean(key: String): Boolean = pref.getBoolean(key, false)

    fun set(key: String, value: Boolean) =
        pref.putBoolean(key, value)
            .also { observingBooleans[key]?.forEach { it.value = value } }

    fun observeBoolean(key: String) =
        mutableStateOf(getBoolean(key))
            .also { observingBooleans.add(key, it) }
}