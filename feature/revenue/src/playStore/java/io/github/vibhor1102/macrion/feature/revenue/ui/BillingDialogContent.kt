/*
 * Copyright (C) 2026 Vibhor Goel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package io.github.vibhor1102.macrion.feature.revenue.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.vibhor1102.macrion.core.ui.bindings.buttons.LoadableButtonState
import io.github.vibhor1102.macrion.feature.revenue.R
import io.github.vibhor1102.macrion.feature.revenue.ui.paywall.DialogState
import io.github.vibhor1102.macrion.feature.revenue.ui.purchase.PurchaseDialogState

@Composable
internal fun PurchaseDialogContent(
    state: PurchaseDialogState,
    onOpenSource: () -> Unit,
    onPurchase: () -> Unit,
) {
    BillingSheet {
        BillingMessage(purchased = state is PurchaseDialogState.Purchased)
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = onOpenSource, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.button_text_visit_github))
        }
        Spacer(Modifier.height(8.dp))
        val buttonState = when (state) {
            PurchaseDialogState.Loading -> LoadableButtonState.Loading()
            PurchaseDialogState.Pending -> LoadableButtonState.Loaded.Disabled(
                stringResource(R.string.button_text_buy_pro_pending),
            )
            PurchaseDialogState.Purchased -> LoadableButtonState.Loaded.Enabled(
                stringResource(R.string.button_text_understood),
            )
            PurchaseDialogState.Error -> LoadableButtonState.Loaded.Disabled(
                stringResource(R.string.button_text_buy_pro_error),
            )
            is PurchaseDialogState.Loaded -> LoadableButtonState.Loaded.Enabled(
                stringResource(R.string.button_text_buy_pro, state.price),
            )
        }
        LoadableButton(buttonState, outlined = false, disabledAlpha = 0.5f, onClick = onPurchase)
    }
}

@Composable
internal fun PaywallDialogContent(
    state: DialogState?,
    onTrial: () -> Unit,
    onWatchAd: () -> Unit,
    onPurchase: () -> Unit,
    onUnderstood: () -> Unit,
) {
    BillingSheet {
        when (state) {
            null -> CenteredProgress()
            is DialogState.NotPurchased -> {
                BillingMessage(purchased = false)
                Spacer(Modifier.height(16.dp))
                LoadableButton(state.trialButtonState, outlined = true, onClick = onTrial)
                Spacer(Modifier.height(8.dp))
                LoadableButton(state.adButtonState, outlined = true, onClick = onWatchAd)
                Spacer(Modifier.height(8.dp))
                LoadableButton(state.purchaseButtonState, outlined = false, onClick = onPurchase)
            }
            DialogState.Purchased -> {
                BillingMessage(purchased = true)
                Spacer(Modifier.height(16.dp))
                LoadableButton(
                    LoadableButtonState.Loaded.Enabled(stringResource(R.string.button_text_understood)),
                    outlined = false,
                    onClick = onUnderstood,
                )
            }
            DialogState.AdShowing -> CenteredProgress()
            DialogState.AdWatched -> {
                AnimatedVisibility(visible = true, enter = fadeIn(tween(durationMillis = 250))) {
                    Text(
                        text = stringResource(R.string.header_starting_detection),
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                    )
                }
                Spacer(Modifier.height(16.dp))
                CenteredProgress()
            }
        }
    }
}

@Composable
private fun BillingSheet(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .verticalScroll(rememberScrollState()),
    ) {
        BillingBanner()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 32.dp, end = 32.dp, top = 16.dp, bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content,
        )
    }
}

@Composable
private fun BillingBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    0f to Color(0xFF4CAF50),
                    0.75f to Color(0xAA4CAF50),
                    1f to Color.Transparent,
                ),
            )
            .padding(start = 24.dp, end = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        androidx.compose.foundation.Image(
            painter = painterResource(R.drawable.banner_logo),
            contentDescription = null,
            modifier = Modifier.size(85.dp),
        )
        Column(
            modifier = Modifier.padding(start = 8.dp, top = 16.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Macrion Pro",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "A one time purchase to remove all ads and limitations forever !",
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun BillingMessage(purchased: Boolean) {
    Text(
        text = stringResource(
            if (purchased) R.string.message_billing_success else R.string.message_billing,
        ),
        modifier = Modifier.fillMaxWidth(),
        style = if (purchased) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun LoadableButton(
    state: LoadableButtonState,
    outlined: Boolean,
    disabledAlpha: Float = 0.75f,
    onClick: () -> Unit,
) {
    val enabled = state is LoadableButtonState.Loaded.Enabled
    val content: @Composable () -> Unit = {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (state is LoadableButtonState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = if (outlined) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                )
                if (state.text.isNotEmpty()) Spacer(Modifier.width(8.dp))
            }
            if (state.text.isNotEmpty()) Text(state.text)
        }
    }
    val modifier = Modifier.fillMaxWidth().alpha(if (enabled) 1f else disabledAlpha)
    if (outlined) {
        OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier,
            colors = ButtonDefaults.outlinedButtonColors(
                disabledContentColor = MaterialTheme.colorScheme.primary,
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
            content = { content() },
        )
    } else {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier,
            colors = ButtonDefaults.buttonColors(
                disabledContainerColor = MaterialTheme.colorScheme.primary,
                disabledContentColor = MaterialTheme.colorScheme.onPrimary,
            ),
            content = { content() },
        )
    }
}

@Composable
private fun CenteredProgress() {
    Box(Modifier.fillMaxWidth().height(112.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
