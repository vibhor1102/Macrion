/*
 * Copyright (C) 2026 Vibhor Goel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package io.github.vibhor1102.macrion.feature.externallaunch.localeplugin.di

import io.github.vibhor1102.macrion.feature.externallaunch.localeplugin.data.AndroidKeystoreLocalePluginSigner
import io.github.vibhor1102.macrion.feature.externallaunch.localeplugin.domain.LocalePluginConfigurationSigner
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class LocalePluginModule {
    @Binds
    @Singleton
    abstract fun bindConfigurationSigner(
        signer: AndroidKeystoreLocalePluginSigner,
    ): LocalePluginConfigurationSigner
}
