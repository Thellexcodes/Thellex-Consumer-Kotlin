package com.thellex.payments.features.notifications.ui

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.thellex.payments.R
import com.thellex.payments.core.decorators.ItemSpacingDecoration
import com.thellex.payments.core.utils.ActivityTracker
import com.thellex.payments.core.utils.CustomToast
import com.thellex.payments.core.utils.ErrorHandler
import com.thellex.payments.core.utils.Helpers
import com.thellex.payments.core.utils.Helpers.applyAdvancedSystemBarInsets
import com.thellex.payments.core.utils.Helpers.disableDecorFitsSystemWindows
import com.thellex.payments.core.utils.Helpers.setTransparentStatusBarWithWhiteIcons
import com.thellex.payments.data.enums.UserErrorEnum
import com.thellex.payments.data.model.NotificationEntity
import com.thellex.payments.data.model.NotificationGroup
import com.thellex.payments.data.model.NotificationKindEnum
import com.thellex.payments.databinding.ActivityNotificationsBinding
import com.thellex.payments.features.auth.viewModel.UserViewModel
import com.thellex.payments.features.auth.viewModel.UserViewModelFactory
import com.thellex.payments.features.notifications.adapters.NotificationAdapter
import com.thellex.payments.features.notifications.adapters.SortOptionsAdapter
import com.thellex.payments.network.services.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class NotificationsActivity : AppCompatActivity() {
    private lateinit var topBar: Helpers.TopAppBarController

    private sealed class ConsumeResult {
        object Success : ConsumeResult()
        data class Failure(val userError: UserErrorEnum, val message: String? = null) : ConsumeResult()
        object AuthError : ConsumeResult()
    }

    private lateinit var binding: ActivityNotificationsBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var notificationAdapter: NotificationAdapter
    private lateinit var sortOptionsAdapter: SortOptionsAdapter
    private var isSubmitting = false

    private var currentFilter: NotificationKindEnum? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ActivityTracker.add(this)
        disableDecorFitsSystemWindows()
        setTransparentStatusBarWithWhiteIcons()
        binding.main.applyAdvancedSystemBarInsets()
        topBar = Helpers.setupTopAppBar(
            activity = this,
            rootView = findViewById(R.id.notification_top_app_bar),
            title = "NOTIFICATIONS"
        )

        userViewModel = ViewModelProvider(
            this,
            UserViewModelFactory(applicationContext)
        )[UserViewModel::class.java]

        val options = listOf( "All" to null, "Transaction" to NotificationKindEnum.Transaction,)

        sortOptionsAdapter = SortOptionsAdapter(options) { selected ->
            currentFilter = options.find { it.first == selected }?.second
            observeNotification(currentFilter)
        }

        binding.sortOptionsRecycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.sortOptionsRecycler.adapter = sortOptionsAdapter

        setupRecyclerView()
        observeNotification()
    }

    private fun setupRecyclerView() {
        notificationAdapter = NotificationAdapter { notification ->
            notification.id.let { id ->
                makeConsumeRequest(id) {
                    Log.d("TAG", "Notification consumed: $id")
                }
            }
        }

        binding.notificationsRecycler.layoutManager = LinearLayoutManager(this)
        binding.notificationsRecycler.adapter = notificationAdapter
        binding.notificationsRecycler.addItemDecoration(ItemSpacingDecoration(40))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun observeNotification(kindFilter: NotificationKindEnum? = null) {
        userViewModel.authResult.observe(this) { dto ->
            dto?.notifications?.let { allNotifications ->

                val filtered = if (kindFilter != null) {
                    allNotifications.filter { it.kind == kindFilter }
                } else {
                    allNotifications
                }

                val sorted = filtered.sortedWith(
                    compareByDescending<NotificationEntity> { it.createdAt }
                        .thenBy { it.kind.name }
                )

                if (sorted.isEmpty()) {
                    binding.sortOptionsRecycler.visibility = View.GONE
                    binding.notificationsRecycler.visibility = View.GONE
                    binding.emptyNotificationView.visibility = View.VISIBLE
                } else {
                    binding.sortOptionsRecycler.visibility = View.VISIBLE
                    binding.notificationsRecycler.visibility = View.VISIBLE
                    binding.emptyNotificationView.visibility = View.GONE

                    val grouped = groupNotificationsByDate(sorted)
                    notificationAdapter.submitList(grouped)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun groupNotificationsByDate(notifications: List<NotificationEntity>): List<NotificationGroup> {
        val now = ZonedDateTime.now(ZoneId.systemDefault()).toLocalDate()
        val yesterday = now.minusDays(1)

        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
        val displayFormatter = DateTimeFormatter.ofPattern("EEE, d MMMM") // e.g., Wed, 24 June

        return notifications.sortedByDescending { it.createdAt }
            .groupBy { notification ->
                val parsedDateTime = ZonedDateTime.parse(notification.createdAt, inputFormatter)
                val localDate = parsedDateTime.withZoneSameInstant(ZoneId.systemDefault()).toLocalDate()

                when (localDate) {
                    now -> "Today"
                    yesterday -> "Yesterday"
                    else -> localDate.format(displayFormatter)
                }
            }
            .map { (dateLabel, items) ->
                NotificationGroup(dateLabel, items)
            }
    }

    private suspend fun consumeNotification(txnID: String): ConsumeResult {
        return try {
            val token = withTimeoutOrNull(2000) {
                userViewModel.token.asFlow().first { !it.isNullOrBlank() }
            } ?: return ConsumeResult.AuthError

            val api = ApiClient.getAuthenticatedNotificationApi(token)
            val response = api.consume(txnID)

            if (response.isSuccessful) {
                val body = response.body()?.result
                if (body != null) {
                    Log.d("Notifications", "response is ${response.body()!!.result}")
                    userViewModel.updateNotificationConsumed(body)
                    ConsumeResult.Success
                } else {
                    ConsumeResult.Failure(UserErrorEnum.UNKNOWN_ERROR, "No response body")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val code = JSONObject(errorBody ?: "{}").optString("message")
                val userError = UserErrorEnum.fromCode(code)
                ConsumeResult.Failure(userError, errorBody)
            }
        } catch (e: Exception) {
            val userError = UserErrorEnum.fromCode(e.message)
            ConsumeResult.Failure(userError, e.message)
        }
    }

    private fun makeConsumeRequest(id: String, onSuccess: (() -> Unit)? = null) {
        if (isSubmitting) return
        isSubmitting = true

        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) { consumeNotification(id) }
            when (result) {
                is ConsumeResult.Success -> {
                    onSuccess?.invoke()
                }
                is ConsumeResult.AuthError -> {
                    CustomToast.show(this@NotificationsActivity, "Authentication Error", "Login to proceed")
                }
                is ConsumeResult.Failure -> {
                    ErrorHandler.handle(this@NotificationsActivity, "Error", result.userError)
                }
            }
            isSubmitting = false
        }
    }

    companion object {
        private val TAG = "TAG"
    }
}
