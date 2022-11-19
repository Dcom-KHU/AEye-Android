package com.example.aeye.activity

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

//For TTS API
import android.speech.tts.TextToSpeech
import com.example.aeye.R
import java.util.Locale

class ModeDetectionActivity : AppCompatActivity(), TextToSpeech.OnInitListener{

    private lateinit var binding: ModeDetectionBinding

    //For Object Detection
    private lateinit var objectDetector: ObjectDetector
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    //For SlidingUpPanelLayout
    private lateinit var slidingUpPanel: SlidingUpPanelLayout
    private lateinit var fragmentManager: FragmentManager

    //For Detect Shake Events
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var shakeDetector: ShakeDetector? = null

    //For TTS API
    private lateinit var textToSpeech: TextToSpeech
    private var titleToSpeechOut: CharSequence? = null

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
        slidingUpPanel.isTouchEnabled = false

        //Initialize TTS
        textToSpeech = TextToSpeech(this, this)

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
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )

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

    //SlidingUpPanelLayout 내부 Fragment Transaction 가능 시까지 임시로 사용
    private fun initSlidingUpPanel(title: String, info: String){
        binding.objectDescription.text = info
        binding.objectTitle.text = title
    }

    //For TTS Api
    private fun speakOut(){
        textToSpeech.setPitch(0.6F)
        textToSpeech.setSpeechRate(0.4F)
        textToSpeech.speak(titleToSpeechOut, TextToSpeech.QUEUE_FLUSH, null, "id1")
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech.setLanguage(Locale.ENGLISH)
            if (result == TextToSpeech.LANG_NOT_SUPPORTED || result == TextToSpeech.LANG_MISSING_DATA)
                Log.e("TTS", "This Language is not supported")
            else
                speakOut()// onInit에 음성출력할 텍스트를 넣어줌
        }
        else{
            Log.e("TTS", "Initialization Failed!")
        }
    }



    //PanelEventListener
    inner class PanelEventListener : SlidingUpPanelLayout.PanelSlideListener{
        override fun onPanelSlide(panel: View?, slideOffset: Float) {
            //ignore
        }
        override fun onPanelStateChanged(
            panel: View?,
            previousState: SlidingUpPanelLayout.PanelState?,
            newState: SlidingUpPanelLayout.PanelState?
        ) {
            if (newState == SlidingUpPanelLayout.PanelState.EXPANDED)
                speakOut()
        }
    }

    //Request Permission for Using Camera
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


    //Start Image Analysis
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

                            titleToSpeechOut = i.labels.firstOrNull()?.text ?:"Undefined"

                            val element = Draw(context = this,
                                rect = i.boundingBox,
                                textString = titleToSpeechOut.toString())
                                binding.parentLayout.addView(element)

                            initSlidingUpPanel(titleToSpeechOut.toString(), "Info")
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

    override fun onDestroy() {
        textToSpeech.stop()
        textToSpeech.shutdown()
        super.onDestroy()
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


