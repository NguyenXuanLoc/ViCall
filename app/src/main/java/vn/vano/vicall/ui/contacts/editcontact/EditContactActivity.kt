package vn.vano.vicall.ui.contacts.editcontact

import android.Manifest
import android.app.Activity
import android.content.ContentProviderOperation
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal.*
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_edit_contact.*
import org.jetbrains.anko.toast
import timber.log.Timber
import vn.vano.vicall.R
import vn.vano.vicall.common.Key
import vn.vano.vicall.common.ext.gone
import vn.vano.vicall.common.ext.setImage
import vn.vano.vicall.common.ext.setOnSafeClickListener
import vn.vano.vicall.common.ext.visible
import vn.vano.vicall.common.util.ImageUtil
import vn.vano.vicall.common.util.PermissionUtil
import vn.vano.vicall.data.model.ContactModel
import vn.vano.vicall.ui.base.BaseActivity
import vn.vano.vicall.widget.LayoutImageChooser
import java.io.ByteArrayOutputStream
import java.io.File


class EditContactActivity : BaseActivity<EditContactView, EditContactPresenterImp>(),
    EditContactView, LayoutImageChooser.ImageChooserListener {

    private var contactId: String? = null
    private var contactModel: ContactModel? = null
    private var avtFile: File? = null
    private var newPhoto: Bitmap? = null

    companion object {
        const val RC_PERMISSION_WRITE_CONTACT = 324
        private const val RC_PERMISSION_READ_EXTERNAL_STORAGE = 1
        private const val RC_PERMISSION_CAMERA = 2
        private const val RC_PICK_AVATAR = 256
        private const val RC_TAKE_A_PHOTO = 257
    }

    override fun initView(): EditContactView {
        return this
    }

    override fun initPresenter(): EditContactPresenterImp {
        return EditContactPresenterImp(this)
    }

    override fun getLayoutId(): Int? {
        return R.layout.activity_edit_contact
    }

    override fun getExtrasValue() {
        intent?.extras?.run {
            if (containsKey(Key.CONTACT_MODEL)) {
                contactModel = getSerializable(Key.CONTACT_MODEL) as ContactModel?
            }
        }
    }

    override fun initWidgets() {
        showTitle(getString(R.string.update_contact))
        enableHomeAsUp { finish() }
        contactId = contactModel?.contactIdLocal.toString()

        edtName.setText(contactModel?.name)
        edtCompany.setText(contactModel?.company)
        edtAddress.setText(contactModel?.address)
        edtEmail.setText(contactModel?.email)
        edtNumber.setText(contactModel?.number)
        sdvAvatar.setImageURI(contactModel?.avatar)

        //Listener
        imgChooser.setImageChooserListener(this)
        sdvAvatar.setOnSafeClickListener {
            imgChooser.visible()
            imgChooser.enableVisibleAnim()
        }
        lblDelete.setOnSafeClickListener {
            if (deleteContact(contactId.toString())) {
                toast(getString(R.string.delete_contact_success))
                finish()
            }
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
        imgDeleteName.setOnSafeClickListener {
            edtName.setText("")
            imgDeleteName.gone()
        }

        //test

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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.update_contact_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_update -> {
                if (PermissionUtil.isGranted(
                        self, arrayOf(Manifest.permission.WRITE_CONTACTS),
                        RC_PERMISSION_WRITE_CONTACT
                    )
                    && checkNull(
                        edtName.text.toString(), edtNumber.text.toString()
                    )
                ) {
                    if (updateContact(
                            contactId.toString(),
                            edtName.text.toString(),
                            edtCompany.text.toString(),
                            edtNumber.text.toString(),
                            edtAddress.text.toString(),
                            edtEmail.text.toString()
                        )
                    ) {
                        toast(getString(R.string.edit_success))
                    } else toast(getString(R.string.edit_error))
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateContact(
        contactId: String,
        newName: String,
        newCompany: String,
        newNumber: String,
        newAddress: String,
        newEmail: String
    ): Boolean {
        //selection for name
        var where =
            ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?"
        var operations: ArrayList<ContentProviderOperation> = ArrayList()
        //Change Name
        if (newName.isNotEmpty()) {
            val nameParams = arrayOf(
                contactId, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
            )
            operations.add(
                ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection(where, nameParams)
                    .withValue(
                        ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                        newName
                    )
                    .build()
            )
        }
        // Company
        if (newCompany.isNotEmpty()) {
            val companyParam =
                arrayOf(contactId, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
            operations.add(
                ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection(where, companyParam)
                    .withValue(ContactsContract.CommonDataKinds.Organization.COMPANY, newCompany)
                    .build()
            )
        }
        //Change Phone
        if (newNumber.isNotEmpty()) {
            val numberParams = arrayOf(contactId, Phone.CONTENT_ITEM_TYPE)
            operations.add(
                ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection(where, numberParams)
                    .withValue(Phone.NUMBER, newNumber)
                    .build()
            )
        }
        //Change Address
        if (newAddress.isNotEmpty()) {
            val addressParam =
                arrayOf(
                    contactId,
                    ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
                )

            operations.add(
                ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection(where, addressParam)
                    .withValue(ContactsContract.CommonDataKinds.StructuredPostal.CITY, newAddress)
                    .build()
            )
        }
        //Change Email
        if (newEmail.isNotEmpty()) {
            val emailParams = arrayOf(
                contactId, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE
            )
            operations.add(
                ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection(where, emailParams)
                    .withValue(ContactsContract.CommonDataKinds.Email.ADDRESS, newEmail)
                    .build()
            )
        }
        //Change photo
        if (newPhoto != null) {
            val photoParam =
                arrayOf(contactId, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
            var stream = ByteArrayOutputStream()
            newPhoto?.compress(Bitmap.CompressFormat.JPEG, 50, stream)
            operations.add(
                ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection(where, photoParam)
                    .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, stream.toByteArray())
                    .build()
            )
        }
        try {
            contentResolver.applyBatch(ContactsContract.AUTHORITY, operations)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            Timber.e(e.toString())
        }
        return false
    }

    private fun deleteContact(idContact: String): Boolean {
        val uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, idContact)
        val deleted: Int = contentResolver.delete(uri, null, null)
        return deleted > 0
    }

    // check null name,number
    private fun checkNull(name: String, number: String): Boolean {
        if (name.isNotEmpty() && number.isNotEmpty())
            return true
        else toast(getString(R.string.not_null_number_and_name))
        return false
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

}