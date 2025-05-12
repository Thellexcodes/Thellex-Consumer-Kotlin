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
        val view = inflater.inflate(R.layout.fragment_p_o_s, container, false)
        val businessName = view.findViewById<TextView>(R.id.pos_business_name)
        businessName.text = businessName.text.toString().uppercase()

        val recycler = view.findViewById<RecyclerView>(R.id.transaction_recycler)
        recycler.layoutManager = LinearLayoutManager(requireContext())

        val transactions = listOf(
            PosTransaction(R.drawable.icon_usdt, "USDT", "FARIDA ABDUL", "Today, 10:09 AM", "20 USDT", "$3,890.0938", R.drawable.icon_receive_status),
            PosTransaction(R.drawable.icon_usdc, "USDC", "FARIDA ABDUL", "Today, 10:09 AM", "10 USDC", "$3,890.0938", R.drawable.icon_receive_status),
            PosTransaction(R.drawable.icon_usdt, "USDT", "ALARA Moyo", "Today, 10:09 AM", "20 USDT", "$3,890.0938", R.drawable.icon_send_status),
            PosTransaction(R.drawable.icon_usdc, "USDC", "FARIDA ABDUL", "Today, 10:09 AM", "20 USDC", "$3,890.0938", R.drawable.icon_send_status)
        )
        recycler.adapter = POSTransactionAdapter(transactions)

        // Find the button and set a click listener for navigation
        val navigateButton = view.findViewById<LinearLayout>(R.id.request_button)
        navigateButton.setOnClickListener {
            findNavController().navigate(R.id.action_posFragment_to_requestAccessFragment)
        }

        // Find the button for navigating to request access and set the click listener
//        val requestAccessButton = view.findViewById<LinearLayout>(R.id.request_button)
//        requestAccessButton.setOnClickListener {
//            findNavController().navigate(R.id.action_pos_to_request_access)
//        }

        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment POSFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            POSFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }


}