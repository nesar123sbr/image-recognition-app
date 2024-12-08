package com.course.imagelabelapp

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabel
import com.google.mlkit.vision.label.ImageLabeler
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions

class MainActivity : AppCompatActivity() {

    private lateinit var objectImage : ImageView
    private lateinit var labelText :TextView
    private lateinit var captureImgBtn: Button
    private lateinit var imageLabeler: ImageLabeler
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>


    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        objectImage = findViewById(R.id.objectImage)
        labelText = findViewById(R.id.labelText )
        captureImgBtn = findViewById(R.id.captureImgBtn )

        checkCameraPermission()

        imageLabeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val extras = result.data?.extras
                val imageBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // Use the newer API for API 33+
                    extras?.getParcelable("data", Bitmap::class.java)
                } else {
                    // Use the older API for devices below API 33
                    @Suppress("DEPRECATION")
                    extras?.getParcelable("data") as? Bitmap
                }
                if (imageBitmap != null) {
                    objectImage.setImageBitmap(imageBitmap)
                    labelImage(imageBitmap)
                } else {
                    labelText.text = "Unable to capture image"
                }
            }
        }

        captureImgBtn.setOnClickListener{
            val clickPicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (clickPicture.resolveActivity(packageManager) !=null) {
                cameraLauncher.launch(clickPicture)
            }
        }

    }

    private fun labelImage(bitmap: Bitmap) {
//        0 artinya, ga ada rotation
        val inputImage = InputImage.fromBitmap(bitmap, 0)

        imageLabeler.process(inputImage).addOnSuccessListener { label ->
            displayLabel(label)
        } .addOnFailureListener{ e ->
            labelText.text = "Error: ${e.message}"
        }
    }



    private fun displayLabel(labels: List<ImageLabel>){
        if (labels.isNotEmpty()){
            val mostConfidentLabel = labels[0]
            labelText.text = "${mostConfidentLabel.text}"
        } else {
            labelText.text = "No labels found"
        }
    }

    private fun checkCameraPermission(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), 1)
        }
    }

//    ini ngecek kalo camera permission dikasih atau ga
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can proceed
            } else {
                labelText.text = "Camera permission is required to capture images."
            }
        }
    }

}