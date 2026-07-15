package com.smartedu.tv.auth

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

/**
 * QR 码生成工具
 * 使用 ZXing 库生成二维码 Bitmap
 */
object QrCodeGenerator {

    /**
     * 生成二维码 Bitmap
     * @param content 要编码的字符串内容
     * @param size 图片尺寸（宽高相等，单位 px）
     * @param foregroundColor 前景色（二维码颜色）
     * @param backgroundColor 背景色
     * @return 生成的 Bitmap，失败返回 null
     */
    fun generate(
        content: String,
        size: Int = 400,
        foregroundColor: Int = Color.WHITE,
        backgroundColor: Int = Color.BLACK
    ): Bitmap? {
        return try {
            val hints = HashMap<EncodeHintType, Any>().apply {
                put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M)
                put(EncodeHintType.CHARACTER_SET, "UTF-8")
                put(EncodeHintType.MARGIN, 2)
            }

            val bitMatrix = QRCodeWriter().encode(
                content,
                BarcodeFormat.QR_CODE,
                size,
                size,
                hints
            )

            val width = bitMatrix.width
            val height = bitMatrix.height
            val pixels = IntArray(width * height)

            for (y in 0 until height) {
                for (x in 0 until width) {
                    pixels[y * width + x] = if (bitMatrix[x, y]) {
                        foregroundColor
                    } else {
                        backgroundColor
                    }
                }
            }

            Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
                setPixels(pixels, 0, width, 0, 0, width, height)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
