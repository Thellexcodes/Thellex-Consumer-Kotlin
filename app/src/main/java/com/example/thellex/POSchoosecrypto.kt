package com.example.thellex

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class POSchoosecrypto : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var cryptoAdapter: CryptoAdapter

    private val cryptoList = listOf(
        Crypto("Bitcoin", R.drawable.icon_bitcoin),
        Crypto("USDC", R.drawable.icon_usdc),
        Crypto("USDT", R.drawable.icon_usdt),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = inflater.inflate(R.layout.fragment_p_o_schoosecrypto, container, false)

        // Initialize RecyclerView
        recyclerView = binding.findViewById(R.id.pos_crypto_list_selection)
        recyclerView.layoutManager = LinearLayoutManager(context)

//        // Initialize the adapter with the list of cryptocurrencies
        cryptoAdapter = CryptoAdapter(cryptoList){ selectedCrypto ->
            findNavController().popBackStack()
            findNavController().navigate(R.id.posAddressGeneratorFragment)
        }
        recyclerView.adapter = cryptoAdapter

        // Add spacing between items (e.g., 16dp)
        val spacing = resources.getDimensionPixelSize(R.dimen.txn_margin)
        recyclerView.addItemDecoration(ItemSpacingDecoration(spacing))

        return binding
    }
}
