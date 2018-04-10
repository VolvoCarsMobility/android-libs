package com.drivekitt.common.utils.logging

import okhttp3.internal.platform.Platform

import okhttp3.internal.platform.Platform.INFO

interface LoggerFactory {
    fun createLogger(): Logger
}

interface Logger {
    fun log(level: Int, tag: String, msg: String)

    companion object {

        val DEFAULT: Logger = object : Logger {
            override fun log(level: Int, tag: String, msg: String) {
                Platform.get().log(INFO, msg, null)
                //Timber.tag(tag).log(level, message);
            }
        }
    }
}


