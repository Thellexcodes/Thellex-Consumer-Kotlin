package com.thellex.payments.core.decorators

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.thellex.payments.databinding.AttentionGrabberBinding

class AttentionGrabberView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding: AttentionGrabberBinding =
        AttentionGrabberBinding.inflate(LayoutInflater.from(context), this, true)
    private var onActionClick: (() -> Unit)? = null
    private var onCloseClick: (() -> Unit)? = null

    init {
        binding.closeButton.setOnClickListener {
            visibility = View.GONE
            onCloseClick?.invoke()
        }
    }

    fun setAttentionGrabber(
        message: String,
        actionText: String,
        iconResId: Int,
        onActionClick: () -> Unit,
        onCloseClick: () -> Unit = {}
    ) {
        binding.message.text = message
//        binding.actionButton.text = actionText
        binding.icon.setImageResource(iconResId)
        this.onActionClick = onActionClick
        this.onCloseClick = onCloseClick
//        binding.actionButton.setOnClickListener { onActionClick.invoke() }
        visibility = View.VISIBLE
    }

    fun hide() {
        visibility = View.GONE
    }
}