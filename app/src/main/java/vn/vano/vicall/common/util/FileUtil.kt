package vn.vano.vicall.common.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import org.jetbrains.anko.ctx
import vn.vano.vicall.common.ext.ctx
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

object FileUtil {

    @Throws(IOException::class)
    fun createTempImageFile(ctx: Context): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File? = ctx.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        )
    }

    @Throws(IOException::class)
    fun createTempVideoFile(ctx: Context): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File? = ctx.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
        return File.createTempFile(
            "VID_${timeStamp}_", /* prefix */
            ".mp4", /* suffix */
            storageDir /* directory */
        )
    }

    fun copyStreamToFile(inputStream: InputStream, outputFile: File) {
        inputStream.use { input ->
            val outputStream = FileOutputStream(outputFile)
            outputStream.use { output ->
                val buffer = ByteArray(4 * 1024) // buffer size
                while (true) {
                    val byteCount = input.read(buffer)
                    if (byteCount < 0) break
                    output.write(buffer, 0, byteCount)
                }
                output.flush()
            }
        }
    }

    fun recordVideo(
        context: Any,
        requestCodePermission: Int,
        requestCodeActivityResult: Int
    ): File? {
        val fragment = when (context) {
            is Fragment -> context
            else -> null
        }
        val activity = when (context) {
            is AppCompatActivity -> context
            else -> null
        }

        val ctx = fragment?.ctx ?: activity?.ctx

        var fileResult: File? = null
        ctx?.run {
            if (PermissionUtil.isGranted(
                    fragment ?: activity,
                    arrayOf(Manifest.permission.CAMERA),
                    requestCodePermission
                )
            ) {
                Intent(MediaStore.ACTION_VIDEO_CAPTURE).also { videoIntent ->
                    // Ensure that there's a camera activity to handle the intent
                    videoIntent.resolveActivity(ctx.packageManager)?.also {
                        // Create the File where the photo should go
                        fileResult = try {
                            createTempVideoFile(ctx)
                        } catch (e: IOException) {
                            // Error occurred while creating the File
                            null
                        }
                        // Continue only if the File was successfully created
                        fileResult?.also { file ->
                            val videoUri = FileProvider.getUriForFile(
                                ctx,
                                "vn.vano.vicall.fileprovider" /* This must be equality #android:authorities in Manifest#provider */,
                                file
                            )
                            videoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri)
                            fragment?.startActivityForResult(
                                videoIntent,
                                requestCodeActivityResult
                            ) ?: activity?.startActivityForResult(
                                videoIntent,
                                requestCodeActivityResult
                            )
                        }
                    }
                }
            }
        }

        return fileResult
    }

    /*
    * Return file size as kB
     */
    fun getFileSize(ctx: Context, fileUri: Uri): Float {
        val fd = ctx.contentResolver.openFileDescriptor(fileUri, "r")
        return fd?.use {
            it.statSize / 1000f / 1000f
        } ?: 0f
    }
}