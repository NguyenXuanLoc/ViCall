package vn.vano.vicall.common.ext

fun Long.convertToTimeCounter(): String {
    val hour = this / 60 / 60
    val minute = this / 60 - (hour * 60)
    val second = this % 60

    val strHour = if (hour < 10) {
        "0$hour"
    } else {
        "$hour"
    }

    val strMinute = if (minute < 10) {
        "0$minute"
    } else {
        "$minute"
    }

    val strSecond = if (second < 10) {
        "0$second"
    } else {
        "$second"
    }

    return if (hour > 0) {
        "$strHour:$strMinute:$strSecond"
    } else {
        "$strMinute:$strSecond"
    }
}