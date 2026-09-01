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
package io.github.vibhor1102.macrion.feature.tutorial.ui.slideshow

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.vibhor1102.macrion.core.ui.compose.MacrionDialogSurface
import io.github.vibhor1102.macrion.core.ui.compose.MacrionTheme
import io.github.vibhor1102.macrion.core.ui.utils.getDynamicColorsContext
import io.github.vibhor1102.macrion.feature.tutorial.R
import io.github.vibhor1102.macrion.feature.tutorial.data.mapping.toTutorialSlideshow
import io.github.vibhor1102.macrion.feature.tutorial.domain.model.TutorialSlideshow
import kotlinx.coroutines.launch

internal fun Context.createTutorialSlideshowDialog(
    slideshowType: TutorialSlideshow.Type,
    pageIndex: Int,
    onDismissed: (() -> Unit)?,
): AlertDialog? = createDialog(slideshowType.toTutorialSlideshow(), IntRange(pageIndex, pageIndex), onDismissed)

internal fun Context.createTutorialSlideshowDialog(
    slideshowType: TutorialSlideshow.Type,
    pageRange: IntRange? = null,
    onDismissed: (() -> Unit)?,
): AlertDialog? = createDialog(slideshowType.toTutorialSlideshow(), pageRange, onDismissed)

private fun Context.createDialog(
    slideshow: TutorialSlideshow,
    pageRange: IntRange?,
    onDismissed: (() -> Unit)?,
): AlertDialog? {

    val pages = pageRange ?: IntRange(0, slideshow.slideshowItems.lastIndex)
    if (pageRange != null && (pageRange.first < 0 || pageRange.last > slideshow.slideshowItems.lastIndex)) {
        Log.e(TAG, "Can't create slideshow dialog, page range is invalid: $pageRange; " +
                "slideshowItems=${slideshow.slideshowItems.size}")
        return null
    }

    val dialogContext = getDynamicColorsContext(R.style.AppTheme)
    lateinit var dialog: AlertDialog
    val content = ComposeView(dialogContext).apply {
        setContent {
            MacrionTheme {
                MacrionDialogSurface {
                    TutorialSlideshowDialogContent(
                        slideshow = slideshow,
                        pages = slideshow.slideshowItems.subList(pages.first, pages.last + 1),
                        onDismiss = { dialog.dismiss() },
                    )
                }
            }
        }
    }
    dialog = MaterialAlertDialogBuilder(dialogContext)
        .setView(content)
        .setOnDismissListener { onDismissed?.invoke() }
        .create()
    return dialog
}

@Composable
private fun TutorialSlideshowDialogContent(
    slideshow: TutorialSlideshow,
    pages: List<TutorialSlideshow.SlideshowItem>,
    onDismiss: () -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()
    val pagerHeight = (LocalConfiguration.current.screenHeightDp * 0.55f).dp
    val isLastPage = pagerState.currentPage == pages.lastIndex

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(slideshow.nameRes),
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
        HorizontalDivider()
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth().height(pagerHeight),
            verticalAlignment = Alignment.Top,
        ) { page ->
            TutorialSlideshowPage(pages[page])
        }
        Button(
            onClick = {
                if (isLastPage) {
                    onDismiss()
                } else {
                    coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                }
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 24.dp),
        ) {
            Text(
                stringResource(
                    if (isLastPage) R.string.button_text_tutorial_close
                    else R.string.button_text_tutorial_next,
                ),
            )
        }
    }
}

@Composable
private fun TutorialSlideshowPage(item: TutorialSlideshow.SlideshowItem) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Card(Modifier.padding(top = 24.dp)) {
            Image(
                painter = painterResource(item.tutorialImage),
                contentDescription = null,
                modifier = Modifier
                    .padding(item.tutorialImageFormat.marginDp.dp)
                    .size(
                        width = item.tutorialImageFormat.widthDp.dp,
                        height = item.tutorialImageFormat.heightDp.dp,
                    ),
            )
        }
        Text(
            text = stringResource(item.tutorialTextRes),
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 36.dp),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
    }
}

private const val TAG = "TutorialSlideshowDialog"
