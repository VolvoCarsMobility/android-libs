package com.drivekitt.common.utils.logging

import okhttp3.internal.platform.Platform

enum class Level {
    /**
     * No logs.
     */
    NONE,
    /**
     *
     * Example:
     * <pre>`- URL
     * - Method
     * - Headers
     * - Body
    `</pre> *
     */
    BASIC,
    /**
     *
     * Example:
     * <pre>`- URL
     * - Method
     * - Headers
    `</pre> *
     */
    HEADERS,
    /**
     *
     * Example:
     * <pre>`- URL
     * - Method
     * - Body
    `</pre> *
     */
    BODY
}

class Builder {

    companion object {
        private var TAG = "OkHttp"
    }

    var type = Platform.INFO
    var requestTag: String = TAG
    var responseTag: String = TAG
    var level = Level.BASIC
    var loggerFactory: LoggerFactory? = null
    var decoration = true

    internal fun getTag(isRequest: Boolean): String = if (isRequest) requestTag else responseTag

    @Suppress("unused")
    fun build(): LoggingInterceptor {
        return LoggingInterceptor(this)
    }
}