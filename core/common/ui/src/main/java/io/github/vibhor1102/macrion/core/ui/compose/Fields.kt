/* Copyright (C) 2026 Vibhor Goel */
package io.github.vibhor1102.macrion.core.ui.compose

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

@Composable
fun MacrionSwitchField(title: String, description: String, checked: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().toggleable(checked, role = Role.Switch) { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.width(16.dp))
        Switch(checked = checked, onCheckedChange = null)
    }
}

@Composable
fun MacrionAnimatedDescription(text: String, modifier: Modifier = Modifier) {
    AnimatedContent(
        targetState = text,
        modifier = modifier,
        transitionSpec = {
            fadeIn(tween(100)) togetherWith fadeOut(tween(100)) using
                SizeTransform(clip = false, sizeAnimationSpec = { _, _ -> tween(100) })
        },
        label = "description",
    ) {
        Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun MacrionActionField(title: String, trailingContent: @Composable (() -> Unit)? = null, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        trailingContent?.invoke()
    }
}

@Composable
fun MacrionTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    maxLength: Int? = null,
    trimWhitespace: Boolean = true,
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var fieldValue by remember {
        mutableStateOf(TextFieldValue(value, selection = TextRange(value.length)))
    }
    LaunchedEffect(value) {
        if (value != fieldValue.text) {
            fieldValue = TextFieldValue(
                text = value,
                selection = TextRange(fieldValue.selection.end.coerceAtMost(value.length)),
            )
        }
    }
    fun commitValue() {
        if (!trimWhitespace) return
        val trimmed = fieldValue.text.trim()
        if (trimmed != fieldValue.text) {
            fieldValue = TextFieldValue(trimmed, selection = TextRange(trimmed.length))
            onValueChange(trimmed)
        }
    }

    OutlinedTextField(
        value = fieldValue,
        onValueChange = { candidate ->
            val limitedText = maxLength?.let { candidate.text.take(it) } ?: candidate.text
            fieldValue = candidate.copy(
                text = limitedText,
                selection = candidate.selection.coerceToTextLength(limitedText.length),
                composition = candidate.composition?.coerceToTextLength(limitedText.length),
            )
            onValueChange(limitedText)
        },
        label = { Text(label) },
        modifier = modifier.fillMaxWidth().onFocusChanged {
            if (!it.isFocused) commitValue()
        },
        isError = isError,
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = {
            commitValue()
            focusManager.clearFocus()
            keyboardController?.hide()
        }),
    )
}

private fun TextRange.coerceToTextLength(length: Int): TextRange = TextRange(
    start = start.coerceIn(0, length),
    end = end.coerceIn(0, length),
)

@Composable
fun MacrionLoadableButton(text: String, loading: Boolean, enabled: Boolean, onClick: () -> Unit) {
    TextButton(onClick = onClick, enabled = enabled && !loading) {
        if (loading) CircularProgressIndicator(Modifier.width(20.dp), strokeWidth = 2.dp)
        else Text(text)
    }
}
