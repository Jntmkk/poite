package net.coolshell.poite.shared.core.logging

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.StaticConfig
import co.touchlab.kermit.platformLogWriter

class KermitAppLogger : AppLogger {

    private val logger = Logger(
        config = StaticConfig(
            minSeverity = Severity.Verbose,
            logWriterList = listOf(platformLogWriter()),
        ),
        tag = "poite",
    )

    override fun v(tag: String, message: String) = logger.withTag(tag).v { message }

    override fun d(tag: String, message: String) = logger.withTag(tag).d { message }

    override fun i(tag: String, message: String) = logger.withTag(tag).i { message }

    override fun w(tag: String, message: String, throwable: Throwable?) =
        logger.withTag(tag).w(throwable) { message }

    override fun e(tag: String, message: String, throwable: Throwable?) =
        logger.withTag(tag).e(throwable) { message }
}
