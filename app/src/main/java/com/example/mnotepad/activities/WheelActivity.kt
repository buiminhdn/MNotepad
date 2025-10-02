package com.example.mnotepad.activities

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mnotepad.databinding.ActivityWheelBinding
import com.example.mnotepad.helpers.ThemeManager.applyTheme
import java.util.Random

class WheelActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWheelBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        applyTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivityWheelBinding.inflate(layoutInflater)
        val view = binding.root
        enableEdgeToEdge()
        setContentView(view)
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setUpToolbar()
        handleSpinButton()
    }

    private fun handleSpinButton() {
        val random = Random()

        binding.btnSpin.setOnClickListener {
            it.isEnabled = false

            var spin = random.nextInt(20) + 10

            spin = spin * 36

            object : CountDownTimer(spin.toLong() * 20, 1) {

                override fun onTick(l: Long) {
                    Log.e("Log Time", l.toString())
                    val rotation = binding.ivWheel.rotation + (l / 100)
                    binding.ivWheel.rotation = rotation
                }

                override fun onFinish() {
                    it.isEnabled = true
                }
            }.start()
        }
    }

    private fun setUpToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }
}