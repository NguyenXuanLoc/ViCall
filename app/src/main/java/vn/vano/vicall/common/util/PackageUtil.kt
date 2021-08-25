package vn.vano.vicall.common.util

import vn.vano.vicall.BuildConfig
import java.util.*

object PackageUtil {

    fun getPackageName(): String {
        return BuildConfig.APPLICATION_ID
    }

    fun getUUID(): String {
        return UUID.randomUUID().toString()
    }
}