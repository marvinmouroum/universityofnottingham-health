package de.mouroum.uno_health_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.action_container.*
import kotlinx.coroutines.launch
import retrofit2.HttpException


class LoadSurveyActivity : BaseActivity() {
    private val TAG = "LoadSurveyActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.action_container)

        loadSurvey()
    }

    private fun loadSurvey() = lifecycleScope.launch {
        loading()
        try {
            val survey = api.survey(prefs.currentSurveyId)
            success(survey)
        } catch (e: HttpException) {
            error(e)
        }
    }

    private fun loading() {
        actionTitle.text = getString(R.string.register_access)
        actionMessage.text = null
        button.visibility = View.INVISIBLE
        progressBar.visibility = View.VISIBLE
    }

    private fun success(survey: Survey) {
        val text = survey.description
        val description = "$text\n\n[${survey.questions.size} Questions]"
        val title = survey.title

        actionTitle.text = title
        actionMessage.text = description
        button.visibility = View.VISIBLE
        button.text = getString(R.string.survey_start)
        button.setOnClickListener { jump(survey) }
        progressBar.visibility = View.INVISIBLE
    }

    private fun error(e: Throwable) {
        Log.e(TAG, "Error while loading survey", e)
        actionTitle.setText(R.string.load_survey_failed)
        actionMessage.setText(R.string.try_again)
        button.visibility = View.VISIBLE
        button.setText(R.string.retry)
        button.setOnClickListener {
            loadSurvey()
        }
        progressBar.visibility = View.INVISIBLE
    }

    private fun jump(survey: Survey) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("survey", survey)
        startActivity(intent)
    }
}