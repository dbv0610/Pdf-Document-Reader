package com.alf06.document.reader.ui.home.scanner.fragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import android.view.Surface
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.TransformExperimental
import androidx.camera.view.transform.CoordinateTransform
import androidx.camera.view.transform.ImageProxyTransformFactory
import androidx.camera.view.transform.OutputTransform
import androidx.activity.result.PickVisualMediaRequest
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.alf06.document.reader.R
import com.alf06.document.reader.databinding.FragmentDocumentScannerBinding
import com.alf06.document.reader.ui.home.scanner.ScanSessionViewModel
import com.alf06.document.reader.ui.home.scanner.ScannerNavigator
import com.alf06.document.reader.ui.home.scanner.detection.AutoCaptureTrigger
import com.alf06.document.reader.ui.home.scanner.detection.CornerStabilizer
import com.alf06.document.reader.ui.home.scanner.detection.DocumentCorners
import com.alf06.document.reader.ui.home.scanner.detection.DocumentEdgeDetector
import com.alf06.document.reader.ui.home.scanner.detection.DocumentImageProcessor
import com.alf06.document.reader.ui.home.scanner.detection.YPlaneMatConverter
import com.ui.baselib.base.BaseFragment
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import org.opencv.android.OpenCVLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.opencv.core.Point

@TransformExperimental
class DocumentScannerFragment : BaseFragment<FragmentDocumentScannerBinding>(
    FragmentDocumentScannerBinding::inflate
) {
    private val viewModel: ScanSessionViewModel by activityViewModel()

    override fun FragmentDocumentScannerBinding.initView() {
        bottomPanel.updatePadding(bottom = bottomPanel.paddingBottom + navigationBarHeight)

        isCapturing.set(false)
        liveCorners = null
        setControlsEnabled(true)
        setAutoMode(autoMode)
        updateFlashIcon()
        viewModel.pages.observe(viewLifecycleOwner) { pages ->
            showScannedPages(pages.lastOrNull()?.output, pages.size)
        }
        if (!OpenCVLoader.initLocal()) {
            Toast.makeText(appContext(), R.string.scanner_capture_failed, Toast.LENGTH_LONG).show()
            appActivity?.finish()
        } else if (ContextCompat.checkSelfPermission(
                appContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            binding.previewView.post { startCamera() }
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    override fun FragmentDocumentScannerBinding.onClick() {
        btnClose.setOnClickListener {
            viewModel.clear()
            activity?.finish()
        }
        thumbnailContainer.setOnClickListener { openPages() }
        btnFlash.setOnClickListener { toggleFlash() }
        btnManual.setOnClickListener { setAutoMode(false) }
        btnAuto.setOnClickListener { setAutoMode(true) }
        captureButton.setOnClickListener { capture() }
        btnGallery.setOnClickListener {
            if (isCapturing.get()) return@setOnClickListener
            galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    override fun onBackPressed(): Boolean {
        if (viewModel.pageList.isNotEmpty()) openPages() else activity?.finish()
        return true
    }

    private fun openPages() {
        if (isCapturing.get()) return
        viewModel.retakeIndex = null
        navigateTo(ScannerNavigator.Pages.route)
    }

    private fun setAutoMode(enabled: Boolean) {
        autoMode = enabled
        autoCapture.reset()
        binding.btnManual.isSelected = !enabled
        binding.btnAuto.isSelected = enabled

        binding.scanHint.isVisible = enabled || isCapturing.get()
    }

    private fun toggleFlash() {
        flashOn = !flashOn
        imageCapture?.flashMode = currentFlashMode()
        updateFlashIcon()
    }

    private fun currentFlashMode() =
        if (flashOn) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF

    private fun updateFlashIcon() {
        binding.btnFlash.setImageResource(if (flashOn) R.drawable.ic_app_flash else R.drawable.ic_app_no_flash)
    }

    private fun showScannedPages(lastPage: File?, count: Int) {
        binding.thumbnailContainer.isVisible = lastPage != null && count > 0
        if (lastPage == null || count <= 0) return
        binding.tvPageCount.text = count.toString()
        Glide.with(this).load(lastPage).centerCrop().into(binding.ivThumbnail)
    }

    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var imageCapture: ImageCapture? = null
    private var frameCount = 0
    private val detector = DocumentEdgeDetector()
    private val stabilizer = CornerStabilizer()
    private val autoCapture = AutoCaptureTrigger()
    private val isCapturing = AtomicBoolean(false)
    @Volatile
    private var autoMode = false
    private var flashOn = false
    @Volatile
    private var liveCorners: DocumentCorners? = null

    @Volatile
    private var liveRotationDegrees = 0
    private val imageTransformFactory = ImageProxyTransformFactory().apply {
        isUsingCropRect = false

        isUsingRotationDegrees = false
    }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) startCamera() else showPermissionMessage()
        }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) importFromGallery(uri)
        }

    private fun startCamera() {
        val providerFuture = ProcessCameraProvider.getInstance(appContext())
        providerFuture.addListener({
            val binding = bindingOrNull ?: return@addListener
            try {
                val provider = providerFuture.get()
                val rotation = binding.previewView.display?.rotation ?: Surface.ROTATION_0

                val resolutionSelector = ResolutionSelector.Builder()
                    .setAspectRatioStrategy(AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY)
                    .build()
                val preview = Preview.Builder()
                    .setResolutionSelector(resolutionSelector)
                    .setTargetRotation(rotation)
                    .build()
                    .also { it.setSurfaceProvider(binding.previewView.surfaceProvider) }
                val captureUseCase = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                    .setFlashMode(currentFlashMode())
                    .setResolutionSelector(resolutionSelector)
                    .setTargetRotation(rotation)
                    .build()
                imageCapture = captureUseCase
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setResolutionSelector(resolutionSelector)
                    .setTargetRotation(rotation)
                    .build()
                    .also { useCase ->
                        useCase.setAnalyzer(cameraExecutor) { image -> analyze(image) }
                    }
                provider.unbindAll()

                provider.bindToLifecycle(
                    viewLifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    captureUseCase,
                    analysis
                )
            } catch (exception: Exception) {
                Log.e(TAG, "Unable to start CameraX preview", exception)
                Toast.makeText(
                    appContext(),
                    "Camera preview error: ${exception.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }, ContextCompat.getMainExecutor(appContext()))
    }

    @OptIn(TransformExperimental::class)
    private fun analyze(image: ImageProxy) {
        try {
            frameCount++
            if (isCapturing.get()) {
                autoCapture.reset()
                return
            }
            val mat = YPlaneMatConverter.toMat(image)
            try {
                val startNanos = System.nanoTime()
                val detected = try {
                    detector.detect(mat)
                } catch (exception: Exception) {
                    Log.e(TAG, "Document detection failed for frame", exception)
                    null
                }
                val detectMs = (System.nanoTime() - startNanos) / 1_000_000
                val stable = stabilizer.update(detected)

                val shown = stable?.expandedBy()
                liveCorners = shown
                liveRotationDegrees = image.imageInfo.rotationDegrees

                val holding = detected != null && stable != null
                val shouldCapture = autoMode &&
                    autoCapture.update(if (holding) stable else null, SystemClock.elapsedRealtime())
                if (frameCount % 30 == 0) {
                    Log.d(
                        TAG,
                        "analysis=${mat.cols()}x${mat.rows()}, detectMs=$detectMs, detected=${detected != null}, state=${stabilizer.state}, stable=${stable != null}"
                    )
                }
                val sourceTransform = imageTransformFactory.getOutputTransform(image)
                val overlay = bindingOrNull?.documentOverlay ?: return
                overlay.post {
                    val binding = bindingOrNull ?: return@post
                    if (!isCapturing.get()) {
                        binding.scanHint.setText(if (holding) R.string.scanner_hint_hold_steady else R.string.scanner_hint_searching)
                    }
                    if (shouldCapture) capture()
                    if (shown == null) {
                        binding.documentOverlay.setMappedPoints(null)
                        return@post
                    }
                    mapToPreview(shown, sourceTransform, 0)
                }
            } finally {
                mat.release()
            }
        } finally {
            image.close()
        }
    }

    @OptIn(TransformExperimental::class)
    private fun mapToPreview(
        corners: DocumentCorners,
        sourceTransform: OutputTransform,
        attempt: Int
    ) {
        val binding = bindingOrNull ?: return
        val targetTransform = binding.previewView.outputTransform
        if (targetTransform == null) {
            Log.d(TAG, "PreviewView outputTransform is not ready")
            if (attempt < 10) {
                binding.documentOverlay.postDelayed({
                    mapToPreview(corners, sourceTransform, attempt + 1)
                }, 50L)
            }
            return
        }
        val mapped = FloatArray(8)
        corners.points.forEachIndexed { index, point ->
            mapped[index * 2] = point.x.toFloat()
            mapped[index * 2 + 1] = point.y.toFloat()
        }
        CoordinateTransform(sourceTransform, targetTransform).mapPoints(mapped)
        binding.documentOverlay.setMappedPoints(mapped)
    }

    private fun capture() {
        val capture = imageCapture ?: return
        if (!isCapturing.compareAndSet(false, true)) return
        val cropCorners = liveCorners?.toUprightFractions(liveRotationDegrees)
        setControlsEnabled(false)
        binding.scanHint.setText(R.string.scanner_hint_scanning)
        binding.scanHint.isVisible = true
        val original = newScanFile()
        val options = ImageCapture.OutputFileOptions.Builder(original).build()
        capture.takePicture(
            options,
            ContextCompat.getMainExecutor(appContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    openCrop(original, cropCorners)
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Capture failed", exception)
                    if (!isSafeToUpdateUI) return
                    Toast.makeText(
                        appContext(),
                        R.string.scanner_capture_failed,
                        Toast.LENGTH_SHORT
                    ).show()
                    resetCapture()
                }
            })
    }

    private fun importFromGallery(uri: Uri) {
        if (!isCapturing.compareAndSet(false, true)) return
        setControlsEnabled(false)
        val resolver = appContext().contentResolver
        val original = newScanFile()
        viewLifecycleOwner.lifecycleScope.launch {
            val copied = withContext(Dispatchers.IO) {
                runCatching {
                    resolver.openInputStream(uri)?.use { input ->
                        original.outputStream().use { output -> input.copyTo(output) }
                    } != null
                }.onFailure { Log.e(TAG, "Unable to import gallery image", it) }.getOrDefault(false)
            }

            if (copied) openCrop(original, null) else showCaptureError()
        }
    }

    private fun openCrop(original: File, cropCorners: List<Point>?) {
        val owner = viewLifecycleOwnerLiveData.value ?: return
        owner.lifecycleScope.launch {
            val pending = withContext(Dispatchers.Default) {
                runCatching { DocumentImageProcessor().prepareCrop(original, cropCorners) }
                    .onFailure { Log.e(TAG, "Unable to read captured photo", it) }
                    .getOrNull()
            }
            if (pending == null) {
                original.delete()
                showCaptureError()
                return@launch
            }
            viewModel.pendingCrop = pending
            navigateTo(ScannerNavigator.Crop.route)
        }
    }

    private fun showCaptureError() {
        Toast.makeText(appContext(), R.string.scanner_capture_failed, Toast.LENGTH_SHORT).show()
        resetCapture()
    }

    private fun newScanFile(): File {
        val directory = File(appContext().cacheDir, "scanner").apply { mkdirs() }
        return File(directory, "original_${System.currentTimeMillis()}.jpg")
    }

    private fun setControlsEnabled(enabled: Boolean) {
        binding.captureButton.isEnabled = enabled
        binding.btnGallery.isEnabled = enabled
        binding.btnManual.isEnabled = enabled
        binding.btnAuto.isEnabled = enabled
    }

    private fun resetCapture() {
        setControlsEnabled(true)
        binding.scanHint.setText(R.string.scanner_hint_searching)
        isCapturing.set(false)
        binding.scanHint.isVisible = autoMode
    }

    private fun showPermissionMessage() {
        AlertDialog.Builder(appContext())
            .setMessage(R.string.camera_permission_required)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:${appContext().packageName}")
                    )
                )
            }
            .setNegativeButton(android.R.string.cancel) { _, _ -> activity?.finish() }
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "DocumentScanner"
        const val EXTRA_ORIGINAL_PATH = "scanner_original_path"
        const val EXTRA_OUTPUT_PATH = "scanner_output_path"

        const val EXTRA_OUTPUT_PATHS = "scanner_output_paths"
    }
}
