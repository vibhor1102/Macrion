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
package io.github.vibhor1102.macrion.feature.tutorial.data.items.root.basics.screenconditions.number

import io.github.vibhor1102.macrion.feature.tutorial.R
import io.github.vibhor1102.macrion.feature.tutorial.domain.model.TutorialCategory
import io.github.vibhor1102.macrion.feature.tutorial.domain.model.TutorialItem
import io.github.vibhor1102.macrion.feature.tutorial.domain.model.TutorialSlideshow

internal fun getNumberConditionsCategory() =
    TutorialCategory(
        type = TutorialCategory.Type.NUMBER_CONDITION,
        nameRes = R.string.tutorial_category_number_condition_name,
        shortDescriptionRes = R.string.tutorial_category_number_condition_desc_short,
        longDescriptionRes = R.string.tutorial_category_number_condition_desc_long,
        iconRes = R.drawable.ic_number_condition,
        content = listOf(
            TutorialCategory.Content.Divider,
            TutorialCategory.Content.Tutorial(TutorialItem.Type.NUMBER_CONDITION_STATIC_VALUE),
            TutorialCategory.Content.Divider,
            TutorialCategory.Content.Slideshow(TutorialSlideshow.Type.NUMBER_CONDITION_DETECTION_AREA),
        ),
    )