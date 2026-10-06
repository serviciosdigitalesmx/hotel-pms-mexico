package mx.fixi.app.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton class SessionStore @Inject constructor(@ApplicationContext context: Context) {
    private val prefs = context.getSharedPreferences("fixi_session", Context.MODE_PRIVATE)
    var username: String? get() = prefs.getString("username", null); set(value) { prefs.edit().putString("username", value).apply() }
    fun clear() = prefs.edit().clear().apply()
    fun isLoggedIn() = username != null
}
