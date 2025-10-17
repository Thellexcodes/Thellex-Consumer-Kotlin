package com.thellex.pay.core.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.animation.ValueAnimator
import android.view.animation.OvershootInterpolator
import androidx.core.content.ContextCompat
import com.thellex.pay.R

class CheckmarkView @JvmOverloads constructor(
context: Context,
attrs: AttributeSet? = null,
defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.goldenYellow)
        style = Paint.Style.FILL
    }

    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.goldenYellow)
        style = Paint.Style.STROKE
        strokeWidth = 8f // 4dp border
    }

    private val checkmarkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.white)
        style = Paint.Style.STROKE
        strokeWidth = 10f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val checkmarkPath = Path()
    private var progress = 0f
    private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 600 // Total duration for checkmark (300ms per segment)
        interpolator = AccelerateDecelerateInterpolator()
        addUpdateListener {
            progress = it.animatedValue as Float
            invalidate()
        }
    }

    init {
        // Start with invisible checkmark
        alpha = 0f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f
        val radius = width / 2f - borderPaint.strokeWidth / 2

        // Draw circle background
        canvas.drawCircle(centerX, centerY, radius, circlePaint)

        // Draw circle border
        canvas.drawCircle(centerX, centerY, radius, borderPaint)

        // Draw checkmark
        checkmarkPath.reset()
        val checkSize = width / 3f // Size of checkmark relative to view
        val startX = centerX - checkSize * 0.6f
        val startY = centerY
        val midX = centerX - checkSize * 0.1f
        val midY = centerY + checkSize * 0.5f
        val endX = centerX + checkSize * 0.7f
        val endY = centerY - checkSize * 0.3f

        // Calculate animated path
        checkmarkPath.moveTo(startX, startY)
        if (progress > 0f) {
            // First segment (start to mid)
            val firstSegmentProgress = if (progress <= 0.5f) progress / 0.5f else 1f
            val firstX = startX + (midX - startX) * firstSegmentProgress
            val firstY = startY + (midY - startY) * firstSegmentProgress
            checkmarkPath.lineTo(firstX, firstY)

            // Second segment (mid to end)
            if (progress > 0.5f) {
                val secondSegmentProgress = (progress - 0.5f) / 0.5f
                val secondX = firstX + (endX - midX) * secondSegmentProgress
                val secondY = firstY + (endY - midY) * secondSegmentProgress
                checkmarkPath.lineTo(secondX, secondY)
            }
        }

        canvas.drawPath(checkmarkPath, checkmarkPaint)
    }

    fun startAnimation() {
        // Fade in the entire view (circle)
        animate()
            .alpha(1f)
            .setDuration(2000)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        // Delay checkmark animation for sequential effect
        postDelayed({
            animator.apply {
                // Apply OvershootInterpolator to second segment for bounce effect
                if (progress > 0.5f) {
                    interpolator = OvershootInterpolator(1.5f)
                }
                start()
            }
        }, 200) // Delay checkmark by 200ms
    }
}