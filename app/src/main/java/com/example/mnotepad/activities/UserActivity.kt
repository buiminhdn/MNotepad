package com.example.mnotepad.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mnotepad.adapters.UserAdapter
import com.example.mnotepad.databinding.ActivityUserBinding
import com.example.mnotepad.helpers.ThemeManager.applyTheme
import com.example.mnotepad.helpers.USER_DETAIL_ID
import com.example.mnotepad.viewmodels.UserViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserActivity : AppCompatActivity() {

    lateinit var binding: ActivityUserBinding
    private lateinit var userAdapter: UserAdapter

    private val userViewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        applyTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivityUserBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        val view = binding.root
        setContentView(view)
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setUpToolbar()
        setUpRecyclerView()
        observeUsers()
    }

    private fun setUpToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setUpRecyclerView() {
        userAdapter = UserAdapter(::openUserDetail)
        binding.rvUsers.layoutManager = LinearLayoutManager(this)
        binding.rvUsers.adapter = userAdapter
    }

    private fun openUserDetail(userId: Int) {
        startActivity(
            Intent(this, UserDetailActivity::class.java).apply {
                putExtra(USER_DETAIL_ID, userId)
            }
        )
    }

    private fun observeUsers() {
        userViewModel.users.observe(this) { users ->
            userAdapter.submitList(users)
        }
    }
}
