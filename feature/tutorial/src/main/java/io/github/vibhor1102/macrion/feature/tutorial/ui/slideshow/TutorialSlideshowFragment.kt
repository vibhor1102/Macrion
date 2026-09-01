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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs

import io.github.vibhor1102.macrion.core.ui.compose.MacrionTheme
import io.github.vibhor1102.macrion.feature.tutorial.R
import io.github.vibhor1102.macrion.feature.tutorial.data.mapping.toTutorialSlideshow

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TutorialSlideshowFragment : Fragment() {

    private val args: TutorialSlideshowFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val slideshow = args.slideshowType.toTutorialSlideshow()

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MacrionTheme {
                    Surface(Modifier.fillMaxSize()) {
                        TutorialSlideshowContent(
                            pages = slideshow.slideshowItems,
                            modifier = Modifier.fillMaxSize(),
                            header = {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 24.dp)
                                        .border(
                                            width = 2.dp,
                                            color = colorResource(R.color.tutorial_header_card_border),
                                            shape = MaterialTheme.shapes.medium,
                                        ),
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Image(
                                            painter = painterResource(R.drawable.ic_tutorial_slideshow),
                                            contentDescription = null,
                                            modifier = Modifier.padding(start = 16.dp).size(64.dp),
                                        )
                                        Text(
                                            text = stringResource(slideshow.nameRes),
                                            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                                            style = MaterialTheme.typography.headlineSmall,
                                            maxLines = 2,
                                        )
                                    }
                                }
                            },
                            onClose = { findNavController().popBackStack() },
                        )
                    }
                }
            }
        }
    }
}
