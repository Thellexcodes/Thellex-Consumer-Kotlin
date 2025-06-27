package com.thellex.payments.features.auth.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.thellex.payments.databinding.FragmentBasicKycReviewBinding
import com.thellex.payments.features.auth.viewModel.BasicKycFormViewModel
import com.thellex.payments.features.auth.viewModel.BasicKycFormViewModelFactory

class BasicKycReviewActivity : AppCompatActivity() {

    private lateinit var binding: FragmentBasicKycReviewBinding
    private lateinit var basicKycFormModel: BasicKycFormViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentBasicKycReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        basicKycFormModel = ViewModelProvider(
            this,
            BasicKycFormViewModelFactory(applicationContext)
        )[BasicKycFormViewModel::class.java]

        // Observe form data changes and update UI
//        basicKycFormModel.formData.observe(this) { data ->
//            binding.summaryTextView.text = buildString {
//                appendLine("First Name: ${data.firstName}")
//                appendLine("Middle Name: ${data.middleName}")
//                appendLine("Last Name: ${data.lastName}")
//                appendLine("Phone Number: ${data.phoneNumber}")
//                appendLine("Date of Birth: ${data.dob}")
//                appendLine("NIN: ${data.nin}")
//                appendLine("BVN: ${data.bvn}")
//                appendLine("House Number: ${data.houseNumber}")
//                appendLine("Street Name: ${data.streetName}")
//                appendLine("State: ${data.state}")
//                appendLine("LGA: ${data.lga}")
//                appendLine("ID Type: ${data.idType}")
//                appendLine("Additional ID Type: ${data.additionalIdType}")
//            }
//        }
//
        binding.submitBtn.setOnClickListener {
            startActivity( Intent(this, KycSuccessActivity::class.java))
            finish()
        }
    }
}

