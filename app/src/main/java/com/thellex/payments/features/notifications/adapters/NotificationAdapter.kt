package com.thellex.payments.features.notifications.adapters

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.ThemedSpinnerAdapter.Helper
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.thellex.payments.R
import com.thellex.payments.core.utils.Helpers
import com.thellex.payments.data.model.NotificationEntity
import com.thellex.payments.data.model.NotificationGroup
import com.thellex.payments.databinding.ItemNotificationBinding
import com.thellex.payments.databinding.ItemNotificationDateBinding

class NotificationAdapter(
    private val onNotificationClicked: (NotificationEntity) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<Any>()

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_ITEM = 1
    }

    fun submitList(grouped: List<NotificationGroup>) {
        items.clear()
        grouped.forEach {
            items.add(it.date)
            items.addAll(it.notifications)
        }
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int =
        if (items[position] is String) VIEW_TYPE_HEADER else VIEW_TYPE_ITEM

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_HEADER) {
            val binding = ItemNotificationDateBinding.inflate(layoutInflater, parent, false)
            DateViewHolder(binding)
        } else {
            val binding = ItemNotificationBinding.inflate(layoutInflater, parent, false)
            NotificationViewHolder(binding, onNotificationClicked)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DateViewHolder) holder.bind(items[position] as String)
        else if (holder is NotificationViewHolder) holder.bind(items[position] as NotificationEntity)
    }

    override fun getItemCount(): Int = items.size

    class DateViewHolder(
        private val binding: ItemNotificationDateBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(date: String) {
            binding.dateText.text = date
        }
    }

    class NotificationViewHolder(
        private val binding: ItemNotificationBinding,
        private val onClick: (NotificationEntity) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private var currentNotification: NotificationEntity? = null

        init {
            binding.root.setOnClickListener {
                currentNotification?.let { notification ->
                    if (!notification.consumed) {
                        onClick(notification)
                    }
                }
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(notification: NotificationEntity) {
            currentNotification = notification
            val context = binding.root.context

            binding.titleText.text = Helpers.getFriendlyLabel(notification.title)
            binding.messageText.text = notification.message
            binding.timeText.text = Helpers.formatToDeviceTimeZone(notification.createdAt)

            // Load and tint the custom rounded background drawable
            val backgroundDrawable = ContextCompat.getDrawable(context, R.drawable.rounded_border_midnight)?.mutate()
            backgroundDrawable?.let { drawable ->
                val wrappedDrawable = DrawableCompat.wrap(drawable)
                val bgColor = if (notification.consumed) {
                    ContextCompat.getColor(context, R.color.midnight)
                } else {
                    ContextCompat.getColor(context, R.color.darkBlue)
                }
                DrawableCompat.setTint(wrappedDrawable, bgColor)
                binding.notificationContainer.background = wrappedDrawable
            }

            // Update text colors based on consumed state
            if (notification.consumed) {
                binding.titleText.setTextColor(ContextCompat.getColor(context, R.color.steelBlueGrey))
                binding.messageText.setTextColor(ContextCompat.getColor(context, R.color.steelBlueGrey))
            } else {
                binding.titleText.setTextColor(ContextCompat.getColor(context, R.color.white))
                binding.messageText.setTextColor(ContextCompat.getColor(context, R.color.white))
            }

            binding.root.isClickable = !notification.consumed
            binding.root.isEnabled = !notification.consumed
        }
    }
}
