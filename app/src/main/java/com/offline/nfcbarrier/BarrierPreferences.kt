package com.offline.nfcbarrier

import android.content.Context

object BarrierPreferences {
    private const val FILE = "barrier_preferences"
    private const val PROTECTED = "protected_packages"
    private const val MINUTES = "unlock_minutes"

    fun protectedPackages(context: Context): Set<String> =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .getStringSet(PROTECTED, emptySet())?.toSet().orEmpty()

    fun setProtectedPackages(context: Context, packages: Set<String>) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit()
            .putStringSet(PROTECTED, packages).apply()
    }

    fun unlockDurationMillis(context: Context): Long =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .getInt(MINUTES, 10).coerceIn(1, 120) * 60_000L

    fun setUnlockMinutes(context: Context, minutes: Int) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit()
            .putInt(MINUTES, minutes.coerceIn(1, 120)).apply()
    }
}
