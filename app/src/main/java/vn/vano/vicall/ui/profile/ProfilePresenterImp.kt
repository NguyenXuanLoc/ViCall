package vn.vano.vicall.ui.profile

import android.content.Context
import android.net.Uri
import vn.vano.vicall.common.ext.addToCompositeDisposable
import vn.vano.vicall.common.ext.applyIOWithAndroidMainThread
import vn.vano.vicall.common.ext.uploadVideo
import vn.vano.vicall.common.util.CommonUtil
import vn.vano.vicall.common.util.RxBus
import vn.vano.vicall.data.interactor.UserInteractor
import vn.vano.vicall.data.interactor.VideoInteractor
import vn.vano.vicall.ui.base.BasePresenterImp

class ProfilePresenterImp(private val ctx: Context) : BasePresenterImp<ProfileView>(ctx) {
    val interactor by lazy { VideoInteractor() }
    val userInteractor by lazy { UserInteractor() }

    fun getVideoMost(
        type: String,
        category: String?,
        phone: String?,
        page: Int,
        showProgress: Boolean
    ) {
        view?.also { v ->
            if (networkIsAvailable()) {
                if (showProgress) {
                    showProgressDialog()
                }

                interactor.getVideoList(type, category, phone, page)
                    .applyIOWithAndroidMainThread()
                    .subscribe(
                        {
                            v.onVideosMostLoadedSuccess(it)
                            dismissProgressDialog()
                            v.dismissRefreshIcon()
                        },
                        {
                            v.onApiCallError()
                            dismissProgressDialog()
                            v.dismissRefreshIcon()
                        }
                    ).addToCompositeDisposable(compositeDisposable)
            } else {
                v.onNetworkError()
            }
        }
    }

    fun getVideoPersonal(
        type: String,
        phone: String,
        showProgress: Boolean
    ) {
        view?.also { v ->
            if (networkIsAvailable()) {
                if (showProgress) {
                    showProgressDialog()
                }
                interactor.getVideoList(type, phone = phone)
                    .applyIOWithAndroidMainThread()
                    .subscribe(
                        {
                            v.onVideosPersonalLoadedSuccess(it)
                            dismissProgressDialog()
                            v.dismissRefreshIcon()
                        },
                        {
                            v.onApiCallError()
                            dismissProgressDialog()
                            v.dismissRefreshIcon()
                        }
                    ).addToCompositeDisposable(compositeDisposable)
            } else {
                v.onNetworkError()
            }
        }
    }

    fun updateStatus(type: String, phoneNumber: String, status: String, ctx: Context) {
        view?.also { v ->
            if (networkIsAvailable()) {
                showProgressDialog()
                userInteractor.updateUserSettings(type, phoneNumber, status = status)
                    .applyIOWithAndroidMainThread()
                    .subscribe({
                        if (it.responseIsSuccess()) {
                            v.updateStatusSuccess(it)
                            CommonUtil.saveLoggedInUserToList(ctx, it)
                        } else {
                            v.onApiResponseError(it)
                        }
                        dismissProgressDialog()
                    }, {
                        v.onApiCallError()
                        dismissProgressDialog()
                    }).addToCompositeDisposable(compositeDisposable)
            } else {
                dismissProgressDialog()
                v.onNetworkError()
            }
        }
    }

    fun uploadVideo(phoneNumber: String, fileUri: Uri) {
        view?.also { v ->
            if (networkIsAvailable()) {
                if (v.fileSizeIsValid(fileUri)) {
                    // Init work manager
                    ctx.uploadVideo(phoneNumber, fileUri)
                } else {
                    v.onFileSizeTooLargeError(fileUri)
                    v.deleteTempVideoFile()
                }
            } else {
                v.onNetworkError()
            }
        }
    }

    fun listenUserInfoChanged() {
        view?.also { v ->
            RxBus.listenUserModel()
                .applyIOWithAndroidMainThread()
                .subscribe(
                    {
                        v.onUserInfoChanged(it)
                    },
                    {
                    }
                ).addToCompositeDisposable(compositeDisposable)
        }
    }
}