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
package io.github.vibhor1102.macrion.feature.tutorial.data.mapping

import io.github.vibhor1102.macrion.feature.tutorial.data.items.root.basics.screenconditions.color.ColorConditionsTutorial
import io.github.vibhor1102.macrion.feature.tutorial.data.items.root.basics.screenconditions.image.ImageConditionsMovingTargetTutorial
import io.github.vibhor1102.macrion.feature.tutorial.data.items.root.basics.screenconditions.image.ImageConditionsStillTargetTutorial
import io.github.vibhor1102.macrion.feature.tutorial.data.items.root.basics.screenconditions.number.NumberConditionsStaticValueTutorial
import io.github.vibhor1102.macrion.feature.tutorial.data.items.root.basics.screenconditions.text.TextConditionsMovingTextTutorial
import io.github.vibhor1102.macrion.feature.tutorial.data.items.root.basics.screenconditions.text.TextConditionsStillTextTutorial
import io.github.vibhor1102.macrion.feature.tutorial.data.items.root.basics.triggerconditions.TimerReachedConditionTutorial
import io.github.vibhor1102.macrion.feature.tutorial.data.items.root.combineconditions.CombineConditionsNotVisibleTargetTutorial
import io.github.vibhor1102.macrion.feature.tutorial.data.items.root.combineconditions.CombineConditionsOperatorAndTutorial
import io.github.vibhor1102.macrion.feature.tutorial.data.items.root.combineconditions.CombineConditionsOperatorOrTutorial
import io.github.vibhor1102.macrion.feature.tutorial.data.items.root.combineevents.priority.EventsPriorityTutorial
import io.github.vibhor1102.macrion.feature.tutorial.data.items.root.combineevents.state.EventsStateTutorial
import io.github.vibhor1102.macrion.feature.tutorial.data.items.root.counters.CountersBasicsTutorial
import io.github.vibhor1102.macrion.feature.tutorial.domain.model.TutorialItem
import io.github.vibhor1102.macrion.feature.tutorial.domain.model.TutorialItem.Type.*


internal fun TutorialItem.Type.toTutorialItem(): TutorialItem =
    when (this) {
        COLOR_CONDITION -> ColorConditionsTutorial

        COMBINE_CONDITIONS_NOT_VISIBLE -> CombineConditionsNotVisibleTargetTutorial
        COMBINE_CONDITIONS_OPERATOR_AND -> CombineConditionsOperatorAndTutorial
        COMBINE_CONDITIONS_OPERATOR_OR -> CombineConditionsOperatorOrTutorial

        COUNTERS_BASICS -> CountersBasicsTutorial

        EVENTS_PRIORITY -> EventsPriorityTutorial
        EVENTS_STATE -> EventsStateTutorial

        IMAGE_DETECTION_MOVING_TARGET -> ImageConditionsMovingTargetTutorial
        IMAGE_DETECTION_STILL_TARGET -> ImageConditionsStillTargetTutorial

        NUMBER_CONDITION_STATIC_VALUE -> NumberConditionsStaticValueTutorial

        TEXT_CONDITION_MOVING_TEXT -> TextConditionsMovingTextTutorial
        TEXT_CONDITION_STILL_TEXT -> TextConditionsStillTextTutorial

        TIMER_REACHED_CONDITION -> TimerReachedConditionTutorial
    }
