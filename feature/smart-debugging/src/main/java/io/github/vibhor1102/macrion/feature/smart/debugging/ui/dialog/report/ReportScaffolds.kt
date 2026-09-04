/* Copyright (C) 2026 Vibhor Goel */
package io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.vibhor1102.macrion.core.ui.views.fastscroll.VerticalFastScrollerView
import io.github.vibhor1102.macrion.feature.smart.debugging.R

internal class ReportRecyclerViews(
    val recyclerView: RecyclerView,
    val fastScroller: VerticalFastScrollerView,
)

@Composable
internal fun ReportDialogTopBar(
    title: String,
    onDismiss: () -> Unit,
    onSave: (() -> Unit)? = null,
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onDismiss) {
            Icon(painterResource(R.drawable.ic_cancel), contentDescription = null)
        }
        Text(
            text = title,
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            overflow = TextOverflow.Clip,
        )
        if (onSave != null) {
            FilledIconButton(onClick = onSave) {
                Icon(painterResource(R.drawable.ic_save_filled), contentDescription = null)
            }
        }
    }
}

@Composable
internal fun ReportLoading(color: Color = MaterialTheme.colorScheme.primary) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(Modifier.size(48.dp), color = color)
    }
}

@Composable
internal fun ReportEmptyMessage(
    title: String,
    secondary: String? = null,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    secondaryColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    action: (@Composable () -> Unit)? = null,
) {
    Column(
        Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            title,
            style = MaterialTheme.typography.headlineSmall,
            color = contentColor,
            textAlign = TextAlign.Center,
        )
        if (secondary != null) {
            HorizontalDivider(Modifier.padding(start = 8.dp, end = 8.dp, top = 16.dp, bottom = 8.dp))
            Text(
                secondary,
                style = MaterialTheme.typography.bodyMedium,
                color = secondaryColor,
                textAlign = TextAlign.Center,
            )
        }
        if (action != null) action()
    }
}

@Composable
internal fun ReportRecycler(
    @StringRes contentDescriptionRes: Int,
    modifier: Modifier = Modifier,
    bottomPaddingDp: Int = 0,
    onCreated: (ReportRecyclerViews) -> Unit,
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            val recycler = RecyclerView(context).apply {
                layoutManager = LinearLayoutManager(context)
                clipToPadding = bottomPaddingDp == 0
                setPadding(0, 0, 0, (bottomPaddingDp * resources.displayMetrics.density).toInt())
            }
            val scroller = VerticalFastScrollerView(context).apply {
                contentDescription = context.getString(contentDescriptionRes)
                attachToRecyclerView(recycler)
            }
            FrameLayout(context).apply {
                addView(recycler, FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                ))
                addView(scroller, FrameLayout.LayoutParams(
                    (32 * resources.displayMetrics.density).toInt(),
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.Gravity.END,
                ))
                onCreated(ReportRecyclerViews(recycler, scroller))
            }
        },
    )
}

@Composable
internal fun ReportLoadableList(
    items: List<*>?,
    @StringRes contentDescriptionRes: Int,
    emptyTitle: String? = null,
    emptySecondary: String? = null,
    onCreated: (ReportRecyclerViews) -> Unit,
) {
    when {
        items == null -> ReportLoading()
        items.isEmpty() && emptyTitle != null -> ReportEmptyMessage(
            title = emptyTitle,
            secondary = emptySecondary,
        )
        items.isEmpty() -> Box(Modifier.fillMaxSize())
        else -> ReportRecycler(contentDescriptionRes, Modifier.fillMaxSize(), onCreated = onCreated)
    }
}
