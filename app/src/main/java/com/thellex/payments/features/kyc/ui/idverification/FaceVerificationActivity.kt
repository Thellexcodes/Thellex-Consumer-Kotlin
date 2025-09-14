package com.thellex.payments.features.kyc.ui.idverification

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.github.dhaval2404.imagepicker.ImagePicker
import com.thellex.payments.core.utils.CustomToast
import com.thellex.payments.databinding.ActivityFaceVerificationBinding
import java.io.ByteArrayOutputStream
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.thellex.payments.R
import com.thellex.payments.core.utils.ActivityTracker
import com.thellex.payments.core.utils.Helpers
import com.thellex.payments.core.utils.Helpers.applyAdvancedSystemBarInsets
import com.thellex.payments.core.utils.Helpers.disableDecorFitsSystemWindows
import com.thellex.payments.core.utils.Helpers.setTransparentStatusBarWithWhiteIcons
import com.thellex.payments.features.auth.viewModel.UserViewModel
import com.thellex.payments.features.auth.viewModel.UserViewModelFactory

class FaceVerificationActivity : AppCompatActivity() {
    private lateinit var topBar: Helpers.TopAppBarController
    private lateinit var binding: ActivityFaceVerificationBinding
    private var photoIdImageBase64: String? = null // Changed to nullable
    private lateinit var userViewModel: UserViewModel
    private var selfieImageBase64: String? = null

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                    selfieImageBase64 = encodeBitmapToBase64(bitmap)
                    binding.capturedImagePreview.setImageBitmap(bitmap)
                    CustomToast.show(this, "Success", "Selfie captured successfully")
                    binding.nextPhotoButton.isEnabled = true
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to process captured image", e)
                    CustomToast.show(this, "Failed", "Error processing image")
                }
            } ?: run {
                CustomToast.show(this, "Failed", "No image data received")
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
        binding.faceVerificationRoot.applyAdvancedSystemBarInsets()
        topBar = Helpers.setupTopAppBar(
            activity = this,
            rootView = findViewById(R.id.face_verification_top_app_bar),
            title = "FACE VERIFICATION"
        )

        userViewModel = ViewModelProvider(
            this,
            UserViewModelFactory(applicationContext)
        )[UserViewModel::class.java]

        photoIdImageBase64 = intent.getStringExtra("photoid_image")

        // Launch camera on retake photo button click
        binding.takePhotoButton.setOnClickListener {
            launchCamera()
        }

        binding.nextPhotoButton.setOnClickListener {
            if (selfieImageBase64.isNullOrBlank() || photoIdImageBase64.isNullOrBlank()) {
                CustomToast.show(this, "Error", "Please capture a selfie and ensure an ID photo is provided")
                return@setOnClickListener
            }

            try {
//                 Validate Base64 strings
                Base64.decode(selfieImageBase64, Base64.DEFAULT)
                Base64.decode(photoIdImageBase64, Base64.DEFAULT)

                // Create KycImageData object and pass via Intent
                val kycImageData = KycImageData(
                    photoIdImageBase64 = photoIdImageBase64,
                    selfieImageBase64 = selfieImageBase64
                )
                val intent = Intent(this, IDVerificationAttestationActivity::class.java).apply {
                    putExtra("kyc_image_data", kycImageData)
                }
                startActivity(intent)
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, "Invalid Base64 string", e)
                CustomToast.show(this, "Error", "Invalid image data. Please recapture photos.")
            }
        }
    }

    private fun launchCamera() {
        try {
            ImagePicker.with(this)
                .cameraOnly()
                .compress(1024)
                .createIntent { intent -> cameraLauncher.launch(intent) }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch camera", e)
            CustomToast.show(this, "Error", "Unable to open camera")
        }
    }

    private fun encodeBitmapToBase64(bitmap: Bitmap): String? {
        return try {
            ByteArrayOutputStream().use { stream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to encode bitmap to Base64", e)
            null
        }
    }

    companion object {
        private const val TAG = "FaceVerification"
    }
}

data class KycImageData(
    val photoIdImageBase64: String?,
    val selfieImageBase64: String?
) : java.io.Serializable