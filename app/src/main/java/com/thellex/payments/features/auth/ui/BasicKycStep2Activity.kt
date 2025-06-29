package com.thellex.payments.features.auth.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.thellex.payments.core.utils.ActivityTracker
import com.thellex.payments.core.utils.Helpers.showSingleToast
import com.thellex.payments.data.model.IdTypeEnum
import com.thellex.payments.databinding.FragmentKycStep2Binding
import com.thellex.payments.features.auth.viewModel.BasicKycFormModelData
import com.thellex.payments.features.auth.viewModel.BasicKycFormViewModel
import com.thellex.payments.features.auth.viewModel.BasicKycFormViewModelFactory

class BasicKycStep2Activity : AppCompatActivity() {

    private lateinit var binding: FragmentKycStep2Binding
    private lateinit var basicKycFormModel: BasicKycFormViewModel
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentKycStep2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        ActivityTracker.add(this)

        basicKycFormModel = ViewModelProvider(
            this,
            BasicKycFormViewModelFactory(applicationContext)
        )[BasicKycFormViewModel::class.java]

        // Restore form data JSON passed from Step 1
        intent.getStringExtra("FORM_DATA_JSON")?.let { json ->
            val formData = gson.fromJson(json, BasicKycFormModelData::class.java)
            basicKycFormModel.formData.value = formData
            populateFields(formData)
        }

          binding.fragmentKycStep2BtnContinue.setOnClickListener {
                val nin = binding.fragmentKycStep2EtNin.text.toString().trim()
                val bvn = binding.fragmentKycStep2EtBvn.text.toString().trim()

                // Step 2 Validations
                if (nin.isEmpty()) {
                    showSingleToast("Please enter your NIN")
                    return@setOnClickListener
                }

                if (bvn.isEmpty()) {
                    showSingleToast("Please enter your BVN")
                    return@setOnClickListener
                }

                val current = basicKycFormModel.formData.value ?: BasicKycFormModelData()

                val updated = current.copy(
                    nin = nin,
                    bvn = bvn,
                    idType = IdTypeEnum.NIN,
                    additionalIdType = IdTypeEnum.BVN
                )

                basicKycFormModel.formData.value = updated

                val json = Gson().toJson(updated)
                val intent = Intent(this, BasicKycAttestationActivity::class.java)
                intent.putExtra("FORM_DATA_JSON", json)
                startActivity(intent)
            }
        }

    private fun populateFields(data: BasicKycFormModelData) {
        binding.fragmentKycStep2EtNin.setText(data.nin)
        binding.fragmentKycStep2EtBvn.setText(data.bvn)
    }
}