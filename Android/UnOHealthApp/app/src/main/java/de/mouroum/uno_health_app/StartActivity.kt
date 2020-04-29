package de.mouroum.uno_health_app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.google.gson.Gson
import de.mouroum.uno_health_app.UONApp.Companion.HOST
import de.mouroum.uno_health_app.UONApp.Companion.TOKEN
import kotlinx.android.synthetic.main.survey_start.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URL
import kotlin.concurrent.thread

class StartSurvey: AppCompatActivity() {

    var surveyString:String? = null
    var currentSurvey:Survey? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.survey_start)

        thread {
            get()
        }
    }

    fun get() {
        val client = OkHttpClient()
        val url = URL("$HOST/survey/BASIC")

        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("Authorization", TOKEN)
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body!!.string()

        //Response
        println("Response Body: $responseBody")
        surveyString = responseBody

        convertSurvey()
    }

    private fun convertSurvey() {
        val result = Gson().fromJson(surveyString, Survey::class.java)
        currentSurvey = result
        display()
    }

    private fun display(){
        surveyText.text = currentSurvey?.description ?: "This survey has no description"
        val text = "${currentSurvey?.questions?.size ?: 0} Questions"
        surveySummary.text = text
    }

    fun jump(view:View){

        if (currentSurvey != null) {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("survey", currentSurvey)
            startActivity(intent)
        }
        else{
            Toast.makeText(this,"The download failed",Toast.LENGTH_LONG)
            get()
        }
    }
}