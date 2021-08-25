package vn.vano.vicall.data.mapper

import vn.vano.vicall.common.DateTime
import vn.vano.vicall.common.ext.removePhonePrefix
import vn.vano.vicall.common.ext.removeSpaces
import vn.vano.vicall.common.util.DateTimeUtil
import vn.vano.vicall.data.model.ContactModel
import vn.vano.vicall.data.response.ContactResponse

fun ContactResponse.convertToModel(): ContactModel {
    val response = this
    val data = response.data ?: response
    return ContactModel().apply {
        code = response.code
        message = response.message ?: ""
        contactIdServer = data.id
        name = data.name
        number = data.mobile ?: data.msisdnB ?: data.msisdn ?: ""
        birthday = data.birthdayTimeStamp
        company = data.company
        email = data.email
        address = data.address
        status = data.infoStatus
        avatar = data.infoImage
        isViCallUser = data.activeVicall == 1
    }
}

fun List<ContactResponse>.convertToModels(): ArrayList<ContactModel> {
    return ArrayList(
        map {
            it.convertToModel()
        }
    )
}

fun ContactModel.convertToResponse(): ContactResponse {
    return ContactResponse(
        mobile = number,
        name = name,
        birthday = birthday?.let {
            DateTimeUtil.convertTimeStampToDate(it, DateTime.Format.DD_MM_YYYY)
        },
        birthdayTimeStamp = birthday,
        company = company,
        email = email,
        address = address
    )
}

fun List<ContactModel>.convertToResponses(): ArrayList<ContactResponse> {
    return ArrayList(
        map {
            it.convertToResponse()
        }
    )
}

fun List<ContactModel>.equalTo(contacts: List<ContactModel>): Boolean {
    var equal = true
    for (contact in contacts) {
        equal = find {
            it.number.removeSpaces().removePhonePrefix() == contact.number.removeSpaces()
                .removePhonePrefix()
                    && it.name == contact.name
        } != null

        if (!equal) {
            break
        }
    }

    return equal
}