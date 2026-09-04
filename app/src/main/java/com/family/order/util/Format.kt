package com.family.order.util

import java.util.Calendar
import java.util.Locale

/** 价格：整数不显示小数，小数保留两位 */
fun formatPrice(value: Double): String {
    if (value <= 0.0) return "0"
    return if (value % 1.0 == 0.0) {
        value.toLong().toString()
    } else {
        String.format(Locale.getDefault(), "%.2f", value)
    }
}

/** 时间：今天只显示时分，同年显示月日，跨年补年份 */
fun formatTime(timestamp: Long): String {
    if (timestamp <= 0L) return ""
    val target = Calendar.getInstance().apply { timeInMillis = timestamp }
    val now = Calendar.getInstance()
    val hm = String.format(
        Locale.getDefault(),
        "%02d:%02d",
        target.get(Calendar.HOUR_OF_DAY),
        target.get(Calendar.MINUTE)
    )
    val sameYear = target.get(Calendar.YEAR) == now.get(Calendar.YEAR)
    val sameDay = sameYear && target.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)
    return when {
        sameDay -> "今天 $hm"
        sameYear -> "${target.get(Calendar.MONTH) + 1}月${target.get(Calendar.DAY_OF_MONTH)}日 $hm"
        else -> "${target.get(Calendar.YEAR)}年${target.get(Calendar.MONTH) + 1}月${target.get(Calendar.DAY_OF_MONTH)}日 $hm"
    }
}

/** 订单状态文案 */
fun orderStatusText(status: Int): String = when (status) {
    0 -> "待制作"
    1 -> "制作中"
    2 -> "已完成"
    else -> "待制作"
}
