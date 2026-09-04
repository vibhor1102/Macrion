/* Copyright (C) 2024 Kevin Buzeau; Copyright (C) 2026 Vibhor Goel */
package io.github.vibhor1102.macrion.feature.smart.config.ui.action.click

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.toPoint
import androidx.core.graphics.toPointF
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.github.vibhor1102.macrion.core.common.actions.GESTURE_DURATION_MAX_VALUE
import io.github.vibhor1102.macrion.core.common.overlays.base.viewModels
import io.github.vibhor1102.macrion.core.common.overlays.dialog.OverlayDialog
import io.github.vibhor1102.macrion.core.common.overlays.menu.implementation.PositionSelectorMenu
import io.github.vibhor1102.macrion.core.common.tutorial.domain.model.monitoring.MonitoredOverlayType
import io.github.vibhor1102.macrion.core.domain.model.action.Click
import io.github.vibhor1102.macrion.core.ui.compose.MacrionTextField
import io.github.vibhor1102.macrion.core.ui.compose.MacrionTheme
import io.github.vibhor1102.macrion.core.ui.views.itembrief.renderers.ClickDescription
import io.github.vibhor1102.macrion.feature.smart.config.R
import io.github.vibhor1102.macrion.feature.smart.config.di.ScenarioConfigViewModelsEntryPoint
import io.github.vibhor1102.macrion.feature.smart.config.ui.action.OnActionConfigCompleteListener
import io.github.vibhor1102.macrion.feature.smart.config.ui.action.click.offset.ClickOffsetDialog
import io.github.vibhor1102.macrion.feature.smart.config.ui.common.compose.TutorialClickAnchor
import io.github.vibhor1102.macrion.feature.smart.config.ui.common.dialogs.showCloseWithoutSavingDialog
import io.github.vibhor1102.macrion.feature.smart.config.ui.condition.screen.selection.ScreenConditionSelectionDialog
import kotlinx.coroutines.launch

class ClickDialog(private val listener: OnActionConfigCompleteListener) : OverlayDialog(R.style.ScenarioConfigTheme) {
    override fun tutorialMonitoringTag(): String = MonitoredOverlayType.CLICK.name
    private val viewModel: ClickViewModel by viewModels(
        entryPoint = ScenarioConfigViewModelsEntryPoint::class.java,
        creator = { clickViewModel() },
    )
    private var conditionTypeAnchor: View? = null
    private var selectorAnchor: View? = null
    private var saveAnchor: View? = null

    override fun onCreateView(): ViewGroup = ComposeView(context).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent { MacrionTheme { this@ClickDialog.Content() } }
    }
    override fun onDialogCreated(dialog: BottomSheetDialog) {
        lifecycleScope.launch { repeatOnLifecycle(Lifecycle.State.CREATED) {
            viewModel.isEditingAction.collect { if (!it) { Log.e(TAG, "Closing ClickDialog because there is no action edited"); finish() } }
        } }
    }
    override fun onStart() {
        super.onStart(); viewModel.monitorConditionTypeView(conditionTypeAnchor)
        viewModel.monitorSelectorView(selectorAnchor); viewModel.monitorSaveView(saveAnchor)
    }
    override fun onStop() { viewModel.detachMonitoredViews(); super.onStop() }

    @Composable private fun Content() {
        val ui by viewModel.uiState.collectAsStateWithLifecycle()
        val state = ui ?: return
        var name by rememberSaveable { mutableStateOf(state.name.orEmpty()) }
        var duration by rememberSaveable { mutableStateOf(state.pressDuration.orEmpty()) }
        LaunchedEffect(state.name) { if (state.name != name) name = state.name.orEmpty() }
        LaunchedEffect(state.pressDuration) { if (state.pressDuration != duration) duration = state.pressDuration.orEmpty() }
        Surface(Modifier.fillMaxWidth().heightIn(max = 600.dp), color = MaterialTheme.colorScheme.surfaceContainerLowest) {
            Column {
                TopBar(state.canBeSaved)
                Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    MacrionTextField(name, { name = it; viewModel.setName(it) }, context.getString(R.string.generic_name),
                        isError = state.nameError, maxLength = context.resources.getInteger(R.integer.name_max_length))
                    OutlinedTextField(duration, { input ->
                        val filtered = input.filter(Char::isDigit)
                        if (filtered.isEmpty() || (filtered.toLongOrNull() ?: Long.MAX_VALUE) <= GESTURE_DURATION_MAX_VALUE) {
                            duration = filtered; viewModel.setPressDuration(filtered.toLongOrNull())
                        }
                    }, Modifier.fillMaxWidth(), label = { Text(context.getString(R.string.input_field_label_click_press_duration)) },
                        isError = state.pressDurationError, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    state.positionState?.let { PositionCard(it) }
                }
            }
        }
    }

    @Composable private fun TopBar(saveEnabled: Boolean) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = ::back) { Icon(painterResource(R.drawable.ic_cancel), null) }
            Text(context.getString(R.string.dialog_title_click), Modifier.weight(1f).padding(horizontal = 8.dp),
                style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Clip)
            FilledTonalIconButton(onClick = ::delete) { Icon(painterResource(R.drawable.ic_delete), null) }
            Spacer(Modifier.width(8.dp))
            Box {
                FilledIconButton(onClick = ::save, enabled = saveEnabled) { Icon(painterResource(R.drawable.ic_save_filled), null) }
                TutorialClickAnchor({ saveAnchor = it; viewModel.monitorSaveView(it) }, ::save, saveEnabled)
            }
        }
    }

    @Composable private fun PositionCard(state: ClickPositionUiState) {
        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                if (state.isTypeFieldVisible) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(context.getString(R.string.field_click_type_title), style = MaterialTheme.typography.titleSmall)
                            Text(context.getString(if (state.positionType == Click.PositionType.USER_SELECTED)
                                R.string.field_click_type_desc_on_position else R.string.field_click_type_desc_on_condition),
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        PositionTypeButtons(state.positionType)
                    }
                    HorizontalDivider(Modifier.padding(top = 8.dp))
                }
                SelectorField(state)
                if (state.isClickOffsetVisible) {
                    HorizontalDivider()
                    SelectorRow(context.getString(R.string.field_click_offset_title), state.clickOffsetDescription,
                        state.isClickOffsetEnabled, false, null, ::showClickOffsetDialog)
                }
            }
        }
    }

    @Composable private fun PositionTypeButtons(positionType: Click.PositionType) {
        val shape = RoundedCornerShape(20.dp)
        Row(Modifier.height(32.dp).clip(shape).border(1.dp, MaterialTheme.colorScheme.outline, shape)) {
            listOf(Click.PositionType.USER_SELECTED to R.drawable.ic_click_on_condition,
                Click.PositionType.ON_DETECTED_CONDITION to R.drawable.ic_condition).forEachIndexed { index, item ->
                if (index > 0) Box(Modifier.width(1.dp).fillMaxHeight().background(MaterialTheme.colorScheme.outline))
                Box(Modifier.width(44.dp).fillMaxHeight().background(if (positionType == item.first)
                    MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)
                    .clickable { viewModel.setClickOnCondition(item.first) }, contentAlignment = Alignment.Center) {
                    Icon(painterResource(item.second), null, Modifier.size(18.dp))
                    if (item.first == Click.PositionType.ON_DETECTED_CONDITION) TutorialClickAnchor(
                        { conditionTypeAnchor = it; viewModel.monitorConditionTypeView(it) },
                        { viewModel.setClickOnCondition(Click.PositionType.ON_DETECTED_CONDITION) })
                }
            }
        }
    }

    @Composable private fun SelectorField(state: ClickPositionUiState) {
        Box {
            SelectorRow(state.selectorTitle, state.selectorDescription, state.isSelectorEnabled, state.isSelectorInError,
                state.selectorVisualization, if (state.positionType == Click.PositionType.USER_SELECTED) ::showPositionSelector else ::showConditionSelector)
            TutorialClickAnchor({ selectorAnchor = it; viewModel.monitorSelectorView(it) },
                if (state.positionType == Click.PositionType.USER_SELECTED) ::showPositionSelector else ::showConditionSelector,
                state.isSelectorEnabled)
        }
    }

    @Composable private fun SelectorRow(title: String, description: String?, enabled: Boolean, error: Boolean,
        visualization: Any?, onClick: () -> Unit) {
        val contentColor = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        Row(Modifier.fillMaxWidth().heightIn(min = 62.dp).clickable(enabled = enabled, onClick = onClick).padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically) {
            visualization?.let { value ->
                val bitmap = remember(value) { when (value) { is Bitmap -> value; is Drawable -> value.toBitmap(); else -> null } }
                bitmap?.let { Image(it.asImageBitmap(), null, Modifier.size(40.dp).padding(end = 8.dp), contentScale = ContentScale.Fit) }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = if (error) MaterialTheme.colorScheme.error else contentColor)
                description?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = if (error)
                    MaterialTheme.colorScheme.error else contentColor.copy(alpha = 0.75f)) }
            }
            Icon(painterResource(R.drawable.ic_chevron_right), null, tint = contentColor)
        }
    }

    override fun back() {
        if (viewModel.hasUnsavedModifications()) { context.showCloseWithoutSavingDialog { listener.onDismissClicked(); super.back() }; return }
        listener.onDismissClicked(); super.back()
    }
    private fun save() { viewModel.saveLastConfig(); listener.onConfirmClicked(); super.back() }
    private fun delete() { listener.onDeleteClicked(); super.back() }
    private fun showPositionSelector() { viewModel.getEditedClick()?.let { click -> overlayManager.navigateTo(context,
        PositionSelectorMenu(MonitoredOverlayType.CLICK_POSITION.name,
            ClickDescription(pressDurationMs = click.pressDuration ?: 1L, position = click.position?.toPointF()), { description ->
                (description as ClickDescription).position?.let { viewModel.setPosition(it.toPoint()) }
            }), true) } }
    private fun showConditionSelector() = overlayManager.navigateTo(context, ScreenConditionSelectionDialog(
        viewModel.uiState.value?.availableConditions ?: emptyList(), viewModel::setConditionToBeClicked), false)
    private fun showClickOffsetDialog() = overlayManager.navigateTo(context, ClickOffsetDialog(), false)
}

private const val TAG = "ClickDialog"
