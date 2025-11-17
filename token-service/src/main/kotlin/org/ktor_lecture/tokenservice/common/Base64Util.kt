package org.ktor_lecture.tokenservice.common

import java.util.Base64

object Base64Util {

    fun encode(data: ByteArray): String {
        return Base64.getEncoder().encodeToString(data)
    }

    fun decode(data: String): ByteArray {
        return Base64.getDecoder().decode(data)
    }
}