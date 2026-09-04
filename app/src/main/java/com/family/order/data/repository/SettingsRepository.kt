package com.family.order.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

// DataStore 实例：必须定义在文件顶层，保证全局唯一
private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "family_order_settings")

/**
 * 轻量配置存储：昵称、管理密码、引导是否已看过。
 * 全部存本机，不联网。
 */
class SettingsRepository(private val context: Context) {

    private object Keys {
        val NICKNAME = stringPreferencesKey("nickname")
        val ADMIN_PASSWORD = stringPreferencesKey("admin_password")
        val GUIDE_SHOWN = booleanPreferencesKey("guide_shown")
    }

    private val safeData = context.settingsDataStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences()) else throw e
        }

    val nickname: Flow<String> = safeData.map { it[Keys.NICKNAME].orEmpty() }

    val adminPassword: Flow<String> = safeData.map { it[Keys.ADMIN_PASSWORD].orEmpty() }

    /** 首启引导是否展示过 */
    val guideShown: Flow<Boolean> = safeData.map { it[Keys.GUIDE_SHOWN] ?: false }

    suspend fun setNickname(value: String) {
        context.settingsDataStore.edit { it[Keys.NICKNAME] = value.trim() }
    }

    suspend fun setAdminPassword(value: String) {
        context.settingsDataStore.edit { it[Keys.ADMIN_PASSWORD] = value }
    }

    suspend fun setGuideShown(shown: Boolean) {
        context.settingsDataStore.edit { it[Keys.GUIDE_SHOWN] = shown }
    }

    /** 清空本机配置（不影响菜品与订单数据） */
    suspend fun clearAll() {
        context.settingsDataStore.edit { it.clear() }
    }
}
