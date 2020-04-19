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
import kotlinx.android.synthetic.main.question_container.*
import java.lang.Thread.sleep
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    var adapter:BaseAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        adapter = MyAdapter(this)
        openQuestion()

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

                    if (event.action == MotionEvent.ACTION_DOWN){
                        v.background = getDrawable(R.drawable.round_rect_violet)
                    }
                    else{
                        v.background = getDrawable(R.drawable.round_rect_purple)
                    }
                    return@setOnTouchListener true
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


    private class MyAdapter(context: Context): BaseAdapter(){

        private val mContext: Context

        init {
            mContext = context
        }

        override fun getCount(): Int {
            return 3
        }

        override fun getItem(position: Int): Any {
            return ""
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val layoutInActivity = LayoutInflater.from(mContext)
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

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }
    }
}
