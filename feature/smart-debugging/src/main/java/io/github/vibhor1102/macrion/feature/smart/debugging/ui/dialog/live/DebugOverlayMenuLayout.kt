/* Copyright (C) 2026 Vibhor Goel */
package io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.live

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import io.github.vibhor1102.macrion.core.ui.compose.MacrionTheme
import io.github.vibhor1102.macrion.feature.smart.debugging.R

internal fun createDebugOverlayMenu(
    context: Context,
    contentWidthDp: Int,
    contentHeightDp: Int,
    content: @Composable () -> Unit,
): ViewGroup {
    val density = context.resources.displayMetrics.density
    fun dp(value: Int) = (value * density).toInt()

    val buttons = LinearLayout(context).apply {
        id = R.id.menu_items
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL
        setPadding(dp(4), dp(8), dp(4), dp(8))
    }
    fun button(idValue: Int, icon: Int, description: Int) = ImageButton(context).apply {
        id = idValue
        layoutParams = LinearLayout.LayoutParams(dp(48), dp(48))
        setBackgroundColor(Color.TRANSPARENT)
        scaleType = ImageView.ScaleType.FIT_CENTER
        imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.overlayMenuButtons))
        setImageResource(icon)
        contentDescription = context.getString(description)
    }
    buttons.addView(button(R.id.btn_back, R.drawable.ic_back, R.string.content_desc_go_back))
    buttons.addView(button(R.id.btn_hide_overlay, R.drawable.ic_visible_on, R.string.content_desc_go_back))
    buttons.addView(button(R.id.btn_move, R.drawable.ic_move, R.string.content_desc_move_menu))

    val compose = ComposeView(context).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        var contentInstalled = false
        addOnAttachStateChangeListener(object : android.view.View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(view: android.view.View) {
                if (contentInstalled) return
                contentInstalled = true
                setContent { MacrionTheme { content() } }
            }
            override fun onViewDetachedFromWindow(view: android.view.View) = Unit
        })
    }
    val row = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        addView(buttons)
        addView(compose, LinearLayout.LayoutParams(dp(contentWidthDp), dp(contentHeightDp)))
    }
    val card = MaterialCardView(context).apply {
        id = R.id.menu_background
        radius = dp(10).toFloat()
        cardElevation = 0f
        setCardBackgroundColor(ContextCompat.getColor(context, R.color.overlayMenuBackground))
        addView(row)
    }
    return FrameLayout(context).apply {
        addView(card, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        ))
    }
}
