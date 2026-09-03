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
package io.github.vibhor1102.macrion.feature.dumb.config.ui.brief

import android.content.res.Configuration
import android.view.ViewGroup

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.viewbinding.ViewBinding

import io.github.vibhor1102.macrion.core.common.overlays.menu.implementation.brief.ItemBrief
import io.github.vibhor1102.macrion.core.common.overlays.menu.implementation.brief.ItemBriefViewHolder
import io.github.vibhor1102.macrion.core.ui.compose.MacrionTheme
import io.github.vibhor1102.macrion.feature.dumb.config.ui.actions.ActionText
import io.github.vibhor1102.macrion.feature.dumb.config.ui.actions.copy.DumbActionDetails


class DumbActionBriefViewHolder(
    orientation: Int,
    parent: ViewGroup,
) : ItemBriefViewHolder<ComposeBriefBinding>(ComposeBriefBinding(parent)) {

    private val details = mutableStateOf<DumbActionDetails?>(null)
    private var boundItem: ItemBrief? = null
    private var itemClickedListener: ((Int, ItemBrief) -> Unit)? = null

    init {
        viewBinding.rootView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
            setContent {
                MacrionTheme {
                    details.value?.let { current ->
                        DumbActionBriefItem(current, orientation) {
                            boundItem?.let { item ->
                                itemClickedListener?.invoke(bindingAdapterPosition, item)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onBind(item: ItemBrief, itemClickedListener: (Int, ItemBrief) -> Unit) {
        boundItem = item
        this.itemClickedListener = itemClickedListener
        details.value = item.data as DumbActionDetails
    }
}

class ComposeBriefBinding(parent: ViewGroup) : ViewBinding {
    val rootView = ComposeView(parent.context).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
        )
    }

    override fun getRoot() = rootView
}

@Composable
private fun DumbActionBriefItem(details: DumbActionDetails, orientation: Int, onClick: () -> Unit) {
    if (orientation == Configuration.ORIENTATION_PORTRAIT) {
        Box(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            contentAlignment = Alignment.BottomCenter,
        ) {
            ElevatedCard(onClick = onClick, modifier = Modifier.fillMaxWidth().height(80.dp)) {
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ActionText(
                        details = details,
                        modifier = Modifier.weight(1f).padding(start = 16.dp),
                        horizontalAlignment = Alignment.Start,
                        titleMaxLines = 1,
                        textAlign = TextAlign.Start,
                    )
                    Image(
                        painter = painterResource(details.icon),
                        contentDescription = null,
                        modifier = Modifier.padding(end = 16.dp).size(32.dp),
                    )
                }
            }
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize().padding(vertical = 12.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            ElevatedCard(onClick = onClick, modifier = Modifier.width(124.dp).fillMaxHeight()) {
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    ActionText(
                        details = details,
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        titleMaxLines = 2,
                        textAlign = TextAlign.Center,
                    )
                    Image(
                        painter = painterResource(details.icon),
                        contentDescription = null,
                        modifier = Modifier.padding(bottom = 12.dp).size(32.dp),
                    )
                }
            }
        }
    }
}
