package com.example.mnotepad.activities

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.mnotepad.databinding.ActivityUserDetailBinding
import com.example.mnotepad.helpers.ThemeManager.applyTheme
import com.example.mnotepad.helpers.USER_DETAIL_ID
import com.example.mnotepad.helpers.showToast
import com.example.mnotepad.viewmodels.UserViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserDetailBinding
    private var userId: Int = -1
    private val userViewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        applyTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivityUserDetailBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        val view = binding.root
        setContentView(view)
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setUpToolbar()

        userId = intent.getIntExtra(USER_DETAIL_ID, -1)

        binding.progressBar.visibility = View.VISIBLE
        if (userId != -1) {
            userViewModel.userDetail.observe(this) { item ->
                item?.let {
                    binding.toolbar.setTitle(it.firstName)
                    Glide.with(this)
                        .load(it.image)
                        .into(binding.ivAvatar)
                    binding.tvLastName.text = it.lastName
                    binding.tvFirstName.text = it.firstName
                    binding.tvAge.text = it.age.toString()
                    binding.tvEmail.text = it.email
                    binding.tvPhone.text = it.phone
                    binding.tvGender.text = it.gender
                    binding.progressBar.visibility = View.GONE
                }
            }
            userViewModel.fetchUserById(userId)
        } else {
            showToast("Invalid User ID", this)
            finish()
        }
    }

    private fun setUpToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }
}
