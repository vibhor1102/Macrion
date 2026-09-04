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
package io.github.vibhor1102.macrion.feature.smart.config.ui.event

import android.graphics.Bitmap
import android.view.ViewGroup
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder

import io.github.vibhor1102.macrion.core.domain.model.condition.ScreenCondition
import io.github.vibhor1102.macrion.core.ui.compose.MacrionTheme
import io.github.vibhor1102.macrion.feature.smart.config.R
import io.github.vibhor1102.macrion.feature.smart.config.ui.common.formatters.toEffectDescription
import io.github.vibhor1102.macrion.feature.smart.config.ui.common.formatters.toNaturalDisplayString
import io.github.vibhor1102.macrion.feature.smart.config.ui.common.model.condition.UiScreenCondition

import kotlinx.coroutines.Job


internal class EventImageConditionsAdapter(
    private val itemClickedListener: (index: Int) -> Unit,
    private val bitmapProvider: (ScreenCondition.Image, onBitmapLoaded: (Bitmap?) -> Unit) -> Job?,
) : ListAdapter<UiScreenCondition, EventImageConditionViewHolder>(ImageConditionDiffUtilCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventImageConditionViewHolder =
        EventImageConditionViewHolder(
            parent,
            bitmapProvider,
            itemClickedListener,
        )

    override fun onBindViewHolder(holder: EventImageConditionViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    override fun onViewRecycled(holder: EventImageConditionViewHolder) {
        holder.onUnbind()
    }
}

internal class EventImageConditionViewHolder (
    parent: ViewGroup,
    private val bitmapProvider: (ScreenCondition.Image, onBitmapLoaded: (Bitmap?) -> Unit) -> Job?,
    private val itemClickedListener: (index: Int) -> Unit,
): ViewHolder(ComposeView(parent.context)) {
    private var itemState by mutableStateOf<UiScreenCondition?>(null)

    /** Job for the loading of the condition bitmap. Null until bound. */
    private var bitmapLoadingJob: Job? = null

    init {
        (itemView as ComposeView).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
            setContent {
                MacrionTheme {
                    Box(Modifier.size(108.dp).padding(horizontal = 4.dp, vertical = 4.dp)) {
                        OutlinedCard(
                            modifier = Modifier.fillMaxSize(),
                            elevation = CardDefaults.outlinedCardElevation(defaultElevation = 2.dp),
                            onClick = {
                                val position = bindingAdapterPosition
                                if (position != androidx.recyclerview.widget.RecyclerView.NO_POSITION) {
                                    itemClickedListener(position)
                                }
                            },
                        ) {
                            itemState?.let { condition -> ScreenConditionCard(condition) }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun ScreenConditionCard(uiCondition: UiScreenCondition) {
        var bitmap by androidx.compose.runtime.remember(uiCondition.condition.id) { mutableStateOf<Bitmap?>(null) }
        var bitmapFailed by androidx.compose.runtime.remember(uiCondition.condition.id) { mutableStateOf(false) }
        val imageCondition = uiCondition.condition as? ScreenCondition.Image
        DisposableEffect(imageCondition) {
            bitmapLoadingJob?.cancel()
            bitmapLoadingJob = imageCondition?.let { image ->
                bitmapProvider(image) { loaded ->
                    bitmap = loaded
                    bitmapFailed = loaded == null
                }
            }
            onDispose {
                bitmapLoadingJob?.cancel()
                bitmapLoadingJob = null
            }
        }

        Column(Modifier.fillMaxSize()) {
            Preview(uiCondition, bitmap, bitmapFailed, Modifier.weight(1f))
            HorizontalDivider()
            Text(
                text = uiCondition.name,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                modifier = Modifier.fillMaxWidth().height(20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Icon(painterResource(uiCondition.shouldBeVisibleIconRes), null, Modifier.height(16.dp))
                }
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    if (imageCondition != null) {
                        Icon(painterResource(uiCondition.detectionTypeIconRes), null, Modifier.height(16.dp))
                    }
                }
                Text(
                    uiCondition.thresholdText,
                    Modifier.weight(1f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }
        }
    }

    @Composable
    private fun Preview(
        uiCondition: UiScreenCondition,
        bitmap: Bitmap?,
        bitmapFailed: Boolean,
        modifier: Modifier,
    ) {
        Box(
            modifier.fillMaxWidth().padding(vertical = 2.dp).clipToBounds()
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            when (val condition = uiCondition.condition) {
                is ScreenCondition.Color -> ColorIndicator(condition.color)
                is ScreenCondition.Image -> when {
                    bitmap != null -> Image(
                        bitmap!!.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                    )
                    bitmapFailed -> Icon(
                        painterResource(R.drawable.ic_cancel),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
                is ScreenCondition.Number -> Text(
                    condition.comparisonOperation.toEffectDescription(
                        itemView.context,
                        operand = condition.counterValue.toNaturalDisplayString(),
                    ),
                    modifier = Modifier.padding(horizontal = 4.dp),
                    textAlign = TextAlign.Center,
                )
                is ScreenCondition.Text -> Text(
                    condition.text,
                    modifier = Modifier.padding(horizontal = 4.dp),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }

    @Composable
    private fun ColorIndicator(color: Int) {
        val border = MaterialTheme.colorScheme.onSurfaceVariant
        Canvas(Modifier.size(48.dp)) {
            drawCircle(Color(color), radius = 20.dp.toPx(), center = center)
            drawCircle(border, radius = 22.dp.toPx(), center = center, style = Stroke(4.dp.toPx()))
        }
    }

    fun onBind(uiCondition: UiScreenCondition) {
        bitmapLoadingJob?.cancel()
        bitmapLoadingJob = null
        itemState = uiCondition
    }

    fun onUnbind() {
        bitmapLoadingJob?.cancel()
        bitmapLoadingJob = null
        itemState = null
    }
}

internal object ImageConditionDiffUtilCallback: DiffUtil.ItemCallback<UiScreenCondition>() {
    override fun areItemsTheSame(oldItem: UiScreenCondition, newItem: UiScreenCondition): Boolean =
        oldItem.condition.id == newItem.condition.id
    override fun areContentsTheSame(oldItem: UiScreenCondition, newItem: UiScreenCondition): Boolean =
        oldItem == newItem
}
