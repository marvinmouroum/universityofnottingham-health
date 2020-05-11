package de.mouroum.uno_health_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import de.mouroum.uno_health_app.UONApp.Companion.HOST
import de.mouroum.uno_health_app.UONApp.Companion.MEDIA_TYPE_JSON
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.lang.Thread.sleep
import java.net.URL
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private val adapter = MainAdapter()

    private val currentSurvey by lazy {
        intent.extras!!.get("survey") as Survey
    }

    private var onQuestion: Int = 0

    private val prefs by lazy {
        Prefs(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.question_container)

        prepareUI()
        loadQuestion()
        nextQuestion()
    }

    private fun loadQuestion() {
        if (prefs.currentQuestionId == -1L)
            return

        findQuestion(currentSurvey.questions, prefs.currentQuestionId)
    }

    private fun findQuestion(questions: List<Question>, targetId: Long): Question? {
        for (question in questions) {
            if (question.id == targetId)
                return question

            if (question.container != null) {
                findQuestion(question.container.subQuestions, targetId)
            }
        }

        return null
    }

    @MainThread
    private fun nextQuestion() {
        if (onQuestion >= currentSurvey.questions.size) {
            if (currentSurvey.nameId == "BASIC") {
                prefs.currentSurveyId = "REGULAR"
                val intent = Intent(this, LoadSurveyActivity::class.java)
                startActivity(intent)
            }
            return
        }


        val currQuestion = currentSurvey.questions[onQuestion]
        adapter.setCurrentQuestion(currQuestion)
        findViewById<TextView>(R.id.questionTitle).text =
            currQuestion.question
        onQuestion += 1
        prefs.currentQuestionId = currQuestion.id
        findViewById<ProgressBar>(R.id.progressBar).visibility = View.GONE
        findViewById<Button>(R.id.questionButton).isEnabled = true
    }

    private fun prepareUI() {
        findViewById<ListView>(R.id.answerList).adapter = adapter
//        val button = fragment.view!!.findViewById<Button>(R.id.questionButton)
//        button.setOnTouchListener { v, event ->
//
//            if (event.action == MotionEvent.ACTION_DOWN) {
//                v.background =
//                    this@MainActivity.getDrawable(R.drawable.round_rect_violet)
//            } else {
//                v.background =
//                    this@MainActivity.getDrawable(R.drawable.round_rect_purple)
//            }
//            return@setOnTouchListener false
//        }
    }


    fun uponClick(view: View) {

        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val button = findViewById<Button>(R.id.questionButton)
        var updated = false

        // Disable button ASAP to avoid multiple calls
        button.isEnabled = false


        thread {
            try {
                val answer = getAnswer()

                if (answer != null) {

                    // If answer has been selected, run thread, which displays progress bar after some timeout
                    thread {
                        sleep(500)
                        runOnUiThread {
                            // Avoid update if other thread finished the operation already
                            if (!updated)
                                progressBar.visibility = View.VISIBLE
                        }
                    }
                    // Send the selected answer(s) to the server
                    sendAnswer(currentSurvey.nameId, answer)

                } else return@thread

            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, getString(R.string.error_send_answer), Toast.LENGTH_LONG)
                        .show()
                }
                Log.e("MainActivity", "sending answer error", e)
                return@thread
            } finally {
                // Reset the progress bar utilization and tell the waiting thread not to update the display
                updated = true
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    button.isEnabled = true
                }
            }
            // If this thread has not been suspended, load the next question
            runOnUiThread(this::nextQuestion)
        }
    }

    private fun getAnswer(): SurveyResponse? = adapter.question?.let {
        return when (it.type) {
            AnswerType.BOOL -> {
                if (adapter.selectedBool == null) null
                else SurveyResponse(
                    it.id,
                    null,
                    adapter.selectedBool,
                    null,
                    null,
                    null
                )
            }
            AnswerType.CHOICE -> {
                if (adapter.selectedChoice.isEmpty()) return null

                val answerIds = mutableListOf<Long>()
                for (item in adapter.selectedChoice) {
                    answerIds.add(it.answers[item].id)
                }
                SurveyResponse(
                    it.id,
                    answerIds,
                    null,
                    null,
                    null,
                    null
                )
            }
            AnswerType.TEXT -> {
                if (adapter.selectedText == null) null
                else SurveyResponse(
                    it.id,
                    null,
                    null,
                    adapter.selectedText,
                    null,
                    null
                )
            }
            AnswerType.CHECKLIST -> {
                if (adapter.selectedChecklist.isEmpty()) null
                else SurveyResponse(
                    it.id,
                    null,
                    null,
                    null,
                    null,
                    adapter.selectedChecklist
                )
            }
            AnswerType.RANGE -> {
                if (adapter.selectedRange == null) null
                else SurveyResponse(
                    it.id,
                    null,
                    null,
                    null,
                    adapter.selectedRange,
                    null
                )
            }
        }
    }

    private fun sendAnswer(nameId: String, answer: SurveyResponse) {

        val client = OkHttpClient()
        val url = URL(HOST + "/survey/${nameId}/answer")

        val gson = Gson()
        val json = gson.toJson(answer)
        println(json)

        val body = json.toRequestBody(MEDIA_TYPE_JSON)

        val request = Request.Builder()
            .url(url)
            .post(body)
            .addHeader("Authorization", "Bearer " + prefs.token)
            .build()

        val response = client.newCall(request).execute()

        if (!response.isSuccessful)
            throw Exception("$response.code")
    }

}
