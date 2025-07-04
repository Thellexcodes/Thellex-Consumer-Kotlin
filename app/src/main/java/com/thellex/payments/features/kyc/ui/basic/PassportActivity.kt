package com.thellex.payments.features.kyc.ui.basic

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.dhaval2404.imagepicker.ImagePicker
import com.thellex.payments.core.utils.ActivityTracker
import com.thellex.payments.core.utils.CustomToast
import com.thellex.payments.databinding.ActivityPassportBinding
import com.thellex.payments.features.kyc.ui.FaceVerificationActivity
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.thellex.payments.features.auth.viewModel.UserViewModel
import com.thellex.payments.features.auth.viewModel.UserViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException

class PassportActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "PassportActivity"
        private const val REQUEST_CODE_IMAGE_PICKER = 1001
    }

    private lateinit var binding: ActivityPassportBinding
    private var frontBase64: String? = null
    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPassportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ActivityTracker.add(this)

        userViewModel = ViewModelProvider(
            this,
            UserViewModelFactory(applicationContext)
        )[UserViewModel::class.java]

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.uploadFrontButton.setOnClickListener {
            launchImagePicker()
        }

        binding.nextButton.setOnClickListener {
            if (frontBase64.isNullOrEmpty()) {
                CustomToast.show(this, "Missing", "Please upload your passport front side")
            } else {
                val intent = Intent(this@PassportActivity, FaceVerificationActivity::class.java).apply {
                    putExtra("photoid_image", frontBase64)
                }
                startActivity(intent)
            }
        }
    }

    private fun launchImagePicker() {
        ImagePicker.with(this)
            .crop()
            .compress(1024)
            .maxResultSize(1080, 1080)
            .start(REQUEST_CODE_IMAGE_PICKER)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_IMAGE_PICKER) {
            when (resultCode) {
                RESULT_OK -> {
                    data?.data?.let { uri ->
                        processSelectedImage(uri)
                    }
                }
                ImagePicker.RESULT_ERROR -> {
                    Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Toast.makeText(this, "Image selection cancelled", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun processSelectedImage(uri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val base64 = encodeUriToBase64(uri)
                withContext(Dispatchers.Main) {
                    frontBase64 = base64
                    binding.passportFrontPreview.setImageURI(uri)
                    Log.d(TAG, "Front image updated, base64 length: ${base64.length}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error encoding image", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@PassportActivity, "Failed to process image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun encodeUriToBase64(uri: Uri): String {
        contentResolver.openInputStream(uri)?.use { inputStream ->
            val bitmap = BitmapFactory.decodeStream(inputStream)
            return encodeBitmapToBase64(bitmap)
        } ?: throw IOException("Failed to open image input stream")
    }

    private fun encodeBitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
}

