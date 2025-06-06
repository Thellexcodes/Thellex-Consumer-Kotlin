package com.thellex.pos

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.thellex.pos.adapters.OnboardItem
import com.thellex.pos.adapters.OnboardingAdapter
import com.thellex.pos.databinding.ActivityOnboardingBinding
import com.thellex.pos.ui.login.LoginActivity

class OnboardingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOnboardingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            view.setPadding(
                view.paddingLeft,
                systemBarsInsets.top,
                view.paddingRight,
                systemBarsInsets.bottom
            )

            insets
        }

        val slides = listOf(
            OnboardItem(R.drawable.slide2, "Instant Crypto-to-Cash Conversion", "Auto-convert crypto to your local currency. No hassle."),
            OnboardItem(R.drawable.slide4, "Crypto In. Cash Out. Easy.", "Enter an amount, scan or show a POS code, and confirm the transaction. It's quick and secure.")
        )

        binding.viewPager.adapter = OnboardingAdapter(slides)

        setupIndicators(slides.size)
        setCurrentIndicator(0)

        binding.loginBtn.setOnClickListener {
            navigateToLogin()
        }
    }

    private fun navigateToLogin() {
        // Navigate to the OnboardingActivity
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun setupIndicators(count: Int) {
        val indicators = arrayOfNulls<ImageView>(count)
        val layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(8, 0, 8, 0)
        for (i in indicators.indices) {
            indicators[i] = ImageView(applicationContext)
            indicators[i]?.setImageDrawable(
                ContextCompat.getDrawable(
                    applicationContext,
                    R.drawable.indicator_inactive
                )
            )
            indicators[i]?.layoutParams = layoutParams
            binding.indicatorLayout.addView(indicators[i])
        }
    }

    private fun setCurrentIndicator(index: Int) {
        val childCount = binding.indicatorLayout.childCount
        for (i in 0 until childCount) {
            val imageView = binding.indicatorLayout.getChildAt(i) as ImageView
            val drawableId = if (i == index) {
                R.drawable.indicator_active
            } else {
                R.drawable.indicator_inactive
            }
            imageView.setImageDrawable(
                ContextCompat.getDrawable(applicationContext, drawableId)
            )
        }
    }
}
