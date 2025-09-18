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

        handleClickBack()
        setupViewModel()
        handleClickAdd()
    }

    private fun handleClickAdd() {
        binding.btnAdd.setOnClickListener {
            val name = binding.edtName.text.toString().trim()
            if (name.isNotEmpty()) {
                val newCategory = Category(0, name)
                categoryViewModel.addCategory(newCategory)
                binding.edtName.text.clear()
                showToast("Added: $name", applicationContext)
            } else {
                showToast("Enter category name", applicationContext)
            }
        }
    }

    private fun setupViewModel() {
        categoryAdapter = CategoryAdapter(emptyList(), object : OnItemCategoryClickListener {
            override fun onItemUpdate(category: Category) {
                categoryViewModel.updateCategory(category)
                showToast("Updated: ${category.name}", applicationContext)
            }

            override fun onItemDelete(id: Int) {
                categoryViewModel.deleteCategory(id)
                showToast("Delete Category: $id", applicationContext)
            }

        })

        binding.rvCategories.layoutManager = LinearLayoutManager(this)
        binding.rvCategories.adapter = categoryAdapter

        categoryViewModel.categories.observe(this) {
                categories -> categoryAdapter.setCategories(categories)
        }
    }

    private fun handleClickBack() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
}