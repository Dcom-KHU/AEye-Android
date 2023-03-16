package com.example.aeye.ui.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.*

import android.hardware.camera2.*
import android.hardware.camera2.params.StreamConfigurationMap
import android.media.ImageReader

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread

import android.util.Size
import android.view.*
import android.widget.Toast

import androidx.fragment.app.Fragment

import com.example.aeye.R
import com.example.aeye.env.AutoFitTextureView

import java.lang.Exception
import java.lang.Long.signum
import java.lang.RuntimeException
import java.util.*
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

import kotlin.Comparator
import kotlin.collections.ArrayList
import kotlin.math.max
import kotlin.math.min

/**
 * A simple [Fragment] subclass.
 * Use the [CameraFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

class CameraFragment(
    private var connectionCallback: ConnectionCallback,
    private var imageAvailableListener: ImageReader.OnImageAvailableListener,
    private var inputSize: Size,
    private var cameraId: String
) : Fragment() {

    private var autoFitTextureView : AutoFitTextureView ?= null

    private var backgroundThread : HandlerThread ?= null
    private var backgroundHandler : Handler ?= null

    private var previewSize: Size ?= null
    private var sensorOrientation : Int ?= null

    private val cameraOpenCloseLock : Semaphore = Semaphore(1)

    private var cameraDevice : CameraDevice ?= null
    private var captureSession: CameraCaptureSession ?= null
    private var previewReader: ImageReader ? = null
    private lateinit var previewRequestBuilder: CaptureRequest.Builder


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        autoFitTextureView = view.findViewById(R.id.autoFitTextureView)
    }

    override fun onResume() {
        super.onResume()
        startBackgroundThread()

        if(!autoFitTextureView!!.isAvailable)
            autoFitTextureView!!.surfaceTextureListener = surfaceTextureListener
        else
            openCamera(autoFitTextureView!!.width, autoFitTextureView!!.height)
    }

    override fun onPause() {
        closeCamera()
        stopBackgroundThread()
        super.onPause()
    }

    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("ImageListener")
        backgroundThread!!.start()
        backgroundHandler = Handler(backgroundThread!!.looper)
    }

    private fun stopBackgroundThread() {
        backgroundThread!!.quitSafely()
        try {
            backgroundThread!!.join()
            backgroundThread = null
            backgroundHandler = null
        }catch (e : InterruptedException){
            e.printStackTrace()
        }
    }

    private val surfaceTextureListener = object : TextureView.SurfaceTextureListener{
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            openCamera(width, height)
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
            configureTransform(width, height)
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            return true
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
            //Do Nothing
        }
    }

    @SuppressLint("MissingPermission")
    private fun openCamera(width : Int, height : Int){
        val currentActivity = activity
        val manager : CameraManager = currentActivity?.getSystemService(Context.CAMERA_SERVICE) as CameraManager

        setUpCameraOutputs(manager)
        configureTransform(width, height)

        try {
            if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)){
                Toast.makeText(context,
                        "Time out waiting to lock camera opening.",
                        Toast.LENGTH_LONG).show()
                currentActivity.finish()
            }
            else{
                manager.openCamera(cameraId, stateCallback, backgroundHandler)
            }
        } catch (e : Exception){
            when(e){
                is InterruptedException, is CameraAccessException -> e.printStackTrace()
                else -> throw e
            }
        }

    }

    private fun setUpCameraOutputs(manager: CameraManager){
        try {
            val characteristics : CameraCharacteristics = manager.getCameraCharacteristics(cameraId)
            val map : StreamConfigurationMap ?= characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)

            sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)

            previewSize = chooseOptimalSize(
                map!!.getOutputSizes(SurfaceTexture::class.java),
                inputSize.width,
                inputSize.height
            )

            val orientation : Int = resources.configuration.orientation
            if (orientation == Configuration.ORIENTATION_LANDSCAPE)
                autoFitTextureView!!.setAspectRatio(previewSize!!.width, previewSize!!.height)
            else
                autoFitTextureView!!.setAspectRatio(previewSize!!.height, previewSize!!.width)

        } catch (cae : CameraAccessException){
            cae.printStackTrace()
        }
        connectionCallback.onPreviewSizeChosen(previewSize, sensorOrientation!!)
    }

    private fun configureTransform(viewWidth : Int, viewHeight : Int){
        val currentActivity = activity
        if (null == autoFitTextureView
            || null == previewSize
            || null == activity){
            return
        }

        //API Level 30 이상에서는 context.getDisplay() 사용
        val rotation : Int? = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                context?.display?.rotation
            }
            else -> {
                @Suppress("DEPRECATION")
                currentActivity?.windowManager?.defaultDisplay!!.rotation
            }
        }

        val matrix = Matrix()
        val viewRect = RectF(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat())
        val bufferRect = RectF(0f, 0f, previewSize?.height!!.toFloat(), previewSize?.width!!.toFloat())

        val centerX : Float = viewRect.centerX()
        val centerY : Float = viewRect.centerY()

        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation){
            bufferRect.offset(
                centerX - bufferRect.centerX(),
                centerY - bufferRect.centerY()
            )
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL)
            val scale : Float = max(
                viewHeight.toFloat() / previewSize!!.height,
                viewWidth.toFloat() / previewSize!!.width
            )
            matrix.postScale(scale, scale, centerX, centerY)
            matrix.postRotate(90f * (rotation - 2), centerX, centerY)
        }
        else if (Surface.ROTATION_180 == rotation){
            matrix.postRotate(180f, centerX, centerY)
        }
        autoFitTextureView!!.setTransform(matrix)
    }

    private fun chooseOptimalSize(choices : Array<Size>, width: Int, height: Int) : Size{
        val minSize : Int = min(width, height)
        val desiredSize = Size(width, height)

        val bigEnough = ArrayList<Size>()
        val tooSmall = ArrayList<Size>()

        for (option : Size in choices){
            if (option == desiredSize){
                return desiredSize
            }
            if (option.height >= minSize && option.width >= minSize)
                bigEnough.add(option)
            else
                tooSmall.add(option)

        }
        return if (bigEnough.isNotEmpty())
            Collections.min(bigEnough, CompareSizesByArea())
        else
            Collections.max(tooSmall, CompareSizesByArea())
    }

    private val stateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraOpenCloseLock.release()
            cameraDevice = camera
            createCameraPreviewSession()
        }

        override fun onDisconnected(camera: CameraDevice) {
            cameraOpenCloseLock.release()
            camera.close()
            cameraDevice = null
        }

        override fun onError(camera: CameraDevice, error: Int) {
            cameraOpenCloseLock.release()
            camera.close()
            cameraDevice = null
            if (activity != null)
                activity?.finish()
        }
    }

    @SuppressLint("Recycle")
    private fun createCameraPreviewSession() {
        try {
            val texture : SurfaceTexture? = autoFitTextureView!!.surfaceTexture
            texture?.setDefaultBufferSize(previewSize!!.width, previewSize!!.height)

            val surface = Surface(texture)

            previewReader = ImageReader.newInstance(
                previewSize!!.width,
                previewSize!!.height,
                ImageFormat.YUV_420_888, 2
            )
            previewReader!!.setOnImageAvailableListener(imageAvailableListener, backgroundHandler)

            previewRequestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            previewRequestBuilder.addTarget(surface)
            previewRequestBuilder.addTarget(previewReader!!.surface)

            previewRequestBuilder.set(
                CaptureRequest.CONTROL_AF_MODE,
                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
            )
            previewRequestBuilder.set(
                CaptureRequest.CONTROL_AE_MODE,
                CaptureRequest.CONTROL_AE_MODE_ON_ALWAYS_FLASH
            )
            previewRequestBuilder.set(
                CaptureRequest.FLASH_MODE,
                CameraMetadata.FLASH_MODE_TORCH
            )

            cameraDevice!!.createCaptureSession(
                arrayOf(surface, previewReader!!.surface).toList(),
                sessionStateCallback,
                null
            )

        } catch (e : CameraAccessException){
            e.printStackTrace()
        }
    }

    private val sessionStateCallback = object : CameraCaptureSession.StateCallback() {
        override fun onConfigured(session: CameraCaptureSession) {
            if (null == cameraDevice)
                return

            captureSession = session
            try {
                captureSession!!.setRepeatingRequest(
                    previewRequestBuilder.build(), null, backgroundHandler
                )
            } catch (e : CameraAccessException) {
                e.printStackTrace()
            }
        }

        override fun onConfigureFailed(session: CameraCaptureSession) {
            Toast.makeText(activity, "CameraCaptureSession Failed", Toast.LENGTH_SHORT).show()
        }

    }

    private fun closeCamera() {
        try {
            cameraOpenCloseLock.acquire()
            if (null != captureSession){
                captureSession!!.close()
                captureSession = null
            }
            if (null != cameraDevice){
                cameraDevice!!.close()
                cameraDevice = null
            }
            if (null != previewReader){
                previewReader!!.close()
                previewReader = null
            }
        } catch (e : InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera closing.", e)
        } finally {
            cameraOpenCloseLock.release()
        }
    }

    interface ConnectionCallback {
        fun onPreviewSizeChosen(size: Size?, cameraRotation : Int)
    }

    companion object {
        @JvmStatic
        val TAG : String = "[IC]CameraFragment"

        @JvmStatic
        fun newInstance(
            callback: ConnectionCallback,
            imageAvailableListener: ImageReader.OnImageAvailableListener,
            inputSize: Size,
            cameraId: String) = CameraFragment(callback, imageAvailableListener, inputSize, cameraId)

        class CompareSizesByArea : Comparator<Size>{
            override fun compare(lhs: Size, rhs: Size): Int {
                return signum((lhs.width * lhs.height).toLong() - (rhs.width * rhs.height).toLong())
            }
        }
    }
}
