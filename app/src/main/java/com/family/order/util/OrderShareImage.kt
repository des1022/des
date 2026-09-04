package com.family.order.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import com.family.order.data.local.OrderWithGoods
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Calendar
import java.util.Locale

/**
 * 把一张订单渲染成适合分享的精美长图（原生 Canvas 绘制，宽 1080，高度随内容增长）。
 *
 * 设计语言：米白底 + 暖红主色 + 金色点缀，温馨的家庭点单风。
 * 生成后存放在 cacheDir/share/ 下，由 FileProvider 授权给系统分享面板。
 */
object OrderShareImage {

    private const val W = 1080
    private const val MARGIN = 72
    private val BG = Color_rgb(255, 249, 241)
    private val INK = Color_rgb(58, 44, 34)
    private val GRAY = Color_rgb(158, 136, 124)
    private val LINE = Color_rgb(232, 214, 198)
    private val BRAND = Color_rgb(198, 66, 44)
    private val BRAND_SOFT = Color_rgb(250, 230, 214)
    private val GOLD = Color_rgb(201, 150, 60)
    private val WHITE = Color_rgb(255, 255, 255)

    /** 渲染分享图并保存为 PNG，返回文件；失败返回 null（绝不让调用方崩溃） */
    suspend fun render(context: Context, owg: OrderWithGoods): File? = withContext(Dispatchers.IO) {
        runCatching {
            val order = owg.order
            val goods = owg.goods
            val hasPrice = order.totalPrice > 0

            // —— 布局高度预算 ——
            val topStrip = 12
            val titleH = 120          // 「家庭点菜」主标题区
            val subH = 60             // 昵称 / 时间行
            val dividerPad = 58       // 标题分隔到清单
            val rowH = 108            // 每道菜行高
            val gapToTotal = 44
            val totalH = 150          // 合计块
            val remarkPad = if (order.remark.isNotBlank()) 92 else 0
            val footerH = 200         // 结尾祝福区（含底部贴边装饰条）
            val endPad = 6

            val h = topStrip + titleH + subH + dividerPad + goods.size * rowH +
                gapToTotal + totalH + remarkPad + footerH + endPad

            val bmp = Bitmap.createBitmap(W, h, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            canvas.drawColor(BG)

            // —— 顶部品牌条 ——
            val stripPaint = solid(BRAND)
            canvas.drawRect(0f, 0f, W.toFloat(), topStrip.toFloat(), stripPaint)

            var y = topStrip + 24f

            // —— 主标题：家庭点菜 + 「点单清单」徽标 ——
            val titlePaint = textPaint(66f, INK, true)
            val titleBase = baseline(y + titleH * 0.58f, titlePaint)
            canvas.drawText("家庭点菜", MARGIN.toFloat(), titleBase, titlePaint)

            val badgeText = "点单清单"
            val badgePaint = textPaint(34f, WHITE, true)
            val badgeH = 64f
            val badgeW = 208f
            val badgeX = W - MARGIN - badgeW.toInt()
            val badgeRect = RectF(badgeX.toFloat(), y + 12f, (badgeX + badgeW).toFloat(), y + 12f + badgeH)
            canvas.drawRoundRect(badgeRect, badgeH / 2f, badgeH / 2f, solid(BRAND))
            canvas.drawText(
                badgeText,
                badgeRect.centerX() - badgePaint.measureText(badgeText) / 2f,
                baseline(badgeRect.centerY(), badgePaint),
                badgePaint
            )
            y += titleH

            // —— 副行：下单人 + 时间 ——
            val subPaint = textPaint(36f, GRAY, false)
            val who = order.nickname.ifBlank { "家庭" }
            canvas.drawText("$who 的点单", MARGIN.toFloat(), baseline(y + 6f, subPaint), subPaint)

            val timePaint = textPaint(32f, GRAY, false)
            val timeStr = fullTime(order.createTime)
            canvas.drawText(
                timeStr,
                W - MARGIN - timePaint.measureText(timeStr),
                baseline(y + 6f, timePaint),
                timePaint
            )
            y += subH

            // —— 虚线分隔 ——
            val dash = solid(LINE).apply {
                strokeWidth = 4f
                pathEffect = DashPathEffect(floatArrayOf(18f, 14f), 0f)
            }
            y += 10f
            canvas.drawLine(MARGIN.toFloat(), y, (W - MARGIN).toFloat(), y, dash)
            y += dividerPad - 10f

            // —— 菜品清单 ——
            val namePaint = textPaint(44f, INK, false)
            val unitPaint = textPaint(30f, GRAY, false)
            val sumPaint = textPaint(42f, BRAND, true)
            goods.forEachIndexed { index, g ->
                val cy = y + rowH * 0.52f
                // 菜名（最多 13 字，超长省略）
                canvas.drawText(g.name.clip(13), MARGIN.toFloat(), baseline(cy, namePaint), namePaint)
                if (hasPrice) {
                    // 右侧：¥单价×数量 + 小计
                    val subtotal = formatPrice(g.price * g.num)
                    val subtotalW = sumPaint.measureText("¥$subtotal")
                    val subtotalX = W - MARGIN - subtotalW
                    canvas.drawText("¥$subtotal", subtotalX, baseline(cy, sumPaint), sumPaint)
                    val unitStr = "¥${formatPrice(g.price)} × ${g.num}"
                    val unitW = unitPaint.measureText(unitStr)
                    val unitX = subtotalX - 20f - unitW
                    if (unitX > MARGIN + 380f) {
                        canvas.drawText(unitStr, unitX, baseline(cy, unitPaint), unitPaint)
                    }
                } else {
                    val numStr = "× ${g.num}"
                    val numW = sumPaint.measureText(numStr)
                    canvas.drawText(numStr, W - MARGIN - numW, baseline(cy, sumPaint), sumPaint)
                }
                y += rowH
            }

            // —— 合计 ——
            y += gapToTotal
            val totalRect = RectF(MARGIN - 14f, y, (W - MARGIN + 14).toFloat(), y + totalH)
            canvas.drawRoundRect(totalRect, 26f, 26f, solid(BRAND_SOFT))

            val label = "合计  ${order.totalNum} 道"
            val labelPaint = textPaint(42f, INK, true)
            canvas.drawText(label, MARGIN.toFloat(), baseline(totalRect.centerY(), labelPaint), labelPaint)
            if (hasPrice) {
                val totalStr = "¥" + formatPrice(order.totalPrice)
                val totalPaint = textPaint(66f, BRAND, true)
                canvas.drawText(
                    totalStr,
                    W - MARGIN - totalPaint.measureText(totalStr),
                    baseline(totalRect.centerY(), totalPaint),
                    totalPaint
                )
            } else {
                val note = "时价"
                val notePaint = textPaint(40f, BRAND, false)
                canvas.drawText(note, W - MARGIN - notePaint.measureText(note), baseline(totalRect.centerY(), notePaint), notePaint)
            }
            y += totalH

            // —— 备注 ——
            if (order.remark.isNotBlank()) {
                val remarkPaint = textPaint(32f, GRAY, false)
                val rstr = "备注：${order.remark.clip(24)}"
                canvas.drawText(rstr, MARGIN.toFloat(), baseline(y + remarkPad / 2f, remarkPaint), remarkPaint)
            }
            y += remarkPad

            // —— 结尾祝福 ——
            val blessPaint = textPaint(46f, GOLD, true)
            val bless = "谢谢惠顾 · 欢迎再次光临"
            canvas.drawText(
                bless,
                (W - blessPaint.measureText(bless)) / 2f,
                baseline(y + 58f, blessPaint),
                blessPaint
            )
            val footPaint = textPaint(26f, GRAY, false)
            val foot = "「家庭点菜」为你记录 · 数据仅存本机"
            canvas.drawText(
                foot,
                (W - footPaint.measureText(foot)) / 2f,
                baseline(y + 116f, footPaint),
                footPaint
            )

            // —— 保存 ——
            val dir = File(context.cacheDir, "share").apply { mkdirs() }
            val file = File(dir, "order_${order.id}_${System.currentTimeMillis()}.png")
            file.outputStream().use { out ->
                if (!bmp.compress(Bitmap.CompressFormat.PNG, 100, out)) throw IllegalStateException("PNG 编码失败")
            }
            if (!bmp.isRecycled) bmp.recycle()
            file
        }.getOrNull()
    }

    // —— 小工具 ——

    private fun Color_rgb(r: Int, g: Int, b: Int) = android.graphics.Color.rgb(r, g, b)

    private fun solid(color: Int) = Paint().apply {
        this.color = color
        isAntiAlias = true
    }

    private fun textPaint(size: Float, color: Int, bold: Boolean) = Paint().apply {
        this.color = color
        textSize = size
        isAntiAlias = true
        typeface = Typeface.create("sans-serif", if (bold) Typeface.BOLD else Typeface.NORMAL)
        style = Paint.Style.FILL
    }

    /** 让文本以 cy 为视觉中心居中对齐时需要的 baseline */
    private fun baseline(cy: Float, paint: Paint): Float {
        val fm = paint.fontMetrics
        return cy - (fm.ascent + fm.descent) / 2f
    }

    private fun String.clip(maxChars: Int): String =
        if (length > maxChars) take(maxChars - 1) + "…" else this

    private fun fullTime(ts: Long): String {
        if (ts <= 0L) return ""
        val c = Calendar.getInstance().apply { timeInMillis = ts }
        val hm = String.format(Locale.getDefault(), "%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE))
        val md = "${c.get(Calendar.MONTH) + 1}月${c.get(Calendar.DAY_OF_MONTH)}日"
        return if (c.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR)) "$md $hm" else "${c.get(Calendar.YEAR)}年$md $hm"
    }
}
