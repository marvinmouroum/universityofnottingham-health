package de.mouroum.uno_health_app

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URL

class RegisterActivity:AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registration_activity)
    }


    fun verify(view:View){

        val client = OkHttpClient()
        val url = URL(UONApp.HOST + "/survey/verify")

        val json = """{
            
            }""".trimMargin()

        val body = json.toRequestBody(mediaTypeJson)

        val request = Request.Builder()
            .url(url)
            .post(body)
            .addHeader("Authorization", UONApp.TOKEN)
            .addHeader("Content-Type","application/json")
            .build()

        val response = client.newCall(request).execute()

        val responseBody = response.body!!.string()
        print(response.code)

        //Response
        println("Response Body: $responseBody")
    }
}