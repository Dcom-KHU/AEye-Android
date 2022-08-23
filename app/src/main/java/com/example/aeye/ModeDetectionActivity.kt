package com.example.aeye

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.View
import android.widget.Toast

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.*
import androidx.lifecycle.LifecycleOwner

import com.example.aeye.databinding.ModeDetectionBinding
import com.example.aeye.listener.ShakeDetector

import com.example.aeye.env.Draw
//import com.example.aeye.fragment.ObjectInfo_Fragment
import com.google.common.util.concurrent.ListenableFuture

import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions
import com.sothree.slidinguppanel.SlidingUpPanelLayout

class ModeDetectionActivity : AppCompatActivity(){

    private lateinit var binding: ModeDetectionBinding

    private lateinit var objectDetector: ObjectDetector
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    //For SlidingUpPanelLayout
    private lateinit var slidingUpPanel: SlidingUpPanelLayout
    private lateinit var fragmentManager: FragmentManager

    //For Detect Shake Events
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var shakeDetector: ShakeDetector? = null


    override fun onCreate(savedInstanceState: Bundle?) {

        //For Using ObjectInfo_Fragment
        /*supportFragmentManager.fragmentFactory = CustomFragmentFactory("음료 예시", R.drawable.drink_icon)*/

        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.mode_detection)

        binding.objectIcon.setImageResource(intent.getIntExtra("modeIcon", R.drawable.aeye_icon1))
        binding.objectTitle.setTextColor(intent.getIntExtra("modeColor", R.color.black))

        //Initialize SlidingUpPanel
        slidingUpPanel = binding.mainPanelFrame

        //Add EventListener
        slidingUpPanel.addPanelSlideListener(PanelEventListener())

        //Initialize SensorManager and Accelerometer
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        shakeDetector = ShakeDetector()

        //If device detects shake, open/close the panel layout
        shakeDetector!!.setOnShakeListener(object : ShakeDetector.OnShakeListener {
            override fun onShake(count: Int) {
                val state = slidingUpPanel.panelState
                if (state == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    slidingUpPanel.panelState = SlidingUpPanelLayout.PanelState.ANCHORED
                }
                else if(state == SlidingUpPanelLayout.PanelState.EXPANDED) {
                    slidingUpPanel.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
                }
            }
        })

        if(allPermissionsGranted())
            startDetection()
        else
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)

    }

    //For Using ObjectInfo_Fragment
    /*
    fun AppCompatActivity.replaceFragment(fragmentFactory: FragmentFactory){
        val fragment = fragmentFactory.instantiate(classLoader, ObjectInfo_Fragment::class.java.name)
        supportFragmentManager.commit {
            replace(binding.slideLayout.id, fragment)
            addToBackStack(null)
        }
    }
     */

    private fun initSlidingUpPanel(title: String, info: String){
        binding.objectDescription.text = info
        binding.objectTitle.text = title
    }

    inner class PanelEventListener : SlidingUpPanelLayout.PanelSlideListener{
        override fun onPanelSlide(panel: View?, slideOffset: Float) {
            //ignore
        }
        override fun onPanelStateChanged(
            panel: View?,
            previousState: SlidingUpPanelLayout.PanelState?,
            newState: SlidingUpPanelLayout.PanelState?
        ) {
            //ignore
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == REQUEST_CODE_PERMISSIONS){
            if(allPermissionsGranted()){
                startDetection()
            }else{
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun startDetection() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider = cameraProvider) // Bind Camera provider
        }, ContextCompat.getMainExecutor(this))

        val localModel = LocalModel.Builder()
            .setAssetFilePath("object_detection_example.tflite")
            .build()

        val customObjectDetectorOptions = CustomObjectDetectorOptions.Builder(localModel)
            .setDetectorMode(CustomObjectDetectorOptions.STREAM_MODE)
            .enableClassification()
            .setClassificationConfidenceThreshold(0.5f)
            .setMaxPerObjectLabelCount(3)
            .build()

        objectDetector = ObjectDetection.getClient(customObjectDetectorOptions)
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun bindPreview(cameraProvider: ProcessCameraProvider){
        val preview = Preview.Builder().build()

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        preview.setSurfaceProvider(binding.previewView.surfaceProvider)

        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(1280,720))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this)) { imageProxy ->
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            val image = imageProxy.image

            if (image != null) {
                val processImage = InputImage.fromMediaImage(image, rotationDegrees)

                objectDetector
                    .process(processImage)
                    .addOnSuccessListener { objects ->
                        for(i in objects){
                            if(binding.parentLayout.childCount > 1)
                                binding.parentLayout.removeViewAt(1)

                            val element = Draw(context = this,
                                rect = i.boundingBox,
                                textString = i.labels.firstOrNull()?.text ?:"Undefined")
                                binding.parentLayout.addView(element)

                            initSlidingUpPanel(i.labels.firstOrNull()?.text ?:"Undefined", "Info")
                        }
                        imageProxy.close()
                    }.addOnFailureListener {
                        Log.v("ModeDetectionActivity", "Error - ${it.message}")
                        imageProxy.close()
                    }
            }
        }

        cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, imageAnalysis, preview)
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.also { accel ->
            sensorManager.registerListener(shakeDetector, accel, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(shakeDetector)
    }

    companion object {
        //private const val TAG = "AEye_ObjectDetection"
        //private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                android.Manifest.permission.CAMERA
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P)
                    add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }.toTypedArray()
    }
}


