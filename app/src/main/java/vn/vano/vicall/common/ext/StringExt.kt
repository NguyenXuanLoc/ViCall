package vn.vano.vicall.common.ext

fun String.removePhonePrefix(): String {
    return when {
        startsWith("0") -> {
            removePrefix("0")
        }
        startsWith("84") -> {
            removePrefix("84")
        }
        startsWith("+84") -> {
            removePrefix("+84")
        }
        else -> this
    }
}

fun String.removeSpaces(): String {
    return replace(Regex(" "), "")
}

fun String.isNumeric(): Boolean {
    var isNumeric = true

    try {
        toDouble()
    } catch (ex: NumberFormatException) {
        isNumeric = false
    }

    return isNumeric
}