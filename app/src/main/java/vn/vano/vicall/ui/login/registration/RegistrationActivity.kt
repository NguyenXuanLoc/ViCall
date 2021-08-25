package vn.vano.vicall.ui.login.registration

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import kotlinx.android.synthetic.main.activity_registration.*
import org.jetbrains.anko.bundleOf
import org.jetbrains.anko.ctx
import org.jetbrains.anko.toast
import vn.vano.vicall.R
import vn.vano.vicall.common.Constant
import vn.vano.vicall.common.DateTime
import vn.vano.vicall.common.Key
import vn.vano.vicall.common.ext.*
import vn.vano.vicall.common.util.DateTimeUtil
import vn.vano.vicall.common.util.ImageUtil
import vn.vano.vicall.data.model.UserModel
import vn.vano.vicall.ui.base.BaseActivity
import vn.vano.vicall.ui.main.MainActivity
import vn.vano.vicall.widget.LayoutImageChooser
import java.io.File
import java.util.*

class RegistrationActivity : BaseActivity<RegistrationView, RegistrationPresenterImp>(),
    RegistrationView, LayoutImageChooser.ImageChooserListener {

    companion object {
        private const val RC_PERMISSION_READ_EXTERNAL_STORAGE = 1
        private const val RC_PERMISSION_CAMERA = 2
        private const val RC_PICK_AVATAR = 256
        private const val RC_TAKE_A_PHOTO = 257

        const val ACTION_UPDATE = "update"

        fun start(act: AppCompatActivity, phoneNumber: String?, action: String? = null) {
            bundleOf(Key.ACTION to action, Key.PHONE_NUMBER to phoneNumber).apply {
                act.openActivity(RegistrationActivity::class.java, this)
            }
        }
    }

    private var action: String? = null
    private var phoneNumber: String? = null
    private var avtFile: File? = null
    private var avtFileUri: Uri? = null
    private var sex: String? = null
    private val birthdayCal by lazy {
        Calendar.getInstance().apply {
            add(Calendar.YEAR, -20)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == RC_PERMISSION_READ_EXTERNAL_STORAGE
            && grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            pickAvatar()
        } else if (requestCode == RC_PERMISSION_CAMERA
            && grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            takeAPhoto()
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_PICK_AVATAR && resultCode == Activity.RESULT_OK) {
            data?.data?.run {
                fillAvatar(this)
            }
        } else if (requestCode == RC_TAKE_A_PHOTO && resultCode == Activity.RESULT_OK) {
            avtFile?.run {
                fillAvatar(this)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onDestroy() {
        deleteTempAvt()
        super.onDestroy()
    }

    override fun getExtrasValue() {
        intent?.extras?.run {
            if (containsKey(Key.PHONE_NUMBER)) {
                phoneNumber = getString(Key.PHONE_NUMBER)
            }
            if (containsKey(Key.ACTION)) {
                action = getString(Key.ACTION)
            }
        }
    }

    override fun initView(): RegistrationView {
        return this
    }

    override fun initPresenter(): RegistrationPresenterImp {
        return RegistrationPresenterImp(ctx)
    }

    override fun getLayoutId(): Int? {
        return R.layout.activity_registration
    }

    override fun initWidgets() {
        // Init toolbar
        if (action == ACTION_UPDATE) {
            showTitle(R.string.update_profile)

            // Show sex and birthday views
            lblSex.visible()
            lblBirthday.visible()

            // Fill user info
            currentUser?.run {
                sdvAvt.setImage(avatar, errorImage = R.drawable.ic_mask_with_background)
                txtName.setText(name)

                when {
                    isMale() -> {
                        lblSex.text = getString(R.string.male)
                        sex = Constant.Sex.MALE
                    }
                    isFemale() -> {
                        lblSex.text = getString(R.string.female)
                        sex = Constant.Sex.FEMALE
                    }
                    else -> {
                        lblSex.text = getString(R.string.other)
                        sex = Constant.Sex.OTHER
                    }
                }
                lblBirthday.text = birthday?.let { birthday ->
                    birthdayCal.timeInMillis = birthday
                    DateTimeUtil.convertTimeStampToDate(birthday, DateTime.Format.DD_MM_YYYY)
                } ?: ""
            }
        } else {
            showTitle(R.string.create_profile)
        }
        enableHomeAsUp {
            finish()
        }

        // Listeners
        imgChooser.setImageChooserListener(this)
        sdvAvt.setOnSafeClickListener {
            imgChooser.visible()
            imgChooser.enableVisibleAnim()
        }

        lblSex.setOnSafeClickListener {
            showSexOptionMenu()
        }

        lblBirthday.setOnSafeClickListener {
            showBirthdayPicker()
        }

        btnConfirm.setOnSafeClickListener {
            phoneNumber?.also { phone ->
                if (action == ACTION_UPDATE) {
                    val birthday = if (lblBirthday.text.isNotEmpty()) {
                        birthdayCal.timeInMillis
                    } else {
                        null
                    }

                    presenter.updateProfile(
                        phone,
                        txtName.text.toString(),
                        sex,
                        birthday,
                        avtFileUri
                    )
                } else {
                    presenter.registerAccount(phone, txtName.text.toString(), avtFileUri)
                }
            }
        }
    }

    override fun onRegistrationSuccess(userModel: UserModel) {
        // Open Home page and clear stack
        openActivity(MainActivity::class.java, clearStack = true)
    }

    override fun onUpdateProfileSuccess(userModel: UserModel) {
        finish()
    }

    override fun nameIsValid(): Boolean {
        return txtName.text.isNotBlank()
    }

    override fun onNameError() {
        toast(R.string.err_name_empty)
        txtName.requestFocus()
    }

    override fun deleteTempAvt() {
        avtFile?.delete()
        avtFile = null
    }

    override fun onClickCamera() {
        takeAPhoto()
    }

    override fun onClickAlbum() {
        pickAvatar()
    }

    override fun onClickRemove() {
        fillAvatar(R.drawable.ic_mask_with_background)
        avtFileUri = null
    }

    private fun fillAvatar(src: Any) {
        when (src) {
            is File -> {
                sdvAvt.setImage(src)
                avtFileUri = Uri.fromFile(src)
            }
            is Uri -> {
                sdvAvt.setImage(src)
                avtFileUri = src
            }
            is Int -> {
                sdvAvt.setImage(src)
                avtFileUri = null
            }
        }
    }

    private fun pickAvatar() {
        ImageUtil.pickImage(self, RC_PERMISSION_READ_EXTERNAL_STORAGE, RC_PICK_AVATAR)
    }

    private fun takeAPhoto() {
        avtFile = ImageUtil.takeAPhoto(self, RC_PERMISSION_CAMERA, RC_TAKE_A_PHOTO)
    }

    private fun showSexOptionMenu() {
        val popupMenu = PopupMenu(self, lblSex)
        popupMenu.menuInflater.inflate(R.menu.menu_sex, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            lblSex.text = item.title

            sex = when (item.itemId) {
                R.id.menu_male -> Constant.Sex.MALE
                R.id.menu_female -> Constant.Sex.FEMALE
                else -> Constant.Sex.OTHER
            }

            return@setOnMenuItemClickListener true
        }

        popupMenu.show()
    }

    private fun showBirthdayPicker() {
        val callBack = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            if (view.isShown) {
                birthdayCal.set(year, monthOfYear, dayOfMonth)
                lblBirthday.text = DateTimeUtil.convertTimeStampToDate(
                    birthdayCal.timeInMillis,
                    DateTime.Format.DD_MM_YYYY
                )
            }
        }

        val datePicker = DatePickerDialog(
            self,
            callBack,
            birthdayCal.get(Calendar.YEAR),
            birthdayCal.get(Calendar.MONTH),
            birthdayCal.get(Calendar.DATE)
        )
        datePicker.show()
    }
}
