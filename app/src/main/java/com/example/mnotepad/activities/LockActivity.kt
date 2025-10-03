package com.example.mnotepad.activities

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import com.example.mnotepad.database.PasswordStorage.getPassword
import com.example.mnotepad.databinding.ActivityLockBinding
import com.example.mnotepad.helpers.ThemeManager.applyTheme

class LockActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLockBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        applyTheme(this)
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
        handleCheckbox()
        handleTextChange()
    }

    private fun handleCheckbox() {
        binding.ckbHidePassword.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.edtPassword.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            } else {
                binding.edtPassword.inputType = InputType.TYPE_CLASS_TEXT
            }
        }
    }

    private fun handleTextChange() {
        val password = getPassword(this)
        binding.edtPassword.addTextChangedListener { text ->
            if (text == null || text.isEmpty()) return@addTextChangedListener
            if (text.toString() == password) {
                val i = Intent(this@LockActivity, MainActivity::class.java)
                finish()
                startActivity(i)
            }
        }
    }

    private fun setUpToolbar() {
        setSupportActionBar(binding.toolbar)
    }
}
