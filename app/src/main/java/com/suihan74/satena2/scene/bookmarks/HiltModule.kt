package com.suihan74.satena2.scene.bookmarks

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
    fun bindsBookmarksRepository(
        bookmarksRepository: BookmarksRepositoryImpl
    ) : BookmarksRepository
}
