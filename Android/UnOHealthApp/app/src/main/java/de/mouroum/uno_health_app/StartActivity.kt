package de.mouroum.uno_health_app

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.Button
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

    val PREFS_FILENAME = "de.mouroum.uno_health_app.prefs"
    var prefs: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.survey_start)

        prefs = this.getSharedPreferences(PREFS_FILENAME, 0)

        if(checkIfVerified() == false){
            val intent = Intent(this,RegisterActivity::class.java)
            //startActivity(intent)
            //return
        }

        thread {
            get()
        }

        clicked(View(this))
    }

    fun get() {
        val client = OkHttpClient()
        val url = URL("$HOST/survey/BASIC")

        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("Authorization", TOKEN)
            .build()

        return

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

    fun checkIfVerified():Boolean{

        return prefs?.getBoolean("VERIFY",false) ?: false
    }


    var sm:SensorManager? =null

    var sLightCurrent:Float = 0.0f
    var listener:SensorListener = SensorListener()

    private var smLight:Sensor? = null

    //part for step counter
    fun clicked(view:View){
        sLightCurrent = listener.reference

        sm = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensorList = sm!!.getSensorList(Sensor.TYPE_ALL)

        var sensorListString = ""
        for(sensor in sensorList){
            sensorListString += sensor.name + "\n"
        }

        println(sensorListString)

        listener.context = this

        reactToSensor(Sensor.TYPE_STEP_COUNTER)

    }

    fun reactToSensor(sensor:Int){

        val found = sm?.getDefaultSensor(sensor)

        if(found != null) {
            smLight = found
            sm?.unregisterListener(listener)
            sm?.registerListener(listener,smLight, SensorManager.SENSOR_DELAY_NORMAL)

            thread {
                Thread.sleep(100)
                runOnUiThread{
                    println("The value is " + listener.reference.toString())
                }
            }

        }
        else{
            println("sensor error")
        }
    }
}