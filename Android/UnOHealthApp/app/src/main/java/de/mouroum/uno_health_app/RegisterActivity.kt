package de.mouroum.uno_health_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.action_container.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URL
import kotlin.concurrent.thread

class RegisterActivity:AppCompatActivity() {

    private var prefs: Prefs? = null
    private var token: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = Prefs(this)
        token = intent.data?.path?.substring(1)
        setContentView(R.layout.action_container)

        register(false)
    }

    private fun register(isUserInput: Boolean) {
        thread {
            resetUI()

            if (isUserInput)
                Thread.sleep(1000)

            val verified = performVerification()
            updateUI(verified)
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

    private fun updateUI(verified: Boolean) {

        runOnUiThread {

            if (verified) {
                val intent = Intent(this, StartActivity::class.java)
                startActivity(intent)

                return@runOnUiThread
            }

            actionTitle.text = getString(R.string.register_failed)
            actionMessage.text = getString(R.string.register_failed_description)
            button.visibility = View.VISIBLE
            button.text = getString(R.string.retry)
            button.setOnClickListener { register(true) }
            progressBar.visibility = View.INVISIBLE
        }
    }

    private fun performVerification() : Boolean {

        if (token == null)
            return false

        val client = OkHttpClient()
        val url = URL(UONApp.HOST + "/verify")

        val body = token?.toRequestBody(UONApp.MEDIA_TYPE_TEXT)

        val request = Request.Builder()
            .url(url)
            .post(body!!)
            .build()

        try {
            val response = client.newCall(request).execute()
            println(response)

            if (!response.isSuccessful)
                return false

            val authNToken = response.body!!.string()
            println(authNToken)

            prefs!!.token = authNToken
            return true

        } catch (e: Exception) {
            return false
        }
    }
}