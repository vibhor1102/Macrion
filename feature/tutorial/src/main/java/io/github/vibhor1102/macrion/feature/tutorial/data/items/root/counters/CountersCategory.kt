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
package io.github.vibhor1102.macrion.feature.tutorial.data.items.root.counters

import io.github.vibhor1102.macrion.feature.tutorial.R
import io.github.vibhor1102.macrion.feature.tutorial.domain.model.TutorialCategory
import io.github.vibhor1102.macrion.feature.tutorial.domain.model.TutorialItem
import io.github.vibhor1102.macrion.feature.tutorial.domain.model.TutorialSlideshow


internal fun getCountersCategory() =
    TutorialCategory(
        type = TutorialCategory.Type.COUNTERS,
        nameRes = R.string.tutorial_category_counters_name,
        shortDescriptionRes = R.string.tutorial_category_counters_desc_short,
        longDescriptionRes = R.string.tutorial_category_counters_desc_long,
        iconRes = R.drawable.ic_counter_reached,
        content = listOf(
            TutorialCategory.Content.Divider,
            TutorialCategory.Content.Tutorial(TutorialItem.Type.COUNTERS_BASICS),
            TutorialCategory.Content.Slideshow(TutorialSlideshow.Type.COUNTERS_VALUE_USAGES),
        ),
    )