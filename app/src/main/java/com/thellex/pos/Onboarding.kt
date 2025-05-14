package com.thellex.pos

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.thellex.pos.databinding.ActivityOnboardingBinding
import com.thellex.pos.ui.login.LoginActivity

class Onboarding : AppCompatActivity() {
    private lateinit var binding: ActivityOnboardingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val slides = listOf(
            OnboardItem(R.drawable.slide1, "Accept Crypto Instantly at Checkout", "Easily accept Bitcoin, Ethereum, and stablecoins through your POS."),
            OnboardItem(R.drawable.slide2, "Instant Crypto-to-Cash Conversion", "Auto-convert crypto to your local currency. No hassle."),
            OnboardItem(R.drawable.slide3, "Quick Setup, Low Fees & 24/7 Help", "Start accepting payments in minutes. Real-time support.")
        )

        binding.viewPager.adapter = OnboardingAdapter(slides)

        setupIndicators(slides.size)
        setCurrentIndicator(0)

        binding.loginBtn.setOnClickListener {
            navigateToLogin()
        }

        binding.signUpBtn.setOnClickListener {
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
