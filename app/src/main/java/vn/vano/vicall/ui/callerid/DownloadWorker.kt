package vn.vano.vicall.ui.callerid

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import io.reactivex.SingleObserver
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import timber.log.Timber
import vn.vano.vicall.common.Key
import vn.vano.vicall.common.util.CommonUtil
import vn.vano.vicall.data.interactor.VideoInteractor
import java.io.*

class DownloadWorker(val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    private val videoInteractor by lazy { VideoInteractor() }
    private var disposable: Disposable? = null

    override fun doWork(): Result {
        // Get the input
        val videoUrl = inputData.getString(Key.URL)
        val filePath = inputData.getString(Key.FILE_PATH)
        val callerNumber = inputData.getString(Key.PHONE_NUMBER)
        videoUrl?.also { url ->
            filePath?.also { path ->
                val file = File(path)
                videoInteractor.downloadVideo(url)
                    .subscribeOn(Schedulers.io())
                    .subscribe(object : SingleObserver<ResponseBody> {
                        override fun onSuccess(response: ResponseBody) {
                            writeFileToDisk(response, file, callerNumber)

                            if (disposable?.isDisposed == false) {
                                disposable?.dispose()
                            }
                        }

                        override fun onSubscribe(d: Disposable) {
                            disposable = d
                        }

                        override fun onError(e: Throwable) {
                            e.printStackTrace()
                        }
                    })
            }
        }

        return Result.success()
    }

    private fun writeFileToDisk(response: ResponseBody, fileOutput: File, callerNumber: String?) {
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null

        try {
            val fileReader = ByteArray(4096)
            val fileSize = response.contentLength()
            var fileSizeDownloaded: Long = 0
            inputStream = response.byteStream()
            outputStream = FileOutputStream(fileOutput)

            while (true) {
                val read = inputStream?.read(fileReader)
                // It's the end of file if read == -1
                if (read == -1) {
                    break
                }

                read?.also { _ ->
                    outputStream.write(fileReader, 0, read)
                    fileSizeDownloaded += read.toLong()
                }
                if (fileSizeDownloaded == fileSize) {
                    Timber.e("Downloaded success: ${fileSize / 1024.0 / 1024.0} MB")
                    // Update local video path
                    callerNumber?.also { _ ->
                        CommonUtil.getCallerByPhone(context, callerNumber)?.apply {
                            urlVideoLocal = fileOutput.path
                        }?.run {
                            CommonUtil.saveCallerByPhone(context, this)
                        }
                    }
                }
            }

            outputStream.flush()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            inputStream?.close()
            outputStream?.close()
        }
    }
}