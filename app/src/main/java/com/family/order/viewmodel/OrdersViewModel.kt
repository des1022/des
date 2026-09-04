package com.family.order.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.family.order.data.local.OrderEntity
import com.family.order.data.local.OrderWithGoods
import com.family.order.data.repository.OrderRepository
import com.family.order.data.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * 订单页：我的订单 + 待做清单两种视图。
 * 「待做清单」合并统计所有「待制作(0)+制作中(1)」订单的菜品数量，供厨房直接看备菜量。
 */
class OrdersViewModel(
    private val orderRepository: OrderRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val allOrders: StateFlow<List<OrderWithGoods>> = orderRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val nickname: StateFlow<String> = settingsRepository.nickname
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    /** 0 = 我的订单，1 = 待做清单 */
    val tab: MutableStateFlow<Int> = kotlinx.coroutines.flow.MutableStateFlow(0)

    /** 当前用户提交的订单（按昵称过滤） */
    val myOrders: List<OrderWithGoods>
        get() = allOrders.value.filter { it.order.nickname == nickname.value }

    /** 待制作 + 制作中 的菜品合并统计 */
    val todoItems: List<TodoDish>
        get() {
            val map = LinkedHashMap<String, TodoDish>()
            allOrders.value.forEach { owg ->
                if (owg.order.status == OrderEntity.STATUS_WAIT ||
                    owg.order.status == OrderEntity.STATUS_COOKING
                ) {
                    owg.goods.forEach { g ->
                        val key = g.name
                        val exist = map[key]
                        if (exist == null) {
                            map[key] = TodoDish(g.name, g.imagePath, g.num)
                        } else {
                            map[key] = exist.copy(totalNum = exist.totalNum + g.num)
                        }
                    }
                }
            }
            return map.values.toList()
        }
}

/** 待做清单里的合并菜品项 */
data class TodoDish(
    val name: String,
    val imagePath: String,
    val totalNum: Int
)
