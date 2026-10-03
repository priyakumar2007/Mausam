package com.example

import android.app.Application
import com.example.data.local.MausamDatabase

class MausamApp : Application() {
  val database: MausamDatabase by lazy {
    MausamDatabase.getInstance(this)
  }

  override fun onCreate() {
    super.onCreate()
    instance = this
  }

  companion object {
    lateinit var instance: MausamApp
      private set
  }
}
