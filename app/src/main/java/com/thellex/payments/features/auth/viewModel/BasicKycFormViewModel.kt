package com.thellex.payments.features.auth.viewModel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.gson.annotations.SerializedName
import com.thellex.payments.data.model.IdTypeEnum
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BasicKycFormModelData(
    @SerialName("firstName")
    @SerializedName("firstName")
    var firstName: String = "John",

    @SerialName("middleName")
    @SerializedName("middleName")
    var middleName: String = "Michael",

    @SerialName("lastName")
    @SerializedName("lastName")
    var lastName: String = "Doe",

    @SerialName("phoneNumber")
    @SerializedName("phoneNumber")
    var phoneNumber: String = "08012345678",

    @SerialName("dob")
    @SerializedName("dob")
    var dob: String = "01/01/1990",

    @SerialName("nin")
    @SerializedName("nin")
    var nin: String = "12345678901",

    @SerialName("bvn")
    @SerializedName("bvn")
    var bvn: String = "22345678901",

    @SerialName("houseNumber")
    @SerializedName("houseNumber")
    var houseNumber: String = "12",

    @SerialName("streetName")
    @SerializedName("streetName")
    var streetName: String = "Freedom Street",

    @SerialName("state")
    @SerializedName("state")
    var state: String = "Lagos",

    @SerialName("lga")
    @SerializedName("lga")
    var lga: String = "Ikeja",

    @SerialName("idType")
    @SerializedName("idType")
    var idType: IdTypeEnum = IdTypeEnum.NIN,

    @SerialName("additionalIdType")
    @SerializedName("additionalIdType")
    var additionalIdType: IdTypeEnum = IdTypeEnum.BVN
)

class BasicKycFormViewModel(application: Context): AndroidViewModel(application as Application) {
    val formData = MutableLiveData<BasicKycFormModelData>()
}

class BasicKycFormViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BasicKycFormViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BasicKycFormViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}