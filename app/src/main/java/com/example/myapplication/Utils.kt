package com.example.myapplication

import java.security.MessageDigest
import java.time.ZoneId
import java.time.ZonedDateTime

class Utils {
    companion object {
        fun getMonthName(monthNumber: Int): String {
            return when (GLanguage) {
                Language.ENGLISH -> {
                    when (monthNumber) {
                        1 -> "January"
                        2 -> "February"
                        3 -> "March"
                        4 -> "April"
                        5 -> "May"
                        6 -> "June"
                        7 -> "July"
                        8 -> "August"
                        9 -> "September"
                        10 -> "October"
                        11 -> "November"
                        12 -> "December"
                        else -> "Invalid month number"
                    }
                }
                Language.SPANISH -> {
                    when (monthNumber) {
                        1 -> "Enero"
                        2 -> "Febrero"
                        3 -> "Marzo"
                        4 -> "Abril"
                        5 -> "Mayo"
                        6 -> "Junio"
                        7 -> "Julio"
                        8 -> "Agosto"
                        9 -> "Septiembre"
                        10 -> "Octubre"
                        11 -> "Noviembre"
                        12 -> "Diciembre"
                        else -> "Número de mes inválido"
                    }
                }
            }
        }

        fun composeUrl(userId: String, page: String): String {
            return "http://marupeace.com/goapi/user/" +
                    userId + "/" +
                    composeHash(userId) + "/" +
                    page
        }

        private fun composeHash(userId: String): String {
            val timestamp = ZonedDateTime.now(ZoneId.of("Europe/Madrid")).toEpochSecond() % 2147483648
            val plainText = userId + "H7igfhR3_saY3dk7" + timestamp.toString()

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