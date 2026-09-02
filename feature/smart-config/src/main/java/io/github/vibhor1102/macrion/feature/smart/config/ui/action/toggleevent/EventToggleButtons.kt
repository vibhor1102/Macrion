/* Copyright (C) 2026 Vibhor Goel */
package io.github.vibhor1102.macrion.feature.smart.config.ui.action.toggleevent

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import io.github.vibhor1102.macrion.feature.smart.config.R

@Composable
internal fun EventToggleButtons(checkedIndex: Int?, onCheckedChanged: (Int?) -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    Row(Modifier.height(32.dp).clip(shape).border(1.dp, MaterialTheme.colorScheme.outline, shape)) {
        listOf(R.drawable.ic_confirm, R.drawable.ic_invert, R.drawable.ic_cancel).forEachIndexed { index, icon ->
            if (index > 0) {
                Box(Modifier.width(1.dp).height(32.dp).background(MaterialTheme.colorScheme.outline))
            }
            val selected = checkedIndex == index
            Box(
                Modifier.width(44.dp).height(32.dp)
                    .background(if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface)
                    .clickable { onCheckedChanged(if (selected) null else index) },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painterResource(icon),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}
