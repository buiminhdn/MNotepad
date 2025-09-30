package com.example.mnotepad.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mnotepad.adapters.UserAdapter
import com.example.mnotepad.databinding.ActivityUserBinding
import com.example.mnotepad.helpers.ThemeManager.applyTheme
import com.example.mnotepad.network.UsersApi
import com.example.mnotepad.repositories.UserRepository
import com.example.mnotepad.viewmodels.UserViewModel
import com.example.mnotepad.viewmodels.UserViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserActivity : AppCompatActivity() {

    lateinit var binding: ActivityUserBinding
    private lateinit var userAdapter: UserAdapter

    private val viewModel: UserViewModel by viewModels {
        UserViewModelFactory(UserRepository(UsersApi.retrofitService))
    }

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
        userAdapter = UserAdapter()
        binding.rvUsers.layoutManager = LinearLayoutManager(this)
        binding.rvUsers.adapter = userAdapter
    }

    private fun observeUsers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.users.collect { users ->
                    userAdapter.submitList(users)
                }
            }
        }
    }
}
