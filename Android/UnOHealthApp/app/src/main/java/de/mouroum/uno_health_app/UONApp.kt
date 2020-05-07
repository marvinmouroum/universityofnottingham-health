package de.mouroum.uno_health_app

import android.app.Application
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull

class UONApp: Application() {

    companion object {

        const val HOST:String = "http://192.168.178.24:8080"

        val MEDIA_TYPE_JSON:MediaType? = "application/json; charset=utf-8".toMediaTypeOrNull()
        val MEDIA_TYPE_TEXT:MediaType? = "text/plain; charset=utf-8".toMediaTypeOrNull()
    }

    override fun onCreate() {
        super.onCreate()
    }
}