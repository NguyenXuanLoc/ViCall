package vn.vano.vicall.common.util

import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

object PermissionUtil {

    fun isApi23(): Boolean {
        return Build.VERSION.SDK_INT == Build.VERSION_CODES.M
    }

    fun isApi23orHigher(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    }

    fun isApi24orHigher(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
    }

    fun isApi26orHigher(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    }

    fun isApi27orHigher(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1
    }

    fun isApi28orHigher(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
    }

    fun isApi29orHigher(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    }

    fun isGranted(
        context: Any?,
        permissions: Array<String>,
        reqCode: Int,
        requestPermission: Boolean = true
    ): Boolean {
        var isGranted = true

        if (isApi23orHigher()) {
            for (permission in permissions) {
                when (context) {
                    is AppCompatActivity -> isGranted = ContextCompat.checkSelfPermission(
                        context,
                        permission
                    ) == PackageManager.PERMISSION_GRANTED
                    is Fragment -> isGranted = ContextCompat.checkSelfPermission(
                        context.requireContext(),
                        permission
                    ) == PackageManager.PERMISSION_GRANTED
                }
                if (!isGranted) {
                    break
                }
            }

            // Asking permissions
            if (requestPermission && !isGranted) {
                when (context) {
                    is AppCompatActivity -> ActivityCompat.requestPermissions(
                        context,
                        permissions,
                        reqCode
                    )
                    is Fragment -> context.requestPermissions(permissions, reqCode);
                }
            }
        }

        return isGranted
    }
}