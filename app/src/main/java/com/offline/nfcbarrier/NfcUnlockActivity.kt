package com.offline.nfcbarrier

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.nfc.NfcAdapter
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

class NfcUnlockActivity : Activity(), NfcAdapter.ReaderCallback {
    private var adapter: NfcAdapter? = null
    private lateinit var status: TextView
    private lateinit var nfcButton: Button
    private lateinit var nfcMark: TextView
    private lateinit var pulse: View
    private var pulseAnimation: AnimatorSet? = null
    private var targetPackage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = NfcAdapter.getDefaultAdapter(this)
        targetPackage = intent.getStringExtra(EXTRA_TARGET_PACKAGE)
        buildUi()
    }

    override fun onResume() {
        super.onResume()
        val nfc = adapter
        when {
            nfc == null -> showUnavailable("Este teléfono no tiene NFC")
            !nfc.isEnabled -> showUnavailable("NFC está desactivado", canOpenSettings = true)
            else -> {
                status.text = "Esperando tu llave NFC"
                nfcButton.visibility = View.GONE
                startPulse()
                nfc.enableReaderMode(
                    this,
                    this,
                    NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NFC_B or
                        NfcAdapter.FLAG_READER_NFC_F or NfcAdapter.FLAG_READER_NFC_V,
                    null
                )
            }
        }
    }

    override fun onPause() {
        adapter?.disableReaderMode(this)
        stopPulse()
        super.onPause()
    }

    override fun onTagDiscovered(tag: android.nfc.Tag?) {
        runOnUiThread {
            UnlockSession.start(this)
            stopPulse()
            pulse.background = circle(SUCCESS_SOFT)
            nfcMark.text = "✓"
            nfcMark.rotation = 0f
            nfcMark.setTextColor(SUCCESS)
            status.text = "Pausa completada"
            window.decorView.performHapticFeedback(android.view.HapticFeedbackConstants.CONFIRM)
            window.decorView.postDelayed({ openProtectedApp() }, 900)
        }
    }

    private fun buildUi() {
        val appInfo = targetPackage?.let { runCatching { packageManager.getApplicationInfo(it, 0) }.getOrNull() }
        val appName = appInfo?.loadLabel(packageManager)?.toString() ?: "esta aplicación"

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(24), dp(24), dp(24), dp(24))
            setBackgroundColor(BG)
        }
        applySystemInsets(root)

        root.addView(text("OFFLINE", 12f, ACCENT).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            letterSpacing = .16f
            gravity = Gravity.CENTER
            setPadding(dp(16), dp(7), dp(16), dp(7))
            background = rounded(ACTIVE_SURFACE, 20f)
        })

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
        }

        if (appInfo != null) {
            content.addView(ImageView(this).apply {
                setImageDrawable(appInfo.loadIcon(packageManager))
                contentDescription = appName
            }, LinearLayout.LayoutParams(dp(64), dp(64)).apply { bottomMargin = dp(20) })
        }
        content.addView(text("Tomá una pausa", 34f, INK).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            gravity = Gravity.CENTER
        })
        content.addView(text("Elegiste poner una pequeña barrera antes de abrir $appName.", 18f, MUTED).apply {
            gravity = Gravity.CENTER
            setPadding(dp(18), dp(12), dp(18), dp(34))
        })

        val nfcVisual = FrameLayout(this).apply { clipChildren = false }
        pulse = View(this).apply { background = circle(ACCENT_SOFT) }
        nfcVisual.addView(pulse, FrameLayout.LayoutParams(dp(152), dp(152), Gravity.CENTER))
        val center = View(this).apply { background = circle(SURFACE) }
        nfcVisual.addView(center, FrameLayout.LayoutParams(dp(112), dp(112), Gravity.CENTER))
        nfcMark = text(")))", 27f, ACCENT).apply {
            gravity = Gravity.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            rotation = -90f
        }
        nfcVisual.addView(nfcMark, FrameLayout.LayoutParams(dp(86), dp(86), Gravity.CENTER))
        content.addView(nfcVisual, LinearLayout.LayoutParams(dp(172), dp(172)).apply { bottomMargin = dp(24) })

        status = text("Preparando NFC…", 17f, INK).apply {
            gravity = Gravity.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        content.addView(status)
        content.addView(text("Acercá cualquier tarjeta, llavero o etiqueta a la parte trasera del teléfono.", 14f, MUTED).apply {
            gravity = Gravity.CENTER
            setPadding(dp(18), dp(8), dp(18), 0)
        })
        nfcButton = Button(this).apply {
            text = "Abrir ajustes de NFC"
            isAllCaps = false
            textSize = 15f
            setTextColor(Color.WHITE)
            background = rounded(ACCENT, 14f)
            visibility = View.GONE
            setOnClickListener { startActivity(Intent(Settings.ACTION_NFC_SETTINGS)) }
        }
        content.addView(nfcButton, LinearLayout.LayoutParams(-1, dp(50)).apply { topMargin = dp(20) })

        root.addView(content, LinearLayout.LayoutParams(-1, 0, 1f).apply { gravity = Gravity.CENTER })

        root.addView(text("Privado por diseño  ·  Offline sólo detecta el contacto NFC; no lee ni guarda datos de la tarjeta.", 12f, MUTED).apply {
            gravity = Gravity.CENTER
            setPadding(dp(8), dp(12), dp(8), dp(4))
        })
        setContentView(root)
    }

    private fun openProtectedApp() {
        val launchIntent = targetPackage?.let(packageManager::getLaunchIntentForPackage)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            startActivity(launchIntent)
        }
        finish()
    }

    private fun applySystemInsets(root: View) {
        root.setOnApplyWindowInsetsListener { view, insets ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val bars = insets.getInsets(WindowInsets.Type.systemBars())
                view.setPadding(dp(24) + bars.left, dp(24) + bars.top, dp(24) + bars.right, dp(24) + bars.bottom)
            } else {
                @Suppress("DEPRECATION")
                view.setPadding(
                    dp(24) + insets.systemWindowInsetLeft,
                    dp(24) + insets.systemWindowInsetTop,
                    dp(24) + insets.systemWindowInsetRight,
                    dp(24) + insets.systemWindowInsetBottom
                )
            }
            insets
        }
        root.requestApplyInsets()
    }

    private fun showUnavailable(message: String, canOpenSettings: Boolean = false) {
        stopPulse()
        status.text = message
        nfcMark.text = "!"
        nfcMark.rotation = 0f
        nfcMark.setTextColor(WARNING)
        pulse.background = circle(WARNING_SOFT)
        nfcButton.visibility = if (canOpenSettings) View.VISIBLE else View.GONE
    }

    private fun startPulse() {
        stopPulse()
        val sx = ObjectAnimator.ofFloat(pulse, View.SCALE_X, 0.88f, 1.08f)
        val sy = ObjectAnimator.ofFloat(pulse, View.SCALE_Y, 0.88f, 1.08f)
        val alpha = ObjectAnimator.ofFloat(pulse, View.ALPHA, 0.45f, 0.9f)
        pulseAnimation = AnimatorSet().apply {
            playTogether(sx, sy, alpha)
            duration = 1100
            interpolator = AccelerateDecelerateInterpolator()
            repeatModeForChildren(ObjectAnimator.REVERSE)
            start()
        }
    }

    private fun AnimatorSet.repeatModeForChildren(mode: Int) {
        childAnimations.filterIsInstance<ObjectAnimator>().forEach {
            it.repeatMode = mode
            it.repeatCount = ObjectAnimator.INFINITE
        }
    }

    private fun stopPulse() {
        pulseAnimation?.cancel()
        pulseAnimation = null
        if (::pulse.isInitialized) {
            pulse.scaleX = 1f
            pulse.scaleY = 1f
            pulse.alpha = 1f
        }
    }

    private fun text(value: String, size: Float, color: Int) = TextView(this).apply {
        text = value
        textSize = size
        setTextColor(color)
        includeFontPadding = false
    }

    private fun rounded(fill: Int, radius: Float) = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        setColor(fill)
        cornerRadius = dp(radius.toInt()).toFloat()
    }

    private fun circle(fill: Int) = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(fill)
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    companion object {
        const val EXTRA_TARGET_PACKAGE = "target_package"
        private val BG = Color.rgb(246, 246, 242)
        private val SURFACE = Color.WHITE
        private val ACTIVE_SURFACE = Color.rgb(232, 241, 235)
        private val INK = Color.rgb(28, 34, 31)
        private val MUTED = Color.rgb(101, 109, 104)
        private val ACCENT = Color.rgb(42, 104, 76)
        private val ACCENT_SOFT = Color.rgb(184, 214, 196)
        private val SUCCESS = Color.rgb(37, 148, 91)
        private val SUCCESS_SOFT = Color.rgb(214, 239, 223)
        private val WARNING = Color.rgb(192, 112, 29)
        private val WARNING_SOFT = Color.rgb(248, 224, 190)
    }
}
