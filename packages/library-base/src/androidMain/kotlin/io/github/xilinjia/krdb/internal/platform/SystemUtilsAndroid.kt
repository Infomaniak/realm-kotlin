package io.github.xilinjia.krdb.internal.platform

import android.os.Build
import io.github.xilinjia.krdb.internal.RealmInitializer
import io.github.xilinjia.krdb.internal.RealmInstantImpl
import io.github.xilinjia.krdb.internal.util.Exceptions
import io.github.xilinjia.krdb.log.RealmLogger
import io.github.xilinjia.krdb.types.RealmInstant
import java.io.FileNotFoundException
import java.io.InputStream

@Suppress("MayBeConst") // Cannot make expect/actual const

public actual val OS_NAME: String = "Android"

// Returns the root directory of the platform's App data
public actual fun appFilesDirectory(): String = RealmInitializer.filesDir.absolutePath

public actual fun assetFileAsStream(assetFilename: String): InputStream = try {
    RealmInitializer.asset(assetFilename)
} catch (e: FileNotFoundException) {
    throw Exceptions.assetFileNotFound(assetFilename, e)
}

// Returns the default logger for the platform
public actual fun createDefaultSystemLogger(tag: String): RealmLogger =
    LogCatLogger(tag)

public actual fun currentTime(): RealmInstant {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val jtInstant = java.time.Clock.systemUTC().instant()
        RealmInstantImpl(jtInstant.epochSecond, jtInstant.nano)
    } else {
        val now = System.currentTimeMillis()
        RealmInstantImpl(now / 1000, (now % 1000).toInt() * 1_000_000)
    }
}
