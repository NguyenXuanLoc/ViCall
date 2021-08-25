package vn.vano.vicall.ui.contacts.insertcontact

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_insert_contact.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.toast
import vn.vano.vicall.R
import vn.vano.vicall.common.ext.gone
import vn.vano.vicall.common.ext.setImage
import vn.vano.vicall.common.ext.setOnSafeClickListener
import vn.vano.vicall.common.ext.visible
import vn.vano.vicall.common.util.ImageUtil
import vn.vano.vicall.common.util.PermissionUtil
import vn.vano.vicall.ui.base.BaseActivity
import vn.vano.vicall.widget.LayoutImageChooser
import java.io.File


class InsertContactActivity : BaseActivity<InsertContactView, InsertContactPresenterImp>(),
    InsertContactView, LayoutImageChooser.ImageChooserListener {

    private var avtFile: File? = null
    private var newPhoto: Bitmap? = null

    companion object {
        private const val RC_PERMISSION_READ_EXTERNAL_STORAGE = 1
        private const val RC_PERMISSION_CAMERA = 2
        private const val RC_PERMISSION_WRITE_CONTACT = 3
        private const val RC_PICK_AVATAR = 256
        private const val RC_TAKE_A_PHOTO = 257
    }

    override fun initView(): InsertContactView {
        return this
    }

    override fun initPresenter(): InsertContactPresenterImp {
        return InsertContactPresenterImp(this)
    }

    override fun getLayoutId(): Int? {
        return R.layout.activity_insert_contact
    }

    override fun initWidgets() {
        showTitle(getString(R.string.insert_contact))
        enableHomeAsUp { finish() }

        //Listener
        imgChooser.setImageChooserListener(this)
        sdvAvatar.setOnSafeClickListener {
            imgChooser.visible()
            imgChooser.enableVisibleAnim()
        }

        imgDeleteName.setOnSafeClickListener {
            edtName.setText("")
            imgDeleteName.gone()
        }

        edtName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null) imgDeleteName.visible()
                else imgDeleteName.gone()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.update_contact_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_update -> {
                addContact()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_PICK_AVATAR && resultCode == Activity.RESULT_OK) {
            data?.data?.run {
                newPhoto = MediaStore.Images.Media.getBitmap(contentResolver, this)
                sdvAvatar.setImage(this)
            }
        } else if (requestCode == RC_TAKE_A_PHOTO && resultCode == Activity.RESULT_OK) {
            avtFile?.run {
                newPhoto = BitmapFactory.decodeFile(this.path)
                sdvAvatar.setImage(this)
            }
        } else super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onClickCamera() {
        takeAPhoto()
    }

    override fun onClickAlbum() {
        pickAvatar()
    }

    override fun onClickRemove() {
        newPhoto = null
        sdvAvatar.setImage(R.drawable.ic_mask_with_background)
    }

    override fun onContactAddedSuccess() {
        toast(R.string.insert_contact_success)
    }

    override fun nameIsValid(): Boolean {
        return edtName.text.isNotBlank()
    }

    override fun onNameError() {
        toast(R.string.err_name_empty)
        edtName.requestFocus()
    }

    override fun phoneIsValid(): Boolean {
        return edtNumber.text.isNotBlank()
    }

    override fun onPhoneError() {
        toast(R.string.err_phone_empty)
        edtNumber.requestFocus()
    }

    private fun pickAvatar() {
        ImageUtil.pickImage(
            self,
            RC_PERMISSION_READ_EXTERNAL_STORAGE,
            RC_PICK_AVATAR
        )
    }

    private fun takeAPhoto() {
        avtFile = ImageUtil.takeAPhoto(
            self,
            RC_PERMISSION_CAMERA,
            RC_TAKE_A_PHOTO
        )
    }

    private fun addContact() {
        if (PermissionUtil.isGranted(
                self,
                arrayOf(Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_CONTACTS),
                RC_PERMISSION_WRITE_CONTACT
            )
        ) {
            val name = edtName.text.toString().trim()
            val phone = edtNumber.text.toString().trim()
            val email = edtEmail.text.toString().trim()
            val company = edtCompany.text.toString().trim()
            val address = edtAddress.text.toString().trim()

            presenter.addContact(ctx, name, phone, email, company, address)
        }
    }
}