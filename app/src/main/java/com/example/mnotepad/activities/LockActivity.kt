package com.example.mnotepad.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import com.example.mnotepad.database.PasswordStorage.getPassword
import com.example.mnotepad.databinding.ActivityLockBinding

class LockActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLockBinding
    private val handler = Handler(Looper.getMainLooper())
    private val saveRunnable = object : Runnable {
        override fun run() {
            handleTextChange()
            handler.postDelayed(this, 2000)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLockBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setUpToolbar()

        handler.post(saveRunnable)
    }

    private fun handleTextChange() {
        val password = getPassword(this)
        binding.edtPassword.addTextChangedListener {  text ->
            if (text == null || text.isEmpty()) return@addTextChangedListener
            if (text.equals(password))
                startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun setUpToolbar() {
        setSupportActionBar(binding.toolbar)
    }
}