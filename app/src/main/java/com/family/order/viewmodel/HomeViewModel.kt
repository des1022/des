package com.family.order.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.family.order.data.local.CategoryEntity
import com.family.order.data.local.DishEntity
import com.family.order.data.model.CartItem
import com.family.order.data.repository.CartRepository
import com.family.order.data.repository.DishRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 点菜首页：分类、上架菜品、购物车三者联动。
 * selectedCategory 用快照状态，保证切换分类时界面即时刷新。
 */
class HomeViewModel(
    private val dishRepository: DishRepository,
    private val cartRepository: CartRepository
) : ViewModel() {

    val categories: StateFlow<List<CategoryEntity>> = dishRepository.observeCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dishes: StateFlow<List<DishEntity>> = dishRepository.observeOnSale()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cartItems: StateFlow<List<CartItem>> = cartRepository.observeItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** null 表示「全部」分类 */
    var selectedCategory by mutableStateOf<String?>(null)
        private set

    fun selectCategory(name: String?) {
        selectedCategory = name
    }

    /** 当前分类筛选后的菜品（派生） */
    val visibleDishes: List<DishEntity>
        get() = if (selectedCategory == null) dishes.value
        else dishes.value.filter { it.category == selectedCategory }

    fun addToCart(dishId: Long) = viewModelScope.launch {
        cartRepository.add(dishId)
    }

    fun setCartNum(dishId: Long, num: Int) = viewModelScope.launch {
        cartRepository.setNum(dishId, num)
    }

    fun clearCart() = viewModelScope.launch {
        cartRepository.clear()
    }
}
