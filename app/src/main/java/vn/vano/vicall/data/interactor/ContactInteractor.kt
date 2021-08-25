package vn.vano.vicall.data.interactor

import android.content.ContentProviderOperation
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.BlockedNumberContract.BlockedNumbers
import android.provider.CallLog
import android.provider.ContactsContract
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import io.reactivex.Single
import timber.log.Timber
import vn.vano.vicall.common.Api
import vn.vano.vicall.common.Constant
import vn.vano.vicall.common.ext.removePhonePrefix
import vn.vano.vicall.common.ext.removeSpaces
import vn.vano.vicall.common.util.PermissionUtil
import vn.vano.vicall.data.mapper.convertToModel
import vn.vano.vicall.data.mapper.convertToModels
import vn.vano.vicall.data.mapper.convertToResponses
import vn.vano.vicall.data.model.BaseModel
import vn.vano.vicall.data.model.ContactModel

class ContactInteractor : BaseInteractor() {

    companion object {
        private const val CALL_LOG_LOADER_ID = 1
    }

    private var callLogCursor: Cursor? = null

    fun getBlockedNumbers(ctx: Context, loaded: (ArrayList<ContactModel>) -> Unit) {
        if (PermissionUtil.isApi24orHigher()) {
            var cursor: Cursor? = null
            try {
                cursor = ctx.contentResolver.query(
                    BlockedNumbers.CONTENT_URI,
                    arrayOf(
                        BlockedNumbers.COLUMN_ID,
                        BlockedNumbers.COLUMN_ORIGINAL_NUMBER,
                        BlockedNumbers.COLUMN_E164_NUMBER
                    ), null, null, null
                )

                cursor?.run {
                    val calls = ArrayList<ContactModel>()
                    while (moveToNext()) {
                        val numberId = getLong(getColumnIndex(BlockedNumbers.COLUMN_ID))
                        var blockedNumber =
                            getString(getColumnIndex(BlockedNumbers.COLUMN_ORIGINAL_NUMBER))
                        if (blockedNumber.isBlank()) {
                            blockedNumber =
                                getString(getColumnIndex(BlockedNumbers.COLUMN_E164_NUMBER))
                        }

                        if (blockedNumber?.isNotBlank() == true) {
                            calls.add(
                                ContactModel(numberId).apply {
                                    this.number = blockedNumber
                                }
                            )
                        }
                    }

                    // Notify to the view
                    loaded(calls)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                cursor?.close()
            }
        }
    }

    fun getCallLog(
        ctx: Context,
        page: Int,
        loaded: (ArrayList<ContactModel>) -> Unit
    ) {
        var cursor: Cursor? = null
        try {
            val projection = arrayOf(
                CallLog.Calls._ID,
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.NUMBER,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION,
                CallLog.Calls.CACHED_PHOTO_URI
            )

            val selectOrder =
                "${CallLog.Calls.DATE} DESC limit ${(page - 1) * Constant.PAGE_SIZE}, ${Constant.PAGE_SIZE}"
            cursor = ctx.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                projection, null, null, selectOrder
            )

            cursor?.run {
                val calls = ArrayList<ContactModel>()
                while (moveToNext()) {
                    val callId = getLong(getColumnIndex(CallLog.Calls._ID))
                    val number = getString(getColumnIndex(CallLog.Calls.NUMBER))
                    val name = getString(getColumnIndex(CallLog.Calls.CACHED_NAME))
                    val callType = getInt(getColumnIndex(CallLog.Calls.TYPE))
                    val date = getLong(getColumnIndex(CallLog.Calls.DATE))
                    val duration = getInt(getColumnIndex(CallLog.Calls.DURATION))
                    val photoUri = getString(getColumnIndex(CallLog.Calls.CACHED_PHOTO_URI))

                    // Notify to the view
                    calls.add(ContactModel().apply {
                        this.callId = callId
                        this.number = number
                        this.name = name
                        this.callType = callType
                        this.date = date
                        this.duration = duration
                        this.avatar = photoUri
                    })
                }

                loaded(calls)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
    }

    fun listenCallLogChanged(
        ctx: Context,
        fragment: Fragment,
        loaded: (ContactModel) -> Unit
    ) {
        LoaderManager.getInstance(fragment)
            .initLoader(CALL_LOG_LOADER_ID, null, object : LoaderManager.LoaderCallbacks<Cursor> {
                override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
                    // This is called when a new Loader needs to be created.  This
                    // sample only has one Loader, so we don't care about the ID.
                    // First, pick the base URI to use depending on whether we are
                    // currently filtering.
                    val baseUri = CallLog.Calls.CONTENT_URI

                    // These are the Contacts rows that we will retrieve.
                    val projection = arrayOf(
                        CallLog.Calls._ID,
                        CallLog.Calls.CACHED_NAME,
                        CallLog.Calls.NUMBER,
                        CallLog.Calls.TYPE,
                        CallLog.Calls.DATE,
                        CallLog.Calls.DURATION,
                        CallLog.Calls.CACHED_PHOTO_URI
                    )

                    // Now create and return a CursorLoader that will take care of
                    // creating a Cursor for the data being displayed.
                    val selectOrder = "${CallLog.Calls.DATE} DESC limit 1"

                    return CursorLoader(ctx, baseUri, projection, null, null, selectOrder)
                }

                override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
                    data?.run {
                        callLogCursor = this

                        if (moveToFirst()) {
                            val callId = getLong(getColumnIndex(CallLog.Calls._ID))
                            val number = getString(getColumnIndex(CallLog.Calls.NUMBER))
                            val name = getString(getColumnIndex(CallLog.Calls.CACHED_NAME))
                            val callType = getInt(getColumnIndex(CallLog.Calls.TYPE))
                            val date = getLong(getColumnIndex(CallLog.Calls.DATE))
                            val duration = getInt(getColumnIndex(CallLog.Calls.DURATION))
                            val photoUri = getString(getColumnIndex(CallLog.Calls.CACHED_PHOTO_URI))

                            // Notify to the view
                            loaded(ContactModel().apply {
                                this.callId = callId
                                this.number = number
                                this.name = name
                                this.callType = callType
                                this.date = date
                                this.duration = duration
                                this.avatar = photoUri
                            })
                        }
                    }
                }

                override fun onLoaderReset(loader: Loader<Cursor>) {
                    callLogCursor = null
                }
            })
    }

    fun getLastCallLog(
        ctx: Context,
        loaded: (ContactModel) -> Unit
    ) {
        var cursor: Cursor? = null
        try {
            val projection = arrayOf(
                CallLog.Calls._ID,
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.NUMBER,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION,
                CallLog.Calls.CACHED_PHOTO_URI
            )

            val selectOrder = "${CallLog.Calls.DATE} DESC limit 1"
            cursor = ctx.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                projection, null, null, selectOrder
            )

            cursor?.run {
                if (moveToFirst()) {
                    val callId = getLong(getColumnIndex(CallLog.Calls._ID))
                    val number = getString(getColumnIndex(CallLog.Calls.NUMBER))
                    val name = getString(getColumnIndex(CallLog.Calls.CACHED_NAME))
                    val callType = getInt(getColumnIndex(CallLog.Calls.TYPE))
                    val date = getLong(getColumnIndex(CallLog.Calls.DATE))
                    val duration = getInt(getColumnIndex(CallLog.Calls.DURATION))
                    val photoUri = getString(getColumnIndex(CallLog.Calls.CACHED_PHOTO_URI))

                    // Notify to the view
                    loaded(ContactModel().apply {
                        this.callId = callId
                        this.number = number
                        this.name = name
                        this.callType = callType
                        this.date = date
                        this.duration = duration
                        this.avatar = photoUri
                    })
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
    }

    fun getCallLogByPhoneNumber(
        ctx: Context,
        phoneNumber: String,
        loaded: (ContactModel) -> Unit
    ) {
        var cursor: Cursor? = null
        try {
            val projection = arrayOf(
                CallLog.Calls._ID,
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.NUMBER,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION,
                CallLog.Calls.CACHED_PHOTO_URI
            )

            val selectOrder = "${CallLog.Calls.NUMBER} like ?"
            val queryNumber = phoneNumber.removeSpaces().removePhonePrefix().map {
                "%$it"
            }.joinToString(separator = "")
            val selectArgs = arrayOf("%$queryNumber%")
            cursor = ctx.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                projection, null, selectArgs, selectOrder
            )

            cursor?.run {
                if (moveToLast()) {
                    val callId = getLong(getColumnIndex(CallLog.Calls._ID))
                    val number = getString(getColumnIndex(CallLog.Calls.NUMBER))
                    val name = getString(getColumnIndex(CallLog.Calls.CACHED_NAME))
                    val callType = getInt(getColumnIndex(CallLog.Calls.TYPE))
                    val date = getLong(getColumnIndex(CallLog.Calls.DATE))
                    val duration = getInt(getColumnIndex(CallLog.Calls.DURATION))
                    val photoUri = getString(getColumnIndex(CallLog.Calls.CACHED_PHOTO_URI))

                    // Notify to the view
                    loaded(ContactModel().apply {
                        this.callId = callId
                        this.number = number
                        this.name = name
                        this.callType = callType
                        this.date = date
                        this.duration = duration
                        this.avatar = photoUri
                    })
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
    }

    fun getViCallSpamList(phoneNumber: String): Single<List<ContactModel>> {
        val params = HashMap<String, String>()
        params[Api.MOBILE_NUMBER] = phoneNumber.removePhonePrefix()

        return service.getViCallSpamList(params).map {
            it.data?.convertToModels()
        }
    }

    fun markAsMySpam(myNumber: String, spamNumber: String, action: String): Single<BaseModel> {
        val params = HashMap<String, String>()
        params[Api.MOBILE_NUMBER_A] = myNumber.removePhonePrefix()
        params[Api.MOBILE_NUMBER_B] = spamNumber.removePhonePrefix()
        params[Api.ACTION] = action

        return service.markAsMySpam(params).map {
            it.convertToModel()
        }
    }

    fun getServerContacts(phoneNumber: String, type: String): Single<List<ContactModel>> {
        val params = HashMap<String, String>()
        params[Api.MOBILE_NUMBER] = phoneNumber.removePhonePrefix()
        params[Api.T] = type

        return service.getContacts(params).map {
            it.data?.convertToModels()
        }
    }

    fun getLocalContacts(
        ctx: Context,
        query: String? = null,
        limit: Int? = null,
        page: Int? = null,
        loaded: (ArrayList<ContactModel>) -> Unit
    ) {
        var cursor: Cursor? = null
        try {
            val projection = arrayOf(
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.Contacts.LOOKUP_KEY
            )
            val selection = query?.run {
                "${ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER} = 1 AND " +
                        "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ? OR " +
                        "${ContactsContract.CommonDataKinds.Phone.NUMBER} LIKE ?"
            } ?: "${ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER} = 1"
            val selectionArgs = query?.let {
                val queryNumber = query.removeSpaces().map {
                    "%$it"
                }.joinToString(separator = "")
                arrayOf("%$query%", "%$queryNumber%")
            }
            val selectOrder = limit?.let {
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} limit $limit"
            } ?: page?.let {
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} limit ${(page - 1) * Constant.PAGE_SIZE}, ${Constant.PAGE_SIZE}"
            } ?: ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
            cursor = ctx.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection, selection, selectionArgs, selectOrder
            )

            cursor?.run {
                val contacts = ArrayList<ContactModel>()
                while (moveToNext()) {
                    val contactId =
                        getLong(getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID))
                    val number =
                        getString(getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    val name =
                        getString(getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                    val photoUri =
                        getString(getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO_THUMBNAIL_URI))

                    val lookUpKey = getString(getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY))
                    val lookUpUri =
                        ContactsContract.Contacts.getLookupUri(contactId, lookUpKey).toString()

                    contacts.add(
                        ContactModel(contactId).apply {
                            this.number = number
                            this.name = name
                            this.avatar = photoUri
                            this.lookUpUri = lookUpUri
                        }
                    )
                }

                // Notify to the view
                loaded(contacts)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
    }

    fun getContactsDetailFromLookUpKey(
        ctx: Context,
        lookUpKey: String?,
        loaded: (ContactModel) -> Unit
    ) {
        var cursor: Cursor? = null
        try {
            val projection = arrayOf(
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.Contacts.LOOKUP_KEY
            )
            val selection = "${ContactsContract.Data.LOOKUP_KEY} = '$lookUpKey'"
            cursor = ctx.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection, selection, null, null
            )

            cursor?.run {
                if (moveToFirst()) {
                    val contactId =
                        getLong(getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID))
                    val number =
                        getString(getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    val name =
                        getString(getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                    val photoUri =
                        getString(getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO_THUMBNAIL_URI))

                    val key = getString(getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY))
                    val lookUpUri =
                        ContactsContract.Contacts.getLookupUri(contactId, key).toString()

                    // Notify to the view
                    loaded(ContactModel(contactId).apply {
                        this.number = number
                        this.name = name
                        this.avatar = photoUri
                        this.lookUpUri = lookUpUri
                    })
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
    }

    fun getContactsDetailFromNumber(
        ctx: Context,
        phoneNumber: String,
        loaded: (ContactModel) -> Unit
    ) {
        var cursor: Cursor? = null
        try {
            val projection = arrayOf(
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.Contacts.LOOKUP_KEY
            )
            val selection = "${ContactsContract.CommonDataKinds.Phone.NUMBER} = '$phoneNumber'"
            cursor = ctx.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection, selection, null, null
            )

            cursor?.run {
                if (moveToFirst()) {
                    val contactId =
                        getLong(getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID))
                    val number =
                        getString(getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    val name =
                        getString(getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                    val photoUri =
                        getString(getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO_THUMBNAIL_URI))

                    val key = getString(getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY))
                    val lookUpUri =
                        ContactsContract.Contacts.getLookupUri(contactId, key).toString()

                    // Notify to the view
                    loaded(ContactModel(contactId).apply {
                        this.number = number
                        this.name = name
                        this.avatar = photoUri
                        this.lookUpUri = lookUpUri
                    })
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
    }

    fun searchCallLog(
        ctx: Context,
        query: String,
        maxItems: Int,
        loaded: (ArrayList<ContactModel>) -> Unit
    ) {
        var cursor: Cursor? = null
        try {
            val projection = arrayOf(
                CallLog.Calls._ID,
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.NUMBER,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION,
                CallLog.Calls.CACHED_PHOTO_URI
            )
            val queryNumber = query.replace(Regex(" "), "")
            val selection = "${CallLog.Calls.CACHED_NAME} LIKE ? OR ${CallLog.Calls.NUMBER} LIKE ?"
            val selectionArgs = arrayOf("%$query%", "%$queryNumber%")
            val selectOrder = "${CallLog.Calls.DATE} DESC limit $maxItems"
            cursor = ctx.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                selectOrder
            )

            cursor?.run {
                val calls = ArrayList<ContactModel>()
                while (moveToNext()) {
                    val callId = getLong(getColumnIndex(CallLog.Calls._ID))
                    val number = getString(getColumnIndex(CallLog.Calls.NUMBER))
                    val name = getString(getColumnIndex(CallLog.Calls.CACHED_NAME))
                    val callType = getInt(getColumnIndex(CallLog.Calls.TYPE))
                    val date = getLong(getColumnIndex(CallLog.Calls.DATE))
                    val duration = getInt(getColumnIndex(CallLog.Calls.DURATION))
                    val photoUri = getString(getColumnIndex(CallLog.Calls.CACHED_PHOTO_URI))

                    calls.add(
                        ContactModel().apply {
                            this.callId = callId
                            this.number = number
                            this.name = name
                            this.callType = callType
                            this.date = date
                            this.duration = duration
                            this.avatar = photoUri
                        }
                    )
                }

                // Notify to the view
                loaded(calls)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
    }

    fun syncContacts(
        phoneNumber: String,
        contacts: List<ContactModel>
    ): Single<ArrayList<ContactModel>> {
        val params = HashMap<String, Any?>()
        params[Api.MSISDN] = phoneNumber.removePhonePrefix()
        params[Api.ARR_CONTACT] = contacts.convertToResponses()

        return service.syncContacts(params).map {
            it.data?.convertToModels()
        }
    }

    fun addContact(
        ctx: Context,
        name: String,
        phoneNumber: String,
        email: String? = null,
        company: String? = null,
        address: String? = null
    ) {
        val rawContactId = 0
        val ops = ArrayList<ContentProviderOperation>()
        ops.add(
            ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build()
        )
        ops.add(
            ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                .withValue(
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
                )
                .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, name)
                .build()
        )
        ops.add(
            ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                .withValue(
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                )
                .withValue(
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    phoneNumber
                )
                .withValue(
                    ContactsContract.CommonDataKinds.Phone.TYPE,
                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
                )
                .build()
        )
        email?.run {
            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE
                    )
                    .withValue(ContactsContract.CommonDataKinds.Email.DATA, this)
                    .build()
            )
        }
        company?.run {
            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE
                    )
                    .withValue(ContactsContract.CommonDataKinds.Organization.COMPANY, this)
                    .build()
            )
        }
        address?.run {
            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE
                    )
                    .withValue(ContactsContract.CommonDataKinds.StructuredPostal.CITY, this)
                    .build()
            )
        }
        ctx.contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
    }

    fun openContactAddingDefaultApp(
        ctx: Context,
        phoneNumber: String? = null,
        name: String? = null
    ) {
        Intent(ContactsContract.Intents.Insert.ACTION).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            type = ContactsContract.RawContacts.CONTENT_TYPE
            putExtra("finishActivityOnSaveCompleted", true)

            phoneNumber?.also {
                putExtra(ContactsContract.Intents.Insert.PHONE, phoneNumber)
                putExtra(
                    ContactsContract.Intents.Insert.PHONE_TYPE,
                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
                )
            }
            name?.also {
                putExtra(ContactsContract.Intents.Insert.NAME, name)
                putExtra(
                    ContactsContract.RawContacts.ACCOUNT_NAME,
                    ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME
                )
            }
        }.run {
            ctx.startActivity(this)
        }
    }

    fun openContactEditingDefaultApp(ctx: Any, lookUpUri: String, requestCode: Int) {
        val intent = Intent(Intent.ACTION_EDIT).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            setDataAndType(
                Uri.parse(lookUpUri),
                ContactsContract.Contacts.CONTENT_ITEM_TYPE
            )
            putExtra("finishActivityOnSaveCompleted", true)
        }

        when (ctx) {
            is Fragment -> {
                ctx.startActivityForResult(intent, requestCode)
            }
            is AppCompatActivity -> {
                ctx.startActivityForResult(intent, requestCode)
            }
            else -> {
                Timber.e("ctx should be #Fragment or #AppCompatActivity")
            }
        }
    }
}