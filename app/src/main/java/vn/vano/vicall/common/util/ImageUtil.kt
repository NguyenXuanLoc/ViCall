package vn.vano.vicall.common.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException

object ImageUtil {

    // Decode bitmap
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        // Raw height and width of image
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight = height / 2
            val halfWidth = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize > reqHeight && halfWidth / inSampleSize > reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    fun getFilePathFromUri(context: Context, uri: Uri): String? {
        var cursor: Cursor? = null
        try {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            cursor = context.contentResolver.query(uri, proj, null, null, null)
            cursor?.run {
                val column_index = getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                moveToFirst()
                return getString(column_index)
            }
        } catch (ex: NullPointerException) {
            ex.printStackTrace()
        } finally {
            cursor?.close()
        }

        return ""
    }

    fun decodeBitmapFromFile(imageFile: String, reqWidth: Int, reqHeight: Int): Bitmap {
        // First decode with inJustDecodeBounds=true to check dimensions
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(imageFile, options)

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeFile(imageFile, options)
    }

    /*
    * This method is requesting too much memory
    fun convertBitmapToFile(filePath: String, bitmap: Bitmap): File {
        val file = File(filePath)
        val bos = BufferedOutputStream(FileOutputStream(file))
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bos)
        bos.flush()
        bos.close()

        return file
    }*/

    fun takeAPhoto(
        activity: AppCompatActivity,
        requestCodePermission: Int,
        requestCodeActivityResult: Int
    ): File? {
        var fileResult: File? = null
        if (PermissionUtil.isGranted(
                activity,
                arrayOf(Manifest.permission.CAMERA),
                requestCodePermission
            )
        ) {
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                // Ensure that there's a camera activity to handle the intent
                takePictureIntent.resolveActivity(activity.packageManager)?.also {
                    // Create the File where the photo should go
                    fileResult = try {
                        FileUtil.createTempImageFile(activity)
                    } catch (e: IOException) {
                        // Error occurred while creating the File
                        null
                    }
                    // Continue only if the File was successfully created
                    fileResult?.also { file ->
                        val photoURI = FileProvider.getUriForFile(
                            activity,
                            "vn.vano.vicall.fileprovider" /* This must be equality #android:authorities in Manifest#provider */,
                            file
                        )
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                        activity.startActivityForResult(
                            takePictureIntent,
                            requestCodeActivityResult
                        )
                    }
                }
            }
        }

        return fileResult
    }

    fun pickImage(
        activity: AppCompatActivity,
        requestCodePermission: Int,
        requestCodeActivityResult: Int
    ) {
        if (PermissionUtil.isGranted(
                activity,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                requestCodePermission
            )
        ) {
            val mimeTypes = arrayOf("image/jpeg", "image/png")
            Intent(Intent.ACTION_PICK).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            }.run {
                activity.startActivityForResult(this, requestCodeActivityResult)
            }
        }
    }
}