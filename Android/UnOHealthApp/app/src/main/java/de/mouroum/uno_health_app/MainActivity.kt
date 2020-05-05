package de.mouroum.uno_health_app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.gson.Gson
import de.mouroum.uno_health_app.UONApp.Companion.HOST
import de.mouroum.uno_health_app.UONApp.Companion.MEDIA_TYPE_JSON
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.lang.Exception
import java.lang.Thread.sleep
import java.net.URL
import java.util.ArrayList
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {

    private var adapter: MyAdapter? = null
    private var currentSurvey: Survey? = null

    private var onQuestion: Int = 0

    private val fragment = GeneralFragment.newInstance(R.layout.question_container)

    var prefs: Prefs? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        currentSurvey = intent.extras?.get("survey") as Survey
        adapter = MyAdapter(this)
        prefs = Prefs(this)

        openQuestion()
        nextQuestion()
    }

    private fun nextQuestion() {

        if (onQuestion >= currentSurvey?.questions?.size ?: 0) {
            return
        }

        thread {
            while (fragment.createdView == null) {
                sleep(50)
            }

            runOnUiThread {
                adapter?.setCurrentQuestion(currentSurvey!!.questions[onQuestion])
                fragment.view!!.findViewById<TextView>(R.id.questionTitle).text =
                    currentSurvey!!.questions[onQuestion].question
                onQuestion += 1
                adapter?.notifyDataSetChanged()
            }
        }
    }

    private fun openQuestion() {
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

                fragment.view!!.findViewById<Button>(R.id.questionButton)
                    .setOnTouchListener { v, event ->

                        if (event.action == MotionEvent.ACTION_DOWN) {
                            v.background =
                                this@MainActivity.getDrawable(R.drawable.round_rect_violet)
                        } else {
                            v.background =
                                this@MainActivity.getDrawable(R.drawable.round_rect_purple)
                        }
                        return@setOnTouchListener false
                    }
            }
        }
    }

    fun closeQuestion() {
        supportFragmentManager
            .beginTransaction()
            .remove(fragment)
            .commit()
    }

    fun uponClick(view: View) {

        thread {

            val answer = getAnswer() ?: return@thread

            try {
                sendAnswer(currentSurvey!!.nameId, answer)
            } catch (e: Exception) {
                Toast.makeText(this, "Unable to send answer to the server.", Toast.LENGTH_LONG).show()
                return@thread
            }

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
                    null)
            }
            AnswerType.TITLE -> return null
            AnswerType.RANGE -> {
                if (adapter!!.selectedRange == null) null
                else SurveyResponse(
                    adapter!!.question!!.id,
                    null,
                    null,
                    null,
                    adapter!!.selectedRange)
            }
            null -> null
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
            .addHeader("Authorization", "Bearer " + prefs!!.token!!)
            .build()

        val response = client.newCall(request).execute()
    }

    private class MyAdapter(context: MainActivity) : BaseAdapter() {

        private val mContext: MainActivity = context

        var question: Question? = null

        var selectedChoice = mutableListOf<Int>()
        var selectedBool: Boolean? = null
        var selectedRange: Int? = null
        var selectedText: String? = null

        fun setCurrentQuestion(q: Question) {
            this.question = q

            this.selectedChoice = mutableListOf<Int>()
            this.selectedBool = null
            this.selectedRange = null
            this.selectedText = null

            notifyDataSetChanged()
        }

        override fun getCount(): Int {

            if (question == null) return 0

            return when (question!!.type) {
                AnswerType.CHOICE -> question?.answers?.size ?: 0
                AnswerType.BOOL -> 2
                AnswerType.TITLE -> question?.container?.subQuestions?.size ?: 0
                AnswerType.TEXT -> 1
                AnswerType.RANGE -> 1
            }
        }

        override fun getItem(position: Int): Any {
            return ""
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
            val layoutInActivity = LayoutInflater.from(mContext)

            if (question == null) return null

            return when (question!!.type) {
                AnswerType.CHOICE -> getChoiceView(position, parent, layoutInActivity)
                AnswerType.BOOL -> getBoolView(position, parent, layoutInActivity)
                AnswerType.RANGE -> getSliderView(position, parent, layoutInActivity)
                AnswerType.TEXT -> null
                AnswerType.TITLE -> null
            }
        }

        private fun getSliderView(
            position: Int,
            parent: ViewGroup?,
            layoutInActivity: LayoutInflater
        ): View? {

            val min = question!!.minValue
            val max = question!!.maxValue
            val step = 1

            val cell = layoutInActivity.inflate(R.layout.slider_question, parent, false)
            val seekbar = cell.findViewById<SeekBar>(R.id.seekBar)
            val minValueText = cell.findViewById<TextView>(R.id.minValueText)
            val maxValueText = cell.findViewById<TextView>(R.id.maxValueText)
            val minText = cell.findViewById<TextView>(R.id.minText)
            val maxText = cell.findViewById<TextView>(R.id.maxText)

            minValueText.text = "$min"
            maxValueText.text = "$max"
            minText.text = question!!.minText
            maxText.text = question!!.maxText
            seekbar.max = (max - min) / step

            seekbar.setOnSeekBarChangeListener(
                object : OnSeekBarChangeListener {
                    override fun onStopTrackingTouch(seekBar: SeekBar) {}
                    override fun onStartTrackingTouch(seekBar: SeekBar) {}
                    override fun onProgressChanged(
                        seekBar: SeekBar,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                        selectedRange = min + progress * step
                    }
                }
            )

            return cell
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        private fun getChoiceView(
            position: Int,
            parent: ViewGroup?,
            layoutInActivity: LayoutInflater
        ): View {

            val cell = layoutInActivity.inflate(R.layout.multiple_choice_question, parent, false)
            val textview = cell.findViewById<TextView>(R.id.answerTextview)
            val constLayOut = textview.layoutParams as ConstraintLayout.LayoutParams

            textview.text = question!!.answers[position].value

            cell.setOnClickListener {
                if (selectedChoice.contains(position)) {
                    selectedChoice.remove(position)
                } else {
                    if (question!!.multiple)
                        selectedChoice.add(position)
                    else {
                        selectedChoice.clear()
                        selectedChoice.add(position)
                    }
                }
                this@MyAdapter.notifyDataSetChanged()
            }

            if (selectedChoice.contains(position)) {
                constLayOut.leftMargin = 20
                textview.setPadding(0, 0, 0, 0)
            } else {
                constLayOut.leftMargin = 0
                textview.setPadding(20, 0, 0, 0)
            }
            textview.layoutParams = constLayOut

            return cell
        }

        private fun getBoolView(
            position: Int,
            parent: ViewGroup?,
            layoutInActivity: LayoutInflater
        ): View {
            val answer =
                if (position == 1) mContext.resources.getString(R.string.answer_no)
                else mContext.resources.getString(R.string.answer_yes)

            val cell = layoutInActivity.inflate(R.layout.multiple_choice_question, parent, false)
            val textview = cell.findViewById<TextView>(R.id.answerTextview)
            val constLayOut = textview.layoutParams as ConstraintLayout.LayoutParams

            textview.text = answer

            cell.setOnClickListener {
                selectedBool = position == 0
                this@MyAdapter.notifyDataSetChanged()
            }

            if (selectedBool != null && (position == 0 && selectedBool == true || position == 1 && selectedBool == false)) {
                constLayOut.leftMargin = 20
                textview.setPadding(0, 0, 0, 0)
            } else {
                constLayOut.leftMargin = 0
                textview.setPadding(20, 0, 0, 0)
            }
            textview.layoutParams = constLayOut

            return cell
        }
    }
}
