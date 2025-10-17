package com.example.pinga.core
import java.time.Instant

object Time {
    fun nowIso(): String = Instant.now().toString()
}