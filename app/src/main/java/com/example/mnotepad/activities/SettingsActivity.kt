package com.example.mnotepad.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mnotepad.R
import com.example.mnotepad.databinding.ActivitySettingsBinding
import com.example.mnotepad.entities.enums.AppTheme
import com.example.mnotepad.helpers.ThemeManager
import com.example.mnotepad.helpers.ThemeManager.isThemeChange
import com.example.mnotepad.helpers.ThemeManager.toggleThemeChange

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applyTheme(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        val view = binding.root

        setContentView(view)
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setUpToolbar()
        handleButtonsClick()
    }

    private fun handleButtonsClick() {
        binding.btnTheme.setOnClickListener { handleThemeClick() }
        binding.btnPasswordSetting.setOnClickListener { handlePasswordClick() }
    }

    private fun handlePasswordClick() {
        startActivity(Intent(this, PasswordActivity::class.java))
    }

    private fun handleThemeClick() {
        val themes = AppTheme.entries.toTypedArray()
        val names = themes.map { it.displayName }.toTypedArray()
        val current = ThemeManager.getSavedTheme(this)
        val checkedIndex = themes.indexOf(current)

        AlertDialog.Builder(this)
            .setTitle("Chọn giao diện")
            .setSingleChoiceItems(names, checkedIndex) { dialog, which ->
                val selected = themes[which]
                ThemeManager.setTheme(this, selected)
                toggleThemeChange(true)
                dialog.dismiss()
            }
            .setNegativeButton("Huỷ", null)
            .show()
    }

    private fun setUpToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            if (isThemeChange) setResult(RESULT_OK)
            finish()
        }
    }


}