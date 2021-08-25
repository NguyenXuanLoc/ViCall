package vn.vano.vicall.ui.video.list

import android.content.Context
import vn.vano.vicall.common.ext.addToCompositeDisposable
import vn.vano.vicall.common.ext.applyIOWithAndroidMainThread
import vn.vano.vicall.common.util.RxBus
import vn.vano.vicall.data.interactor.VideoInteractor
import vn.vano.vicall.ui.base.BasePresenterImp

class VideoListPresenterImp(ctx: Context) : BasePresenterImp<VideoListView>(ctx) {

    private val interactor by lazy { VideoInteractor() }

    fun getVideos(
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
                        { videos ->
                            v.onVideosLoadedSuccess(videos)
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
                v.dismissRefreshIcon()
                v.onNetworkError()
            }
        }
    }

    fun listenLayoutChanged() {
        view?.also { v ->
            RxBus.listenVideoLayoutSwitcherEventModel()
                .applyIOWithAndroidMainThread()
                .subscribe(
                    {
                        v.onLayoutChanged()
                    },
                    {
                    }
                ).addToCompositeDisposable(compositeDisposable)
        }
    }
}