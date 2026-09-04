package com.family.order.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.family.order.data.local.CategoryEntity
import com.family.order.data.local.DishEntity
import com.family.order.data.repository.DishRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** 管理端：菜品管理（上下架、删除、分类）。无需密码，进入即用。 */
class AdminViewModel(
    private val dishRepository: DishRepository
) : ViewModel() {

    val dishes: StateFlow<List<DishEntity>> = dishRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<CategoryEntity>> = dishRepository.observeCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleStatus(dish: DishEntity) = viewModelScope.launch {
        val next = if (dish.status == DishEntity.STATUS_ON) DishEntity.STATUS_OFF else DishEntity.STATUS_ON
        dishRepository.setStatus(dish.id, next)
    }

    fun deleteDish(dish: DishEntity) = viewModelScope.launch {
        dishRepository.delete(dish.id)
    }

    fun addCategory(name: String) = viewModelScope.launch {
        dishRepository.addCategory(name.trim())
    }
}
