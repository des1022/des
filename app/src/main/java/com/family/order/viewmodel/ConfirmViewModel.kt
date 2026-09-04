package com.family.order.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.family.order.data.model.CartItem
import com.family.order.data.repository.CartRepository
import com.family.order.data.repository.OrderRepository
import com.family.order.data.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** 确认下单页：展示购物车明细、填昵称与备注、提交订单 */
class ConfirmViewModel(
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val cartItems: StateFlow<List<CartItem>> = cartRepository.observeItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rememberedNickname: StateFlow<String> = settingsRepository.nickname
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    var remark by mutableStateOf("")
        private set

    var submitting by mutableStateOf(false)
        private set

    fun setRemark(value: String) {
        remark = value
    }

    val totalNum: Int
        get() = cartItems.value.sumOf { it.num }

    val totalPrice: Double
        get() = cartItems.value.sumOf { it.price * it.num }

    fun submit(nickname: String, onSuccess: () -> Unit) = viewModelScope.launch {
        if (cartItems.value.isEmpty()) return@launch
        submitting = true
        settingsRepository.setNickname(nickname)
        orderRepository.submit(nickname, remark, cartItems.value)
        cartRepository.clear()
        submitting = false
        onSuccess()
    }
}
