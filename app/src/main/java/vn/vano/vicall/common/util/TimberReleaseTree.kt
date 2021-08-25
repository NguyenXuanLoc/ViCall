package vn.vano.vicall.common.util

import timber.log.Timber

class TimberReleaseTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        // Do nothing in release version
    }
}