/* Copyright (C) 2026 Kevin Buzeau; Copyright (C) 2026 Vibhor Goel */
package io.github.vibhor1102.macrion.feature.smart.config.ui.condition.screen.text.alphabet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.vibhor1102.macrion.feature.smart.config.R

@Composable
internal fun AlphabetModelSheet(
    items: List<AlphabetSelectionItem>?,
    showSave: Boolean,
    saveEnabled: Boolean,
    onDismiss: () -> Unit,
    onItemClicked: (AlphabetSelectionItem) -> Unit,
) {
    Surface(
        Modifier.fillMaxWidth().heightIn(min = 600.dp, max = 680.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onDismiss) { Icon(painterResource(R.drawable.ic_cancel), null) }
                Text(
                    stringResource(R.string.dialog_title_condition_selection),
                    Modifier.weight(1f).padding(horizontal = 8.dp),
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                )
                if (showSave) {
                    FilledIconButton(onDismiss, enabled = saveEnabled) {
                        Icon(painterResource(R.drawable.ic_save_filled), null)
                    }
                }
            }
            when {
                items == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                items.isEmpty() -> Spacer(Modifier.weight(1f))
                else -> LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 12.dp),
                ) {
                    items(items, key = ::itemKey) { item ->
                        when (item) {
                            is AlphabetSelectionItem.Header -> AlphabetHeader(item)
                            is AlphabetSelectionItem.Alphabet -> AlphabetRow(item) { onItemClicked(item) }
                        }
                    }
                }
            }
        }
    }
}

private fun itemKey(item: AlphabetSelectionItem): String = when (item) {
    is AlphabetSelectionItem.Header -> "header:${item.text}"
    is AlphabetSelectionItem.Alphabet -> "alphabet:${item.alphabet.name}"
}

@Composable
private fun AlphabetHeader(header: AlphabetSelectionItem.Header) {
    ElevatedCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
        Text(
            stringResource(header.text),
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

@Composable
private fun AlphabetRow(item: AlphabetSelectionItem.Alphabet, onClick: () -> Unit) {
    Column(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 62.dp).padding(horizontal = 24.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(stringResource(item.alphabetName), style = MaterialTheme.typography.bodyLarge)
                Text(
                    stringResource(item.alphabetDesc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                when (val state = item.downloadState) {
                    AlphabetDownloadUiState.Error,
                    AlphabetDownloadUiState.NotDownloaded -> FilledTonalIconButton(onClick) {
                        Icon(painterResource(R.drawable.ic_download), null)
                    }
                    is AlphabetDownloadUiState.Downloading -> Text(
                        state.progressText,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    AlphabetDownloadUiState.Downloaded -> if (item.selectableWhenInstalled) {
                        RadioButton(item.selected, onClick)
                    } else {
                        Icon(painterResource(R.drawable.ic_confirm), null)
                    }
                }
            }
        }
        HorizontalDivider()
    }
}
