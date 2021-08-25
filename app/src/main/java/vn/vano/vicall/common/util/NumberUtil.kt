package vn.vano.vicall.common.util

import timber.log.Timber
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.ParseException
import java.util.*
import kotlin.math.ceil
import kotlin.math.roundToLong

/**
 * Created by NaPro on 05/01/2017.
 */

object NumberUtil {

    private val LOCALE = Locale("vi")

    fun formatNumber(number: Any?, numberAfterDecimal: Int = 0): String {
        return when (number) {
            is Int -> {
                String.format(LOCALE, "%,.0" + 0 + "f", ((number * 100f).roundToLong() / 100f))
            }
            is BigInteger -> {
                String.format(LOCALE, "%,.0" + 0 + "f", number)
            }
            is Float -> {
                val newNumberAfterDecimal = if (numberHasSurPlus(number)) {
                    numberAfterDecimal
                } else {
                    0
                }
                String.format(
                    LOCALE,
                    "%,.0" + newNumberAfterDecimal + "f",
                    ((number * 100f).roundToLong() / 100f)
                )
            }
            is Double -> {
                val newNumberAfterDecimal = if (numberHasSurPlus(number)) {
                    numberAfterDecimal
                } else {
                    0
                }
                val bigNumber = BigDecimal.valueOf(number)
                String.format(LOCALE, "%,.0" + newNumberAfterDecimal + "f", bigNumber)
            }
            is Long -> {
                val bigNumber = BigDecimal.valueOf(number)
                String.format(LOCALE, "%,.0" + 0 + "f", bigNumber)
            }
            is BigDecimal -> {
                String.format(LOCALE, "%,.0" + numberAfterDecimal + "f", number)
            }
            else -> "NaN"
        }
    }

    fun convertStringToDecimalNumber(inputNumber: String): Double {
        var strNumber = inputNumber
        val decimalFormat = DecimalFormat()
        val format = NumberFormat.getInstance(LOCALE)
        try {
            val decimalSeparator = decimalFormat.decimalFormatSymbols.decimalSeparator.toString()
            if (decimalSeparator == ".") {
                if (strNumber.contains(",")) {
                    strNumber = strNumber.replace(",", "")
                }
            } else if (decimalSeparator == ",") {
                if (strNumber.contains(".")) {
                    strNumber = strNumber.replace(".", "")
                }
            }

            val number = format.parse(strNumber)
            return number?.toDouble() ?: 0.0
        } catch (e: ParseException) {
            Timber.e("$e")
        }

        return 0.0
    }

    fun round(value: Double, places: Int): Double {
        var newPlaces = places
        if (newPlaces < 0) {
            newPlaces = 0
        }

        var bd = BigDecimal(value)
        bd = bd.setScale(newPlaces, RoundingMode.HALF_UP)
        return bd.toDouble()
    }

    fun numberHasSurPlus(number: Any): Boolean {
        return when (number) {
            is Float -> ceil(number) > number
            is Double -> ceil(number) > number
            else -> false
        }
    }
}