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
import com.thellex.payments.data.enums.UserErrorEnum
import com.thellex.payments.data.model.VerifySelfieWithPhotoIdDto
import com.thellex.payments.features.auth.ui.KycSuccessActivity
import com.thellex.payments.features.auth.viewModel.UserViewModel
import com.thellex.payments.features.auth.viewModel.UserViewModelFactory
import kotlinx.coroutines.launch
import org.json.JSONObject

class FaceVerificationActivity : AppCompatActivity() {
    companion object {
        private  val TAG = "TAG"
    }

    private lateinit var binding: ActivityFaceVerificationBinding
    private lateinit var photoIdImageBase64: String
    private lateinit var userViewModel: UserViewModel

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) {
                @Suppress("DEPRECATION")
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                val base64Selfie = encodeBitmapToBase64(bitmap)
                verifySelfie(base64Selfie, photoIdImageBase64)
            } else {
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

        applyWindowInsets()

        userViewModel = ViewModelProvider(
            this,
            UserViewModelFactory(applicationContext)
        )[UserViewModel::class.java]

        photoIdImageBase64 = intent.getStringExtra("photoid_image") ?: ""

        binding.startButton.setOnClickListener {
            launchCamera()
        }
    }

    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun launchCamera() {
        ImagePicker.with(this)
            .cameraOnly()
            .compress(1024)
            .createIntent { intent ->
                cameraLauncher.launch(intent)
            }
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
                // Disable start button on Main thread
                withContext(Dispatchers.Main) {
                    binding.startButton.isEnabled = false
                }

                val token = withTimeoutOrNull(5000) {
                    userViewModel.token.asFlow().first { !it.isNullOrBlank() }
                }

                if (token.isNullOrEmpty()) {
                    withContext(Dispatchers.Main) {
                        CustomToast.show(this@FaceVerificationActivity, "Error", "User not authenticated")
                    }
                    return@launch
                }

                val api = ApiClient.getAuthenticatedKycApi(token)

                val response = api.verifySelfieWithPhotoId(
                    VerifySelfieWithPhotoIdDto(
                        selfieImageBase64 = selfieBase64,
                        photoIdImageBase64 = photoIdBase64,
                    )
                )

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val result = response.body()?.result
                        Log.d(TAG, "KYC Result: $result")

                        if (result != null) {
//                            userViewModel.updateUserWithKycResult(result)
                            CustomToast.show(this@FaceVerificationActivity, "Success", "✅ Selfie Verified Successfully")
                            startActivity(Intent(this@FaceVerificationActivity, KycSuccessActivity::class.java))
                        } else {
                            Log.e(TAG, "KYC result body was null")
                            CustomToast.show(this@FaceVerificationActivity, "Error", "❌ Verification returned empty result")
                        }

                    } else {
                        Log.e(TAG, "Verification failed with status: ${response.code()}")

                        val errorBodyString = response.errorBody()?.string()
                        val code = try {
                            JSONObject(errorBodyString ?: "").optString("message")
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to parse error body", e)
                            null
                        }
                        val userError = UserErrorEnum.fromCode(code)
                        ErrorHandler.handle(this@FaceVerificationActivity, "Error", userError)
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    val userError = UserErrorEnum.fromCode(e.message)
                    ErrorHandler.handle(this@FaceVerificationActivity, "Error", userError)
                }
            } finally {
                withContext(Dispatchers.Main) {
                    binding.startButton.isEnabled = true
                }
            }
        }
    }
}


