package de.mouroum.uno_health_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import kotlinx.android.synthetic.main.action_container.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.lang.Exception
import java.net.URL
import kotlin.concurrent.thread

class LoadSurveyActivity  : AppCompatActivity() {

    var currentSurvey:Survey? = null
    var prefs: Prefs? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = Prefs(this)
        setContentView(R.layout.action_container)

        loadSurvey(false)
    }

    private fun loadSurvey(isUserInput: Boolean) {
        thread {
            resetUI()

            if (isUserInput)
                Thread.sleep(1000)

            updateUI(load(prefs!!.currentSurveyId))
        }
    }

    private fun resetUI() {

        runOnUiThread {
            actionTitle.text = getString(R.string.register_access)
            actionMessage.text = null
            button.visibility = View.INVISIBLE
            progressBar.visibility = View.VISIBLE
        }
    }

    private fun updateUI(success: Boolean) {

        runOnUiThread {

            if (success) {
                val text = currentSurvey?.description ?: "This survey has no description"
                val description = "$text\n\n[${currentSurvey?.questions?.size ?: 0} Questions]"
                val title = currentSurvey?.title ?: null

                actionTitle.text = title
                actionMessage.text = description
                button.visibility = View.VISIBLE
                button.text = getString(R.string.survey_start)
                button.setOnClickListener { jump() }
                progressBar.visibility = View.INVISIBLE

            } else {
                actionTitle.text = "Failed to load survey"
                actionMessage.text = "Please try again later."
                button.visibility = View.VISIBLE
                button.text = getString(R.string.retry)
                button.setOnClickListener { loadSurvey(true) }
                progressBar.visibility = View.INVISIBLE
            }
        }
    }

    private fun load(surveyId: String) :Boolean {

        val token = prefs!!.token ?: return false

        val client = OkHttpClient()
        val url = URL("${UONApp.HOST}/survey/$surveyId")

        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("Authorization", "Bearer $token")
            .build()

        try {
            val response = client.newCall(request).execute()

            if (!response.isSuccessful)
                return false

            val responseBody = response.body!!.string()

            convertSurvey(responseBody)
            return true
        } catch (e :Exception) {
            return false
        }
    }

    private fun convertSurvey(surveyString: String) {
        val result = Gson().fromJson(surveyString, Survey::class.java)
        currentSurvey = result
    }

    private fun jump(){

        if (currentSurvey != null) {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("survey", currentSurvey)
            startActivity(intent)
        }
        else {
            Toast.makeText(this,"The download failed", Toast.LENGTH_LONG).show()
        }
    }
}