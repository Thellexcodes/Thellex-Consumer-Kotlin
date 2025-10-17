package com.thellex.pay.features.fiat

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.thellex.pay.R
import com.thellex.pay.core.utils.Helpers.applyAdvancedSystemBarInsets
import com.thellex.pay.core.utils.Helpers.disableDecorFitsSystemWindows
import com.thellex.pay.core.utils.Helpers.setTransparentStatusBarWithWhiteIcons
import com.thellex.pay.databinding.ActivityFiatWithdrawBinding
import com.thellex.pay.databinding.DialogBankSelectionBinding
import com.thellex.pay.databinding.DialogReasonSelectionBinding
import com.thellex.pay.features.fiat.adapters.BankAdapter
import com.thellex.pay.features.fiat.adapters.ReasonSelectionAdapter

class FiatWithdrawActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFiatWithdrawBinding

    private val bankList = listOf(
        "Access Bank", "First Bank of Nigeria", "Zenith Bank", "GTBank", "UBA",
        "Stanbic IBTC", "Fidelity Bank", "Wema Bank", "Union Bank", "EcoBank"
    )

    private val reasonList = listOf(
        "Gift", "Bills", "Groceries", "Travel", "Health",
        "Entertainment", "Housing", "School Fees", "Other"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFiatWithdrawBinding.inflate(layoutInflater)
        setContentView(binding.root)
        disableDecorFitsSystemWindows()
        setTransparentStatusBarWithWhiteIcons()
        binding.main.applyAdvancedSystemBarInsets()

        binding.edittextBankName.setOnClickListener {
            showBankSelectionBottomSheet(binding.edittextBankName)
        }

        binding.edittextReasonName.setOnClickListener {
            showReasonSelectionBottomSheet(binding.edittextReasonName)
        }

        listOf(
            binding.edittextBankName,
            binding.edittextAccountNumber,
            binding.edittextWithdrawAmount,
            binding.edittextReasonName
        ).forEach { it.addTextChangedListener(SimpleTextWatcher {
            //TODO: CLEAR ERROR HERE
        }) }

//        binding.buttonSubmit.setOnClickListener {
//            if (validateInputs()) {
//                performWithdrawal()
//            }
//        }
    }

    private fun validateInputs(): Boolean {
        var ok = true
        val bank = binding.edittextBankName.text.toString().trim()
        val account = binding.edittextAccountNumber.text.toString().trim()
        val amountStr = binding.edittextWithdrawAmount.text.toString().trim()
        val reason = binding.edittextReasonName.text.toString().trim()

//        clearErrorDrawable(binding.edittextBankName)
//        clearErrorDrawable(binding.edittextAccountNumber)
//        clearErrorDrawable(binding.edittextWithdrawAmount)
//        clearErrorDrawable(binding.edittextReasonName)

        if (bank.isEmpty()) {
            showErrorDrawable(binding.edittextBankName)
            ok = false
        }

        if (account.isEmpty() || !account.matches(Regex("\\d{10,12}"))) {
            showErrorDrawable(binding.edittextAccountNumber)
            ok = false
        }

//        val amount = amountStr.toDoubleOrNull()
//        val available = binding.textAvailableBalance.text
//            .substringAfter(':').trim().substringBefore(" ")
//            .toDoubleOrNull() ?: 0.0
//
//        if (amountStr.isEmpty() || amount == null || amount <= 0 || amount > available) {
//            showErrorDrawable(binding.edittextWithdrawAmount)
//            ok = false
//        }

        if (reason.isEmpty()) {
            showErrorDrawable(binding.edittextReasonName)
            ok = false
        }

        if (!ok) {
            AlertDialog.Builder(this)
                .setTitle("Missing or invalid inputs")
                .setMessage("Please correct the highlighted fields and try again.")
                .setPositiveButton("OK", null)
                .show()
        }

        return ok
    }


    private fun showBankSelectionBottomSheet(targetEditText: EditText) {
        val dialogBinding = DialogBankSelectionBinding.inflate(layoutInflater)
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(dialogBinding.root)

        val adapter = BankAdapter(bankList) { selectedBank ->
            targetEditText.setText(selectedBank)
            bottomSheetDialog.dismiss()
        }
        dialogBinding.recyclerviewBankList.layoutManager = LinearLayoutManager(this)
        dialogBinding.recyclerviewBankList.adapter = adapter

        dialogBinding.edittextSearchBank.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                adapter.updateBanks(bankList.filter { it.contains(query, ignoreCase = true) })
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        bottomSheetDialog.show()
    }

    private fun showReasonSelectionBottomSheet(targetEditText: EditText) {
        val dialogBinding = DialogReasonSelectionBinding.inflate(layoutInflater)
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(dialogBinding.root)

        val adapter = ReasonSelectionAdapter(reasonList) { selectedItem ->
            targetEditText.setText(selectedItem)
            bottomSheetDialog.dismiss()
        }
        dialogBinding.recyclerviewReasonList.layoutManager = LinearLayoutManager(this)
        dialogBinding.recyclerviewReasonList.adapter = adapter

        bottomSheetDialog.show()
    }

    private fun showErrorDrawable(editText: EditText) {
        val errorDrawable = ContextCompat.getDrawable(this, R.drawable.bg_edittext_error)
        errorDrawable?.setBounds(0, 0, errorDrawable.intrinsicWidth, errorDrawable.intrinsicHeight)
        editText.setCompoundDrawables(null, null, errorDrawable, null)
    }

    private fun clearErrorDrawable() {
//        editText.setCompoundDrawables(null, null, null, null)
    }

    class SimpleTextWatcher(val afterTextChangedAction: (Editable?) -> Unit) : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
        override fun afterTextChanged(s: Editable?) = afterTextChangedAction(s)
    }
}
