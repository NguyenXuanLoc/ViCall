package vn.vano.vicall.common.util

import android.content.Context
import timber.log.Timber
import vn.vano.vicall.R
import vn.vano.vicall.common.DateTime
import vn.vano.vicall.common.DateTime.Unit.MILLISECOND
import vn.vano.vicall.common.DateTime.Unit.SECOND
import java.text.SimpleDateFormat
import java.util.*

object DateTimeUtil {

    /**
     * @param timeStamp    As milliseconds
     * @param outputFormat Expected output format
     * @return String of date
     */
    fun convertTimeStampToDate(timeStamp: Any?, outputFormat: String): String {
        val sdf = SimpleDateFormat(outputFormat, Locale.getDefault())
        var date = Date()
        try {
            timeStamp?.run {
                date = when (this) {
                    is String -> Date(toLong())
                    is Long -> Date(this)
                    is Double -> Date(toLong())
                    is Float -> Date(toLong())
                    else -> Date()
                }
            }
        } catch (ex: NumberFormatException) {
            Timber.e(ex.localizedMessage)
        }

        return sdf.format(date)
    }

    /**
     * @param timeStamp As milliseconds
     * @return timestamp of the date(00:00:00:000) from timeStamp
     */
    fun convertTimeStampToStartOfDate(
        timeStamp: Long,
        timeZone: String = TimeZone.getDefault().id,
        unit: String = MILLISECOND
    ): Long {
        val cal = Calendar.getInstance(TimeZone.getTimeZone(timeZone))
        cal.timeInMillis = timeStamp
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        return when (unit) {
            SECOND -> {
                val result = cal.timeInMillis.toString()
                result.substring(0, result.length - 3).toLong()
            }
            else -> cal.timeInMillis
        }
    }

    /**
     * @param timeStamp As milliseconds
     * @return timestamp of the date(23:59:59:999) from timeStamp
     */
    fun convertTimeStampToEndOfDate(
        timeStamp: Long,
        timeZone: String = TimeZone.getDefault().id,
        unit: String = MILLISECOND
    ): Long {
        val cal = Calendar.getInstance(TimeZone.getTimeZone(timeZone))
        cal.timeInMillis = timeStamp
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)

        return when (unit) {
            SECOND -> {
                val result = cal.timeInMillis.toString()
                result.substring(0, result.length - 3).toLong()
            }
            else -> cal.timeInMillis
        }
    }

    /**
     * @param targetTime As milliseconds
     * @param currentTime As milliseconds
     * @return "1 phut truoc" or "1 gio truoc" or "18/06/2019"
     */
    fun getTimeDiff(ctx: Context, targetTime: Long, currentTime: Long): String {
        val timeDiff = (currentTime / 1000 - targetTime / 1000) / 60
        return if (timeDiff >= 0) {
            when (timeDiff) {
                0L -> ctx.getString(R.string.just_now)
                in 1..60 -> String.format(ctx.getString(R.string._minute_ago, timeDiff.toString()))
                in 60..(60 * 23 + 59) -> String.format(
                    ctx.getString(
                        R.string._hour_ago,
                        (timeDiff / 60).toInt().toString()
                    )
                )
                else -> convertTimeStampToDate(targetTime, DateTime.Format.DD_MM_YYYY)
            }
        } else {
            convertTimeStampToDate(targetTime, DateTime.Format.DD_MM_YYYY)
        }
    }

    /**
     * @param targetTime As milliseconds
     * @param currentTime As milliseconds
     * @return "Hom nay" or "Hom qua" or "18/06/2020"
     */
    fun getDateDiff(ctx: Context, targetTime: Long, currentTime: Long): String {
        val dateDiff =
            (convertTimeStampToEndOfDate(currentTime) - convertTimeStampToStartOfDate(targetTime)) / 24 / 60 / 60 / 1000
        return when (dateDiff) {
            0L -> ctx.getString(R.string.today)
            1L -> ctx.getString(R.string.yesterday)
            else -> convertTimeStampToDate(targetTime, DateTime.Format.DD_MM_YYYY)
        }
    }

    /**
     * @param duration As second
     * @return "Thoi gian cuoc goi vd: 1 giờ 20 phút 3 giây"
     */
    fun getDuration(ctx: Context, duration: Int): String? {
        val hour = duration / 60 / 60
        val minute = (duration - hour * 60 * 60) / 60
        val second = (duration - hour * 60 * 60 - minute * 60) % 60

        return if (hour > 0) {
            "$hour ${ctx.getString(R.string.hour)} $minute ${ctx.getString(R.string.minutes)} $second ${ctx.getString(
                R.string.seconds
            )}"
        } else {
            if (minute > 0) {
                "$minute ${ctx.getString(R.string.minutes)} $second ${ctx.getString(R.string.seconds)}"
            } else {
                "$second ${ctx.getString(R.string.seconds)}"
            }
        }
    }
}