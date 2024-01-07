package com.suihan74.satena2.scene.preferences

import com.suihan74.satena2.scene.preferences.page.info.ReleaseNotesRepository
import com.suihan74.satena2.scene.preferences.page.info.ReleaseNotesRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
interface HiltModule {
    @Binds
    @ViewModelScoped
    fun bindsReleaseNotesRepository(
        releaseNotesRepositoryImpl: ReleaseNotesRepositoryImpl
    ) : ReleaseNotesRepository
}
