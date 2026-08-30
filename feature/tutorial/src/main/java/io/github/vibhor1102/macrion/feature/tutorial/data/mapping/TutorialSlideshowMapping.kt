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

import io.github.vibhor1102.macrion.feature.tutorial.data.items.root.basics.actions.getActionsListSlideshow
import io.github.vibhor1102.macrion.feature.tutorial.data.items.root.basics.actions.changecounter.getChangeCounterActionSlideshow
import io.github.vibhor1102.macrion.feature.tutorial.data.items.root.basics.actions.click.getClickActionOffsetSlideshow
import io.github.vibhor1102.macrion.feature.tutorial.data.items.root.basics.actions.click.getClickActionTargetSlideshow
import io.github.vibhor1102.macrion.feature.tutorial.data.items.root.basics.actions.pause.getPauseActionSlideshow
import io.github.vibhor1102.macrion.feature.tutorial.data.items.root.basics.actions.swipe.getSwipeActionSlideshow
import io.github.vibhor1102.macrion.feature.tutorial.data.items.root.basics.actions.intent.getIntentActionSlideshow
import io.github.vibhor1102.macrion.feature.tutorial.data.items.root.basics.actions.notification.getNotificationActionSlideshow
import io.github.vibhor1102.macrion.feature.tutorial.data.items.root.basics.actions.changeeventstate.getChangeEventStateActionSlideshow
import io.github.vibhor1102.macrion.feature.tutorial.data.items.root.basics.actions.system.getSystemActionSlideshow
import io.github.vibhor1102.macrion.feature.tutorial.data.items.root.basics.actions.writetext.getWriteTextActionSlideshow
import io.github.vibhor1102.macrion.feature.tutorial.data.items.root.basics.screenconditions.getScreenConditionsThresholdSlideshow
import io.github.vibhor1102.macrion.feature.tutorial.data.items.root.basics.screenconditions.getScreenConditionsTypeSlideshow
import io.github.vibhor1102.macrion.feature.tutorial.data.items.root.basics.screenconditions.image.getImageConditionsCaptureSlideshow
import io.github.vibhor1102.macrion.feature.tutorial.data.items.root.basics.screenconditions.image.getImageConditionsDetectionAreaSlideshow
import io.github.vibhor1102.macrion.feature.tutorial.data.items.root.basics.screenconditions.number.getNumberConditionsDetectionAreaSlideshow
import io.github.vibhor1102.macrion.feature.tutorial.data.items.root.basics.screenconditions.text.getTextConditionsDetectionAreaSlideshow
import io.github.vibhor1102.macrion.feature.tutorial.data.items.root.basics.triggerconditions.getBroadcastReceivedSlideshow
import io.github.vibhor1102.macrion.feature.tutorial.data.items.root.basics.triggerconditions.getCounterReachedSlideshow
import io.github.vibhor1102.macrion.feature.tutorial.data.items.root.combineconditions.getCombineConditionsOrderingSlideshow
import io.github.vibhor1102.macrion.feature.tutorial.data.items.root.combineevents.getEventsReloadingSlideshow
import io.github.vibhor1102.macrion.feature.tutorial.data.items.root.combineevents.state.getEventsStateBasicsSlideshow
import io.github.vibhor1102.macrion.feature.tutorial.data.items.root.combineevents.priority.getEventsPrioritySlideshow
import io.github.vibhor1102.macrion.feature.tutorial.data.items.root.counters.getCountersValueUsagesSlideshow
import io.github.vibhor1102.macrion.feature.tutorial.domain.model.TutorialSlideshow
import io.github.vibhor1102.macrion.feature.tutorial.domain.model.TutorialSlideshow.Type.*


internal fun TutorialSlideshow.Type.toTutorialSlideshow(): TutorialSlideshow =
    when (this) {
        ACTIONS_LIST -> getActionsListSlideshow()
        BROADCAST_RECEIVED_CONDITION -> getBroadcastReceivedSlideshow()
        CHANGE_COUNTER_ACTION -> getChangeCounterActionSlideshow()
        CLICK_ACTION_OFFSET -> getClickActionOffsetSlideshow()
        CLICK_ACTION_TARGET -> getClickActionTargetSlideshow()
        COMBINE_CONDITIONS_ORDERING -> getCombineConditionsOrderingSlideshow()
        COUNTER_REACHED_CONDITION -> getCounterReachedSlideshow()
        COUNTERS_VALUE_USAGES -> getCountersValueUsagesSlideshow()
        EVENTS_RELOADING -> getEventsReloadingSlideshow()
        EVENTS_STATE_BASICS -> getEventsStateBasicsSlideshow()
        EVENTS_PRIORITY_BASICS -> getEventsPrioritySlideshow()
        IMAGE_CONDITION_CAPTURE -> getImageConditionsCaptureSlideshow()
        IMAGE_CONDITION_DETECTION_AREA -> getImageConditionsDetectionAreaSlideshow()
        INTENT_ACTION -> getIntentActionSlideshow()
        NOTIFICATION_ACTION -> getNotificationActionSlideshow()
        NUMBER_CONDITION_DETECTION_AREA -> getNumberConditionsDetectionAreaSlideshow()
        PAUSE_ACTION -> getPauseActionSlideshow()
        SCREEN_CONDITIONS_DETECTION_THRESHOLD -> getScreenConditionsThresholdSlideshow()
        SCREEN_CONDITIONS_TYPE -> getScreenConditionsTypeSlideshow()
        SWIPE_ACTION -> getSwipeActionSlideshow()
        SYSTEM_ACTION -> getSystemActionSlideshow()
        TOGGLE_EVENT_ACTION -> getChangeEventStateActionSlideshow()
        TEXT_CONDITION_DETECTION_AREA -> getTextConditionsDetectionAreaSlideshow()
        WRITE_TEXT_ACTION -> getWriteTextActionSlideshow()
    }
