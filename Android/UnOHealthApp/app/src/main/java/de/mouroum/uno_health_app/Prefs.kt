package de.mouroum.uno_health_app

import android.content.Context
import android.content.SharedPreferences

class Prefs (context: Context) {

    private val file = "one.tracking.framework.uon.app.prefs"
    private val keyToken = "token"
    private val keyCurrentSurveyId = "currentSurveyId"
    private val keyCurrentQuestionId = "currentQuestionId"
    private val prefs: SharedPreferences = context.getSharedPreferences(file, Context.MODE_PRIVATE);

    var token: String?
        get() = prefs.getString(keyToken, null)
        set(value) = prefs.edit().putString(keyToken, value).apply()

    var currentSurveyId: String
        get() = prefs.getString(keyCurrentSurveyId, "BASIC").toString()
        set(value) = prefs.edit().putString(keyCurrentSurveyId, value).apply()

    var currentQuestionId: Long
        get() = prefs.getLong(keyCurrentQuestionId, -1)
        set(value) = prefs.edit().putLong(keyCurrentQuestionId, value).apply()

    fun reset() {
        prefs.edit().clear().apply()
    }
}