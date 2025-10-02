package com.example.mnotepad.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mnotepad.adapters.CategoryAdapter
import com.example.mnotepad.callbacks.ItemMoveCallback
import com.example.mnotepad.callbacks.OnItemCategoryClickListener
import com.example.mnotepad.databinding.ActivityCategoryBinding
import com.example.mnotepad.entities.models.Category
import com.example.mnotepad.entities.models.Note
import com.example.mnotepad.helpers.ThemeManager.applyTheme
import com.example.mnotepad.helpers.showToast
import com.example.mnotepad.viewmodels.CategoryViewModel
import com.example.mnotepad.viewmodels.NoteViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CategoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCategoryBinding
    private lateinit var categoryAdapter: CategoryAdapter
    private val categoryViewModel: CategoryViewModel by viewModels()
    private val noteViewModel: NoteViewModel by viewModels()
    private var currentNotes: List<Note> = emptyList()
    var touchHelper: ItemTouchHelper? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        applyTheme(this)
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
        setupDragAndDrop()
        handleClickBack()
        handleClickAdd()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        categoryAdapter = CategoryAdapter(object : OnItemCategoryClickListener {
            override fun onItemUpdate(category: Category) {
                categoryViewModel.updateCategory(category)
                toast("Updated: ${category.name}")
            }

            override fun onItemDelete(id: Int) {
                // Get list user with deleted ID
                val currentNotes = noteViewModel.filterByDeletedCategory(id)
                if (currentNotes.isEmpty()) return

                val updatedNotes: MutableList<Note> = mutableListOf()

                for (note in currentNotes) {
                    val newCategoryIds = note.categoryIds?.filter { it != id }
                    updatedNotes.add(note.copy(categoryIds = newCategoryIds))
                }
                categoryViewModel.deleteCategory(id)
                toast("Deleted category: $id")
                noteViewModel.updateNotes(updatedNotes)
            }

            override fun onItemDrag(viewHolder: RecyclerView.ViewHolder) {
                touchHelper?.startDrag(viewHolder)
            }

            override fun onUpdateOrder() {
                updateOrderIndexes()
            }
        })

        binding.rvCategories.apply {
            layoutManager = LinearLayoutManager(this@CategoryActivity)
            adapter = categoryAdapter
        }
    }

    private fun updateOrderIndexes() {
        val updatedList = categoryAdapter.currentList
            .mapIndexed { index, category ->
                category.copy(orderIndex = index + 1)
            }

        categoryViewModel.updateCategories(updatedList)

        showToast("Order updated", applicationContext)
    }

    fun setupDragAndDrop() {
        touchHelper = ItemTouchHelper(
            ItemMoveCallback(categoryAdapter)
        )
        touchHelper?.attachToRecyclerView(binding.rvCategories)
    }

    private fun setupObservers() {
        categoryViewModel.categories.observe(this) { categories ->
            categoryAdapter.submitList(categories)
        }
        noteViewModel.filteredNotes.observe(this) { notes ->
            currentNotes = notes
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
                categoryViewModel.addCategoryWithOrder(name)
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
