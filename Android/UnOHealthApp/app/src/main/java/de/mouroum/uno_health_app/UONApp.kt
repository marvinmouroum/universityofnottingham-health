package de.mouroum.uno_health_app

import android.app.Application
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class UONApp: Application() {

    companion object {

        const val HOST:String = "http://192.168.178.24:8080"

        val MEDIA_TYPE_JSON: MediaType? = "application/json; charset=utf-8".toMediaTypeOrNull()
        val MEDIA_TYPE_TEXT: MediaType? = "text/plain; charset=utf-8".toMediaTypeOrNull()
    }

    val api by lazy {
        val retrofit: Retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("$HOST/")
            .build()
        retrofit.create(SurveyApi::class.java)
    }
}