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
package io.github.vibhor1102.macrion.feature.tutorial.data.items.root.combineevents

import io.github.vibhor1102.macrion.feature.tutorial.R
import io.github.vibhor1102.macrion.feature.tutorial.domain.model.TutorialCategory
import io.github.vibhor1102.macrion.feature.tutorial.domain.model.TutorialSlideshow


internal fun getCombineEventsCategory() =
    TutorialCategory(
        type = TutorialCategory.Type.COMBINE_EVENTS,
        nameRes = R.string.tutorial_category_combine_event_name,
        shortDescriptionRes = R.string.tutorial_category_combine_event_desc_short,
        longDescriptionRes = R.string.tutorial_category_combine_event_desc_long,
        iconRes = R.drawable.ic_screen_event,
        content = listOf(
            TutorialCategory.Content.Divider,
            TutorialCategory.Content.Category(TutorialCategory.Type.EVENTS_PRIORITY),
            TutorialCategory.Content.Category(TutorialCategory.Type.EVENTS_STATE),
            TutorialCategory.Content.Divider,
            TutorialCategory.Content.Slideshow(TutorialSlideshow.Type.EVENTS_RELOADING),
        ),
    )