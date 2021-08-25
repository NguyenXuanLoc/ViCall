package vn.vano.vicall.ui.dialer

import android.Manifest
import android.content.pm.PackageManager
import android.text.method.ArrowKeyMovementMethod
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_dialer.*
import org.jetbrains.anko.ctx
import vn.vano.vicall.R
import vn.vano.vicall.common.Constant
import vn.vano.vicall.common.ext.*
import vn.vano.vicall.common.util.CommonUtil
import vn.vano.vicall.common.util.EventBusUtil
import vn.vano.vicall.common.util.PermissionUtil
import vn.vano.vicall.data.model.ContactModel
import vn.vano.vicall.ui.base.BaseActivity
import vn.vano.vicall.ui.contacts.allcontact.ContactAllAdapter
import vn.vano.vicall.ui.contacts.detail.ContactDetailActivity

class DialerActivity : BaseActivity<DialerView, DialerPresenterImp>(), DialerView {

    companion object {
        private const val RC_PERMISSION_READ_CONTACT = 1
        private const val RC_PERMISSION_CALL_PHONE = 2
        private const val RC_PERMISSION_CALL_LOG = 3
    }

    //check if = true -> send event bus = dismiss to ram to CallActivity get data to dismiss or show popup
    private var isAsterisk = false

    private var clickedContactNumber: String? = null
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
                call(it.number)
            })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == RC_PERMISSION_READ_CONTACT && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            checkContactPermission()
        } else if (requestCode == RC_PERMISSION_CALL_PHONE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            call(clickedContactNumber)
        } else if (requestCode == RC_PERMISSION_CALL_LOG) {
            var granted = grantResults.isNotEmpty()
            for (p in grantResults) {
                if (p != PackageManager.PERMISSION_GRANTED) {
                    granted = false
                    break
                }
            }

            if (granted) {
                getLastCall()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.nothing, R.anim.slide_out_bottom)
    }

    override fun initView(): DialerView {
        return this
    }

    override fun initPresenter(): DialerPresenterImp {
        return DialerPresenterImp(ctx)
    }

    override fun getLayoutId(): Int? {
        return R.layout.activity_dialer
    }

    override fun initWidgets() {
        // Request READ_CONTACT permission if it's not granted
        checkContactPermission()

        // Hide toolbar base
        hideToolbarBase()

        // Init recyclerview
        rclContacts.layoutManager = LinearLayoutManager(ctx, LinearLayoutManager.VERTICAL, false)
        rclContacts.setHasFixedSize(true)
        rclContacts.addItemDecoration(DividerItemDecoration(ctx, DividerItemDecoration.VERTICAL))
        rclContacts.adapter = contactsAdapter

        // Init dial number label
        txtDialNumber.movementMethod = ArrowKeyMovementMethod.getInstance()
        txtDialNumber.setHorizontallyScrolling(true)

        // Listeners
        presenter.listenDialNumberChanged(txtDialNumber)

        imgAddContact.setOnSafeClickListener {
            if (txtDialNumber.text.isNotBlank()) {
                presenter.openContactAddingDefaultApp(txtDialNumber.text.toString())
            }
        }

        imgClear.setOnClickListener {
            fillDialNumber(null)
        }

        imgClear.setOnLongClickListener {
            isAsterisk = false
            fillDialNumber(null, true)
            return@setOnLongClickListener true
        }

        btnOne.setOnClickListener {
            fillDialNumber(btnOne.text.toString())
        }

        btnTwo.setOnClickListener {
            fillDialNumber(btnTwo.text.toString())
        }

        btnThree.setOnClickListener {
            fillDialNumber(btnThree.text.toString())
        }

        btnFour.setOnClickListener {
            fillDialNumber(btnFour.text.toString())
        }

        btnFive.setOnClickListener {
            fillDialNumber(btnFive.text.toString())
        }

        btnSix.setOnClickListener {
            fillDialNumber(btnSix.text.toString())
        }

        btnSeven.setOnClickListener {
            fillDialNumber(btnSeven.text.toString())
        }

        btnEight.setOnClickListener {
            fillDialNumber(btnEight.text.toString())
        }

        btnNine.setOnClickListener {
            fillDialNumber(btnNine.text.toString())
        }

        btnAsterisk.setOnClickListener {
            isAsterisk = true
            fillDialNumber(btnAsterisk.text.toString())
        }

        btnZero.setOnClickListener {
            fillDialNumber(btnZero.text.toString())
        }

        btnShark.setOnClickListener {
            fillDialNumber(btnShark.text.toString())
        }

        btnCall.setOnSafeClickListener {
            getLastCall()
        }

        btnCollapse.setOnSafeClickListener {
            if (layoutContacts.isVisible) {
                hideKeyboard()
            } else {
                onBackPressed()
            }
        }

        lblGrant.setOnSafeClickListener {
            checkContactPermission()
        }

        // Hide keyboard when user starts scrolling the contact list
        rclContacts.setOnTouchListener { _, _ ->
            hideKeyboard()
            return@setOnTouchListener false
        }

        fabKeyboard.setOnSafeClickListener {
            showKeyboard()
        }
    }

    override fun onContactsLoadedSuccess(contacts: List<ContactModel>) {
        this.contacts.clear()
        this.contacts.addAll(contacts)
        contactsAdapter.notifyDataSetChanged()

        if (this.contacts.isNotEmpty()) {
            layoutContacts.visible()
        } else {
            layoutContacts.gone()
        }

        // Show/hide contact adding icon
        contacts.find {
            it.number.removeSpaces().removePhonePrefix() ==
                    txtDialNumber.text.toString().removeSpaces().removePhonePrefix()
        }?.run {
            imgAddContact.invisible()
        } ?: run {
            imgAddContact.visible()
        }
    }

    override fun onDialNumberChanged(number: String) {
        // Only searching if query is not blank
        if (contactPermissionIsGranted(false)) {
            if (number.isNotBlank()) {
                presenter.searchContacts(number)
            } else {
                layoutContacts.gone()
            }
        }
    }

    override fun onGetLastCallSuccess(call: ContactModel) {
        fillDialNumber(call.number)
    }

    private fun fillDialNumber(text: String?, reset: Boolean = false) {
        if (reset) {
            txtDialNumber.setText("")
        } else {
            val str = txtDialNumber.text
            txtDialNumber.setText(text?.run {
                "$str$text"
            } ?: str.substring(0, str.length - 1))

            if (txtDialNumber.text.isEmpty()) isAsterisk = false
        }

        if (txtDialNumber.text.isNotBlank()) {
            txtDialNumber.visible()
            txtDialNumber.setSelection(txtDialNumber.length())
            imgClear.visible()
        } else {
            txtDialNumber.gone()
            imgClear.gone()
            imgAddContact.invisible()
        }
    }

    private fun call(contactNumber: String?) {
        clickedContactNumber = contactNumber

        if (PermissionUtil.isGranted(
                self, arrayOf(Manifest.permission.CALL_PHONE),
                RC_PERMISSION_CALL_PHONE
            )
        ) {
            if (isAsterisk) {
                EventBusUtil.senDataSticky(Constant.DISMISS)
            }
            CommonUtil.call(self, contactNumber)
        }
    }

    private fun getLastCall() {
        if (txtDialNumber.text.isNotBlank()) {
            call(txtDialNumber.text.toString())
        } else {
            if (PermissionUtil.isGranted(
                    self,
                    arrayOf(Manifest.permission.READ_CALL_LOG),
                    RC_PERMISSION_CALL_LOG
                )
            ) {
                presenter.getLastCall()
            }
        }
    }

    private fun checkContactPermission() {
        if (contactPermissionIsGranted(true)) {
            llPermission.gone()
        }
    }

    private fun contactPermissionIsGranted(requestPermission: Boolean): Boolean {
        return PermissionUtil.isGranted(
            self,
            arrayOf(Manifest.permission.READ_CONTACTS),
            RC_PERMISSION_READ_CONTACT,
            requestPermission
        )
    }

    private fun hideKeyboard() {
        layoutKeyboard.setAnimation(View.GONE, R.anim.slide_out_bottom)
    }

    private fun showKeyboard() {
        layoutKeyboard.setAnimation(View.VISIBLE, R.anim.slide_in_bottom)
    }
}
