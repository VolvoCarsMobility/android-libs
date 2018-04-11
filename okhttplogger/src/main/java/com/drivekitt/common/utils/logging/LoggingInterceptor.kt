package com.drivekitt.common.utils.logging

import android.util.Log
import okhttp3.Interceptor
import okhttp3.internal.platform.Platform

typealias Logger = (level: Int, tag: String, msg: String) -> Unit

fun createLoggerInterceptor(action: LoggingInterceptor.Builder.() -> Unit): LoggingInterceptor =
        LoggingInterceptor.Builder().apply(action).build()

interface LoggingInterceptor : Interceptor {
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
        var loggerFactory: () -> Logger = { DEFAULT_LOGGER }
        var decoration = true

        internal fun getTag(isRequest: Boolean): String = if (isRequest) requestTag else responseTag

        @Suppress("unused")
        fun build(): LoggingInterceptor {
            return LoggingInterceptorImpl(this)
        }
    }

    companion object {
        val DEFAULT_LOGGER: Logger = { level, tag, msg -> Log.println(level, tag, msg) }
    }
}