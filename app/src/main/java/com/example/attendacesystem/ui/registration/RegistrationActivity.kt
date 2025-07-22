package com.example.attendacesystem.ui.registration

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.attendacesystem.databinding.ActivityRegistrationBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import org.tensorflow.lite.Interpreter

class RegistrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrationBinding
    private var detectedFaceBitmap: Bitmap? = null
    private var faceDetected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestCameraPermission()

        binding.buttonRegister.setOnClickListener {
            if (detectedFaceBitmap != null) {
                showUserInfoDialog()
            } else {
                Toast.makeText(this, "No face detected yet.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            val launcher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                if (granted) startCamera()
                else Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show()
            }
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    @androidx.camera.core.ExperimentalGetImage
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.cameraPreview.surfaceProvider)
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            val options = FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .build()

            val faceDetector = FaceDetection.getClient(options)

            imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this)) { imageProxy ->
                processImageProxy(imageProxy, faceDetector)
            }

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)

        }, ContextCompat.getMainExecutor(this))
    }

    private fun processImageProxy(imageProxy: ImageProxy, faceDetector: com.google.mlkit.vision.face.FaceDetector) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            faceDetector.process(image)
                .addOnSuccessListener { faces ->
                    if (faces.isNotEmpty()) {
                        faceDetected = true
                        val face = faces[0]
                        val boundingBox = face.boundingBox

                        // Convert mediaImage to Bitmap
                        val bitmap = toBitmap(mediaImage)
                        detectedFaceBitmap = Bitmap.createBitmap(
                            bitmap,
                            boundingBox.left.coerceAtLeast(0),
                            boundingBox.top.coerceAtLeast(0),
                            boundingBox.width().coerceAtMost(bitmap.width - boundingBox.left),
                            boundingBox.height().coerceAtMost(bitmap.height - boundingBox.top)
                        )
                        binding.textStatus.text = "Face detected, ready to capture."
                    } else {
                        faceDetected = false
                        binding.textStatus.text = "No face detected."
                    }
                }
                .addOnCompleteListener { imageProxy.close() }
        } else {
            imageProxy.close()
        }
    }

    private fun showUserInfoDialog() {
        val dialogView = layoutInflater.inflate(com.example.attendacesystem.R.layout.dialog_register_info, null)
        val editName = dialogView.findViewById<android.widget.EditText>(com.example.attendacesystem.R.id.editName)
        val editId = dialogView.findViewById<android.widget.EditText>(com.example.attendacesystem.R.id.editId)

        AlertDialog.Builder(this)
            .setTitle("Enter User Info")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val name = editName.text.toString().trim()
                val id = editId.text.toString().trim()
                if (name.isNotEmpty()) {
                    saveUser(name, id)
                } else {
                    Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveUser(name: String, id: String) {
        if (detectedFaceBitmap == null) {
            Toast.makeText(this, "No face captured.", Toast.LENGTH_SHORT).show()
            return
        }

        // TODO:
        // 1. Generate embedding using FaceNet with detectedFaceBitmap
        // 2. Save (name, id, embedding) into Room DB

        Toast.makeText(this, "User $name saved!", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun toBitmap(image: android.media.Image): Bitmap {
        val yBuffer = image.planes[0].buffer
        val vuBuffer = image.planes[2].buffer
        val ySize = yBuffer.remaining()
        val vuSize = vuBuffer.remaining()
        val nv21 = ByteArray(ySize + vuSize)

        yBuffer.get(nv21, 0, ySize)
        vuBuffer.get(nv21, ySize, vuSize)

        val yuvImage = android.graphics.YuvImage(nv21, android.graphics.ImageFormat.NV21, image.width, image.height, null)
        val out = java.io.ByteArrayOutputStream()
        yuvImage.compressToJpeg(android.graphics.Rect(0, 0, image.width, image.height), 100, out)
        val imageBytes = out.toByteArray()
        return android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    private fun getFaceEmbedding(bitmap: Bitmap): FloatArray {
        val tfliteModel = assets.open("facenet_512.tflite").use { input ->
            val model = ByteArray(input.available())
            input.read(model)
            model
        }
        val interpreter = Interpreter(tfliteModel)

        // Preprocess bitmap to 160x160 and normalize
        val inputImage = Bitmap.createScaledBitmap(bitmap, 160, 160, true)
        val input = Array(1) { Array(160) { Array(160) { FloatArray(3) } } }
        for (y in 0 until 160) {
            for (x in 0 until 160) {
                val pixel = inputImage.getPixel(x, y)
                input[0][y][x][0] = ((pixel shr 16 and 0xFF) - 127.5f) / 128f
                input[0][y][x][1] = ((pixel shr 8 and 0xFF) - 127.5f) / 128f
                input[0][y][x][2] = ((pixel and 0xFF) - 127.5f) / 128f
            }
        }

        val embedding = Array(1) { FloatArray(512) }
        interpreter.run(input, embedding)
        interpreter.close()
        return embedding[0]
    }
}
