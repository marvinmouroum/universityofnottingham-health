package de.mouroum.uno_health_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope
import de.mouroum.uno_health_app.UONApp.Companion.HOST
import kotlinx.android.synthetic.main.action_container.*
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.HttpException
import java.net.URL

class StartActivity : BaseActivity() {
    private val TAG = "StartActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.action_container)
        validate()
    }

    private fun validate() = lifecycleScope.launch {
        loading()
        try {
            api.check()
            success()
        } catch (e: HttpException) {
            error(e)
        }
    }


    private fun loading() {
        actionTitle.text = getString(R.string.verify_access)
        actionMessage.text = null
        button.visibility = View.INVISIBLE
        progressBar.visibility = View.VISIBLE
    }

    private fun success() {
        val intent = Intent(this, LoadSurveyActivity::class.java)
        startActivity(intent)
    }

    private fun error(e: Throwable) {
        Log.e(TAG, "Error while checking authorization", e)
        actionTitle.text = getString(R.string.invalid_access)
        actionMessage.text = getString(R.string.invalid_access_description)
        button.visibility = View.VISIBLE
        button.text = getString(R.string.retry)
        button.setOnClickListener { validate() }
        progressBar.visibility = View.INVISIBLE
    }

    private fun validateVerification(): Boolean {
        val token = prefs.token ?: return false

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
            Log.e("authorization", "check failed", e)
            false
        }
    }
}