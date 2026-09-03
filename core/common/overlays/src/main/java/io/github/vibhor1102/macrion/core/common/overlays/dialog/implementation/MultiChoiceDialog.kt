/*
 * Copyright (C) 2024 Kevin Buzeau
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.vibhor1102.macrion.core.common.overlays.dialog.implementation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder

import io.github.vibhor1102.macrion.core.common.overlays.dialog.OverlayDialog
import io.github.vibhor1102.macrion.core.common.overlays.R
import io.github.vibhor1102.macrion.core.ui.compose.MacrionTheme
import io.github.vibhor1102.macrion.core.ui.databinding.IncludeDialogNavigationTopBarBinding

import com.google.android.material.bottomsheet.BottomSheetDialog

/**
 * [OverlayDialog] implementation for a dialog displaying a list of choices to the user.
 *
 * @param T the type of choices in the list. Must extends [DialogChoice].
 * @param theme the resource id of the theme to apply.
 * @param dialogTitleText the title of the dialog.
 * @param choices the choices to be displayed.
 * @param onChoiceSelected the callback to be notified upon user choice selection.
 */
open class MultiChoiceDialog<T : DialogChoice>(
    @StyleRes theme: Int,
    @field:StringRes private val dialogTitleText: Int,
    private val choices: List<T>,
    private val onChoiceSelected: (T) -> Unit,
    private val onCanceled: (() -> Unit)? = null,
) : OverlayDialog(theme) {

    private lateinit var list: RecyclerView
    /** The adapter displaying the choices. */
    protected lateinit var adapter: ChoiceAdapter<T>

    override fun onCreateView(): ViewGroup {
        val inflater = LayoutInflater.from(context)
        val topBarBinding = IncludeDialogNavigationTopBarBinding.inflate(inflater).apply {
            dialogTitle.setText(dialogTitleText)
            buttonDismiss.setDebouncedOnClickListener {
                onCanceled?.invoke()
                back()
            }
        }
        list = RecyclerView(context).apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            isVerticalScrollBarEnabled = true
        }

        adapter = ChoiceAdapter(
            choices = choices,
            onChoiceSelected = { choice ->
                debounceUserInteraction {
                    back()
                    onChoiceSelected(choice)
                }
            },
            onChoiceViewBound = ::onChoiceViewBound,
        )

        return ComposeView(context).apply {
            setContent {
                MacrionTheme {
                    ListDialogScaffold(
                        topBar = topBarBinding.root,
                        list = list,
                        enforceMinimumHeight = false,
                        listBottomPadding = false,
                    )
                }
            }
        }
    }

    override fun onDialogCreated(dialog: BottomSheetDialog) {
        list.adapter = adapter
    }

    open fun onChoiceViewBound(choice: T, view: View?) = Unit
}

/**
 * Adapter displaying the choices in the dialog.
 *
 * @param T the type of choices in the list.
 * @param choices the choices to be displayed in the list.
 * @param onChoiceSelected called when the user clicks on a choice.
 */
class ChoiceAdapter<T : DialogChoice>(
    private val choices: List<T>,
    private val onChoiceSelected: (T) -> Unit,
    private val onChoiceViewBound: ((T, View?) -> Unit),
): RecyclerView.Adapter<MultiChoiceViewHolder<T>>() {

    private companion object {
        const val TYPE_SMALL = 0
        const val TYPE_FULL = 1
    }

    override fun getItemCount(): Int = choices.size

    override fun getItemViewType(position: Int): Int {
        val item = choices[position]

        return when {
            item.description == null && item.iconId == null -> TYPE_SMALL
            else -> TYPE_FULL
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MultiChoiceViewHolder<T> =
        when (viewType) {
            TYPE_SMALL -> SmallChoiceViewHolder(parent)
            TYPE_FULL -> ChoiceViewHolder(parent)
            else -> throw IllegalArgumentException("Unsupported view type !")
        }

    override fun onBindViewHolder(holder: MultiChoiceViewHolder<T>, position: Int) {
        choices[position].let { choice ->
            holder.onBind(choice, onChoiceSelected)
            onChoiceViewBound(choice, holder.itemView)
        }
    }

    override fun onViewRecycled(holder: MultiChoiceViewHolder<T>) {
        super.onViewRecycled(holder)
        holder.boundChoice?.let { onChoiceViewBound(it, null) }
        holder.onUnbind()
    }
}

/**
 * Base view holder for a choice.
 * @param itemView the root view of the item.
 */
abstract class MultiChoiceViewHolder<T : DialogChoice>(parent: ViewGroup): ViewHolder(ComposeView(parent.context)) {

    private var choice by mutableStateOf<T?>(null)
    private var onChoiceSelected by mutableStateOf<((T) -> Unit)?>(null)
    var boundChoice: T? = null
        private set

    init {
        (itemView as ComposeView).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
            setContent {
                MacrionTheme {
                    choice?.let { value -> Content(value) { onChoiceSelected?.invoke(value) } }
                }
            }
        }
    }

    /**
     * Binds a choice to this view holder.
     * @param choice the choice object to be bound.
     * @param onChoiceSelected listener upon user click on the choice item.
     */
    fun onBind(choice: T, onChoiceSelected: (T) -> Unit) {
        boundChoice = choice
        this.choice = choice
        this.onChoiceSelected = onChoiceSelected
    }

    fun onUnbind() {
        boundChoice = null
        choice = null
        onChoiceSelected = null
    }

    @Composable protected abstract fun Content(choice: T, onClick: () -> Unit)
}

/**
 * View holder for a choice with an icon and a description.
 * @param holderViewBinding the view binding containing the holder root view.
 */
private class ChoiceViewHolder<T : DialogChoice>(parent: ViewGroup) : MultiChoiceViewHolder<T>(parent) {
    @Composable override fun Content(choice: T, onClick: () -> Unit) {
        val alpha = if (choice.enabled) ENABLED_ITEM_ALPHA else DISABLED_ITEM_ALPHA
        Row(
            Modifier.fillMaxWidth().height(78.dp).padding(horizontal = 16.dp, vertical = 8.dp).clickable(onClick = onClick),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            choice.iconId?.let {
                Icon(painterResource(it), null, Modifier.size(32.dp).alpha(alpha), tint = Color.Unspecified)
                Spacer(Modifier.width(8.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(choice.title),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
                    style = MaterialTheme.typography.bodyLarge,
                )
                choice.description?.let {
                    Text(
                        stringResource(it),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Icon(
                painterResource(if (choice.enabled) R.drawable.ic_chevron_right else choice.disabledIconId ?: R.drawable.ic_chevron_right),
                null,
            )
        }
    }
}

private const val ENABLED_ITEM_ALPHA = 1f
private const val DISABLED_ITEM_ALPHA = 0.5f

/**
 * View holder for a choice with only a title.
 * @param holderViewBinding the view binding containing the holder root view.
 */
private class SmallChoiceViewHolder<T : DialogChoice>(parent: ViewGroup) : MultiChoiceViewHolder<T>(parent) {
    @Composable override fun Content(choice: T, onClick: () -> Unit) {
        Row(
            Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 16.dp, vertical = 8.dp).clickable(onClick = onClick),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(choice.title),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
            )
            Icon(painterResource(R.drawable.ic_chevron_right), null)
        }
    }
}

/** Base class for a dialog choice. */
open class DialogChoice(
    @field:StringRes val title: Int,
    @field:StringRes val description: Int? = null,
    @field:DrawableRes val iconId: Int? = null,
    val enabled: Boolean = true,
    @field:DrawableRes val disabledIconId: Int? = null,
)
