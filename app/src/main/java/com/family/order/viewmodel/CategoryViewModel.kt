package com.family.order.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.family.order.data.local.CategoryEntity
import com.family.order.data.repository.DishRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** 分类管理：新增、删除（含菜品转移） */
class CategoryViewModel(
    private val dishRepository: DishRepository
) : ViewModel() {

    val categories: StateFlow<List<CategoryEntity>> = dishRepository.observeCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** 各分类下的菜品数量，用于删除前判断是否需要转移 */
    val dishCounts: StateFlow<Map<String, Int>> = dishRepository.observeAll()
        .map { list -> list.groupingBy { it.category }.eachCount() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    /** 新增分类，重名返回 false */
    suspend fun addCategory(name: String): Boolean {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return false
        return dishRepository.addCategory(trimmed)
    }

    /**
     * 删除分类。若 targetName 不为空，先把该分类下菜品整体迁移到目标分类，再删除分类本身。
     */
    fun deleteCategory(category: CategoryEntity, targetName: String?) = viewModelScope.launch {
        if (!targetName.isNullOrBlank() && targetName != category.name) {
            dishRepository.moveCategory(category.name, targetName)
        }
        dishRepository.deleteCategory(category.id)
    }
}
