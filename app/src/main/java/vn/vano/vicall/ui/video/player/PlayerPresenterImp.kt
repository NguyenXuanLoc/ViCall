package vn.vano.vicall.ui.video.player

import android.content.Context
import vn.vano.vicall.common.ext.addToCompositeDisposable
import vn.vano.vicall.common.ext.applyIOWithAndroidMainThread
import vn.vano.vicall.data.interactor.VideoInteractor
import vn.vano.vicall.data.model.VideoModel
import vn.vano.vicall.ui.base.BasePresenterImp

class PlayerPresenterImp(ctx: Context) : BasePresenterImp<PlayerView>(ctx) {

    private val videoInteractor by lazy { VideoInteractor() }

    fun applyVideo(phoneNumber: String, fileId: String, fromViStore: Boolean) {
        view?.also { v ->
            if (networkIsAvailable()) {
                videoInteractor.applyVideo(phoneNumber, fileId, fromViStore)
                    .applyIOWithAndroidMainThread()
                    .subscribe(
                        {
                            if (it.responseIsSuccess()) {
                                v.onVideoAppliedSuccess()
                            } else {
                                v.onApiResponseError(it)
                            }
                        },
                        {
                            v.onApiCallError()
                        }
                    ).addToCompositeDisposable(compositeDisposable)
            } else {
                v.onNetworkError()
            }
        }
    }

    fun deleteVideo(phoneNumber: String, videoModel: VideoModel) {
        view?.also { v ->
            if (networkIsAvailable()) {
                videoModel.fileId?.run {
                    showProgressDialog()

                    videoInteractor.deleteVideo(phoneNumber, videoModel.fileId)
                        .applyIOWithAndroidMainThread()
                        .subscribe(
                            {
                                if (it.responseIsSuccess()) {
                                    v.onVideoDeletedSuccess(videoModel)
                                } else {
                                    v.onApiResponseError(it)
                                }

                                dismissProgressDialog()
                            },
                            {
                                v.onApiCallError()
                                dismissProgressDialog()
                            }
                        ).addToCompositeDisposable(compositeDisposable)
                }
            } else {
                v.onNetworkError()
            }
        }
    }
}