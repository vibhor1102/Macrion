/*
 * Copyright (C) 2023 Kevin Buzeau
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
package io.github.vibhor1102.macrion.feature.smart.config.ui.action.intent.component

import android.content.ComponentName
import android.view.LayoutInflater
import android.view.ViewGroup

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DividerItemDecoration

import io.github.vibhor1102.macrion.core.android.application.AndroidApplicationInfo
import io.github.vibhor1102.macrion.core.ui.bindings.lists.updateState
import io.github.vibhor1102.macrion.core.common.overlays.base.viewModels
import io.github.vibhor1102.macrion.core.common.overlays.dialog.OverlayDialog
import io.github.vibhor1102.macrion.feature.smart.config.R
import io.github.vibhor1102.macrion.feature.smart.config.databinding.DialogBaseListBinding
import io.github.vibhor1102.macrion.feature.smart.config.di.ScenarioConfigViewModelsEntryPoint

import com.google.android.material.bottomsheet.BottomSheetDialog

import kotlinx.coroutines.launch
import io.github.vibhor1102.macrion.core.common.tutorial.domain.model.monitoring.MonitoredOverlayType

/**
 * [OverlayDialog] implementation for displaying a list of Android activities.
 *
 * @param onApplicationSelected called when the user clicks on an application.
 */
class ComponentSelectionDialog(
    private val onApplicationSelected: (ComponentName) -> Unit,
) : OverlayDialog(R.style.ScenarioConfigTheme) {

    override fun tutorialMonitoringTag(): String = MonitoredOverlayType.COMPONENT_SELECTION.name

    /** The view model for this dialog. */
    private val viewModel: ComponentSelectionModel by viewModels(
        entryPoint = ScenarioConfigViewModelsEntryPoint::class.java,
        creator = { componentSelectionViewModel() },
    )
    /** ViewBinding containing the views for this dialog. */
    private lateinit var viewBinding: DialogBaseListBinding

    /** Handle the binding between the application list and the views displaying them. */
    private lateinit var activitiesAdapter: ComponentSelectionAdapter

    override fun onCreateView(): ViewGroup {
        viewBinding = DialogBaseListBinding.inflate(LayoutInflater.from(context)).apply {
            layoutTopBar.apply {
                dialogTitle.setText(R.string.dialog_title_intent_component_name)
                buttonDismiss.setDebouncedOnClickListener { back() }
            }

            activitiesAdapter = ComponentSelectionAdapter { selectedComponentName ->
                debounceUserInteraction {
                    onApplicationSelected(selectedComponentName)
                    back()
                }
            }

            layoutLoadableList.list.apply {
                adapter = activitiesAdapter
                addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            }
        }

        return viewBinding.root
    }

    override fun onDialogCreated(dialog: BottomSheetDialog) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.activities.collect(::updateActivityList)
            }
        }
    }

    private fun updateActivityList(activities: List<AndroidApplicationInfo>) {
        viewBinding.layoutLoadableList.updateState(activities)
        activitiesAdapter.submitList(activities)
    }
}
