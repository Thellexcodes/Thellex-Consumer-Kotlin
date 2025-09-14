package com.thellex.payments.features.kyc.ui.idverification

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import com.thellex.payments.R
import com.thellex.payments.core.utils.ActivityTracker
import com.thellex.payments.core.utils.CustomToast
import com.thellex.payments.core.utils.ErrorHandler
import com.thellex.payments.core.utils.Helpers
import com.thellex.payments.core.utils.Helpers.applyAdvancedSystemBarInsets
import com.thellex.payments.core.utils.Helpers.disableDecorFitsSystemWindows
import com.thellex.payments.core.utils.Helpers.setSubmitting
import com.thellex.payments.core.utils.Helpers.setTransparentStatusBarWithWhiteIcons
import com.thellex.payments.data.enums.UserErrorEnum
import com.thellex.payments.data.model.VerifySelfieWithPhotoIdDto
import com.thellex.payments.databinding.ActivityIdverificationAttestationBinding
import com.thellex.payments.features.auth.viewModel.UserViewModel
import com.thellex.payments.features.auth.viewModel.UserViewModelFactory
import com.thellex.payments.features.kyc.ui.basic.KycSuccessActivity
import com.thellex.payments.network.services.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject
import android.util.Base64
import android.widget.ImageView
import androidx.annotation.RequiresApi
import kotlinx.coroutines.withTimeout
import java.util.concurrent.TimeUnit

class IDVerificationAttestationActivity : AppCompatActivity() {
    private lateinit var topBar: Helpers.TopAppBarController
    private lateinit var binding: ActivityIdverificationAttestationBinding
    private lateinit var userViewModel: UserViewModel
    private var photoIdImageBase64: String? = null
    private var selfieIdImageBase64: String? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIdverificationAttestationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ActivityTracker.add(this)
        disableDecorFitsSystemWindows()
        setTransparentStatusBarWithWhiteIcons()
        binding.clIdVerificationAttestation.applyAdvancedSystemBarInsets()
        topBar = Helpers.setupTopAppBar(
            activity = this,
            rootView = findViewById(R.id.id_attestation_top_app_bar),
            title = "ATTESTATION"
        )

        userViewModel = ViewModelProvider(
            this,
            UserViewModelFactory(applicationContext)
        )[UserViewModel::class.java]

        // Retrieve KycImageData from intent
        val kycImageData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("kyc_image_data", KycImageData::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("kyc_image_data") as? KycImageData
        }
        if (kycImageData == null) {
            Log.e(TAG, "KycImageData is null")
            CustomToast.show(this, "Error", "Missing image data. Please try again.")
            finish() // Close activity if no data
            return
        }

        photoIdImageBase64 = kycImageData.photoIdImageBase64
        selfieIdImageBase64 = kycImageData.selfieImageBase64

        // Set image previews with scaling
        setImagePreview(binding.ivIdPreview, photoIdImageBase64, "Photo ID")
        setImagePreview(binding.ivPhotoPreview, selfieIdImageBase64, "Selfie")

        // Submit button click listener
        binding.btnSubmitAttestation.setOnClickListener {
            if (!binding.cbAttestation.isChecked) {
                CustomToast.show(this, "Error", "Please attest to the information")
                return@setOnClickListener
            }

            if (photoIdImageBase64.isNullOrBlank() || selfieIdImageBase64.isNullOrBlank()) {
                CustomToast.show(this, "Error", "Missing image data. Please try again.")
                return@setOnClickListener
            }

            verifySelfie(selfieIdImageBase64!!, photoIdImageBase64!!)
        }

        Log.d(TAG, "Photoid image length: ${photoIdImageBase64?.length ?: 0}")
        Log.d(TAG, "Selfie image length: ${selfieIdImageBase64?.length ?: 0}")
    }

    private fun setImagePreview(imageView: ImageView, base64: String?, type: String) {
        if (base64.isNullOrBlank()) {
            Log.w(TAG, "Empty or null Base64 string for $type")
            imageView.setImageResource(R.drawable.thellex_logo_dark)
            CustomToast.show(this, "Warning", "No $type image provided")
            return
        }

        try {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            // Scale bitmap to match ImageView size (245dp)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, this)
                inSampleSize = calculateInSampleSize(this, 245, 245)
                inJustDecodeBounds = false
            }
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap)
            } else {
                Log.e(TAG, "Failed to decode $type bitmap")
                imageView.setImageResource(R.drawable.thellex_logo_dark)
                CustomToast.show(this, "Error", "Failed to load $type image")
            }
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Invalid Base64 string for $type", e)
            imageView.setImageResource(R.drawable.thellex_logo_dark)
            CustomToast.show(this, "Error", "Invalid $type image data")
        } catch (e: OutOfMemoryError) {
            Log.e(TAG, "Out of memory while decoding $type image", e)
            imageView.setImageResource(R.drawable.thellex_logo_dark)
            CustomToast.show(this, "Error", "Image too large to load")
        } catch (e: Exception) {
            Log.e(TAG, "Error decoding $type image", e)
            imageView.setImageResource(R.drawable.thellex_logo_dark)
            CustomToast.show(this, "Error", "Failed to load $type image")
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun verifySelfie(selfieBase64: String, photoIdBase64: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) {
                    binding.btnSubmitAttestation.setSubmitting(true)
                }

                val token = withTimeoutOrNull(5000) {
                    userViewModel.token.asFlow().first { !it.isNullOrBlank() }
                }

                if (token.isNullOrEmpty()) {
                    Log.e(TAG, "Token is null or empty - user not authenticated")
                    withContext(Dispatchers.Main) {
                        CustomToast.show(this@IDVerificationAttestationActivity, "Error", "User not authenticated")
                    }
                    return@launch
                }

                Log.d(TAG, "Starting KYC verification")

                val api = ApiClient.getAuthenticatedKycApi(this@IDVerificationAttestationActivity, token)
//                val response = api.verifySelfieWithPhotoId(
//                    VerifySelfieWithPhotoIdDto(
//                        selfieImageBase64 = selfieBase64,
//                        photoIdImageBase64 = photoIdBase64
//                    )
//                )

                val response = withTimeout(TimeUnit.SECONDS.toMillis(60)) {
                    api.verifySelfieWithPhotoId(
                        VerifySelfieWithPhotoIdDto(
                            selfieImageBase64 = selfieBase64,
                            photoIdImageBase64 = photoIdBase64
                        )
                    )
                }

                if (response.isSuccessful) {
                    val result = response.body()?.result
                    Log.d(TAG, "KYC verification successful. Result: $result")

                    if (result != null) {
                        userViewModel.updateUserWithKycResult(result)
                        withContext(Dispatchers.Main) {
                            startActivity(Intent(this@IDVerificationAttestationActivity, KycSuccessActivity::class.java))
                            finish()
                        }
                    } else {
                        Log.e(TAG, "Verification success but result body is null")
                        withContext(Dispatchers.Main) {
                            CustomToast.show(this@IDVerificationAttestationActivity, "Error", "Verification returned empty result")
                        }
                    }
                } else {
                    val errorBodyString = response.errorBody()?.string()
                    Log.e(TAG, "Verification failed with status code: ${response.code()} - error: $errorBodyString")

                    val code = try {
                        JSONObject(errorBodyString ?: "").optString("message")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to parse error body JSON", e)
                        null
                    }

                    val userError = UserErrorEnum.fromCode(code)
                    withContext(Dispatchers.Main) {
                        ErrorHandler.handle(this@IDVerificationAttestationActivity, "Error", userError)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during KYC verification", e)
                withContext(Dispatchers.Main) {
                    val userError = UserErrorEnum.fromCode(e.message)
                    ErrorHandler.handle(this@IDVerificationAttestationActivity, "Error", userError)
                }
            } finally {
                withContext(Dispatchers.Main) {
                    binding.btnSubmitAttestation.setSubmitting(false)
                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // Ensure ActivityTracker is updated when back button is pressed
        ActivityTracker.remove(this)
        Log.d(TAG, "Activity closed via back button")
        finish() // Explicitly finish to ensure clean closure
    }

    override fun onDestroy() {
        super.onDestroy()
        ActivityTracker.remove(this)
        Log.d(TAG, "Activity destroyed, resources cleaned up")
    }

    companion object {
        private const val TAG = "IDVerificationAttestation"
    }
}