package de.mouroum.uno_health_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import de.mouroum.uno_health_app.UONApp.Companion.HOST
import de.mouroum.uno_health_app.UONApp.Companion.MEDIA_TYPE_JSON
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.lang.Exception
import java.lang.Thread.sleep
import java.net.URL
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private var adapter: MainAdapter? = null
    private var currentSurvey: Survey? = null

    private var onQuestion: Int = 0

    private val fragment = GeneralFragment.newInstance(R.layout.question_container)

    var prefs: Prefs? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        currentSurvey = intent.extras?.get("survey") as Survey
        adapter = MainAdapter(this)
        prefs = Prefs(this)

        prepareUI()
        loadQuestion()
        nextQuestion()
    }

    private fun loadQuestion() {
        if (prefs!!.currentQuestionId == -1L)
            return

        findQuestion(currentSurvey!!.questions, prefs!!.currentQuestionId)
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

    private fun nextQuestion() {

        if (onQuestion >= currentSurvey?.questions?.size ?: 0) {
            if (currentSurvey!!.nameId == "BASIC") {
                prefs!!.currentSurveyId = "REGULAR"
                val intent = Intent(this, LoadSurveyActivity::class.java)
                startActivity(intent)
            }
            return
        }

        thread {
            while (fragment.createdView == null) {
                sleep(50)
            }

            runOnUiThread {
                val currQuestion = currentSurvey!!.questions[onQuestion]
                adapter?.setCurrentQuestion(currQuestion)
                fragment.view!!.findViewById<TextView>(R.id.questionTitle).text =
                    currQuestion.question
                onQuestion += 1
                prefs!!.currentQuestionId = currQuestion.id
                fragment.view!!.findViewById<ProgressBar>(R.id.progressBar).visibility = View.GONE
                fragment.view!!.findViewById<Button>(R.id.questionButton).isEnabled = true
                adapter?.notifyDataSetChanged()
            }
        }
    }

    private fun prepareUI() {
        supportFragmentManager
            // 3
            .beginTransaction()
            // 4
            .add(R.id.purpelContainer, fragment, "questionFragment")
            // 5
            .commit()

        thread {
            while (fragment.createdView == null) {
                sleep(50)
            }
            runOnUiThread {

                fragment.view!!.findViewById<ListView>(R.id.answerList).adapter = adapter

//                val button = fragment.view!!.findViewById<Button>(R.id.questionButton)

//                button.setOnTouchListener { v, event ->
//
//                        if (event.action == MotionEvent.ACTION_DOWN) {
//                            v.background =
//                                this@MainActivity.getDrawable(R.drawable.round_rect_violet)
//                        } else {
//                            v.background =
//                                this@MainActivity.getDrawable(R.drawable.round_rect_purple)
//                        }
//                        return@setOnTouchListener false
//                    }
            }
        }
    }

    private fun cleanupUI() {
        supportFragmentManager
            .beginTransaction()
            .remove(fragment)
            .commit()
    }

    fun uponClick(view :View) {

        val progressBar = fragment.view!!.findViewById<ProgressBar>(R.id.progressBar)
        val button = fragment.view!!.findViewById<Button>(R.id.questionButton)
        var updated = false

        // Disable button ASAP to avoid multiple calls
        runOnUiThread {
            button.isEnabled = false
        }

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
                    sendAnswer(currentSurvey!!.nameId, answer)

                } else return@thread

            } catch (e: Exception) {

                runOnUiThread {
                    Toast.makeText(this, getString(R.string.error_send_answer), Toast.LENGTH_LONG)
                        .show()
                }
                println(e)
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
            nextQuestion()
        }
    }

    private fun getAnswer(): SurveyResponse? {
        return when (adapter?.question?.type) {
            AnswerType.BOOL -> {
                if (adapter!!.selectedBool == null) null
                else SurveyResponse(
                    adapter!!.question!!.id,
                    null,
                    adapter!!.selectedBool,
                    null,
                    null,
                    null
                )
            }
            AnswerType.CHOICE -> {
                if (adapter!!.selectedChoice.isEmpty()) return null

                val answerIds = mutableListOf<Long>()
                for (item in adapter!!.selectedChoice) {
                    answerIds.add(adapter!!.question!!.answers[item].id)
                }
                SurveyResponse(
                    adapter!!.question!!.id,
                    answerIds,
                    null,
                    null,
                    null,
                    null
                )
            }
            AnswerType.TEXT -> {
                if (adapter!!.selectedText == null) null
                else SurveyResponse(
                    adapter!!.question!!.id,
                    null,
                    null,
                    adapter!!.selectedText,
                    null,
                    null
                )
            }
            AnswerType.CHECKLIST -> {
                if (adapter!!.selectedChecklist.isEmpty()) null
                else SurveyResponse(
                    adapter!!.question!!.id,
                    null,
                    null,
                    null,
                    null,
                    adapter!!.selectedChecklist
                )
            }
            AnswerType.RANGE -> {
                if (adapter!!.selectedRange == null) null
                else SurveyResponse(
                    adapter!!.question!!.id,
                    null,
                    null,
                    null,
                    adapter!!.selectedRange,
                    null
                )
            }
            null -> null
        }
    }

    private fun sendAnswer(nameId: String, answer: SurveyResponse) {

        val client = OkHttpClient()
        val url = URL(HOST + "/survey/${nameId}/answer")

        val gson = Gson()
        val json = gson.toJson(answer)

        val body = json.toRequestBody(MEDIA_TYPE_JSON)

        val request = Request.Builder()
            .url(url)
            .post(body)
            .addHeader("Authorization", "Bearer " + prefs!!.token!!)
            .build()

        val response = client.newCall(request).execute()

        if (!response.isSuccessful)
            throw Exception("$response.code")
    }

}
