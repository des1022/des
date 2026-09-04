package com.family.order.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.family.order.data.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** 「我的」页：昵称、引导是否看过、管理密码是否设置、清空缓存 */
class MineViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val nickname: StateFlow<String> = settingsRepository.nickname
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val guideShown: StateFlow<Boolean> = settingsRepository.guideShown
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val hasAdminPassword: StateFlow<Boolean> = settingsRepository.adminPassword
        .map { it.isNotBlank() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setGuideShown(shown: Boolean) = viewModelScope.launch {
        settingsRepository.setGuideShown(shown)
    }

    /** 清空本地缓存（昵称、管理密码、引导标记），不影响菜品与订单数据 */
    fun clearCache() = viewModelScope.launch {
        settingsRepository.clearAll()
    }
}
