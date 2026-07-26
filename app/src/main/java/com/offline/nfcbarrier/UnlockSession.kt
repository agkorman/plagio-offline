package com.offline.nfcbarrier

import android.content.Context

object UnlockSession {
    private const val FILE = "unlock_session"
    private const val EXPIRES_AT = "expires_at"

    fun isActive(context: Context): Boolean =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .getLong(EXPIRES_AT, 0L) > System.currentTimeMillis()

    fun start(context: Context) {
        val expiresAt = System.currentTimeMillis() + BarrierPreferences.unlockDurationMillis(context)
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putLong(EXPIRES_AT, expiresAt).apply()
    }
}
