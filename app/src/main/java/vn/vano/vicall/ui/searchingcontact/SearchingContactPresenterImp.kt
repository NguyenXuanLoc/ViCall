package vn.vano.vicall.ui.searchingcontact

import android.content.Context
import android.widget.EditText
import vn.vano.vicall.common.ext.addToCompositeDisposable
import vn.vano.vicall.common.ext.applyIOWithAndroidMainThread
import vn.vano.vicall.common.util.RxSearchObservable
import vn.vano.vicall.data.interactor.ContactInteractor
import vn.vano.vicall.ui.base.BasePresenterImp

class SearchingContactPresenterImp(private val ctx: Context) :
    BasePresenterImp<SearchingContactView>(ctx) {

    private val interactor by lazy { ContactInteractor() }

    fun searchContacts(query: String) {
        view?.also { v ->
            interactor.getLocalContacts(ctx, query, 20) {
                v.onContactsLoadedSuccess(it)
            }
        }
    }

    fun registerSearchTypingListener(txtPhone: EditText) {
        view?.let { v ->
            RxSearchObservable.fromView(txtPhone)
                .applyIOWithAndroidMainThread()
                .subscribe {
                    v.onTypingSearch(it)
                }.addToCompositeDisposable(compositeDisposable)
        }
    }
}