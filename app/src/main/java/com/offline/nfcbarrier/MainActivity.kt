package com.offline.nfcbarrier

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import android.view.inputmethod.InputMethodManager
import kotlin.math.max

class MainActivity : Activity() {
    private lateinit var barrierStatus: TextView
    private lateinit var protectedCount: TextView
    private lateinit var appsTitle: TextView
    private lateinit var searchBackButton: ImageButton
    private lateinit var appList: LinearLayout
    private lateinit var search: EditText
    private lateinit var durationRow: LinearLayout
    private lateinit var overviewSection: LinearLayout
    private var apps: List<ResolveInfo> = emptyList()

    private val selected: MutableSet<String>
        get() = BarrierPreferences.protectedPackages(this).toMutableSet()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        apps = loadLaunchableApps()
        buildUi()
    }

    override fun onResume() {
        super.onResume()
        updateBarrierStatus()
        renderApps(search.text?.toString().orEmpty())
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(BG)
            setPadding(dp(20), dp(16), dp(20), dp(12))
        }
        applySystemAndKeyboardInsets(root)
        overviewSection = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        val mainHeader = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        mainHeader.addView(label("Offline", 36f, INK).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }, LinearLayout.LayoutParams(0, -2, 1f))
        barrierStatus = label("Comprobando…", 13f, MUTED).apply {
            gravity = Gravity.CENTER
            maxLines = 1
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setPadding(dp(12), dp(8), dp(12), dp(8))
            setOnClickListener {
                if (!isAccessibilityServiceEnabled()) {
                    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                }
            }
        }
        mainHeader.addView(barrierStatus)
        overviewSection.addView(mainHeader)
        overviewSection.addView(label("Una pausa física antes de entrar en piloto automático.", 16f, MUTED).apply {
            setPadding(0, dp(4), 0, dp(24))
        })

        overviewSection.addView(sectionTitle("Duración de cada acceso"))
        overviewSection.addView(label("Después de detectar NFC, estas apps quedan disponibles durante:", 14f, MUTED).apply {
            setPadding(0, dp(4), 0, dp(12))
        })
        durationRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        overviewSection.addView(durationRow, LinearLayout.LayoutParams(-1, dp(44)).apply { bottomMargin = dp(24) })
        renderDurationOptions()
        root.addView(overviewSection)

        val appsHeader = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        searchBackButton = ImageButton(this).apply {
            setImageResource(R.drawable.ic_arrow_back)
            contentDescription = "Volver a la pantalla principal"
            background = borderlessSelectableBackground()
            setPadding(dp(10), dp(10), dp(10), dp(10))
            visibility = View.GONE
            setOnClickListener { exitSearchMode() }
        }
        appsHeader.addView(searchBackButton, LinearLayout.LayoutParams(dp(44), dp(44)).apply { marginEnd = dp(10) })
        appsTitle = sectionTitle("Aplicaciones")
        appsHeader.addView(appsTitle, LinearLayout.LayoutParams(0, -2, 1f))
        protectedCount = label("", 13f, MUTED).apply {
            gravity = Gravity.CENTER
            setPadding(dp(10), dp(5), dp(10), dp(5))
            background = rounded(SURFACE_ALT, 20f)
        }
        appsHeader.addView(protectedCount)
        root.addView(appsHeader)

        search = EditText(this).apply {
            hint = "Buscar una aplicación"
            textSize = 15f
            setTextColor(INK)
            setHintTextColor(MUTED)
            setSingleLine(true)
            background = rounded(SURFACE, 15f, BORDER)
            setPadding(dp(16), 0, dp(16), 0)
            setOnFocusChangeListener { view, focused ->
                setSearchMode(focused)
                if (focused) view.postDelayed({
                    view.requestRectangleOnScreen(android.graphics.Rect(0, 0, view.width, view.height), true)
                }, 250)
            }
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = renderApps(s?.toString().orEmpty())
                override fun afterTextChanged(s: Editable?) = Unit
            })
        }
        root.addView(search, LinearLayout.LayoutParams(-1, dp(50)).apply {
            topMargin = dp(12)
            bottomMargin = dp(10)
        })

        appList = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, dp(24))
        }
        root.addView(ScrollView(this).apply {
            isFillViewport = true
            clipToPadding = false
            addView(appList)
        }, LinearLayout.LayoutParams(-1, 0, 1f))

        setContentView(root)
        renderApps("")
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if (::search.isInitialized && search.hasFocus()) {
            exitSearchMode()
        } else {
            super.onBackPressed()
        }
    }

    private fun setSearchMode(searching: Boolean) {
        overviewSection.visibility = if (searching) View.GONE else View.VISIBLE
        searchBackButton.visibility = if (searching) View.VISIBLE else View.GONE
        appsTitle.text = if (searching) "Buscar aplicaciones" else "Aplicaciones"
    }

    private fun exitSearchMode() {
        search.setText("")
        search.clearFocus()
        getSystemService(InputMethodManager::class.java)
            .hideSoftInputFromWindow(search.windowToken, 0)
        setSearchMode(false)
    }

    private fun applySystemAndKeyboardInsets(root: View) {
        val horizontal = dp(20)
        val minimumTop = dp(16)
        val minimumBottom = dp(12)
        root.setOnApplyWindowInsetsListener { view, insets ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val systemBars = insets.getInsets(WindowInsets.Type.systemBars())
                val keyboard = insets.getInsets(WindowInsets.Type.ime())
                view.setPadding(
                    horizontal + systemBars.left,
                    minimumTop + systemBars.top,
                    horizontal + systemBars.right,
                    minimumBottom + max(systemBars.bottom, keyboard.bottom)
                )
            } else {
                @Suppress("DEPRECATION")
                view.setPadding(
                    horizontal + insets.systemWindowInsetLeft,
                    minimumTop + insets.systemWindowInsetTop,
                    horizontal + insets.systemWindowInsetRight,
                    minimumBottom + insets.systemWindowInsetBottom
                )
            }
            insets
        }
        root.requestApplyInsets()
    }

    private fun updateBarrierStatus() {
        val active = isAccessibilityServiceEnabled()
        barrierStatus.text = if (active) "Barrera activa" else "Barrera inactiva"
        barrierStatus.setTextColor(if (active) ACCENT else WARNING)
        barrierStatus.background = rounded(if (active) ACTIVE_SURFACE else WARNING_SURFACE, 20f)
        barrierStatus.isClickable = !active
        barrierStatus.isFocusable = !active
    }

    private fun renderDurationOptions() {
        durationRow.removeAllViews()
        val current = (BarrierPreferences.unlockDurationMillis(this) / 60_000L).toInt()
        listOf(5, 10, 20, 30).forEachIndexed { index, minutes ->
            val active = minutes == current
            durationRow.addView(Button(this).apply {
                text = "$minutes min"
                isAllCaps = false
                textSize = 13f
                minHeight = 0
                minimumHeight = 0
                setPadding(0, 0, 0, 0)
                setTextColor(if (active) Color.WHITE else INK)
                background = rounded(if (active) ACCENT else SURFACE, 14f, if (active) null else BORDER)
                setOnClickListener {
                    BarrierPreferences.setUnlockMinutes(this@MainActivity, minutes)
                    renderDurationOptions()
                }
            }, LinearLayout.LayoutParams(0, -1, 1f).apply {
                if (index > 0) marginStart = dp(8)
            })
        }
    }

    private fun renderApps(query: String) {
        if (!::appList.isInitialized) return
        appList.removeAllViews()
        val selectedPackages = selected
        protectedCount.text = "${selectedPackages.size} protegidas"
        val filtered = apps.filter {
            it.loadLabel(packageManager).toString().contains(query.trim(), ignoreCase = true)
        }
        if (filtered.isEmpty()) {
            appList.addView(label("No encontramos aplicaciones con ese nombre.", 15f, MUTED).apply {
                gravity = Gravity.CENTER
                setPadding(dp(16), dp(32), dp(16), dp(32))
            })
            return
        }
        filtered.forEach { info -> appList.addView(appRow(info, selectedPackages)) }
    }

    private fun appRow(info: ResolveInfo, selectedPackages: MutableSet<String>): View {
        val appPackage = info.activityInfo.packageName
        val checked = appPackage in selectedPackages
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(11), dp(10), dp(11))
            background = rounded(if (checked) ACTIVE_SURFACE else SURFACE, 18f, if (checked) ACCENT_SOFT else BORDER)

            addView(ImageView(this@MainActivity).apply {
                setImageDrawable(info.loadIcon(packageManager))
                contentDescription = null
            }, LinearLayout.LayoutParams(dp(42), dp(42)).apply { marginEnd = dp(13) })

            val copy = LinearLayout(this@MainActivity).apply { orientation = LinearLayout.VERTICAL }
            copy.addView(label(info.loadLabel(packageManager).toString(), 16f, INK).apply {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                maxLines = 1
            })
            copy.addView(label(if (checked) "Pausa NFC activada" else "Sin barrera", 13f, if (checked) ACCENT else MUTED))
            addView(copy, LinearLayout.LayoutParams(0, -2, 1f))

            addView(Switch(this@MainActivity).apply {
                isChecked = checked
                contentDescription = "Proteger ${info.loadLabel(packageManager)}"
                setOnCheckedChangeListener { _, enabled ->
                    val updated = BarrierPreferences.protectedPackages(this@MainActivity).toMutableSet()
                    if (enabled) updated += appPackage else updated -= appPackage
                    BarrierPreferences.setProtectedPackages(this@MainActivity, updated)
                    renderApps(search.text?.toString().orEmpty())
                }
            })
        }.also {
            it.layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(9) }
        }
    }

    private fun loadLaunchableApps(): List<ResolveInfo> = packageManager.queryIntentActivities(
        Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),
        PackageManager.MATCH_ALL
    ).filter { it.activityInfo.packageName != packageName }
        .distinctBy { it.activityInfo.packageName }
        .sortedBy { it.loadLabel(packageManager).toString().lowercase() }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val expected = ComponentName(this, AppBarrierAccessibilityService::class.java)
        val enabled = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
            ?: return false
        return enabled.split(':').mapNotNull(ComponentName::unflattenFromString).any { it == expected }
    }

    private fun sectionTitle(text: String) = label(text, 20f, INK).apply {
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private fun label(value: String, size: Float, color: Int) = TextView(this).apply {
        text = value
        textSize = size
        setTextColor(color)
        includeFontPadding = false
    }

    private fun rounded(fill: Int, radius: Float, stroke: Int? = null) = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        setColor(fill)
        cornerRadius = dp(radius.toInt()).toFloat()
        if (stroke != null) setStroke(dp(1), stroke)
    }

    private fun circle(fill: Int) = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(fill)
    }

    private fun borderlessSelectableBackground(): android.graphics.drawable.Drawable? {
        val value = android.util.TypedValue()
        theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, value, true)
        return if (value.resourceId != 0) getDrawable(value.resourceId) else null
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    companion object {
        private val BG = Color.rgb(246, 246, 242)
        private val SURFACE = Color.WHITE
        private val SURFACE_ALT = Color.rgb(236, 237, 231)
        private val ACTIVE_SURFACE = Color.rgb(232, 241, 235)
        private val WARNING_SURFACE = Color.rgb(249, 239, 224)
        private val INK = Color.rgb(28, 34, 31)
        private val MUTED = Color.rgb(101, 109, 104)
        private val ACCENT = Color.rgb(42, 104, 76)
        private val ACCENT_SOFT = Color.rgb(157, 198, 175)
        private val SUCCESS = Color.rgb(37, 148, 91)
        private val WARNING = Color.rgb(207, 130, 36)
        private val BORDER = Color.rgb(222, 225, 219)
    }
}
