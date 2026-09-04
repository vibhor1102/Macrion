/*
 * Copyright (C) 2026 Vibhor Goel
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
package io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.sort

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.widget.ListPopupWindow
import io.github.vibhor1102.macrion.feature.smart.debugging.R
import com.google.android.material.card.MaterialCardView
import com.google.android.material.R as MaterialR
import com.google.android.material.color.MaterialColors
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import kotlin.math.roundToInt

internal data class DebugReportSortOption<T>(
    val value: T,
    @field:StringRes val titleRes: Int,
    val selected: Boolean,
)

/** Compact sort chooser which keeps the report visible behind it. */
internal class DebugReportSortPopup<T>(
    private val anchor: View,
    options: List<DebugReportSortOption<T>>,
    onSelected: (T) -> Unit,
) {
    private val density = anchor.resources.displayMetrics.density
    private val popup = ListPopupWindow(anchor.context).apply {
        anchorView = anchor
        setAdapter(SortOptionsAdapter(options))
        width = minOf((280 * density).toInt(), anchor.resources.displayMetrics.widthPixels - (32 * density).toInt())
        height = dp(POPUP_VERTICAL_PADDING_DP * 2 + OPTION_HEIGHT_DP * options.size)
        isModal = true
        inputMethodMode = PopupWindow.INPUT_METHOD_NOT_NEEDED
        setDropDownGravity(Gravity.END)
        setBackgroundDrawable(createPopupBackground())
        setOnItemClickListener { _, _, position, _ ->
            dismiss()
            onSelected(options[position].value)
        }
    }

    fun show() {
        popup.show()
        popup.listView?.apply {
            divider = null
            selector = ColorDrawable(Color.TRANSPARENT)
            val padding = dp(POPUP_VERTICAL_PADDING_DP)
            setPadding(padding, padding, padding, padding)
            clipToPadding = false
            isVerticalScrollBarEnabled = false
            overScrollMode = View.OVER_SCROLL_NEVER
        }
    }

    fun dismiss() {
        popup.dismiss()
    }

    private fun createPopupBackground(): MaterialShapeDrawable {
        val cornerRadius = 20 * density
        return MaterialShapeDrawable(
            ShapeAppearanceModel.builder().setAllCornerSizes(cornerRadius).build()
        ).apply {
            fillColor = ColorStateList.valueOf(
                MaterialColors.getColor(anchor, MaterialR.attr.colorSurfaceContainer)
            )
            elevation = 8 * density
        }
    }

    private fun dp(value: Int): Int = (value * density).roundToInt()
}

private const val OPTION_HEIGHT_DP = 56
private const val POPUP_VERTICAL_PADDING_DP = 8

private class SortOptionsAdapter<T>(
    private val options: List<DebugReportSortOption<T>>,
) : BaseAdapter() {

    override fun getCount(): Int = options.size
    override fun getItem(position: Int): DebugReportSortOption<T> = options[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val row = (convertView as? SortOptionView) ?: SortOptionView(parent)
        val option = getItem(position)
        row.bind(option)
        return row
    }
}

private class SortOptionView(parent: ViewGroup) : FrameLayout(parent.context) {
    private val density = resources.displayMetrics.density
    private val card = MaterialCardView(context).apply {
        radius = 16 * density
        cardElevation = 0f
        strokeWidth = 0
    }
    private val title = TextView(context).apply {
        setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyLarge)
        maxLines = 1
    }
    private val selectedIcon = ImageView(context).apply {
        setImageResource(R.drawable.ic_debug_confirm)
        visibility = View.INVISIBLE
    }

    init {
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (56 * density).roundToInt())
        addView(card, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT).apply {
            topMargin = (2 * density).roundToInt()
            bottomMargin = (2 * density).roundToInt()
        })
        val content = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            val horizontal = (16 * density).roundToInt()
            setPadding(horizontal, 0, horizontal, 0)
        }
        card.addView(content, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        content.addView(title, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        content.addView(selectedIcon, LinearLayout.LayoutParams((24 * density).roundToInt(), (24 * density).roundToInt()).apply {
            marginStart = (16 * density).roundToInt()
        })
    }

    fun <T> bind(option: DebugReportSortOption<T>) {
        val selectedContainer = MaterialColors.getColor(this, MaterialR.attr.colorSecondaryContainer)
        val selectedContent = MaterialColors.getColor(this, MaterialR.attr.colorOnSecondaryContainer)
        val normalContent = MaterialColors.getColor(this, MaterialR.attr.colorOnSurface)
        isSelected = option.selected
        card.setCardBackgroundColor(if (option.selected) selectedContainer else Color.TRANSPARENT)
        title.setText(option.titleRes)
        title.setTextColor(if (option.selected) selectedContent else normalContent)
        selectedIcon.visibility = if (option.selected) View.VISIBLE else View.INVISIBLE
        selectedIcon.imageTintList = ColorStateList.valueOf(selectedContent)
    }
}
