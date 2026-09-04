/* Copyright (C) 2024 Kevin Buzeau; Copyright (C) 2026 Vibhor Goel */
package io.github.vibhor1102.macrion.feature.smart.config.ui.event

import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.github.vibhor1102.macrion.core.common.overlays.base.viewModels
import io.github.vibhor1102.macrion.core.common.overlays.dialog.OverlayDialog
import io.github.vibhor1102.macrion.core.common.tutorial.domain.model.monitoring.MonitoredOverlayType
import io.github.vibhor1102.macrion.core.domain.model.AND
import io.github.vibhor1102.macrion.core.domain.model.OR
import io.github.vibhor1102.macrion.core.ui.bindings.dropdown.TimeUnitDropDownItem
import io.github.vibhor1102.macrion.core.ui.compose.MacrionTextField
import io.github.vibhor1102.macrion.core.ui.compose.MacrionTheme
import io.github.vibhor1102.macrion.feature.smart.config.R
import io.github.vibhor1102.macrion.feature.smart.config.di.ScenarioConfigViewModelsEntryPoint
import io.github.vibhor1102.macrion.feature.smart.config.ui.action.brief.SmartActionsBriefMenu
import io.github.vibhor1102.macrion.feature.smart.config.ui.action.brief.SmartActionsLegacyDialog
import io.github.vibhor1102.macrion.feature.smart.config.ui.common.compose.TutorialClickAnchor
import io.github.vibhor1102.macrion.feature.smart.config.ui.common.dialogs.showCloseWithoutSavingDialog
import io.github.vibhor1102.macrion.feature.smart.config.ui.common.dialogs.showDeleteEventWithAssociatedActionsDialog
import io.github.vibhor1102.macrion.feature.smart.config.ui.condition.screen.brief.ScreenConditionsBriefMenu
import io.github.vibhor1102.macrion.feature.smart.config.ui.condition.trigger.TriggerConditionListDialog
import io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.live.eventtry.TryEventOverlayMenu
import kotlinx.coroutines.launch

class EventDialog(private val onConfigComplete: () -> Unit, private val onDelete: () -> Unit,
    private val onDismiss: () -> Unit) : OverlayDialog(R.style.ScenarioConfigTheme) {
    override fun tutorialMonitoringTag(): String = MonitoredOverlayType.EVENT.name
    private val viewModel: EventDialogViewModel by viewModels(
        entryPoint = ScenarioConfigViewModelsEntryPoint::class.java, creator = { eventDialogViewModel() })
    private var conditionsAnchor: View? = null
    private var andAnchor: View? = null
    private var orAnchor: View? = null
    private var actionsAnchor: View? = null
    private var stateAnchor: View? = null
    private var saveAnchor: View? = null

    override fun onCreateView(): ViewGroup = ComposeView(context).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent { MacrionTheme { this@EventDialog.Content() } }
    }
    override fun onDialogCreated(dialog: BottomSheetDialog) {
        lifecycleScope.launch { repeatOnLifecycle(Lifecycle.State.CREATED) {
            viewModel.isEditingEvent.collect { if (!it) { Log.e(TAG, "Closing EventDialog because no event is edited"); finish() } }
        } }
    }
    override fun onStart() { super.onStart(); attachAnchors() }
    override fun onStop() { viewModel.detachMonitoredViews(); super.onStop() }
    private fun attachAnchors() { viewModel.monitorConditionsView(conditionsAnchor); viewModel.monitorOperatorAndView(andAnchor)
        viewModel.monitorOperatorOrView(orAnchor); viewModel.monitorActionsView(actionsAnchor)
        viewModel.monitorInitialStateView(stateAnchor); viewModel.monitorSaveView(saveAnchor) }

    @Composable private fun Content() {
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        val ui = state ?: return
        var name by rememberSaveable { mutableStateOf(ui.name.orEmpty()) }
        LaunchedEffect(ui.name) { if (name != ui.name) name = ui.name.orEmpty() }
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surfaceContainerLowest) {
            Column { TopBar(ui.canBeSaved)
                Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    MacrionTextField(name, { name = it; viewModel.setEventName(it) }, context.getString(R.string.generic_name),
                        isError = ui.nameError, maxLength = context.resources.getInteger(R.integer.name_max_length))
                    ConditionsCard(ui)
                    ActionsCard(ui.actionsItems)
                    StateCard(ui)
                    if (ui is EventDialogUiState.ScreenEvent) TestCard(ui.canTryEvent)
                }
            }
        }
    }

    @Composable private fun TopBar(enabled: Boolean) { Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = ::back) { Icon(painterResource(R.drawable.ic_cancel), null) }
        Text(context.getString(if (viewModel.isConfiguringScreenEvent()) R.string.dialog_title_image_event else R.string.dialog_title_trigger_event),
            Modifier.weight(1f).padding(horizontal = 8.dp), style = MaterialTheme.typography.titleLarge,
            maxLines = 1, overflow = TextOverflow.Clip)
        FilledTonalIconButton(onClick = ::delete) { Icon(painterResource(R.drawable.ic_delete), null) }
        Spacer(Modifier.width(8.dp)); Box {
            FilledIconButton(onClick = ::save, enabled = enabled) { Icon(painterResource(R.drawable.ic_save_filled), null) }
            TutorialClickAnchor({ saveAnchor = it; viewModel.monitorSaveView(it) }, ::save, enabled)
        }
    } }

    @Composable private fun ConditionsCard(ui: EventDialogUiState) {
        EventCard(context.getString(R.string.menu_item_title_conditions), colorResource(R.color.event_conditions_color)) {
            Box { when (ui) {
                    is EventDialogUiState.ScreenEvent -> ScreenConditionSelector(ui.imageConditionsItems)
                    is EventDialogUiState.TriggerEvent -> ChildrenSelector(ui.triggerConditionsItems, true, ::showConditions)
                }
                TutorialClickAnchor({ conditionsAnchor = it; viewModel.monitorConditionsView(it) }, ::showConditions) }
            HorizontalDivider()
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f).padding(end = 16.dp)) { Text(context.getString(R.string.field_operator_title), style = MaterialTheme.typography.bodyLarge)
                    Text(context.getString(if (ui.conditionOperator == AND) R.string.field_operator_desc_and else R.string.field_operator_desc_or),
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                VerticalDivider(Modifier.height(48.dp))
                Spacer(Modifier.width(12.dp))
                OperatorButtons(ui.conditionOperator)
            }
        }
    }

    @Composable private fun ActionsCard(items: List<EventChildrenItem>) = EventCard(
        context.getString(R.string.menu_item_title_actions), colorResource(R.color.event_actions_color)) {
        Box { ChildrenSelector(items, false, ::showActionsOverlay)
            TutorialClickAnchor({ actionsAnchor = it; viewModel.monitorActionsView(it) }, { showActionsOverlay() }) }
    }

    @Composable private fun EventCard(title: String, accent: Color, content: @Composable ColumnScope.() -> Unit) {
        ElevatedCard(Modifier.fillMaxWidth().border(2.dp, accent, RoundedCornerShape(12.dp))) {
            Text(title, Modifier.fillMaxWidth().background(accent).padding(horizontal = 16.dp, vertical = 6.dp),
                style = MaterialTheme.typography.bodyLarge, color = Color.White, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), content = content)
        }
    }

    @Composable private fun ScreenConditionSelector(items: List<io.github.vibhor1102.macrion.feature.smart.config.ui.common.model.condition.UiScreenCondition>) {
        if (items.isEmpty()) { EmptySelector(true, ::showConditions); return }
        Row(Modifier.fillMaxWidth().height(116.dp).clickable(onClick = ::showConditions), verticalAlignment = Alignment.CenterVertically) {
            AndroidView(factory = { ctx -> RecyclerView(ctx).apply {
                layoutManager = LinearLayoutManager(ctx, RecyclerView.HORIZONTAL, false)
                adapter = EventImageConditionsAdapter(::showImageConditionsBriefMenu, viewModel::getConditionBitmap)
                overScrollMode = View.OVER_SCROLL_NEVER
            } }, update = { (it.adapter as EventImageConditionsAdapter).submitList(items) }, modifier = Modifier.weight(1f).fillMaxHeight())
            Icon(painterResource(R.drawable.ic_chevron_right), null)
        }
    }

    @Composable private fun ChildrenSelector(items: List<EventChildrenItem>, conditions: Boolean, onClick: () -> Unit) {
        if (items.isEmpty()) { EmptySelector(conditions, onClick); return }
        Row(Modifier.fillMaxWidth().heightIn(min = 62.dp).clickable(onClick = onClick).padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically) {
            AndroidView(factory = { ctx -> RecyclerView(ctx).apply {
                layoutManager = LinearLayoutManager(ctx, RecyclerView.HORIZONTAL, false)
                adapter = EventChildrenCardsAdapter { index -> if (conditions) showTriggerConditionsDialog() else showActionsOverlay(index) }
                overScrollMode = View.OVER_SCROLL_NEVER
            } }, update = { (it.adapter as EventChildrenCardsAdapter).submitList(items) }, modifier = Modifier.weight(1f).height(64.dp))
            Icon(painterResource(R.drawable.ic_chevron_right), null)
        }
    }

    @Composable private fun EmptySelector(conditions: Boolean, onClick: () -> Unit) {
        Row(Modifier.fillMaxWidth().heightIn(min = 62.dp).clickable(onClick = onClick).padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(context.getString(if (conditions) { if (viewModel.isConfiguringScreenEvent()) R.string.message_empty_screen_condition_list_title
                    else R.string.message_empty_trigger_condition_list_title } else R.string.message_empty_action_list_title),
                    style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
                Text(context.getString(if (conditions) { if (viewModel.isConfiguringScreenEvent()) R.string.message_empty_screen_condition_list_desc
                    else R.string.message_empty_trigger_condition_list_desc } else R.string.message_empty_action_list_desc),
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
            Icon(painterResource(R.drawable.ic_chevron_right), null)
        }
    }

    @Composable private fun OperatorButtons(selected: Int) { val shape = RoundedCornerShape(18.dp)
        Row(Modifier.height(32.dp).clip(shape).border(1.dp, MaterialTheme.colorScheme.outline, shape)) {
            listOf(AND to R.string.condition_operator_and, OR to R.string.condition_operator_or).forEachIndexed { index, pair ->
                if (index > 0) Box(Modifier.width(1.dp).fillMaxHeight().background(MaterialTheme.colorScheme.outline))
                Box(Modifier.width(48.dp).fillMaxHeight().background(if (selected == pair.first) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)
                    .clickable { viewModel.setConditionOperator(pair.first) }, contentAlignment = Alignment.Center) {
                    Text(stringResource(pair.second), style = MaterialTheme.typography.labelMedium)
                    TutorialClickAnchor({ view -> if (pair.first == AND) { andAnchor = view; viewModel.monitorOperatorAndView(view) }
                        else { orAnchor = view; viewModel.monitorOperatorOrView(view) } }, { viewModel.setConditionOperator(pair.first) })
                }
            }
        }
    }

    @Composable private fun StateCard(ui: EventDialogUiState) = EventCard(context.getString(R.string.menu_item_title_state),
        colorResource(R.color.event_state_color)) {
        Box { SwitchField(context.getString(R.string.field_event_state_title),
            context.getString(if (ui.enabledOnStart) R.string.field_event_state_desc_enabled else R.string.field_event_state_desc_disabled),
            ui.enabledOnStart, viewModel::toggleEventState)
            TutorialClickAnchor({ stateAnchor = it; viewModel.monitorInitialStateView(it) }, viewModel::toggleEventState) }
        if (ui is EventDialogUiState.ScreenEvent) { HorizontalDivider(); SwitchField(context.getString(R.string.field_event_keep_detecting_title),
            context.getString(if (ui.keepDetecting) R.string.field_event_keep_detecting_desc_enabled else R.string.field_event_keep_detecting_desc_disabled),
            ui.keepDetecting, viewModel::toggleKeepDetectingState); HorizontalDivider(); CooldownField(ui) }
    }

    @Composable private fun SwitchField(title: String, desc: String, checked: Boolean, toggle: () -> Unit) {
        Row(Modifier.fillMaxWidth().clickable(onClick = toggle).padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f).padding(end = 16.dp)) { Text(title, style = MaterialTheme.typography.bodyLarge); Text(desc,
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            VerticalDivider(Modifier.height(48.dp))
            Spacer(Modifier.width(12.dp))
            Switch(checked, { toggle() })
        }
    }

    @Composable private fun CooldownField(ui: EventDialogUiState.ScreenEvent) { var value by rememberSaveable { mutableStateOf(ui.cooldownValue) }
        LaunchedEffect(ui.cooldownValue) { if (value != ui.cooldownValue) value = ui.cooldownValue }
        SwitchField(context.getString(R.string.field_event_cooldown_title), context.getString(R.string.field_event_cooldown_desc),
            ui.cooldownEnabled, viewModel::toggleCooldownState)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            OutlinedTextField(value, { text -> val filtered = text.filter(Char::isDigit); value = filtered
                viewModel.setCooldownValue(filtered.toLongOrNull()) }, Modifier.weight(.7f), enabled = ui.cooldownEnabled,
                label = { Text(context.getString(R.string.field_event_cooldown_edit_label)) }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            Spacer(Modifier.width(12.dp)); TimeUnitDropdown(ui.cooldownUnit, ui.cooldownEnabled, Modifier.weight(.3f))
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable private fun TimeUnitDropdown(unit: TimeUnitDropDownItem, enabled: Boolean, modifier: Modifier) { var expanded by remember { mutableStateOf(false) }
        val units = listOf(TimeUnitDropDownItem.Milliseconds, TimeUnitDropDownItem.Seconds, TimeUnitDropDownItem.Minutes)
        ExposedDropdownMenuBox(expanded, { if (enabled) expanded = it }, modifier) {
            OutlinedTextField(stringResource(unit.title), {}, Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                enabled = enabled, readOnly = true, label = { Text(context.getString(R.string.dropdown_label_time_unit)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) })
            ExposedDropdownMenu(expanded, { expanded = false }) { units.forEach { item -> DropdownMenuItem({ Text(stringResource(item.title)) },
                { viewModel.setCooldownTimeUnit(item); expanded = false }) } }
        }
    }

    @Composable private fun TestCard(enabled: Boolean) {
        ElevatedCard(Modifier.fillMaxWidth().border(2.dp, colorResource(R.color.event_test_color), RoundedCornerShape(12.dp))) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(context.getString(R.string.item_title_try_element, context.getString(R.string.dialog_title_image_event)),
                    Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                FilledIconButton(::showTryElementMenu, enabled = enabled) {
                    Icon(painterResource(R.drawable.ic_play_arrow), null, Modifier.size(18.dp))
                }
            }
        }
    }

    override fun back() { if (viewModel.hasUnsavedModifications()) { context.showCloseWithoutSavingDialog { onDismiss(); super.back() }; return }
        onDismiss(); super.back() }
    private fun save() { onConfigComplete(); super.back() }
    private fun delete() { if (viewModel.isEventHaveRelatedActions()) context.showDeleteEventWithAssociatedActionsDialog { confirmDelete() } else confirmDelete() }
    private fun confirmDelete() { onDelete(); super.back() }
    private fun showConditions() { if (viewModel.isConfiguringScreenEvent()) showImageConditionsBriefMenu() else showTriggerConditionsDialog() }
    private fun showImageConditionsBriefMenu(index: Int = 0) = overlayManager.navigateTo(context, ScreenConditionsBriefMenu(index), true)
    private fun showTriggerConditionsDialog() = overlayManager.navigateTo(context, TriggerConditionListDialog())
    private fun showActionsOverlay(index: Int = 0) = overlayManager.navigateTo(context,
        if (viewModel.isLegacyActionUiEnabled()) SmartActionsLegacyDialog() else SmartActionsBriefMenu(index), true)
    private fun showTryElementMenu() { viewModel.getTryInfo()?.let { (scenario, event) ->
        overlayManager.navigateTo(context, TryEventOverlayMenu(scenario, event), true) } }
}
private const val TAG = "EventDialog"
