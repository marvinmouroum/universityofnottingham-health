package de.mouroum.uno_health_app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.github.kittinunf.fuel.httpGet
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.lang.Thread.sleep
import java.net.URL
import kotlin.concurrent.thread


enum class AnswerType {
    Boolean , MultipleChoice
}
class MainActivity : AppCompatActivity() {

    private var adapter:MyAdapter? = null
    private var currentSurvey:Survey? = null

    private var onQuestion:Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        adapter = MyAdapter(this)

        var gson = Gson()
        val basic_survey = resources.openRawResource(R.raw.basic_survey)
        .bufferedReader().use { it.readText() }

        var result = gson?.fromJson(basic_survey, Survey::class.java)
        currentSurvey = result

        openQuestion()
        nextQuestion()

        thread {
            get()
        }
    }

    val fragment = GeneralFragment.newInstance(R.layout.question_container)

    fun nextQuestion(){

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

    fun openQuestion(){
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

            if(adapter?.question?.answers != null && adapter!!.selected != null){
                val answer = adapter!!.question!!.answers[adapter!!.selected!!]
                sendAnswer(currentSurvey!!.nameId, adapter!!.question!!.id,answer.id)
            }
            else if(adapter!!.selected != null){
                sendAnswer(currentSurvey!!.nameId,adapter!!.question!!.id,adapter!!.selected!!)
            }
            else{
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

    val JSON: MediaType? = "application/json; charset=utf-8".toMediaTypeOrNull()


    fun sendAnswer(nameId:String, Qid:Int,Aid:Int) {

        val client = OkHttpClient()
        val url = URL("http://192.168.178.41:8080/survey/${nameId}/answer")

        val json = """{
            "questionId":${Qid},
            "answerId":${Aid}
            }""".trimMargin()

        val body = RequestBody.create(JSON,json);


        val request = Request.Builder()
            .url(url)
            .post(body)
            .addHeader("Authorization","Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxNmVlZTYwNi00YTE0LTRlZWUtODIwYi03MzhlMDg0Yjg2NWIifQ.xoa28DDYgWUEIV_oP2-MTmuXnDprSyUE9Rs-sf-m-jPdLpY_iGrVHmfDC4Cz3fVd0btX9wvHzF7lZsvbZ0dyeA")
            .addHeader("Content-Type","application/json")
            .build()

        val response = client.newCall(request).execute()

        val responseBody = response.body!!.string()
        print(response.code)

        //Response
        println("Response Body: " + responseBody)

    }

    fun getSurvey(){
        "http://localhost:8080".httpGet().header("Authorization","Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxNmVlZTYwNi00YTE0LTRlZWUtODIwYi03MzhlMDg0Yjg2NWIifQ.xoa28DDYgWUEIV_oP2-MTmuXnDprSyUE9Rs-sf-m-jPdLpY_iGrVHmfDC4Cz3fVd0btX9wvHzF7lZsvbZ0dyeA")
    }

    fun get() {
        val client = OkHttpClient()
        val url = URL("http://192.168.178.41:8080/survey/REGULAR")

        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("Authorization","Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxNmVlZTYwNi00YTE0LTRlZWUtODIwYi03MzhlMDg0Yjg2NWIifQ.xoa28DDYgWUEIV_oP2-MTmuXnDprSyUE9Rs-sf-m-jPdLpY_iGrVHmfDC4Cz3fVd0btX9wvHzF7lZsvbZ0dyeA")
            .build()


        val response = client.newCall(request).execute()

        val responseBody = response.body!!.string()

        //Response
        println("Response Body: " + responseBody)

    }

    private class MyAdapter(context: MainActivity): BaseAdapter(){

        private val mContext: MainActivity

        var type:AnswerType = AnswerType.MultipleChoice
        var question:Question? = null
        var selected:Int? = null

        init {
            mContext = context
        }

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
            }
        }

        override fun getItem(position: Int): Any {
            return ""
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val layoutInActivity = LayoutInflater.from(mContext)

            when(type){
                AnswerType.MultipleChoice -> {
                    val cell = layoutInActivity.inflate(R.layout.multiple_choice_question,parent,false)
                    var textview = cell.findViewById<TextView>(R.id.answerTextview)
                    val constLayOut = textview.layoutParams as ConstraintLayout.LayoutParams
                    textview.text = question!!.answers!![position].value
                    cell.setOnClickListener {

                        var textview = it.findViewById<TextView>(R.id.answerTextview)
                        val constLayOut = textview.layoutParams as ConstraintLayout.LayoutParams

                        selected = position

                        this@MyAdapter.notifyDataSetChanged()
                    }

                    if(selected == position){
                        constLayOut.leftMargin   =  20
                    }
                    else{
                        constLayOut.leftMargin   =  0
                    }
                    textview.layoutParams = constLayOut

                    return cell
                }
                AnswerType.Boolean -> {
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


            return View(mContext)
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }
    }
}
