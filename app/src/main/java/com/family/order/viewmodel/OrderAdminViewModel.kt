package com.family.order.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.family.order.data.repository.OrderRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** 订单管理（管理员）：修改状态、删除订单 */
class OrderAdminViewModel(
    private val orderRepository: OrderRepository
) : ViewModel() {

    val orders: StateFlow<List<com.family.order.data.local.OrderWithGoods>> = orderRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateStatus(id: Long, status: Int) = viewModelScope.launch {
        orderRepository.updateStatus(id, status)
    }

    fun deleteOrder(id: Long) = viewModelScope.launch {
        orderRepository.delete(id)
    }
}
