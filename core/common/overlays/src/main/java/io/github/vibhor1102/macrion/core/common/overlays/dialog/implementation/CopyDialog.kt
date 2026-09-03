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
import android.view.ViewGroup

import androidx.annotation.StyleRes
import androidx.compose.ui.platform.ComposeView

import io.github.vibhor1102.macrion.core.common.overlays.dialog.OverlayDialog
import io.github.vibhor1102.macrion.core.ui.compose.MacrionTheme
import io.github.vibhor1102.macrion.core.ui.databinding.IncludeDialogSearchTopBarBinding
import io.github.vibhor1102.macrion.core.ui.databinding.IncludeLoadableListBinding
import io.github.vibhor1102.macrion.core.ui.bindings.lists.setEmptyText
import io.github.vibhor1102.macrion.core.ui.bindings.dialogs.setOnDismissClickedListener
import io.github.vibhor1102.macrion.core.ui.bindings.dialogs.setOnTextChangedListener
import io.github.vibhor1102.macrion.core.ui.bindings.dialogs.setup

abstract class CopyDialog(
    @StyleRes theme: Int,
) : OverlayDialog(theme) {

    /** List content retained as a RecyclerView for large copy sources. */
    protected lateinit var loadableListBinding: IncludeLoadableListBinding
    /** The resource id for the dialog title. */
    protected abstract val titleRes: Int
    /** The resource id for the search hint text. */
    protected abstract val searchHintRes: Int
    /** The resource id for the text displayed when there is nothing to copy. */
    protected abstract val emptyRes: Int

    final override fun onCreateView(): ViewGroup {
        val inflater = LayoutInflater.from(context)
        val topBarBinding = IncludeDialogSearchTopBarBinding.inflate(inflater).apply {
            setup(titleRes, searchHintRes)
            setOnDismissClickedListener { debounceUserInteraction { back() } }
            setOnTextChangedListener(::onSearchQueryChanged)
            buttonCopy.setOnClickListener { onCopyClicked() }
        }
        loadableListBinding = IncludeLoadableListBinding.inflate(inflater).apply {
            setEmptyText(emptyRes)
        }

        return ComposeView(context).apply {
            setContent {
                MacrionTheme {
                    ListDialogScaffold(
                        topBar = topBarBinding.root,
                        list = loadableListBinding.root,
                        enforceMinimumHeight = true,
                        listBottomPadding = true,
                    )
                }
            }
        }
    }

    abstract fun onSearchQueryChanged(newText: String?)
    abstract fun onCopyClicked()
}
