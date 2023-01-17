package com.example.myapplication

import java.security.MessageDigest
import java.time.ZoneId
import java.time.ZonedDateTime

class Utils {
    companion object {
        fun composeUrl(userId: String, page: String): String {
            return "http://marupeace.com/goapi/user/" +
                    userId + "/" +
                    composeHash(userId) + "/" +
                    page
        }

        private fun composeHash(userId: String): String {
            val timestamp = ZonedDateTime.now(ZoneId.of("Europe/Madrid")).toEpochSecond() % 2147483648
            val plainText = userId + "X249CIAoi_22j%J3" + timestamp.toString()

            return sha256(plainText)
        }

        private fun sha256(text : String): String {
            val bytes = text.toByteArray()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(bytes)
            return digest.fold("", { str, it -> str + "%02x".format(it) })
        }
    }
}