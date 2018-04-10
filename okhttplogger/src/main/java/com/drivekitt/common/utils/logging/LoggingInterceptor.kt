package com.drivekitt.common.utils.logging

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.internal.platform.Platform
import java.io.IOException
import java.util.concurrent.TimeUnit

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


interface LoggerFactory {
    fun createLogger(): Logger
}

interface Logger {
    fun log(level: Int, tag: String, msg: String)

    companion object {

        val DEFAULT: Logger = object : Logger {
            override fun log(level: Int, tag: String, msg: String) {
                Platform.get().log(Platform.INFO, msg, null)
                //Timber.tag(tag).log(level, message);
            }
        }
    }
}

class LoggingInterceptor internal constructor(private val builder: Builder) : Interceptor {

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

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {

        val printer = Printer(builder.loggerFactory?.createLogger() ?: Logger.DEFAULT)

        val request = chain.request()

        if (builder.level == Level.NONE) {
            return chain.proceed(request)
        }

        val requestBody = request.body()

        val rSubtype: String? = requestBody?.contentType()?.subtype()


        if (isNotFileRequest(rSubtype)) {
            printer.printJsonRequest(builder, request)
        } else {
            printer.printFileRequest(builder, request)
        }

        val st = System.nanoTime()
        val response = chain.proceed(request)
        val chainMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - st)

        val segmentList = request.url().encodedPathSegments()
        val header = response.headers().toString()
        val code = response.code()
        val isSuccessful = response.isSuccessful
        val message = response.message()
        val responseBody = response.body()
        val contentType = responseBody!!.contentType()

        var subtype: String? = null
        val body: ResponseBody

        if (contentType != null) {
            subtype = contentType.subtype()
        }

        if (isNotFileRequest(subtype)) {
            val bodyString = Printer.getJsonString(responseBody.string())
            val url = response.request().url().toString()

            printer.printJsonResponse(builder, chainMs, isSuccessful, code, header, bodyString,
                segmentList, message, url)
            body = ResponseBody.create(contentType, bodyString)
        } else {
            printer.printFileResponse(builder, chainMs, isSuccessful, code, header, segmentList, message)
            return response
        }

        return response.newBuilder().body(body).build()
    }

    private fun isNotFileRequest(subtype: String?): Boolean {
        return subtype != null && (subtype.contains("json")
            || subtype.contains("xml")
            || subtype.contains("plain")
            || subtype.contains("html"))
    }
}
