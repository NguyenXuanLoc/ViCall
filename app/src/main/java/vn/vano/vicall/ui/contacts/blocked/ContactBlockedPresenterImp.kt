package vn.vano.vicall.ui.contacts.blocked

import android.content.Context
import vn.vano.vicall.common.ext.removePhonePrefix
import vn.vano.vicall.common.ext.removeSpaces
import vn.vano.vicall.common.ext.unblockNumber
import vn.vano.vicall.common.util.CommonUtil
import vn.vano.vicall.common.util.PermissionUtil
import vn.vano.vicall.data.interactor.ContactInteractor
import vn.vano.vicall.data.model.ContactModel
import vn.vano.vicall.ui.base.BasePresenterImp

class ContactBlockedPresenterImp(private val ctx: Context) :
    BasePresenterImp<ContactBlockedView>(ctx) {

    private val interactor by lazy { ContactInteractor() }

    fun getBlockedList() {
        view?.also { v ->
            interactor.getBlockedNumbers(ctx) { blockedList ->
                if (blockedList.isNotEmpty()) {
                    val userContacts = CommonUtil.getUserContacts(ctx)
                    for (contact in blockedList) {
                        userContacts.find { c ->
                            contact.number.removeSpaces()
                                .removePhonePrefix() == c.number.removeSpaces().removePhonePrefix()
                        }?.run {
                            contact.name = name
                        }
                    }
                }

                v.onContactsBlockedLoaded(blockedList)
                v.dismissRefreshIcon()
            }
        }
    }

    fun unblockContact(contact: ContactModel) {
        view?.also { v ->
            if (PermissionUtil.isApi24orHigher()) {
                if (ctx.unblockNumber(contact.number)) {
                    v.onUnblockedSuccess(contact)
                }
            }
        }
    }
}