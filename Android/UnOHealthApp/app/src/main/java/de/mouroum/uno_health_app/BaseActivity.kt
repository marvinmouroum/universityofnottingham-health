package de.mouroum.uno_health_app

import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {
    val prefs by lazy { (application as UONApp).prefs }
    val api by lazy { (application as UONApp).api }
}