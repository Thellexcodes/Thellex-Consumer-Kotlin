package com.example.thellex

import android.graphics.Rect
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.thellex.databinding.FragmentUserDashboardBinding

class UserDashboard : Fragment() {
    private var _binding: FragmentUserDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var carouselAdapter: DashboardCarouselAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Setup Carousel
        carouselAdapter = DashboardCarouselAdapter(this)
        binding.userDashboardCarousel.apply {
            adapter = carouselAdapter
            offscreenPageLimit = 1
        }

        // 2. Setup Transactions List
        val transactions = listOf(
            Transaction(
                id = "1",
                type = TransactionType.DEPOSIT,
                description = "Received USDC",
                amount = "+₦50,000",
                timestamp = "Today, 09:45 AM",
                iconResId = R.drawable.icon_usdc
            ),
            Transaction(
                id = "2",
                type = TransactionType.WITHDRAW,
                description = "Sent USDT",
                amount = "-₦20,000",
                timestamp = "Yesterday, 03:22 PM",
                iconResId = R.drawable.icon_usdt
            )
        )

        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.txn_margin)

        binding.transactionsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = TransactionsAdapter(transactions)
        }

        val spacing = resources.getDimensionPixelSize(R.dimen.txn_margin)
        binding.transactionsRecyclerView.addItemDecoration(ItemSpacingDecoration(spacing))

        binding.cashOutButton.setOnClickListener {
            showCashModal()
        }
    }

    private fun showCashModal() {
        val modal = CashOutBottomSheet()
        modal.show(parentFragmentManager, "CashModal")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
