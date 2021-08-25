package vn.vano.vicall.common.util

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

object RxSearchObservable {

    fun fromView(searchView: TextView): Observable<String> {
        val subject = PublishSubject.create<String>()

        searchView.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                subject.onNext(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }
        })

        return subject.debounce(300, TimeUnit.MILLISECONDS)
            .filter {
                return@filter true
            }
            .distinctUntilChanged()
            .switchMap {
                return@switchMap Observable.just(it)
            }
    }
}