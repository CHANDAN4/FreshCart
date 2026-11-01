package com.app.freshcart.presentation.admin.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.freshcart.common.Resource
import com.app.freshcart.domain.model.Category
import com.app.freshcart.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminCategoriesState(
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val selectedCategory: Category? = null,
    val isAddEditOpen: Boolean = false,
    val deleteConfirmationCategory: Category? = null
)

@HiltViewModel
class AdminCategoriesViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminCategoriesState())
    val uiState: StateFlow<AdminCategoriesState> = _uiState.asStateFlow()

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            categoryRepository.getAllCategories().collect { categories ->
                _uiState.update { it.copy(categories = categories, isLoading = false) }
            }
        }
    }

    fun openAddCategory() {
        _uiState.update { it.copy(selectedCategory = null, isAddEditOpen = true) }
    }

    fun openEditCategory(category: Category) {
        _uiState.update { it.copy(selectedCategory = category, isAddEditOpen = true) }
    }

    fun dismissAddEdit() {
        _uiState.update { it.copy(selectedCategory = null, isAddEditOpen = false) }
    }

    fun saveCategory(name: String, description: String, imageUrl: String) {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) {
            viewModelScope.launch { _snackbarEvent.emit("Category name cannot be empty") }
            return
        }

        viewModelScope.launch {
            val selected = _uiState.value.selectedCategory
            val finalImage = imageUrl.trim().ifBlank {
                "https://images.unsplash.com/photo-1542838132-92c53300491e?w=500&q=80"
            }

            if (selected == null) {
                // Add new category
                val newCategory = Category(
                    id = 0,
                    name = trimmedName,
                    description = description.trim(),
                    imageUrl = finalImage
                )
                when (val result = categoryRepository.addCategory(newCategory)) {
                    is Resource.Success -> {
                        _snackbarEvent.emit("Category '${trimmedName}' added successfully")
                        dismissAddEdit()
                    }
                    is Resource.Error -> {
                        _snackbarEvent.emit(result.message)
                    }
                    is Resource.Loading -> Unit
                }
            } else {
                // Edit existing category
                val updatedCategory = selected.copy(
                    name = trimmedName,
                    description = description.trim(),
                    imageUrl = finalImage
                )
                when (val result = categoryRepository.updateCategory(updatedCategory)) {
                    is Resource.Success -> {
                        _snackbarEvent.emit("Category updated successfully")
                        dismissAddEdit()
                    }
                    is Resource.Error -> {
                        _snackbarEvent.emit(result.message)
                    }
                    is Resource.Loading -> Unit
                }
            }
        }
    }

    fun confirmDeleteCategory(category: Category) {
        _uiState.update { it.copy(deleteConfirmationCategory = category) }
    }

    fun dismissDeleteConfirmation() {
        _uiState.update { it.copy(deleteConfirmationCategory = null) }
    }

    fun deleteCategory() {
        val categoryToDelete = _uiState.value.deleteConfirmationCategory ?: return
        viewModelScope.launch {
            when (val result = categoryRepository.deleteCategory(categoryToDelete.id)) {
                is Resource.Success -> {
                    _snackbarEvent.emit("Category '${categoryToDelete.name}' deleted")
                    dismissDeleteConfirmation()
                }
                is Resource.Error -> {
                    _snackbarEvent.emit(result.message)
                }
                is Resource.Loading -> Unit
            }
        }
    }
}
