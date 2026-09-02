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
package io.github.vibhor1102.macrion.feature.smart.config.ui.action.brief

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

import io.github.vibhor1102.macrion.core.common.overlays.base.viewModels
import io.github.vibhor1102.macrion.core.common.overlays.dialog.OverlayDialog
import io.github.vibhor1102.macrion.core.common.overlays.menu.implementation.brief.ItemBrief
import io.github.vibhor1102.macrion.core.ui.compose.MacrionTheme
import io.github.vibhor1102.macrion.feature.smart.config.R
import io.github.vibhor1102.macrion.feature.smart.config.databinding.ItemSmartActionLegacyBinding
import io.github.vibhor1102.macrion.feature.smart.config.di.ScenarioConfigViewModelsEntryPoint
import io.github.vibhor1102.macrion.feature.smart.config.ui.action.selection.ActionTypeSelectionDialog
import io.github.vibhor1102.macrion.feature.smart.config.ui.common.model.action.UiAction

import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.Collections
import io.github.vibhor1102.macrion.core.common.tutorial.domain.model.monitoring.MonitoredOverlayType


class SmartActionsLegacyDialog : OverlayDialog(R.style.ScenarioConfigTheme) {

    override fun tutorialMonitoringTag(): String = MonitoredOverlayType.SMART_ACTIONS_LEGACY.name


    /** View model for this content. */
    private val viewModel: SmartActionsBriefViewModel by viewModels(
        entryPoint = ScenarioConfigViewModelsEntryPoint::class.java,
        creator = { smartActionsBriefViewModel() }
    )

    /** TouchHelper applied to [actionAdapter] allowing to drag and drop the items. */
    private val itemTouchHelper = ItemTouchHelper(ActionReorderTouchHelper())

    private lateinit var actionAdapter: ActionAdapter

    override fun onCreateView(): ViewGroup {
        actionAdapter = ActionAdapter(
            actionClickedListener = ::onActionClicked,
            actionReorderListener = viewModel::updateActionOrder,
        )
        return ComposeView(context).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent { MacrionTheme { this@SmartActionsLegacyDialog.Content() } }
        }
    }

    override fun onDialogCreated(dialog: BottomSheetDialog) = Unit

    private fun onCreateButtonClicked() {
        overlayManager.navigateTo(
            context = context,
            newOverlay = ActionTypeSelectionDialog(
                choices = viewModel.actionTypeChoices.value,
                onChoiceSelectedListener = { choiceClicked ->
                    showActionConfigDialog(viewModel, viewModel.createAction(context, choiceClicked))
                },
            ),
        )
    }

    private fun onCopyButtonClicked() {
        showActionCopyDialog(viewModel)
    }

    private fun onActionClicked(item: ItemBrief) {
        debounceUserInteraction { showActionConfigDialog(viewModel, (item.data as UiAction).action) }
    }

    @Composable private fun Content() {
        val canCopy = viewModel.canCopyActions.collectAsStateWithLifecycle(false).value
        val items = viewModel.actionBriefList.collectAsStateWithLifecycle(null).value
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surfaceContainerLowest) {
            Column {
                Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = ::back) { Icon(painterResource(R.drawable.ic_cancel), null) }
                    Text(context.getString(R.string.menu_item_title_actions), Modifier.weight(1f).padding(horizontal = 8.dp),
                        style = MaterialTheme.typography.titleLarge)
                }
                Box(Modifier.fillMaxWidth().weight(1f)) {
                    when {
                        items == null -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                        items.isEmpty() -> Column(Modifier.align(Alignment.Center).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(context.getString(R.string.message_empty_action_list_title), style = MaterialTheme.typography.headlineSmall)
                            Spacer(Modifier.height(8.dp))
                            Text(context.getString(R.string.message_empty_action_list_desc), style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        else -> AndroidView(factory = { ctx -> RecyclerView(ctx).apply {
                            layoutManager = LinearLayoutManager(ctx)
                            adapter = actionAdapter
                            addItemDecoration(DividerItemDecoration(ctx, DividerItemDecoration.VERTICAL))
                            itemTouchHelper.attachToRecyclerView(this)
                        } }, update = { actionAdapter.submitList(items) }, modifier = Modifier.fillMaxSize())
                    }
                    Column(Modifier.align(Alignment.BottomEnd).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (canCopy) FloatingActionButton(onClick = ::onCopyButtonClicked,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                            Icon(painterResource(R.drawable.ic_copy), context.getString(R.string.content_desc_copy_button))
                        }
                        FloatingActionButton(onClick = ::onCreateButtonClicked) {
                            Icon(painterResource(R.drawable.ic_add), context.getString(R.string.content_desc_add_button))
                        }
                    }
                }
            }
        }
    }
}

private class ActionAdapter(
    private val actionClickedListener: (ItemBrief) -> Unit,
    private val actionReorderListener: (List<ItemBrief>) -> Unit,
) : ListAdapter<ItemBrief, ActionItemBriefViewHolder>(ActionItemBriefDiffUtilCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActionItemBriefViewHolder =
        ActionItemBriefViewHolder(ItemSmartActionLegacyBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ActionItemBriefViewHolder, position: Int) {
        holder.onBind(getItem(position), actionClickedListener)
    }

    /**
     * Swap the position of two events in the list.
     *
     * @param from the position of the click to be moved.
     * @param to the new position of the click to be moved.
     */
    fun moveActions(from: Int, to: Int) {
        val newList = currentList.toMutableList()
        Collections.swap(newList, from, to)
        submitList(newList)
    }

    /** Notify for an item drag and drop completion. */
    fun notifyMoveFinished() {
        actionReorderListener(currentList)
    }
}

private object ActionItemBriefDiffUtilCallback: DiffUtil.ItemCallback<ItemBrief>() {
    override fun areItemsTheSame(
        oldItem: ItemBrief,
        newItem: ItemBrief,
    ): Boolean = oldItem.id == newItem.id

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(
        oldItem: ItemBrief,
        newItem: ItemBrief,
    ): Boolean = oldItem.data == newItem.data
}

private class ActionItemBriefViewHolder(
    private val viewBinding: ItemSmartActionLegacyBinding,
) : RecyclerView.ViewHolder(viewBinding.root) {

    fun onBind(item: ItemBrief, itemClickedListener: (ItemBrief) -> Unit) {
        viewBinding.apply {
            root.setOnClickListener { itemClickedListener(item) }

            val details = item.data as UiAction
            itemIcon.setImageResource(details.icon)
            itemName.text = details.name
            itemDescription.text = details.description
            errorBadge.visibility = if (details.haveError) View.VISIBLE else View.GONE
        }
    }
}

private class ActionReorderTouchHelper
    : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {

    /** Tells if the user is currently dragging an item. */
    private var isDragging: Boolean = false

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        isDragging = true

        (recyclerView.adapter as ActionAdapter).moveActions(
            viewHolder.bindingAdapterPosition,
            target.bindingAdapterPosition
        )
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // Nothing do to
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)

        if (isDragging) {
            (recyclerView.adapter as ActionAdapter).notifyMoveFinished()
            isDragging = false
        }
    }
}
