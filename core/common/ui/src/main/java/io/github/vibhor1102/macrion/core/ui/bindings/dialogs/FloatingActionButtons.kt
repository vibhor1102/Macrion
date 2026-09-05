package io.github.vibhor1102.macrion.core.ui.bindings.dialogs

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Badge
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import io.github.vibhor1102.macrion.core.ui.R
import io.github.vibhor1102.macrion.core.ui.compose.MacrionTheme

class FloatingActionButtonsView(context: Context) : LinearLayout(context) {
    val root: View get() = this
    val primary = ComposeView(context)
    val secondary = ComposeView(context)
    private val primaryIcon = mutableIntStateOf(R.drawable.ic_add)
    private val secondaryIcon = mutableIntStateOf(R.drawable.ic_copy)
    private val badgeText = mutableStateOf<String?>(null)
    private var onPrimary: () -> Unit = {}
    private var onSecondary: () -> Unit = {}

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL
        clipChildren = false
        clipToPadding = false
        translationZ = 100 * resources.displayMetrics.density
        addView(
            secondary,
            LayoutParams(40.dpPx, 40.dpPx).apply {
                gravity = Gravity.CENTER_HORIZONTAL
                bottomMargin = 16.dpPx
            },
        )
        addView(
            primary,
            LayoutParams(64.dpPx, 64.dpPx).apply { gravity = Gravity.CENTER_HORIZONTAL },
        )
        secondary.visibility = View.GONE
        secondary.setContent { MacrionTheme { SmallFloatingActionButton(onClick = { onSecondary() }) {
            Icon(painterResource(secondaryIcon.intValue), secondary.contentDescription?.toString())
        } } }
        primary.setContent { MacrionTheme { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            FloatingActionButton(onClick = { onPrimary() }, modifier = Modifier.size(56.dp)) {
                Icon(painterResource(primaryIcon.intValue), primary.contentDescription?.toString())
            }
            badgeText.value?.let { Badge(Modifier.align(Alignment.TopEnd).size(18.dp)) { Text(it) } }
        } } }
    }

    fun configure(@DrawableRes primaryIcon: Int, @DrawableRes secondaryIcon: Int, onPrimary: () -> Unit, onSecondary: () -> Unit) {
        this.primaryIcon.intValue = primaryIcon; this.secondaryIcon.intValue = secondaryIcon
        this.onPrimary = onPrimary; this.onSecondary = onSecondary
    }
    fun setSecondaryVisible(visible: Boolean) { secondary.visibility = if (visible) View.VISIBLE else View.GONE }
    fun setBadge(text: String?, description: CharSequence? = null) { badgeText.value = text; primary.contentDescription = description }
    private val Int.dpPx get() = (this * resources.displayMetrics.density).toInt()
}
