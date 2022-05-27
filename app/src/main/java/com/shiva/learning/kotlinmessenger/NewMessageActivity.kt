package com.shiva.learning.kotlinmessenger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class NewMessageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)

        supportActionBar?.title=getString(R.string.select_user)
    }
}