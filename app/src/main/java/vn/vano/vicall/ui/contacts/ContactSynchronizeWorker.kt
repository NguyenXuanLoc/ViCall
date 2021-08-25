package vn.vano.vicall.ui.contacts

import android.Manifest
import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import io.reactivex.SingleObserver
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import vn.vano.vicall.common.ext.currentUser
import vn.vano.vicall.common.util.CommonUtil
import vn.vano.vicall.common.util.PermissionUtil
import vn.vano.vicall.data.interactor.ContactInteractor
import vn.vano.vicall.data.mapper.equalTo
import vn.vano.vicall.data.model.ContactModel

class ContactSynchronizeWorker(private val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    private val contactInteractor by lazy { ContactInteractor() }
    private var disposable: Disposable? = null

    override fun doWork(): Result {
        val phoneNumber = context.currentUser?.phoneNumberNonPrefix
        phoneNumber?.run {
            if (PermissionUtil.isGranted(
                    context,
                    arrayOf(Manifest.permission.READ_CONTACTS),
                    0,
                    false
                )
            ) {
                contactInteractor.getLocalContacts(context) {
                    if (it.isNotEmpty()) {
                        if (!CommonUtil.getUserContacts(context).equalTo(it)) {
                            contactInteractor.syncContacts(phoneNumber, it)
                                .subscribeOn(Schedulers.io())
                                .subscribe(object : SingleObserver<List<ContactModel>> {
                                    override fun onSubscribe(d: Disposable) {
                                        disposable = d
                                    }

                                    override fun onSuccess(contacts: List<ContactModel>) {
                                        // Save user contacts into shared pref
                                        CommonUtil.saveUserContacts(context, contacts)

                                        // Mark all contacts are synced
                                        CommonUtil.markContactsAreSynced(context)

                                        // Dispose the disposable
                                        if (disposable?.isDisposed == false) {
                                            disposable?.dispose()
                                        }
                                    }

                                    override fun onError(e: Throwable) {
                                        e.printStackTrace()
                                    }
                                })
                        }
                    }
                }
            }
        }

        return Result.success()
    }
}