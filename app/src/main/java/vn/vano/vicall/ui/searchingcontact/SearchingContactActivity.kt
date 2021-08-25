package vn.vano.vicall.ui.searchingcontact

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_search_contact.*
import org.jetbrains.anko.bundleOf
import org.jetbrains.anko.ctx
import vn.vano.vicall.R
import vn.vano.vicall.common.Constant
import vn.vano.vicall.common.Key
import vn.vano.vicall.common.ext.*
import vn.vano.vicall.common.util.CommonUtil
import vn.vano.vicall.common.util.PermissionUtil
import vn.vano.vicall.data.model.ContactModel
import vn.vano.vicall.ui.base.BaseActivity
import vn.vano.vicall.ui.contacts.allcontact.ContactAllAdapter
import vn.vano.vicall.ui.contacts.detail.ContactDetailActivity

class SearchingContactActivity : BaseActivity<SearchingContactView, SearchingContactPresenterImp>(),
    SearchingContactView {

    companion object {
        const val ACTION_GIVE_FRIEND = "action_give_friend"

        private const val RC_PERMISSION_READ_CONTACT = 1
        private const val RC_PERMISSION_CALL_PHONE = 2

        fun start(act: AppCompatActivity, action: String? = null, videoFileId: String? = null) {
            bundleOf(Key.ACTION to action, Key.ID to videoFileId).apply {
                act.openActivity(SearchingContactActivity::class.java, this)
            }
        }
    }

    private var action: String? = null
    private var videoFileId: String? = null
    private var clickedContactModel: ContactModel? = null
    private val contacts by lazy { ArrayList<ContactModel>() }
    private val contactsAdapter by lazy {
        ContactAllAdapter(contacts,
            showInfoListener = {
                ContactDetailActivity.start(ctx, it)
            },
            sendMessageListener = {
                // Open SMS intent
                CommonUtil.composeSms(self, it.number)
            },
            itemClickListener = {
                if (action == ACTION_GIVE_FRIEND) {
                    videoFileId?.also { fileId ->
                        // Open SMS composer
                        CommonUtil.composeSms(
                            self,
                            Constant.CALL_CENTER_1556,
                            "${Constant.GIVE} ${it.number.removeSpaces()} $fileId"
                        )

                        // Finish this activity
                        finish()
                    }
                } else {
                    call(it)
                }
            })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == RC_PERMISSION_READ_CONTACT) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkContactPermission()
            } else {
                finish()
            }
        } else if (requestCode == RC_PERMISSION_CALL_PHONE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            call(clickedContactModel)
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun getExtrasValue() {
        intent?.extras?.run {
            if (containsKey(Key.ACTION)) {
                action = getString(Key.ACTION)
            }
            if (containsKey(Key.ID)) {
                videoFileId = getString(Key.ID)
            }
        }
    }

    override fun initView(): SearchingContactView {
        return this
    }

    override fun initPresenter(): SearchingContactPresenterImp {
        return SearchingContactPresenterImp(this)
    }

    override fun getLayoutId(): Int? {
        return R.layout.activity_search_contact
    }

    override fun initWidgets() {
        // Request READ_CONTACT permission if it's not granted
        checkContactPermission()

        // Init toolbar
        hideToolbarBase()
        applyToolbar(toolbarSearchContact)
        enableHomeAsUp(toolbarSearchContact) {
            finish()
        }

        // Init recyclerview
        rclContacts.layoutManager = LinearLayoutManager(ctx, LinearLayoutManager.VERTICAL, false)
        rclContacts.setHasFixedSize(true)
        rclContacts.isNestedScrollingEnabled = false
        rclContacts.addItemDecoration(DividerItemDecoration(ctx, DividerItemDecoration.VERTICAL))
        rclContacts.adapter = contactsAdapter

        // Listeners
        presenter.registerSearchTypingListener(txtSearch)

        imgClear.setOnSafeClickListener {
            txtSearch.setText("")
        }
    }

    override fun onTypingSearch(query: String) {
        // Only searching if query is not blank
        if (query.isNotBlank()) {
            presenter.searchContacts(query)
            imgClear.visible()
        } else {
            contacts.clear()
            contactsAdapter.notifyDataSetChanged()
            imgClear.gone()
        }
    }

    override fun onContactsLoadedSuccess(contacts: List<ContactModel>) {
        this.contacts.clear()
        this.contacts.addAll(contacts)

        if (this.contacts.isEmpty()) {
            this.contacts.add(ContactModel().apply {
                number = txtSearch.text.toString().trim()
            })
        }

        contactsAdapter.notifyDataSetChanged()
    }

    private fun call(contact: ContactModel?) {
        if (PermissionUtil.isGranted(
                self, arrayOf(Manifest.permission.CALL_PHONE), RC_PERMISSION_CALL_PHONE
            )
        ) {
            clickedContactModel = contact
            CommonUtil.call(self, clickedContactModel?.number)
        }
    }

    private fun checkContactPermission() {
        if (PermissionUtil.isGranted(
                self,
                arrayOf(Manifest.permission.READ_CONTACTS),
                RC_PERMISSION_READ_CONTACT
            )
        ) {
            txtSearch.requestFocus()
        }
    }
}