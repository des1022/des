package com.family.order.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.family.order.data.local.CategoryEntity
import com.family.order.data.local.DishEntity
import com.family.order.data.repository.DishRepository
import com.family.order.data.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** 管理端：菜品管理（密码校验、上下架、删除、分类） */
class AdminViewModel(
    private val dishRepository: DishRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val dishes: StateFlow<List<DishEntity>> = dishRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<CategoryEntity>> = dishRepository.observeCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adminPassword: StateFlow<String> = settingsRepository.adminPassword
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    /** 密码校验通过后解锁管理内容 */
    var unlocked by mutableStateOf(false)
        private set

    fun hasPassword(): Boolean = adminPassword.value.isNotBlank()

    fun verify(input: String): Boolean = input == adminPassword.value

    fun unlock() {
        unlocked = true
    }

    fun setPassword(password: String) = viewModelScope.launch {
        settingsRepository.setAdminPassword(password)
        unlocked = true
    }

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
