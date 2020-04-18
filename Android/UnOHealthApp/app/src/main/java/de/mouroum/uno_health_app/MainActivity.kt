package de.mouroum.uno_health_app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

    }

    fun closeQuestion(){
        supportFragmentManager
            .beginTransaction()
            .remove(fragment)
            .commit()
    }
}
