/* Copyright (C) 2026 Vibhor Goel */
package io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.details.condition.adapter

import android.graphics.Bitmap
import android.graphics.Color
import android.widget.ImageView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import io.github.vibhor1102.macrion.core.domain.model.condition.ScreenCondition
import io.github.vibhor1102.macrion.core.ui.utils.setColorIndicatorDrawable
import io.github.vibhor1102.macrion.feature.smart.debugging.R
import io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.details.condition.EventOccurrenceItem

internal data class ScreenConditionResultState(
    val item: EventOccurrenceItem.Screen,
    val bitmap: Bitmap? = null,
    val bitmapFailed: Boolean = false,
)

@Composable
internal fun ScreenConditionResultRow(state: ScreenConditionResultState) {
    val item = state.item
    val primary = MaterialTheme.colorScheme.onSurface
    val secondary = MaterialTheme.colorScheme.onSurfaceVariant
    ElevatedCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(100.dp).clipToBounds().background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                AndroidView(
                    factory = { context -> ImageView(context).apply { scaleType = ImageView.ScaleType.CENTER_CROP } },
                    modifier = Modifier.size(100.dp).clipToBounds(),
                    update = { image -> image.bindScreenCondition(state) },
                )
            }
            Column(Modifier.weight(1f).padding(start = 16.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        item.conditionName,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = primary,
                    )
                    Text(item.durationText, style = MaterialTheme.typography.bodySmall, color = secondary)
                }
                ResultLine(item.confidenceText, item.confidenceValid)
                ResultLine(
                    stringResource(R.string.item_event_occurrence_details_image_should_be_detected),
                    item.shouldDetectedValue,
                )
                ResultLine(
                    stringResource(R.string.item_event_occurrence_details_image_fulfilled),
                    item.isFulfilledValue,
                )
            }
        }
    }
}

@Composable
private fun ResultLine(text: String, valid: Boolean) {
    val primary = MaterialTheme.colorScheme.onSurface
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(text, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Image(
            painter = painterResource(if (valid) R.drawable.ic_debug_confirm else R.drawable.ic_debug_cancel),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            colorFilter = ColorFilter.tint(primary),
        )
    }
}

private fun ImageView.bindScreenCondition(state: ScreenConditionResultState) {
    setImageDrawable(null)
    when (val condition = state.item.condition) {
        is ScreenCondition.Color -> setColorIndicatorDrawable(condition.color)
        is ScreenCondition.Image -> when {
            state.bitmap != null -> setImageBitmap(state.bitmap)
            state.bitmapFailed -> setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_cancel)?.apply {
                setTint(Color.RED)
            })
        }
        is ScreenCondition.Number -> setImageResource(R.drawable.ic_number_condition)
        is ScreenCondition.Text -> setImageResource(R.drawable.ic_text_condition)
    }
}
