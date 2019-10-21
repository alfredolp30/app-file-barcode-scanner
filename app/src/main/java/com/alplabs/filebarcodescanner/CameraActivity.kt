package com.alplabs.filebarcodescanner

import android.os.Bundle
import android.Manifest.permission
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_DENIED
import androidx.core.app.ActivityCompat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import com.alplabs.filebarcodescanner.metrics.CALog
import android.media.ImageReader.OnImageAvailableListener
import com.alplabs.filebarcodescanner.model.BarcodeModel
import com.alplabs.filebarcodescanner.scanner.AsyncFirebaseBarcodeBufferDetector
import android.content.Intent
import android.media.ImageReader.newInstance
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils


import kotlinx.android.synthetic.main.activity_camera.*
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit


class CameraActivity : BaseActivity(), AsyncFirebaseBarcodeBufferDetector.Listener {

    companion object {

        private val REQUEST_CAMERA_PERMISSION = nextRequestCode()

        const val BARCODE = "barcode"

        const val WIDTH = 1280
        const val HEIGHT = 720

    }


    private var cameraDevice: CameraDevice? = null
    private var captureRequestBuilder: CaptureRequest.Builder? = null
    private var captureSession: CameraCaptureSession? = null

    private var backgroundThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null

    private var frameCounter = 0


    private var imageReader: ImageReader? = null


    private val cameraOpenCloseLock = Semaphore(1)


    private val surface: Surface by lazy {
        textureView.surfaceTexture.setDefaultBufferSize(WIDTH, HEIGHT)
        Surface(textureView.surfaceTexture)
    }


    private val textureListener: TextureView.SurfaceTextureListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            //open your camera here
            openCamera()
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
            // Transform you image captured size according to the surface width and height
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            return false
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {

        }
    }


    private val deviceStateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraOpenCloseLock.release()
            cameraDevice = camera
            createCameraPreview()
        }

        override fun onDisconnected(camera: CameraDevice) {
            cameraOpenCloseLock.release()
            cameraDevice?.close()
        }

        override fun onError(camera: CameraDevice, error: Int) {
            cameraOpenCloseLock.release()
            cameraDevice?.close()
            cameraDevice = null
        }
    }


    private val captureSessionStateCallback =  object : CameraCaptureSession.StateCallback() {

        override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
            if (null == cameraDevice) {
                return
            }

            captureSession = cameraCaptureSession
            updatePreview()
        }

        override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
            showToast("Configuration failed")
        }

    }

    private val imageAvailableListener = OnImageAvailableListener { reader ->
        backgroundHandler?.post {

            try {

                val image = reader.acquireLatestImage()

                if ((frameCounter++ % 30) == 0) {
                    val buffer = image.planes[0].buffer
                    AsyncFirebaseBarcodeBufferDetector(this, WIDTH, HEIGHT).execute(buffer)
                }

                image?.close()

            } catch (th: Throwable) {
                CALog.e("imageAvailableListener", "no acquire latest image", th)
            }
        }

    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)


        val anim = AnimationUtils.loadAnimation(this, R.anim.translate_divider_camera)
        anim.repeatCount = Animation.INFINITE
        divider.startAnimation(anim)

        imgBtnClose.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()

        startBackgroundThread()

        if (textureView.isAvailable) {
            openCamera()
        } else {
            textureView.surfaceTextureListener = textureListener
        }
    }

    override fun onPause() {
        closeCamera()
        stopBackgroundThread()

        super.onPause()
    }


    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("camera_background").also { it.start() }
        backgroundHandler = Handler(backgroundThread!!.looper)
    }

    private fun stopBackgroundThread() {
        backgroundThread?.quitSafely()

        try {
            backgroundThread?.join()
            backgroundThread = null
            backgroundHandler = null
        } catch (e: InterruptedException) {
            CALog.e(CameraActivity::stopBackgroundThread.name, "Join thread for quit", e)
        }

    }

    private fun createCameraPreview() {

        try {

            imageReader = newInstance(WIDTH, HEIGHT, android.graphics.ImageFormat.YUV_420_888, 24)
            imageReader?.setOnImageAvailableListener(imageAvailableListener, backgroundHandler)
            val imageReaderSurface = imageReader!!.surface


            captureRequestBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequestBuilder?.addTarget(surface)
            captureRequestBuilder?.addTarget(imageReaderSurface)

            cameraDevice?.createCaptureSession(listOf(surface, imageReaderSurface), captureSessionStateCallback, null)

        } catch (e: CameraAccessException) {
            CALog.e(CameraActivity::createCameraPreview.name, "Not access camera", e)
        }

    }

    private fun openCamera() {
        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        try {
            val cameraId = manager.cameraIdList.firstOrNull() ?: return

            if (ActivityCompat.checkSelfPermission(this, permission.CAMERA) == PERMISSION_DENIED) {

                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(permission.CAMERA),
                    REQUEST_CAMERA_PERMISSION
                )

                return
            }

            // Wait for camera to open - 2.5 seconds is sufficient
            if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw RuntimeException("Time out waiting to lock camera opening.")
            }

            manager.openCamera(cameraId, deviceStateCallback, backgroundHandler)

        } catch (e: CameraAccessException) {
            CALog.e(CameraActivity::createCameraPreview.name, "Not access camera", e)
        }
    }

    private fun updatePreview() {

        val requestBuilder = captureRequestBuilder ?: return

        requestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)

        try {
            captureSession?.setRepeatingRequest(requestBuilder.build(), null, backgroundHandler)
        } catch (e: CameraAccessException) {
            CALog.e(CameraActivity::createCameraPreview.name, "Not access camera", e)
        }
    }

    private fun closeCamera() {

        try {
            cameraOpenCloseLock.acquire()

            captureSession?.close()
            captureSession = null

            cameraDevice?.close()
            cameraDevice = null

            imageReader?.close()
            imageReader = null

            captureRequestBuilder = null

        } catch (e: InterruptedException) {
            CALog.e(CameraActivity::closeCamera.name, "Close camera", e)
        } finally {
            cameraOpenCloseLock.release()
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {

            if (grantResults[0] == PERMISSION_DENIED) {
                showToast("Sorry!!!, you can't use this app without granting permission")
                finish()
            }

        }
    }

    override fun onDetectorFinish(barcodeModel: BarcodeModel?) {

        barcodeModel?.let { model ->
            CALog.i(CameraActivity::onDetectorFinish.name, "Found barcode with camera")

            Intent().also {
                it.putParcelableArrayListExtra(BARCODE, arrayListOf(model))
                setResult(Activity.RESULT_OK, it)
                finish()
            }
        }

    }
}