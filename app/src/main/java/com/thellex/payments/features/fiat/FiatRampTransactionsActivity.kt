package com.thellex.payments.features.fiat

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.res.Resources
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.thellex.payments.R
import com.thellex.payments.core.utils.ActivityTracker
import com.thellex.payments.core.utils.Helpers
import com.thellex.payments.core.utils.Helpers.applyAdvancedSystemBarInsets
import com.thellex.payments.core.utils.Helpers.disableDecorFitsSystemWindows
import com.thellex.payments.core.utils.Helpers.setTransparentStatusBarWithWhiteIcons
import com.thellex.payments.data.model.IFiatCryptoRampTransactionsDto
import com.thellex.payments.data.model.TransactionTypeEnum
import com.thellex.payments.databinding.ActivityFiatRampTransactionsBinding
import com.thellex.payments.features.auth.viewModel.UserViewModel
import com.thellex.payments.features.auth.viewModel.UserViewModelFactory
import com.thellex.payments.features.fiat.adapters.RampTransactionsAdapter

class FiatRampTransactionsActivity : AppCompatActivity() {
    private lateinit var topBar: Helpers.TopAppBarController
    private lateinit var binding: ActivityFiatRampTransactionsBinding
    private lateinit var userViewModel: UserViewModel

    private var allTransactions: List<IFiatCryptoRampTransactionsDto> = emptyList()
    private lateinit var adapter: RampTransactionsAdapter

    private val animationDuration = 300L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFiatRampTransactionsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ActivityTracker.add(this)
        // System bar setup
        disableDecorFitsSystemWindows()
        setTransparentStatusBarWithWhiteIcons()
        binding.main.applyAdvancedSystemBarInsets()

        // Top bar
        topBar = Helpers.setupTopAppBar(
            activity = this,
            rootView = findViewById(R.id.include_top_app_bar),
            title = "Ramp Transactions"
        )

        // ViewModel setup
        userViewModel = ViewModelProvider(
            this,
            UserViewModelFactory(applicationContext)
        )[UserViewModel::class.java]

        setupTabs()
        setupRecyclerView()
        observeUser()
    }

    private fun observeUser() {
        userViewModel.authResult.observe(this) { userDto ->
            userDto?.fiatCryptoRampTransactions?.let { transactions ->
                allTransactions = transactions
                filterTransactions(binding.tabLayout.selectedTabPosition)
            }
        }
    }

    private fun setupTabs() {
        val tabTitles = listOf("Buy", "Sell")
        val darkBlue = ContextCompat.getColor(binding.root.context, R.color.darkBlue)
        val midnight = ContextCompat.getColor(binding.root.context, R.color.midnight)
        val white = ContextCompat.getColor(binding.root.context, R.color.white)

        tabTitles.forEach { title ->
            val tab = binding.tabLayout.newTab()
            tab.customView = layoutInflater.inflate(R.layout.ramp_custom_tab, null).apply {
                findViewById<TextView>(R.id.tabText).text = title

                // Set rounded drawable background with midnight color
                val bg = GradientDrawable().apply {
                    cornerRadius = resources.getDimension(R.dimen.dp_4)
                    setColor(midnight)
                    setStroke(1.dpToPx(), ContextCompat.getColor(context, R.color.transparent))
                }
                background = bg

                findViewById<TextView>(R.id.tabText).setTextColor(white)
            }
            binding.tabLayout.addTab(tab)
        }

        fun animateBackgroundDrawableColor(
            drawable: GradientDrawable,
            fromColor: Int,
            toColor: Int
        ) {
            val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), fromColor, toColor)
            colorAnimation.duration = animationDuration
            colorAnimation.addUpdateListener { animator ->
                drawable.setColor(animator.animatedValue as Int)
            }
            colorAnimation.start()
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                tab.customView?.let { customView ->
                    val bg = customView.background as? GradientDrawable ?: return
                    val currentColor = bg.color?.defaultColor ?: midnight
                    animateBackgroundDrawableColor(bg, currentColor, darkBlue)

                    val textView = customView.findViewById<TextView>(R.id.tabText)
                    textView.setTextColor(white)
                    customView.isSelected = true
                }
                filterTransactions(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                tab.customView?.let { customView ->
                    val bg = customView.background as? GradientDrawable ?: return
                    val currentColor = bg.color?.defaultColor ?: darkBlue
                    animateBackgroundDrawableColor(bg, currentColor, midnight)

                    val textView = customView.findViewById<TextView>(R.id.tabText)
                    textView.setTextColor(white)
                    customView.isSelected = false
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        // Select and highlight the first tab instantly on load with no animation
        binding.tabLayout.getTabAt(0)?.let { firstTab ->
            firstTab.select()
            firstTab.customView?.let { customView ->
                val bg = customView.background as? GradientDrawable ?: return@let
                bg.setColor(darkBlue)
                customView.findViewById<TextView>(R.id.tabText).setTextColor(white)
                customView.isSelected = true
            }
        }
    }

    private fun filterTransactions(tabPosition: Int) {
        val filtered = when (tabPosition) {
            0 -> allTransactions.filter { it.transactionType == TransactionTypeEnum.FIAT_TO_CRYPTO_DEPOSIT }
            1 -> allTransactions.filter { it.transactionType == TransactionTypeEnum.CRYPTO_TO_FIAT_WITHDRAWAL }
            else -> allTransactions
        }.sortedByDescending { it.createdAt }

        Log.d("TAGY", "all tttt $filtered")

        adapter.submitList(filtered)
    }

    private fun setupRecyclerView() {
        adapter = RampTransactionsAdapter()
        binding.transactionsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.transactionsRecyclerView.adapter = adapter
        binding.transactionsRecyclerView.addItemDecoration(VerticalSpaceItemDecoration(25))
    }
}

class VerticalSpaceItemDecoration(private val verticalSpaceHeight: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: android.graphics.Rect, view: android.view.View, parent: RecyclerView, state: RecyclerView.State
    ) {
        if (parent.getChildAdapterPosition(view) != state.itemCount - 1) {
            outRect.bottom = verticalSpaceHeight
        } else {
            outRect.bottom = 0
        }
    }
}

// Extension function to convert dp to pixels
fun Int.dpToPx(): Int =
    (this * Resources.getSystem().displayMetrics.density).toInt()
