package com.thellex.payments.features.kyc.ui

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import com.github.dhaval2404.imagepicker.ImagePicker
import com.thellex.payments.core.utils.CustomToast
import com.thellex.payments.databinding.ActivityFaceVerificationBinding
import com.thellex.payments.network.services.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.ByteArrayOutputStream
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.thellex.payments.core.utils.ActivityTracker
import com.thellex.payments.core.utils.ErrorHandler
import com.thellex.payments.core.utils.Helpers.applyAdvancedSystemBarInsets
import com.thellex.payments.core.utils.Helpers.disableDecorFitsSystemWindows
import com.thellex.payments.core.utils.Helpers.setSubmitting
import com.thellex.payments.core.utils.Helpers.setTransparentStatusBarWithWhiteIcons
import com.thellex.payments.data.enums.UserErrorEnum
import com.thellex.payments.data.model.VerifySelfieWithPhotoIdDto
import com.thellex.payments.features.kyc.ui.basic.KycSuccessActivity
import com.thellex.payments.features.auth.viewModel.UserViewModel
import com.thellex.payments.features.auth.viewModel.UserViewModelFactory
import kotlinx.coroutines.launch
import org.json.JSONObject

class FaceVerificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFaceVerificationBinding
    private lateinit var photoIdImageBase64: String
    private lateinit var userViewModel: UserViewModel

    // Store the selfie base64 after photo is captured
    private var selfieImageBase64: String? = null

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                selfieImageBase64 = encodeBitmapToBase64(bitmap)
                binding.ivSelfieCapture.setImageBitmap(bitmap)  // show captured selfie preview on ImageView
                CustomToast.show(this, "Success", "Selfie captured successfully")
                binding.startButton.isEnabled = true  // enable submit button after selfie captured
            } ?: run {
                CustomToast.show(this, "Failed", "Failed to capture image")
            }
        } else {
            CustomToast.show(this, "Failed", "Camera cancelled or failed")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFaceVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ActivityTracker.add(this)
        disableDecorFitsSystemWindows()
        setTransparentStatusBarWithWhiteIcons()
        binding.main.applyAdvancedSystemBarInsets()

        userViewModel = ViewModelProvider(
            this,
            UserViewModelFactory(applicationContext)
        )[UserViewModel::class.java]

        photoIdImageBase64 = intent.getStringExtra("photoid_image") ?: ""

        // Launch camera on ImageView click
        binding.ivSelfieCapture.setOnClickListener {
            launchCamera()
        }

        // Submit verification on button click
        binding.startButton.setOnClickListener {
            selfieImageBase64?.let { selfieBase64 ->
                verifySelfie(selfieBase64, photoIdImageBase64)
            } ?: run {
                CustomToast.show(this, "Error", "Please capture a selfie first")
            }
        }
    }

    private fun launchCamera() {
        ImagePicker.with(this)
            .cameraOnly()
            .compress(1024)
            .createIntent { intent -> cameraLauncher.launch(intent) }
    }

    private fun encodeBitmapToBase64(bitmap: Bitmap): String {
        return ByteArrayOutputStream().use { stream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
            Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
        }
    }

    private fun verifySelfie(selfieBase64: String, photoIdBase64: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) {
                    binding.startButton.setSubmitting(true)
                }

                val token = withTimeoutOrNull(5000) {
                    userViewModel.token.asFlow().first { !it.isNullOrBlank() }
                }

                if (token.isNullOrEmpty()) {
                    Log.e(TAG, "Token is null or empty - user not authenticated")
                    withContext(Dispatchers.Main) {
                        CustomToast.show(this@FaceVerificationActivity, "Error", "User not authenticated")
                    }
                    return@launch
                }

                Log.d(TAG, "Starting KYC verification")

                val api = ApiClient.getAuthenticatedKycApi(token)
                val response = api.verifySelfieWithPhotoId(
                    VerifySelfieWithPhotoIdDto(
                        selfieImageBase64 = selfieBase64,
                        photoIdImageBase64 = photoIdBase64
                    )
                )

                if (response.isSuccessful) {
                    val result = response.body()?.result
                    Log.d(TAG, "KYC verification successful. Result: $result")

                    if (result != null) {
                        userViewModel.updateUserWithKycResult(result)
                        startActivity(Intent(this@FaceVerificationActivity, KycSuccessActivity::class.java))
                    } else {
                        Log.e(TAG, "Verification success but result body is null")
                        CustomToast.show(this@FaceVerificationActivity, "Error", "❌ Verification returned empty result")
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
                    ErrorHandler.handle(this@FaceVerificationActivity, "Error", userError)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Exception during KYC verification", e)
                withContext(Dispatchers.Main) {
                    val userError = UserErrorEnum.fromCode(e.message)
                    ErrorHandler.handle(this@FaceVerificationActivity, "Error", userError)
                }
            } finally {
                withContext(Dispatchers.Main) {
                    binding.startButton.setSubmitting(false)
                }
            }
        }
    }

    companion object {
        private const val TAG = "FaceVerification"
    }
}
