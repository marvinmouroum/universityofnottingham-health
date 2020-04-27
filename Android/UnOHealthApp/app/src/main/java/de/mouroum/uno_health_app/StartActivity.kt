package de.mouroum.uno_health_app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.google.gson.Gson
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
        val url = URL("http://192.168.178.41:8080/survey/REGULAR")

        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("Authorization","Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxNmVlZTYwNi00YTE0LTRlZWUtODIwYi03MzhlMDg0Yjg2NWIifQ.xoa28DDYgWUEIV_oP2-MTmuXnDprSyUE9Rs-sf-m-jPdLpY_iGrVHmfDC4Cz3fVd0btX9wvHzF7lZsvbZ0dyeA")
            .build()


        val response = client.newCall(request).execute()

        val responseBody = response.body!!.string()

        //Response
        println("Response Body: " + responseBody)
        surveyString = responseBody

        convertSurvey()

    }

    fun convertSurvey(){
        var result = Gson().fromJson(surveyString, Survey::class.java)
        currentSurvey = result

        display()
    }

    fun display(){
        surveyText.text = currentSurvey?.description ?: "This survey has no description"
        surveySummary.text = "${currentSurvey?.questions?.size ?: 0} Questions"
    }

    fun jump(view:View){

        if(currentSurvey != null) {
            var intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        else{
            Toast.makeText(this,"The download failed",Toast.LENGTH_LONG)
            get()
        }
    }

}