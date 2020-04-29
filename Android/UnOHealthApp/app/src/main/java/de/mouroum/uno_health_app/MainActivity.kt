package de.mouroum.uno_health_app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import de.mouroum.uno_health_app.UONApp.Companion.HOST
import de.mouroum.uno_health_app.UONApp.Companion.TOKEN
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.lang.Thread.sleep
import java.net.URL
import kotlin.concurrent.thread

enum class AnswerType {
    Boolean , MultipleChoice, Text, Title, Range
}

class MainActivity : AppCompatActivity() {

    private var adapter:MyAdapter? = null
    private var currentSurvey:Survey? = null

    private var onQuestion:Int = 0

    private val mediaTypeJson: MediaType? = "application/json; charset=utf-8".toMediaTypeOrNull()

    private val fragment = GeneralFragment.newInstance(R.layout.question_container)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        currentSurvey = intent.extras?.get("survey") as Survey
        adapter = MyAdapter(this)

        openQuestion()
        nextQuestion()

        thread {
            get()
        }
    }

    private fun nextQuestion(){

        if(onQuestion >= currentSurvey?.questions?.size ?: 0){
            return
        }

        thread {
            while(fragment.createdView == null){
                sleep(50)
            }

            runOnUiThread {
                adapter?.setCurrentQuestion(currentSurvey!!.questions[onQuestion])
                fragment.view!!.findViewById<TextView>(R.id.questionTitle).text = currentSurvey!!.questions[onQuestion].question
                onQuestion += 1
                adapter?.notifyDataSetChanged()
            }
        }
    }

    private fun openQuestion(){
        supportFragmentManager
            // 3
            .beginTransaction()
            // 4
            .add(R.id.purpelContainer, fragment, "questionFragment")
            // 5
            .commit()

        thread {
            while(fragment.createdView == null){
                sleep(50)
            }
            runOnUiThread {

                fragment.view!!.findViewById<ListView>(R.id.answerList).adapter = adapter

                fragment.view!!.findViewById<Button>(R.id.questionButton).setOnTouchListener { v, event ->

                        if (event.action == MotionEvent.ACTION_DOWN) {
                            v.background = this@MainActivity.getDrawable(R.drawable.round_rect_violet)
                        } else {
                            v.background = this@MainActivity.getDrawable(R.drawable.round_rect_purple)
                        }
                        return@setOnTouchListener false
                    }
            }
        }
    }

    fun closeQuestion(){
        supportFragmentManager
            .beginTransaction()
            .remove(fragment)
            .commit()
    }

    fun uponClick(view:View){

        thread {

            if (adapter?.question?.answers != null && adapter!!.selected != null) {
                val answer = adapter!!.question!!.answers[adapter!!.selected!!]
                sendAnswer(currentSurvey!!.nameId, adapter!!.question!!.id,answer.id)
            }
            else if (adapter!!.selected != null) {
                sendAnswer(currentSurvey!!.nameId,adapter!!.question!!.id,adapter!!.selected!!)
            }
            else {
                runOnUiThread {
                    Toast.makeText(this,"UPPS",Toast.LENGTH_SHORT)
                }

                return@thread
            }


            runOnUiThread {
                nextQuestion()
            }
        }
    }

    private fun sendAnswer(nameId:String, Qid:Int, Aid:Int) {

        val client = OkHttpClient()
        val url = URL(HOST + "/survey/${nameId}/answer")

        val json = """{
            "questionId":${Qid},
            "answerId":${Aid}
            }""".trimMargin()

        val body = json.toRequestBody(mediaTypeJson)

        val request = Request.Builder()
            .url(url)
            .post(body)
            .addHeader("Authorization", TOKEN)
            .addHeader("Content-Type","application/json")
            .build()

        val response = client.newCall(request).execute()

        val responseBody = response.body!!.string()
        print(response.code)

        //Response
        println("Response Body: $responseBody")

    }

    fun get() {
        val client = OkHttpClient()
        val url = URL("$HOST/survey/REGULAR")

        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("Authorization", TOKEN)
            .build()


        val response = client.newCall(request).execute()

        val responseBody = response.body!!.string()

        //Response
        println("Response Body: $responseBody")

    }

    private class MyAdapter(context: MainActivity): BaseAdapter(){

        private val mContext: MainActivity = context

        var type:AnswerType = AnswerType.MultipleChoice
        var question:Question? = null
        var selected:Int? = null

        fun setCurrentQuestion(q:Question){
            this.question = q

            when(q.type){
                "CHOICE" -> type = AnswerType.MultipleChoice
                "BOOL" -> type = AnswerType.Boolean
            }

            notifyDataSetChanged()

        }

        override fun getCount(): Int {
            when(type){
                AnswerType.MultipleChoice -> return question?.answers?.size ?: 0
                AnswerType.Boolean -> return 1
                AnswerType.Title -> return question?.container?.subQuestions?.size ?: 0
                AnswerType.Text -> return 1
                AnswerType.Range -> return 1
            }
        }

        override fun getItem(position: Int): Any {
            return ""
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
            val layoutInActivity = LayoutInflater.from(mContext)

            when(type) {
                AnswerType.MultipleChoice -> return getChoiceView(position, parent, layoutInActivity)
                AnswerType.Boolean -> return getBoolView(position, parent, layoutInActivity)
                AnswerType.Range -> return null
                AnswerType.Text -> return null
                AnswerType.Title -> return null
            }
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        fun getChoiceView(position: Int, parent: ViewGroup?, layoutInActivity:LayoutInflater): View {

            val cell = layoutInActivity.inflate(R.layout.multiple_choice_question, parent,false)
            val textview = cell.findViewById<TextView>(R.id.answerTextview)
            val constLayOut = textview.layoutParams as ConstraintLayout.LayoutParams
            textview.text = question!!.answers!![position].value

            cell.setOnClickListener {
                selected = position
                this@MyAdapter.notifyDataSetChanged()
            }

            if (selected == position) {
                constLayOut.leftMargin   =  20
            }
            else {
                constLayOut.leftMargin   =  0
            }
            textview.layoutParams = constLayOut

            return cell
        }

        fun getBoolView(position: Int, parent: ViewGroup?, layoutInActivity:LayoutInflater): View {
            val cell = layoutInActivity.inflate(R.layout.boolean_choice_question,parent,false)
            cell.findViewById<RadioGroup>(R.id.radiogroup).setOnCheckedChangeListener { group, checkedId ->
                when(checkedId){
                    R.id.radiotrue -> selected = 1
                    R.id.radiofalse -> selected = 0
                }
            }
            return cell
        }
    }
}
