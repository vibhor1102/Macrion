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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.vibhor1102.macrion.feature.tutorial.di

import io.github.vibhor1102.macrion.core.common.navigation.TutorialNavigator
import io.github.vibhor1102.macrion.core.common.overlays.di.OverlayComponent
import io.github.vibhor1102.macrion.feature.tutorial.navigation.TutorialNavigatorImpl
import io.github.vibhor1102.macrion.feature.tutorial.ui.overlay.TutorialOverlayViewModel

import dagger.Binds
import dagger.Module
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@EntryPoint
@InstallIn(OverlayComponent::class)
interface TutorialViewModelsEntryPoint {
    fun tutorialOverlayViewModel(): TutorialOverlayViewModel
}

@Module
@InstallIn(SingletonComponent::class)
internal abstract class TutorialNavigationModule {

    @Binds
    @Singleton
    abstract fun bindTutorialNavigator(impl: TutorialNavigatorImpl): TutorialNavigator
}