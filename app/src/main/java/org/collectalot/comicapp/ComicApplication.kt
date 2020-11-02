package org.collectalot.comicapp

import android.app.Application
import android.util.Log

import io.realm.Realm
import io.realm.mongodb.App
import io.realm.mongodb.AppConfiguration

lateinit var comicApp: App

class ComicApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
        val appId = getString(R.string.realm_app_id)
        comicApp = App(
            AppConfiguration.Builder(appId)
                .build()
        )
        Log.v("ComicApplication", "Initialized Realm App with id: ${comicApp.configuration.appId}")
    }
}