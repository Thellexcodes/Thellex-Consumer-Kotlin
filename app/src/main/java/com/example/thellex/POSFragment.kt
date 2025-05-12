package com.example.thellex

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [POSFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class POSFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_p_o_s, container, false)

        val businessNameTextView = rootView.findViewById<TextView>(R.id.pos_business_name)
        businessNameTextView.text = businessNameTextView.text.toString().uppercase()

        val transactionRecyclerView = rootView.findViewById<RecyclerView>(R.id.transaction_recycler)
        transactionRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        val transactionList = listOf(
            PosTransaction(R.drawable.icon_usdt, "USDT", "FARIDA ABDUL", "Today, 10:09 AM", "20 USDT", "$3,890.0938", R.drawable.icon_receive_status),
            PosTransaction(R.drawable.icon_usdc, "USDC", "FARIDA ABDUL", "Today, 10:09 AM", "10 USDC", "$3,890.0938", R.drawable.icon_receive_status),
            PosTransaction(R.drawable.icon_usdt, "USDT", "ALARA Moyo", "Today, 10:09 AM", "20 USDT", "$3,890.0938", R.drawable.icon_send_status),
            PosTransaction(R.drawable.icon_usdc, "USDC", "FARIDA ABDUL", "Today, 10:09 AM", "20 USDC", "$3,890.0938", R.drawable.icon_send_status)
        )
        transactionRecyclerView.adapter = POSTransactionAdapter(transactionList)

        val itemSpacing = resources.getDimensionPixelSize(R.dimen.txn_margin)
        transactionRecyclerView.addItemDecoration(ItemSpacingDecoration(itemSpacing))

        val requestAccessButton = rootView.findViewById<LinearLayout>(R.id.request_button)
        requestAccessButton.setOnClickListener {
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.nav_pos, false) // Keeps POS fragment in the back stack
                .build()

            findNavController().navigate(R.id.action_posFragment_to_requestAccessFragment, null, navOptions)
        }
        return rootView
    }
}