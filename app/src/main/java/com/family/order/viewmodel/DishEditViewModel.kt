package com.family.order.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.net.Uri
import com.family.order.data.local.DishEntity
import com.family.order.data.repository.DishRepository
import com.family.order.util.ImageStore
import com.family.order.util.formatPrice
import kotlinx.coroutines.launch

/**
 * 新增 / 编辑菜品：表单字段用快照状态承载，编辑时记录原图路径，
 * 更换图片后由仓库层清理旧文件，避免私有目录堆积垃圾。
 */
class DishEditViewModel(
    private val dishRepository: DishRepository,
    private val imageStore: ImageStore
) : ViewModel() {

    var dishId: Long? by mutableStateOf(null)
        private set
    var name by mutableStateOf("")
        private set
    var category by mutableStateOf("")
        private set
    var imagePath by mutableStateOf("")
        private set
    /** 价格文本，空字符串表示「无价格」 */
    var priceText by mutableStateOf("")
        private set
    var desc by mutableStateOf("")
        private set
    var status by mutableStateOf(DishEntity.STATUS_ON)
        private set

    var loaded by mutableStateOf(false)
        private set

    private var originalImagePath: String = ""

    fun load(id: Long?) = viewModelScope.launch {
        if (id == null) {
            loaded = true
            return@launch
        }
        dishRepository.getById(id)?.let { d ->
            dishId = d.id
            name = d.name
            category = d.category
            imagePath = d.imagePath
            originalImagePath = d.imagePath
            priceText = if (d.price > 0) formatPrice(d.price) else ""
            desc = d.desc
            status = d.status
        }
        loaded = true
    }

    fun updateName(v: String) { name = v }
    fun updateCategory(v: String) { category = v }
    fun updatePriceText(v: String) { priceText = v.filter { it.isDigit() || it == '.' } }
    fun updateDesc(v: String) { desc = v }
    fun updateImagePath(v: String) { imagePath = v }
    fun updateStatus(v: Int) { status = v }

    fun onPickImage(uri: Uri) = viewModelScope.launch {
        val path = imageStore.saveFromUri(uri)
        imagePath = path
    }

    /** 表单校验：名称必填、分类必填、图片必填 */
    fun validate(): String? {
        if (name.isBlank()) return "请填写菜品名称"
        if (category.isBlank()) return "请选择或填写分类"
        if (imagePath.isBlank()) return "请选择菜品图片"
        if (priceText.isNotBlank()) {
            val p = priceText.toDoubleOrNull()
            if (p == null || p < 0) return "价格格式不正确"
        }
        return null
    }

    fun save(onSaved: () -> Unit) = viewModelScope.launch {
        val price = priceText.toDoubleOrNull() ?: 0.0
        val entity = DishEntity(
            id = dishId ?: 0,
            name = name.trim(),
            category = category.trim(),
            imagePath = imagePath,
            price = price,
            desc = desc.trim(),
            status = status
        )
        if (dishId == null) {
            dishRepository.add(entity)
        } else {
            dishRepository.updateWithImageCleanup(entity, originalImagePath)
        }
        onSaved()
    }
}
