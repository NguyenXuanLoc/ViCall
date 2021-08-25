package vn.vano.vicall.ui.login.registration

import android.content.Context
import android.net.Uri
import vn.vano.vicall.common.ext.addToCompositeDisposable
import vn.vano.vicall.common.ext.applyIOWithAndroidMainThread
import vn.vano.vicall.common.ext.createImageFileFromUri
import vn.vano.vicall.common.util.CommonUtil
import vn.vano.vicall.data.interactor.UserInteractor
import vn.vano.vicall.ui.base.BasePresenterImp
import java.io.File

class RegistrationPresenterImp(private val ctx: Context) : BasePresenterImp<RegistrationView>(ctx) {

    private val userInteractor by lazy { UserInteractor() }

    fun registerAccount(phoneNumber: String, name: String, avtFileUri: Uri?) {
        view?.also { v ->
            if (networkIsAvailable()) {
                if (v.nameIsValid()) {
                    showProgressDialog()

                    val avtFile = ctx.createImageFileFromUri(avtFileUri)
                    userInteractor.registerAccount(phoneNumber, name, avtFile)
                        .applyIOWithAndroidMainThread()
                        .subscribe(
                            { user ->
                                // Delete temp file
                                deleteTempFile(avtFile)

                                if (user.responseIsSuccess()) {
                                    // Save logged in user into shared preference
                                    CommonUtil.saveLoggedInUserToList(ctx, user)

                                    // Notify to view
                                    v.onRegistrationSuccess(user)
                                } else {
                                    v.onApiResponseError(user)
                                }

                                dismissProgressDialog()
                            },
                            {
                                // Delete temp file
                                deleteTempFile(avtFile)

                                dismissProgressDialog()
                                v.onApiCallError()
                            }
                        ).addToCompositeDisposable(compositeDisposable)
                } else {
                    v.onNameError()
                }
            } else {
                v.onNetworkError()
            }
        }
    }

    private fun deleteTempFile(file: File?) {
        file?.delete()
    }

    fun updateProfile(
        phoneNumber: String,
        name: String,
        sex: String?,
        birthday: Long?,
        avtFileUri: Uri?
    ) {
        view?.also { v ->
            if (networkIsAvailable()) {
                if (v.nameIsValid()) {
                    showProgressDialog()

                    val avtFile = ctx.createImageFileFromUri(avtFileUri)
                    userInteractor.updateUserProfile(
                        phoneNumber,
                        name = name,
                        gender = sex,
                        birthday = birthday,
                        avtFile = avtFile
                    )
                        .applyIOWithAndroidMainThread()
                        .subscribe(
                            { user ->
                                // Delete temp file
                                deleteTempFile(avtFile)

                                if (user.responseIsSuccess()) {
                                    // Save logged in user into shared preference
                                    CommonUtil.saveLoggedInUserToList(ctx, user)

                                    // Notify to view
                                    v.onUpdateProfileSuccess(user)
                                } else {
                                    v.onApiResponseError(user)
                                }

                                dismissProgressDialog()
                            },
                            {
                                // Delete temp file
                                deleteTempFile(avtFile)

                                dismissProgressDialog()
                                v.onApiCallError()
                            }
                        ).addToCompositeDisposable(compositeDisposable)
                } else {
                    v.onNameError()
                }
            } else {
                v.onNetworkError()
            }
        }
    }
}