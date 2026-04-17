package com.example.luontopeli

import android.app.Application
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LuontopeliApplication : Application(){
    override fun onCreate(){
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}