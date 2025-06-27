package com.thellex.payments.features.auth.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.thellex.payments.core.utils.ActivityTracker
import com.thellex.payments.data.model.IdTypeEnum
import com.thellex.payments.databinding.FragmentKycStep2Binding
import com.thellex.payments.features.auth.viewModel.BasicKycFormViewModel
import com.thellex.payments.features.auth.viewModel.BasicKycFormViewModelFactory

class BasicKycStep2Activity : AppCompatActivity() {

    private lateinit var binding: FragmentKycStep2Binding
    private lateinit var basicKycFormModel: BasicKycFormViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentKycStep2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        ActivityTracker.add(this)

        basicKycFormModel = ViewModelProvider(
            this,
            BasicKycFormViewModelFactory(applicationContext)
        )[BasicKycFormViewModel::class.java]

        binding.fragmentKycStep2BtnContinue.setOnClickListener {
            basicKycFormModel.formData.value = basicKycFormModel.formData.value?.copy(
                nin = binding.fragmentKycStep2EtNin.text.toString().trim(),
                bvn = binding.fragmentKycStep2EtBvn.text.toString().trim(),
                houseNumber = binding.fragmentKycStep2EtHouseNumber.text.toString().trim(),
                streetName = binding.fragmentKycStep2EtStreetName.text.toString().trim(),
                state = binding.fragmentKycStep2EtState.text.toString().trim(),
                lga = binding.fragmentKycStep2EtLga.text.toString().trim(),
                idType = IdTypeEnum.NIN,
                additionalIdType = IdTypeEnum.BVN
            )
            goToNextStep()
        }
    }

    private fun goToNextStep() {
         startActivity(Intent(this, BasicKycReviewActivity::class.java))
    }
}
