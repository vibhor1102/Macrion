/* Copyright (C) 2024 Kevin Buzeau — GPLv3 */
package io.github.vibhor1102.macrion.feature.smart.config.ui.action.brief

import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import io.github.vibhor1102.macrion.core.common.overlays.menu.implementation.brief.ItemBrief
import io.github.vibhor1102.macrion.core.common.overlays.menu.implementation.brief.ItemBriefViewHolder
import io.github.vibhor1102.macrion.core.ui.compose.MacrionTheme
import io.github.vibhor1102.macrion.feature.smart.config.ui.common.compose.ComposeItemBriefBinding
import io.github.vibhor1102.macrion.feature.smart.config.ui.common.model.action.UiAction

class SmartActionBriefViewHolder(
    @Suppress("UNUSED_PARAMETER") layoutInflater: LayoutInflater,
    orientation: Int,
    parent: ViewGroup,
) : ItemBriefViewHolder<ComposeItemBriefBinding>(ComposeItemBriefBinding(ComposeView(parent.context))) {
    private var itemState by mutableStateOf<ItemBrief?>(null)
    private var clickListener: ((Int, ItemBrief) -> Unit)? = null
    private val portrait = orientation == Configuration.ORIENTATION_PORTRAIT

    init {
        viewBinding.composeView.apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
            setContent { MacrionTheme { itemState?.let { ActionBriefCard(it) } } }
        }
    }

    override fun onBind(item: ItemBrief, itemClickedListener: (Int, ItemBrief) -> Unit) {
        clickListener = itemClickedListener
        itemState = item
    }

    @Composable
    private fun ActionBriefCard(item: ItemBrief) {
        val details = item.data as UiAction
        Box(
            Modifier.fillMaxSize().then(if (portrait) Modifier.padding(horizontal = 16.dp) else Modifier.padding(vertical = 16.dp)),
            contentAlignment = if (portrait) Alignment.BottomCenter else Alignment.CenterStart,
        ) {
            ElevatedCard(
                Modifier.then(if (portrait) Modifier.fillMaxWidth().height(80.dp) else Modifier.width(124.dp).fillMaxHeight())
                    .clickable {
                        val position = bindingAdapterPosition
                        if (position != androidx.recyclerview.widget.RecyclerView.NO_POSITION) clickListener?.invoke(position, item)
                    },
            ) {
                if (portrait) ActionPortrait(details) else ActionLandscape(details)
            }
        }
    }

    @Composable
    private fun ActionPortrait(details: UiAction) {
        Row(Modifier.fillMaxSize().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f).padding(start = 16.dp)) {
                BriefText(details.name, 17, true, 1)
                BriefText(details.description, 14, false, 1)
            }
            BriefIcon(details, Modifier.padding(end = 16.dp))
        }
    }

    @Composable
    private fun ActionLandscape(details: UiAction) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 4.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            BriefText(details.name, 17, true, 2, TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            BriefText(details.description, 14, false, 2, TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            BriefIcon(details)
        }
    }

    @Composable
    private fun BriefText(text: String, size: Int, title: Boolean, lines: Int, align: TextAlign? = null) {
        Text(
            text = text,
            fontSize = size.sp,
            fontWeight = if (title) FontWeight.Bold else FontWeight.Normal,
            fontStyle = if (title) FontStyle.Normal else FontStyle.Italic,
            maxLines = lines,
            overflow = TextOverflow.Ellipsis,
            textAlign = align,
        )
    }

    @Composable
    private fun BriefIcon(details: UiAction, modifier: Modifier = Modifier) {
        Box(modifier.size(32.dp)) {
            AndroidView(
                factory = { ImageView(it).apply { scaleType = ImageView.ScaleType.FIT_CENTER } },
                update = { it.setImageResource(details.icon) },
                modifier = Modifier.fillMaxSize(),
            )
            if (details.haveError) Box(
                Modifier.align(Alignment.TopEnd).size(6.dp).background(MaterialTheme.colorScheme.error, CircleShape),
            )
        }
    }
}
