package de.mouroum.uno_health_app

import android.app.Application
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class UONApp : Application() {

    companion object {

        const val HOST:String = "http://192.168.178.24:8080"

        val MEDIA_TYPE_JSON: MediaType? = "application/json; charset=utf-8".toMediaTypeOrNull()
        val MEDIA_TYPE_TEXT: MediaType? = "text/plain; charset=utf-8".toMediaTypeOrNull()
    }

    val api by lazy {
        val okHttpClient = OkHttpClient.Builder()
//            .authenticator(object : Authenticator {
//                override fun authenticate(route: Route?, response: Response): Request? {
//                    return response.request.newBuilder()
//                        .header("Authorization", "Bearer ${prefs.token}")
//                        .build()
//                }
//            })
            .addInterceptor {
                val token = prefs.token
                val request =
                    if (token != null) it.request().newBuilder()
                        .header("Authorization", "Bearer ${prefs.token}")
                        .build()
                    else it.request()
                it.proceed(request)
            }
            .build()

        val retrofit: Retrofit = Retrofit.Builder()
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("$HOST/")
            .build()
        retrofit.create(SurveyApi::class.java)
    }

    val prefs by lazy {
        Prefs(this)
    }
}