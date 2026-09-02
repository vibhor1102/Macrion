/* Copyright (C) 2026 Vibhor Goel */
package io.github.vibhor1102.macrion.feature.smart.config.ui.action.toggleevent

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import io.github.vibhor1102.macrion.feature.smart.config.R

@Composable
internal fun EventToggleButtons(checkedIndex: Int?, onCheckedChanged: (Int?) -> Unit) {
    SingleChoiceSegmentedButtonRow {
        listOf(R.drawable.ic_confirm, R.drawable.ic_invert, R.drawable.ic_cancel).forEachIndexed { index, icon ->
            SegmentedButton(
                selected = checkedIndex == index,
                onClick = { onCheckedChanged(if (checkedIndex == index) null else index) },
                shape = SegmentedButtonDefaults.itemShape(index, 3),
                icon = {},
                label = { Icon(painterResource(icon), null, Modifier.size(20.dp)) },
            )
        }
    }
}
