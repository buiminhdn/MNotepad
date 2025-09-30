package com.example.mnotepad.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mnotepad.adapters.HelpAdapter
import com.example.mnotepad.assets.SampleData.helpData
import com.example.mnotepad.databinding.ActivityHelpBinding

class HelpActivity : AppCompatActivity() {

    lateinit var binding: ActivityHelpBinding
    private lateinit var helpAdapter: HelpAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHelpBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        val view = binding.root
        setContentView(view)
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setUpToolbar()
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        helpAdapter = HelpAdapter(helpData)

        binding.rvHelps.layoutManager = LinearLayoutManager(this)
        binding.rvHelps.adapter = helpAdapter
    }

    private fun setUpToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
}