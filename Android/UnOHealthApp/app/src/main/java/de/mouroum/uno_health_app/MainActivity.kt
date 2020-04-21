package de.mouroum.uno_health_app

import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.beust.klaxon.Klaxon
import kotlinx.android.synthetic.main.question_container.*
import java.lang.Thread.sleep
import kotlin.concurrent.thread

enum class AnswerType {
    Boolean , MultipleChoice
}
class MainActivity : AppCompatActivity() {

    private var adapter:MyAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        adapter = MyAdapter(this)
        openQuestion()

        val result = Klaxon()
            .parse<Survey>("""
    {
    "id": 35,
    "questions": [
        {
            "id": 5,
            "question": "What is your age?",
            "type": "CHOICE",
            "order": 1,
            "specificQuestion":{
                "answers": [
                    {
                        "id": 1,
                        "value": "18 - 24"
                    },
                    {
                        "id": 2,
                        "value": "25 - 44"
                    },
                    {
                        "id": 3,
                        "value": "45 - 64"
                    },
                    {
                        "id": 4,
                        "value": "65 +"
                    }
                ],
                "multiple": false,
                },
            "defaultAnswer": null,
            "container": null
        }],
    "nameId": "BASIC",
    "version": 0
}
    """)

        assert(result?.nameId == "BASIC")

    }

    val fragment = GeneralFragment.newInstance(R.layout.question_container)


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
        if(adapter?.type == AnswerType.MultipleChoice){
            adapter?.type = AnswerType.Boolean
        }
        else{
            adapter?.type = AnswerType.MultipleChoice
        }

        adapter?.notifyDataSetChanged()
    }

    private class MyAdapter(context: Context): BaseAdapter(){

        private val mContext: Context

        var type:AnswerType = AnswerType.MultipleChoice

        init {
            mContext = context
        }

        override fun getCount(): Int {
            when(type){
                AnswerType.MultipleChoice -> return 3
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
                    cell.setOnClickListener {

                        var textview = it.findViewById<TextView>(R.id.answerTextview)
                        val constLayOut = textview.layoutParams as ConstraintLayout.LayoutParams

                        if(constLayOut.leftMargin != 20){
                            constLayOut.leftMargin   =  20
                        }
                        else{
                            constLayOut.leftMargin   =  0
                        }


                        textview.layoutParams = constLayOut
                    }
                    return cell
                }
                AnswerType.Boolean -> {
                    val cell = layoutInActivity.inflate(R.layout.boolean_choice_question,parent,false)
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
