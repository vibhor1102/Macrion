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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.vibhor1102.macrion.core.ui.bindings.buttons

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import io.github.vibhor1102.macrion.core.ui.compose.MacrionTheme

sealed class LoadableButtonState {
    abstract val text: String
    data class Loading(override val text: String = "") : LoadableButtonState()
    sealed class Loaded : LoadableButtonState() {
        data class Enabled(override val text: String) : Loaded()
        data class Disabled(override val text: String) : Loaded()
    }
}

class LoadableButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {

    internal val state = mutableStateOf<LoadableButtonState>(LoadableButtonState.Loading())
    internal var onButtonClick: () -> Unit = {}

    init {
        addView(
            ComposeView(context).apply {
                setContent {
                    MacrionTheme {
                        val currentState = state.value
                        val enabled = currentState is LoadableButtonState.Loaded.Enabled

                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Button(
                                onClick = { onButtonClick() },
                                enabled = enabled,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .alpha(if (enabled) 1f else 0.5f),
                            ) {
                                Text(currentState.text)
                            }

                            if (currentState is LoadableButtonState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.width(20.dp),
                                    strokeWidth = 2.dp,
                                )
                            }
                        }
                    }
                }
            },
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT),
        )
    }
}

fun LoadableButtonView.setState(state: LoadableButtonState) {
    this.state.value = state
}

fun LoadableButtonView.setOnClickListener(onClick: () -> Unit) {
    onButtonClick = onClick
}
