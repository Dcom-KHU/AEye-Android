package com.example.aeye.activity

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.Image
import android.media.ImageReader
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import android.view.KeyEvent
import android.view.Surface
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.example.aeye.ObjectInfoViewModel
import com.example.aeye.ObjectInfoViewModelFactory
import com.example.aeye.R
import com.example.aeye.application.DetectApplication
import com.example.aeye.databinding.ActivityLiveAnalysisBinding
import com.example.aeye.fragment.CameraFragment
import com.example.aeye.listener.ShakeDetector
import com.example.aeye.listener.TextToSpeechManager
import com.example.aeye.tflite.CustomClassifier
import com.example.aeye.tflite.YuvToRgbConverter
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.synchronized
import java.io.IOException
import java.util.*


class ModeLiveAnalysisActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLiveAnalysisBinding

    /**For slidingUpPanelLayout**/
    private lateinit var slidingUpPanel: SlidingUpPanelLayout

    /**For detecting shake events**/
    private lateinit var sensorManager : SensorManager
    private var accelerometer : Sensor ?= null
    private var shakeDetector : ShakeDetector ?= null

    /**For TTS API**/
    private lateinit var textToSpeech: TextToSpeechManager
    private var infoToSpeechOut : CharSequence ?= null

    /** For Classifier **/
    private lateinit var cls : CustomClassifier
    private var previewWidth : Int = 0
    private var previewHeight : Int = 0
    private var sensorOrientation : Int = 0
    private var rgbFrameBitmap : Bitmap ?= null

    /** For handling background thread**/
    private var handlerThread: HandlerThread ?= null
    private var handler: Handler ?= null
    private var isProcessingFrame : Boolean = false

    /** Implement ObjectInfoViewModel to observe ObjectInfoDatabase**/
    private val objectInfoViewModel : ObjectInfoViewModel by viewModels{
        ObjectInfoViewModelFactory((application as DetectApplication).repository)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLiveAnalysisBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /**Get Intent from ModeInfo_Fragment**/
        val modeIcon : Int = intent.getIntExtra("modeIcon", R.drawable.aeye_icon1)
        val modeColor : Int = intent.getIntExtra("modeColor", R.color.black)

        /**Init slidingUpPanel and its elements**/
        slidingUpPanel = binding.mainPanelFrame
        slidingUpPanel.addPanelSlideListener(PanelEventListener())
        slidingUpPanel.isTouchEnabled = false

        binding.objectIcon.setImageResource(modeIcon)
        binding.objectTitle.setTextColor(modeColor)

        /**Initialize TTS**/
        textToSpeech = TextToSpeechManager()
        textToSpeech.init(this)

        /**Initialize SensorManager and Accelerometer **/
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        shakeDetector = ShakeDetector()

        shakeDetector!!.setOnShakeListener(object : ShakeDetector.OnShakeListener {
            override fun onShake(count: Int) {

            }
        })

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        /** Init Classifier **/
        if (modeIcon == R.drawable.drink_medicine || modeIcon == R.drawable.drink_icon)
            cls = CustomClassifier("object_detection_example.tflite")

        try {
            cls.init()
        } catch (ioe : IOException) {
            ioe.printStackTrace()
        }

        if (checkSelfPermission(CAMERA_PERMISSION) == PackageManager.PERMISSION_GRANTED){
            setFragment()
        }
        else {
            requestPermissions(Array(1) { CAMERA_PERMISSION }, PERMISSION_REQUEST_CODE)
        }

    }

    /**Detect pressing volume down key**/
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
            if (slidingUpPanel.panelState == SlidingUpPanelLayout.PanelState.COLLAPSED)
                slidingUpPanel.panelState = SlidingUpPanelLayout.PanelState.ANCHORED
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    @OptIn(InternalCoroutinesApi::class)
    override fun onDestroy() {
        synchronized(this) {
            cls.finish()
            super.onDestroy()
        }
    }

    @OptIn(InternalCoroutinesApi::class)
    override fun onResume() {
        synchronized(this) {
            super.onResume()

            handlerThread = HandlerThread("InferenceThread")
            handlerThread!!.start()
            handler = Handler(handlerThread!!.looper)
        }

    }

    @OptIn(InternalCoroutinesApi::class)
    override fun onPause() {
        synchronized(this) {
            handlerThread!!.quitSafely()

            try {
                handlerThread!!.join()
                handlerThread = null
                handler = null
            } catch (e : InterruptedException){
                e.printStackTrace()
            }

            super.onPause()
        }
    }

    @OptIn(InternalCoroutinesApi::class)
    override fun onStart() {
        synchronized(this){
            super.onStart()
        }
    }

    @OptIn(InternalCoroutinesApi::class)
    override fun onStop() {
        synchronized(this){
            super.onStop()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST_CODE){
            if (grantResults.isNotEmpty() && allPermissionsGranted(grantResults)){
                setFragment()
            }
            else {
                Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun allPermissionsGranted(grantResults: IntArray) : Boolean{
        for (result : Int in grantResults){
            if (result != PackageManager.PERMISSION_GRANTED){
                return false
            }
        }
        return true
    }

    private fun setFragment(){
        val inputSize : Size = cls.getModelInputSize()
        val cameraId : String = chooseCamera()

        if (inputSize.width > 0 && inputSize.height > 0 && cameraId.isNotEmpty()){
            val fragment : Fragment = CameraFragment.newInstance(
                object : CameraFragment.ConnectionCallback {
                    override fun onPreviewSizeChosen(size: Size?, cameraRotation: Int) {
                        previewWidth = size!!.width
                        previewHeight = size.height
                        sensorOrientation = cameraRotation - getScreenOrientation()
                    }
                },
                { reader -> processImage(reader!!) },
                inputSize,
                cameraId
            )

            Log.d(TAG, "inputSize : " + cls.getModelInputSize() +
                    "sensorOrientation : " + sensorOrientation)

            supportFragmentManager.commit {
                replace(binding.cameraFragment.id, fragment)
            }
        }
        else {
            Toast.makeText(this, "Can't find camera", Toast.LENGTH_SHORT).show()
        }
    }

    private fun chooseCamera() : String {
        val manager : CameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            for (cameraId : String in manager.cameraIdList){
                val characteristics : CameraCharacteristics =
                    manager.getCameraCharacteristics(cameraId)

                val facing : Int? = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_BACK){
                    return cameraId
                }
            }
        } catch (e : CameraAccessException){
            e.printStackTrace()
        }

        return ""
    }

    private fun getScreenOrientation() : Int {
        //API Level 30 이상에서는 context.getDisplay() 사용
        val rotation : Int? = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                this.display?.rotation
            }
            else -> {
                @Suppress("DEPRECATION")
                windowManager?.defaultDisplay!!.rotation
            }
        }
        return when (rotation) {
            Surface.ROTATION_270 -> 270
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_90 -> 90
            else -> 0
        }
    }

    private fun processImage(reader: ImageReader){
        if (previewWidth == 0 || previewHeight == 0) return

        if (rgbFrameBitmap == null){
            rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888)
        }

        if (isProcessingFrame) return
        isProcessingFrame = true

        val image : Image? = reader.acquireLatestImage()
        if (image == null) { isProcessingFrame = false; return }

        YuvToRgbConverter.yuvToRgb(this, image, rgbFrameBitmap!!)

        runInBackground {
            if (cls.isInitialized()){
                val output : Pair<String, Float> = cls.classify(rgbFrameBitmap!!, sensorOrientation)

                runOnUiThread {
                    val resultStr : String = String.format(Locale.ENGLISH,
                        "class : %s, prob : %.2f%%",
                        output.first, output.second * 100)
                    binding.objectTitle.text = resultStr
                }
            }
            image.close()
            isProcessingFrame = false
        }

    }

    private fun runInBackground(r : Runnable){
        if (handler != null) handler!!.post(r)
    }

    inner class PanelEventListener : SlidingUpPanelLayout.PanelSlideListener {
        override fun onPanelSlide(panel: View?, slideOffset: Float) {
            //ignore
        }
        override fun onPanelStateChanged(
            panel: View?,
            previousState: SlidingUpPanelLayout.PanelState?,
            newState: SlidingUpPanelLayout.PanelState?
        ) {
           /**if (newState == SlidingUpPanelLayout.PanelState.EXPANDED)**/
        }
    }

    companion object{
        @JvmStatic
        val TAG : String = "[IC]MainActivity"

        @JvmStatic
        val CAMERA_PERMISSION : String = android.Manifest.permission.CAMERA

        @JvmStatic
        val PERMISSION_REQUEST_CODE : Int = 1
    }
}