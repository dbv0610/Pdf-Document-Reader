package com.alf06.document.reader.ui.home.scanner.detection

import android.graphics.Bitmap
import com.alf06.document.reader.ui.home.scanner.PendingCrop
import java.io.File
import kotlin.math.hypot
import kotlin.math.max
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Size
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc

data class ProcessedScan(val original: File, val output: File)

class DocumentImageProcessor(
    private val detector: DocumentEdgeDetector = DocumentEdgeDetector(),
) {
    fun process(original: File, liveCorners: List<Point>? = null): ProcessedScan {
        val source = Imgcodecs.imread(original.absolutePath, Imgcodecs.IMREAD_COLOR)
        if (source.empty()) {
            source.release()
            return ProcessedScan(original, original)
        }
        val gray = Mat()
        return try {
            val points = liveCorners?.takeIf { it.size == 4 }?.let { fractions ->
                DocumentEdgeDetector.orderPoints(fractions.map { Point(it.x * source.cols(), it.y * source.rows()) })
            } ?: run {
                Imgproc.cvtColor(source, gray, Imgproc.COLOR_BGR2GRAY)
                detector.detect(gray)?.expandedBy()?.points
            } ?: return ProcessedScan(original, original)

            val tl = points[0]
            val tr = points[1]
            val br = points[2]
            val bl = points[3]
            val outputWidth = max(distance(tl, tr), distance(bl, br)).toInt().coerceAtLeast(1)
            val outputHeight = max(distance(tl, bl), distance(tr, br)).toInt().coerceAtLeast(1)
            val sourcePoints = MatOfPoint2f(tl, tr, br, bl)
            val targetPoints = MatOfPoint2f(
                Point(0.0, 0.0), Point(outputWidth - 1.0, 0.0),
                Point(outputWidth - 1.0, outputHeight - 1.0), Point(0.0, outputHeight - 1.0),
            )
            val transform = Imgproc.getPerspectiveTransform(sourcePoints, targetPoints)
            val output = Mat()
            try {
                Imgproc.warpPerspective(source, output, transform, Size(outputWidth.toDouble(), outputHeight.toDouble()), Imgproc.INTER_CUBIC)
                val corrected = File(original.parentFile, "scan_${System.currentTimeMillis()}.jpg")
                if (Imgcodecs.imwrite(corrected.absolutePath, output)) ProcessedScan(original, corrected)
                else ProcessedScan(original, original)
            } finally {
                sourcePoints.release()
                targetPoints.release()
                transform.release()
                output.release()
            }
        } finally {
            gray.release()
            source.release()
        }
    }

    fun prepareCrop(original: File, liveCorners: List<Point>? = null): PendingCrop? {
        val source = Imgcodecs.imread(original.absolutePath, Imgcodecs.IMREAD_COLOR)
        val gray = Mat()
        try {
            if (source.empty()) return null
            val width = source.cols()
            val height = source.rows()
            val corners = liveCorners?.takeIf { it.size == 4 } ?: run {
                Imgproc.cvtColor(source, gray, Imgproc.COLOR_BGR2GRAY)
                detector.detect(gray)?.expandedBy()?.points?.map { Point(it.x / width, it.y / height) }
            } ?: listOf(
                Point(DEFAULT_INSET, DEFAULT_INSET), Point(1 - DEFAULT_INSET, DEFAULT_INSET),
                Point(1 - DEFAULT_INSET, 1 - DEFAULT_INSET), Point(DEFAULT_INSET, 1 - DEFAULT_INSET),
            )
            return PendingCrop(original, width, height, corners)
        } finally {
            gray.release()
            source.release()
        }
    }

    fun rotateClockwise(file: File): File? {
        val source = Imgcodecs.imread(file.absolutePath, Imgcodecs.IMREAD_COLOR)
        val rotated = Mat()
        try {
            if (source.empty()) return null
            Core.rotate(source, rotated, Core.ROTATE_90_CLOCKWISE)
            val target = File(file.parentFile, "rotated_${System.currentTimeMillis()}.jpg")
            return if (Imgcodecs.imwrite(target.absolutePath, rotated)) target else null
        } finally {
            rotated.release()
            source.release()
        }
    }

    /** Writes [file] with [filter] applied to a new JPEG; [ScanFilter.Default] returns [file] itself. */
    fun applyFilter(file: File, filter: ScanFilter): File? {
        if (filter == ScanFilter.Default) return file
        val source = Imgcodecs.imread(file.absolutePath, Imgcodecs.IMREAD_COLOR)
        val filtered = Mat()
        try {
            if (source.empty()) return null
            filter.apply(source, filtered)
            val target = File(file.parentFile, "filter_${System.currentTimeMillis()}.jpg")
            return if (Imgcodecs.imwrite(target.absolutePath, filtered)) target else null
        } finally {
            filtered.release()
            source.release()
        }
    }

    /** Decodes [file] shrunk so its longer side is at most [maxSide], for on-screen previews. */
    fun loadPreview(file: File, maxSide: Int): Bitmap? {
        val source = Imgcodecs.imread(file.absolutePath, Imgcodecs.IMREAD_COLOR)
        val scaled = Mat()
        try {
            if (source.empty()) return null
            val scale = minOf(1.0, maxSide.toDouble() / max(source.cols(), source.rows()))
            Imgproc.resize(source, scaled, Size(), scale, scale, Imgproc.INTER_AREA)
            return scaled.toBitmap()
        } finally {
            scaled.release()
            source.release()
        }
    }

    /** Returns a new bitmap with [filter] applied to [bitmap]. */
    fun filterBitmap(bitmap: Bitmap, filter: ScanFilter): Bitmap {
        val rgba = Mat()
        val bgr = Mat()
        val filtered = Mat()
        try {
            Utils.bitmapToMat(bitmap, rgba)
            Imgproc.cvtColor(rgba, bgr, Imgproc.COLOR_RGBA2BGR)
            filter.apply(bgr, filtered)
            return filtered.toBitmap()
        } finally {
            filtered.release()
            bgr.release()
            rgba.release()
        }
    }

    private fun Mat.toBitmap(): Bitmap {
        val rgba = Mat()
        try {
            Imgproc.cvtColor(this, rgba, Imgproc.COLOR_BGR2RGBA)
            return Bitmap.createBitmap(cols(), rows(), Bitmap.Config.ARGB_8888).also { Utils.matToBitmap(rgba, it) }
        } finally {
            rgba.release()
        }
    }

    private fun distance(a: Point, b: Point): Double = hypot(a.x - b.x, a.y - b.y)

    private companion object {
        const val DEFAULT_INSET = 0.05
    }
}
