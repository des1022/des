package com.family.order.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.roundToInt

/**
 * 菜品图片的本地存储与压缩。
 * 图片统一放在应用私有目录 filesDir/dish-images/ 下，卸载应用时自动清除。
 *
 * 压缩策略：先按宽度采样解码（避免大图一次性载入内存），
 * 再把宽度压到 800px 以内，最后按质量逐级下调直到单张 <= 200KB。
 */
class ImageStore(private val context: Context) {

    private val dir: File
        get() = File(context.filesDir, DIR_NAME)

    /**
     * 从相册 Uri 读取图片 → 压缩 → 存入私有目录。
     * @return 保存后的本地绝对路径
     */
    suspend fun saveFromUri(uri: Uri): String = withContext(Dispatchers.IO) {
        if (!dir.exists()) dir.mkdirs()

        val decoded = decodeSampledFromUri(uri, MAX_WIDTH)
            ?: throw IllegalArgumentException("无法读取所选图片")

        // 采样后宽度仍超标（例如超长图）时再做一次精确缩放
        val scaled = if (decoded.width > MAX_WIDTH) {
            val targetHeight = (decoded.height * MAX_WIDTH.toFloat() / decoded.width).roundToInt()
            Bitmap.createScaledBitmap(decoded, MAX_WIDTH, targetHeight, true)
        } else {
            decoded
        }

        val bytes = compressToLimit(scaled)
        val file = File(dir, "dish_${System.currentTimeMillis()}_${(0..9999).random()}.jpg")
        file.writeBytes(bytes)
        file.absolutePath
    }

    /**
     * 删除菜品图片。
     * 出于安全考虑，只删除 dish-images 目录内的文件。
     */
    suspend fun delete(path: String) = withContext(Dispatchers.IO) {
        if (path.isBlank()) return@withContext
        runCatching {
            val file = File(path)
            if (file.exists() && file.canonicalPath.startsWith(File(context.filesDir, DIR_NAME).canonicalPath)) {
                file.delete()
            }
        }
    }

    /** 按目标宽度采样解码，避免 OOM */
    private fun decodeSampledFromUri(uri: Uri, reqWidth: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, bounds)
        } ?: return null

        val options = BitmapFactory.Options().apply {
            inSampleSize = calculateInSampleSize(bounds.outWidth, reqWidth)
        }
        return context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        }
    }

    /** 采样率取 2 的幂，保证解码质量与内存平衡 */
    private fun calculateInSampleSize(actualWidth: Int, reqWidth: Int): Int {
        var sample = 1
        while (actualWidth / (sample * 2) >= reqWidth) {
            sample *= 2
        }
        return sample.coerceAtLeast(1)
    }

    /** 逐级降低 JPEG 质量，直到体积达标；实在压不下去就用最低质量的结果 */
    private fun compressToLimit(bitmap: Bitmap): ByteArray {
        val qualities = intArrayOf(85, 78, 70, 60, 50, 40, 30, 20)
        var result = ByteArray(0)
        for (quality in qualities) {
            val buffer = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, buffer)
            result = buffer.toByteArray()
            if (result.size <= MAX_SIZE_BYTES) break
        }
        return result
    }

    companion object {
        private const val DIR_NAME = "dish-images"
        private const val MAX_WIDTH = 800
        private const val MAX_SIZE_BYTES = 200 * 1024
    }
}

/** 按目标宽度采样解码本地图片文件，供列表展示使用 */
fun decodeSampledBitmap(path: String, reqWidth: Int, reqHeight: Int = reqWidth): Bitmap? {
    return try {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        var sample = 1
        while (bounds.outWidth / (sample * 2) >= reqWidth && bounds.outHeight / (sample * 2) >= reqHeight) {
            sample *= 2
        }
        BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample })
    } catch (e: Exception) {
        null
    }
}
