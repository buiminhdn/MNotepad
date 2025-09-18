package com.example.mnotepad.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mnotepad.R
import com.example.mnotepad.adapters.CategoryAdapter
import com.example.mnotepad.callbacks.OnItemCategoryClickListener
import com.example.mnotepad.databinding.ActivityCategoryBinding
import com.example.mnotepad.entities.models.Category
import com.example.mnotepad.helpers.showToast
import com.example.mnotepad.viewmodels.CategoryViewModel

class CategoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCategoryBinding
    private lateinit var categoryAdapter: CategoryAdapter
    private val categoryViewModel: CategoryViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCategoryBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupToolbar()
        setupRecyclerView()
        setupObservers()
        handleClickBack()
        handleClickAdd()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        categoryAdapter = CategoryAdapter(emptyList(), object : OnItemCategoryClickListener {
            override fun onItemUpdate(category: Category) {
                categoryViewModel.updateCategory(category)
                toast("Updated: ${category.name}")
            }

            override fun onItemDelete(id: Int) {
                categoryViewModel.deleteCategory(id)
                toast("Deleted category: $id")
            }
        })

        binding.rvCategories.apply {
            layoutManager = LinearLayoutManager(this@CategoryActivity)
            adapter = categoryAdapter
        }
    }

    private fun setupObservers() {
        categoryViewModel.categories.observe(this) { categories ->
            categoryAdapter.setCategories(categories)
        }
    }

    private fun handleClickAdd() {
        binding.btnAdd.setOnClickListener {
            val name = binding.edtName.text.toString().trim()
            if (name.isEmpty()) {
                toast("Enter category name")
                return@setOnClickListener
            }

            val exists = categoryViewModel.categories.value?.any {
                it.name.equals(name, ignoreCase = true)
            } ?: false

            if (exists) {
                toast("Category already exists: $name")
            } else {
                categoryViewModel.addCategory(Category(0, name))
                binding.edtName.text.clear()
                toast("Added: $name")
            }
        }
    }

    private fun handleClickBack() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun toast(message: String) {
        showToast(message, applicationContext)
    }
}