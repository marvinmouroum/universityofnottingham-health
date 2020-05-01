package de.mouroum.uno_health_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import de.mouroum.uno_health_app.UONApp.Companion.HOST
import kotlinx.android.synthetic.main.action_container.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URL
import kotlin.concurrent.thread

class StartActivity: AppCompatActivity() {

    private var prefs: Prefs? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = Prefs(this)
        setContentView(R.layout.action_container)

        validate(false)
    }

    private fun validate(isUserInput: Boolean) {
        thread {
            resetUI()

            if (isUserInput)
                Thread.sleep(1000)

            val verified = validateVerification();
            updateUI(verified)
        }
    }

    private fun resetUI() {

        runOnUiThread {
            actionTitle.text = getString(R.string.verify_access)
            actionMessage.text = null
            button.visibility = View.INVISIBLE
            progressBar.visibility = View.VISIBLE
        }
    }

    private fun updateUI(verified: Boolean) {

        runOnUiThread {

            if (verified) {
                val intent = Intent(this, LoadSurveyActivity::class.java)
                startActivity(intent)

                return@runOnUiThread
            }

            actionTitle.text = getString(R.string.invalid_access)
            actionMessage.text = getString(R.string.invalid_access_description)
            button.visibility = View.VISIBLE
            button.text = getString(R.string.retry)
            button.setOnClickListener { validate(true) }
            progressBar.visibility = View.INVISIBLE
        }
    }

    private fun validateVerification(): Boolean {
        val token = prefs!!.token ?: return false

        val client = OkHttpClient()
        val url = URL("$HOST/check")

        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("Authorization", "Bearer $token")
            .build()

        return try {
            val response = client.newCall(request).execute()
            println(response)
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
}