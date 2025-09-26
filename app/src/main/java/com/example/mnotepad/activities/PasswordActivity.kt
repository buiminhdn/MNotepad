package com.example.mnotepad.activities

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.mnotepad.R
import com.example.mnotepad.assets.OptionsData.Companion.unlockTimes
import com.example.mnotepad.database.PasswordStorage.getPassword
import com.example.mnotepad.database.PasswordStorage.getRecoveryEmail
import com.example.mnotepad.database.PasswordStorage.getUnlockTime
import com.example.mnotepad.database.PasswordStorage.isSetPassword
import com.example.mnotepad.database.PasswordStorage.removePassword
import com.example.mnotepad.database.PasswordStorage.setPassword
import com.example.mnotepad.database.PasswordStorage.setRecoveryEmail
import com.example.mnotepad.database.PasswordStorage.setUnlockTime
import com.example.mnotepad.databinding.ActivityPasswordBinding
import com.example.mnotepad.helpers.ThemeManager
import com.example.mnotepad.helpers.showToast
import com.example.mnotepad.workers.PasswordWorker
import java.util.concurrent.TimeUnit


class PasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPasswordBinding

    var tvCurrentPassword: TextView? = null
    var edtCurrentPassword: EditText? = null
    var edtNewPassword: EditText? = null
    var edtRepeatNewPassword: EditText? = null
    var edtRecoveryEmail: EditText? = null
    var errorMessage: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applyTheme(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPasswordBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setUpToolbar()
        handleBtnClicks()
    }


    private fun handleBtnClicks() {
        binding.btnSetPassword.setOnClickListener { handleSetPassword() }

        if (isSetPassword(this)) {
            binding.btnRemovePassword.setOnClickListener { handleRemovePassword() }
            binding.btnUnlockTime.setOnClickListener { handleUnlockTime() }
        } else {
            binding.btnRemovePassword.isEnabled = false
            binding.btnUnlockTime.isEnabled = false
        }
    }

    private fun handleUnlockTime() {
            val names = unlockTimes.map { it.second }.toTypedArray()
            val periods = unlockTimes.map { it.first }.toTypedArray()
            val current = getUnlockTime(this)
            val checkedIndex = periods.indexOf(current)

            AlertDialog.Builder(this)
                .setTitle("Unlock time")
                .setSingleChoiceItems(names, checkedIndex) { dialog, which ->
                    val selected = periods[which]
                    setUnlockTime(this, selected)
                    reSchedulePeriodicSyncWork(selected)
                    dialog.dismiss()
                    showToast("Set period successfully", this)
                }
                .setNegativeButton("Huỷ", null)
                .show()
    }

    private fun reSchedulePeriodicSyncWork(period: Int) {
        val request = OneTimeWorkRequestBuilder<PasswordWorker>()
            .setInitialDelay(period.toLong(), TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniqueWork(
            "PasswordWorker",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    private fun handleRemovePassword() {
        val builder = AlertDialog.Builder(this)

        val customLayout: View = layoutInflater.inflate(R.layout.dialog_remove_password, null)

        tvCurrentPassword = customLayout.findViewById(R.id.tvCurrentPassword)
        edtCurrentPassword = customLayout.findViewById(R.id.edtCurrentPassword)
        errorMessage = customLayout.findViewById(R.id.errorMessage)

        builder.setView(customLayout)

        builder.setPositiveButton("OK", null)
        builder.setNegativeButton("CANCEL") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()

        dialog.setOnShowListener {
            val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener {
                if (edtCurrentPassword != null && edtCurrentPassword?.text.toString() != getPassword(
                        this
                    )
                ) {
                    errorMessage?.text = "Incorrect password"
                    errorMessage?.visibility = View.VISIBLE
                    return@setOnClickListener
                }
                removePassword(this)
                dialog.dismiss()
                showToast("Remove password successfully", this)
                binding.btnRemovePassword.isEnabled = false
                binding.btnUnlockTime.isEnabled = false
            }
        }

        dialog.show()
    }

    private fun handleSetPassword() {
        val builder = AlertDialog.Builder(this)

        val customLayout: View = layoutInflater.inflate(R.layout.dialog_set_password, null)

        tvCurrentPassword = customLayout.findViewById(R.id.tvCurrentPassword)
        edtCurrentPassword = customLayout.findViewById(R.id.edtCurrentPassword)
        edtNewPassword = customLayout.findViewById(R.id.edtNewPassword)
        edtRepeatNewPassword = customLayout.findViewById(R.id.edtRepeatNewPassword)
        edtRecoveryEmail = customLayout.findViewById(R.id.recoveryEmail)
        errorMessage = customLayout.findViewById(R.id.errorMessage)

        val currentPassword = getPassword(this)
        if (currentPassword == "") {
            tvCurrentPassword?.visibility = View.GONE
            edtCurrentPassword?.visibility = View.GONE
        }
        edtRecoveryEmail?.setText(getRecoveryEmail(this))


        builder.setView(customLayout)

        builder.setPositiveButton("OK", null)
        builder.setNegativeButton("CANCEL") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()

        dialog.setOnShowListener {
            val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener {
                if (currentPassword != null && edtCurrentPassword?.text.toString() != currentPassword) {
                    errorMessage?.text = "Incorrect old password"
                    errorMessage?.visibility = View.VISIBLE
                    return@setOnClickListener
                }
                if (edtNewPassword?.text.isNullOrEmpty() || edtRepeatNewPassword?.text.isNullOrEmpty() || edtRepeatNewPassword!!.text.toString() != edtNewPassword!!.text.toString()) {
                    errorMessage?.text = "Invalid new password"
                    errorMessage?.visibility = View.VISIBLE
                    return@setOnClickListener
                }
                if (edtRecoveryEmail?.text.isNullOrEmpty()) {
                    errorMessage?.text = "Recovery Email is required"
                    errorMessage?.visibility = View.VISIBLE
                    return@setOnClickListener
                }
                setPassword(this, edtNewPassword!!.text.toString())
                setRecoveryEmail(this, edtRecoveryEmail!!.text.toString())
                dialog.dismiss()
                showToast("Update password successfully", this)
                binding.btnRemovePassword.isEnabled = true
                binding.btnUnlockTime.isEnabled = true
            }
        }

        dialog.show()
    }

    private fun setUpToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
}


