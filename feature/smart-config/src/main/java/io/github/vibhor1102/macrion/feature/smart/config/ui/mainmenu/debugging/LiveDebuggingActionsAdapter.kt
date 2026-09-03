/*
 * Copyright (C) 2026 Kevin Buzeau
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
package io.github.vibhor1102.macrion.feature.smart.config.ui.mainmenu.debugging

import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.github.vibhor1102.macrion.feature.smart.config.R

class LiveDebuggingActionsAdapter : ListAdapter<LiveDebuggingActionsItem, LiveActionViewHolder>(LiveActionDiffUtilCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LiveActionViewHolder =
        LiveActionViewHolder(parent)

    override fun onBindViewHolder(holder: LiveActionViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

}

object LiveActionDiffUtilCallback: DiffUtil.ItemCallback<LiveDebuggingActionsItem>() {
    override fun areItemsTheSame(oldItem: LiveDebuggingActionsItem, newItem: LiveDebuggingActionsItem): Boolean =
        oldItem.icon == newItem.icon

    override fun areContentsTheSame(oldItem: LiveDebuggingActionsItem, newItem: LiveDebuggingActionsItem): Boolean =
        oldItem == newItem
}

class LiveActionViewHolder(
    parent: ViewGroup,
) : RecyclerView.ViewHolder(ImageView(parent.context).apply {
    val density = resources.displayMetrics.density
    layoutParams = ViewGroup.LayoutParams((32 * density).toInt(), (24 * density).toInt())
    setPadding((4 * density).toInt(), 0, (4 * density).toInt(), 0)
    scaleType = ImageView.ScaleType.FIT_CENTER
    imageTintList = ContextCompat.getColorStateList(context, R.color.overlayViewPrimary)
}) {

    fun onBind(item: LiveDebuggingActionsItem) {
        (itemView as ImageView).setImageResource(item.icon)
    }
}
