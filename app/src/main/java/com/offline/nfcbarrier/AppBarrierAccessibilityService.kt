package com.offline.nfcbarrier

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.content.Intent

class AppBarrierAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val openedPackage = event.packageName?.toString() ?: return
        if (openedPackage !in BarrierPreferences.protectedPackages(this)) return
        if (UnlockSession.isActive(this)) return

        startActivity(Intent(this, NfcUnlockActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra(NfcUnlockActivity.EXTRA_TARGET_PACKAGE, openedPackage)
        })
    }

    override fun onInterrupt() = Unit
}
