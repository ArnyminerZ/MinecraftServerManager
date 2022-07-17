package mc

import exception.UnparseableLineException
import java.sql.Time
import java.time.format.DateTimeFormatter

data class LogLine(
    val time: Time,
    val thread: String,
    val level: LogLevel,
    val line: String,
) {
    companion object {
        @Throws(UnparseableLineException::class)
        fun parse(raw: String): LogLine {
            val firstDividerPosition = raw.indexOf(' ')
            val taggedTime = raw.substring(0, firstDividerPosition) // e.g. [07:56:23]
            val time = try { Time.valueOf(taggedTime.substring(1, taggedTime.length - 1)) } catch (e: IllegalArgumentException) { throw UnparseableLineException(raw, e) }
            val tagClosePosition = raw.indexOf(']', firstDividerPosition)
            val tag = raw.substring(firstDividerPosition + 2, tagClosePosition)
            val tagDividerPosition = tag.indexOf('/')
            val thread = tag.substring(0, tagDividerPosition)
            val levelStr = tag.substring(tagDividerPosition + 1)
            val level = try { LogLevel.valueOf(levelStr) } catch (e: IllegalArgumentException) { throw UnparseableLineException(raw, e) }
            val line = raw.substring(raw.indexOf(':', tagClosePosition) + 2).trimEnd { it == '\n' }
            return LogLine(time, thread, level, line)
        }
    }

    override fun toString(): String =
        "[${time.toLocalTime().format(DateTimeFormatter.ISO_TIME)}] [$thread/${level.name}]: $line"
}
