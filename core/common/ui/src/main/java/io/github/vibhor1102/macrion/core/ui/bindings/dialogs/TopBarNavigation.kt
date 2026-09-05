package io.github.vibhor1102.macrion.core.ui.bindings.dialogs

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.vibhor1102.macrion.core.ui.R
import io.github.vibhor1102.macrion.core.ui.compose.MacrionTheme

class TopBarNavigationView(context: Context) : FrameLayout(context) {
    val root: View get() = this
    val buttonDismiss = ComposeView(context)
    val buttonDelete = ComposeView(context)
    val buttonSave = ComposeView(context)
    private val title = mutableStateOf("")
    private val states = DialogNavigationButton.entries.associateWith { mutableStateOf(TopBarButtonState(it == DialogNavigationButton.DISMISS)) }
    private val callbacks = mutableMapOf<DialogNavigationButton, () -> Unit>()

    init {
        elevation = 3 * resources.displayMetrics.density
        addView(ComposeView(context).apply { setContent { MacrionTheme {
            Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surfaceContainerLow) {
                Box(Modifier.fillMaxSize().padding(start = 64.dp, end = 112.dp), contentAlignment = Alignment.CenterStart) {
                    Text(title.value, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        } } }, LayoutParams(LayoutParams.MATCH_PARENT, resources.getDimensionPixelSize(R.dimen.dialog_top_bar_height)))
        addButton(buttonDismiss, Gravity.START or Gravity.CENTER_VERTICAL, 8, DialogNavigationButton.DISMISS, R.drawable.ic_cancel)
        addButton(buttonSave, Gravity.END or Gravity.CENTER_VERTICAL, 8, DialogNavigationButton.SAVE, R.drawable.ic_save_filled)
        addButton(buttonDelete, Gravity.END or Gravity.CENTER_VERTICAL, 64, DialogNavigationButton.DELETE, R.drawable.ic_delete)
    }

    fun setTitle(text: CharSequence) { title.value = text.toString() }
    fun setTitle(@StringRes text: Int) = setTitle(context.getText(text))
    fun setButtonEnabledState(type: DialogNavigationButton, enabled: Boolean) = update(type) { copy(enabled = enabled) }
    fun setButtonVisibility(type: DialogNavigationButton, visibility: Int) = update(type) { copy(visible = visibility == View.VISIBLE) }
    fun setButtonClickListener(type: DialogNavigationButton, callback: () -> Unit) { callbacks[type] = callback }

    private fun addButton(view: ComposeView, gravity: Int, marginDp: Int, type: DialogNavigationButton, icon: Int) {
        addView(view, LayoutParams(48.dpPx, 48.dpPx, gravity).apply { marginStart = marginDp.dpPx; marginEnd = marginDp.dpPx })
        view.setContent { MacrionTheme {
            val state = states.getValue(type).value
            if (state.visible) when (type) {
                DialogNavigationButton.DISMISS -> IconButton({ callbacks[type]?.invoke() }, enabled = state.enabled) {
                    Icon(painterResource(icon), null, tint = MaterialTheme.colorScheme.onSurface)
                }
                DialogNavigationButton.DELETE -> FilledTonalIconButton({ callbacks[type]?.invoke() }, enabled = state.enabled) { Icon(painterResource(icon), null) }
                DialogNavigationButton.SAVE -> FilledIconButton({ callbacks[type]?.invoke() }, enabled = state.enabled) { Icon(painterResource(icon), null) }
            }
        } }
    }

    private fun update(type: DialogNavigationButton, change: TopBarButtonState.() -> TopBarButtonState) {
        states.getValue(type).let { it.value = it.value.change() }
    }
    private val Int.dpPx get() = (this * resources.displayMetrics.density).toInt()
}

private data class TopBarButtonState(val visible: Boolean, val enabled: Boolean = true)
enum class DialogNavigationButton { DISMISS, DELETE, SAVE }
