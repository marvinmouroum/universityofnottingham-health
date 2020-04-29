package de.mouroum.uno_health_app

import android.app.Application

class UONApp: Application() {

    companion object {
        const val HOST:String = "http://192.168.178.24:8080"
        const val TOKEN:String = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI2NzdhZTBhNi1iNzMzLTQ2ZGYtYmFkMy0yZWZkZGFiYmVlOTUiLCJleHAiOjE2MTk3MDgyNzUsImlhdCI6MTU4ODE3MjI3NSwiaXNzIjoib25lLnRyYWNraW5nLmZyYW1ld29yayJ9.0ZLDAiVkULpuM2sbv0tMJpqOwPkSKJCBQuX8Hi6uDqte-MkpZCWF60tjzRKuHUaud-QH2EygdgKcggDEAg3P0w"
    }

    override fun onCreate() {
        super.onCreate()
        // initialization code here
    }
}