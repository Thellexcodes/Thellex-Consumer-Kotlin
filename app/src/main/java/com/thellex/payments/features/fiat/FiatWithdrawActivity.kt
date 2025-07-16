package com.thellex.payments.features.fiat

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.thellex.payments.R
import com.thellex.payments.core.utils.Helpers.applyAdvancedSystemBarInsets
import com.thellex.payments.core.utils.Helpers.disableDecorFitsSystemWindows
import com.thellex.payments.core.utils.Helpers.setTransparentStatusBarWithWhiteIcons
import com.thellex.payments.databinding.ActivityFiatWithdrawBinding
import com.thellex.payments.databinding.DialogBankSelectionBinding
import com.thellex.payments.databinding.DialogReasonSelectionBinding
import com.thellex.payments.features.fiat.adapters.BankAdapter
import com.thellex.payments.features.fiat.adapters.ReasonSelectionAdapter

class FiatWithdrawActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFiatWithdrawBinding
    private val bankList = listOf(
        "Access Bank",
        "First Bank of Nigeria",
        "Zenith Bank",
        "GTBank",
        "UBA",
        "Stanbic IBTC",
        "Fidelity Bank",
        "Wema Bank",
        "Union Bank",
        "EcoBank"
    )

    private val reasonList = listOf(
        "Gift",
        "Bills",
        "Groceries",
        "Travel",
        "Health",
        "Entertainment",
        "Housing",
        "School Fees",
        "Other"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFiatWithdrawBinding.inflate(layoutInflater)
        setContentView(binding.root)
        disableDecorFitsSystemWindows()
        setTransparentStatusBarWithWhiteIcons()
        binding.main.applyAdvancedSystemBarInsets()


        // Set click listeners for both bank selection EditTexts
        binding.edittextBankName.setOnClickListener { showBankSelectionBottomSheet(binding.edittextBankName) }
        binding.edittextReasonName.setOnClickListener { showReasonSelectionBottomSheet(binding.edittextReasonName) }
    }

    private fun showBankSelectionBottomSheet(targetEditText: EditText) {
        val dialogBinding = DialogBankSelectionBinding.inflate(layoutInflater)
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(dialogBinding.root)

        // Set up RecyclerView
        val adapter = BankAdapter(bankList) { selectedBank ->
            targetEditText.setText(selectedBank)
            bottomSheetDialog.dismiss()
        }
        dialogBinding.recyclerviewBankList.layoutManager = LinearLayoutManager(this)
        dialogBinding.recyclerviewBankList.adapter = adapter

        // Search functionality
        dialogBinding.edittextSearchBank.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                val filteredBanks = bankList.filter { it.contains(query, ignoreCase = true) }
                adapter.updateBanks(filteredBanks)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        bottomSheetDialog.show()
    }

    private fun showReasonSelectionBottomSheet(targetEditText: EditText) {
        val dialogBinding = DialogReasonSelectionBinding.inflate(layoutInflater)
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(dialogBinding.root)

        // Set up RecyclerView
        val adapter = ReasonSelectionAdapter(reasonList) { selectedItem ->
            targetEditText.setText(selectedItem)
            bottomSheetDialog.dismiss()
        }
        dialogBinding.recyclerviewReasonList.layoutManager = LinearLayoutManager(this)
        dialogBinding.recyclerviewReasonList.adapter = adapter

        bottomSheetDialog.show()
    }
}