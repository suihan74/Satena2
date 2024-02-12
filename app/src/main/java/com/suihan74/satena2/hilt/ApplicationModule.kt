package com.suihan74.satena2.hilt

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.room.Room
import androidx.room.RoomDatabase
import com.suihan74.satena2.Application
import com.suihan74.satena2.NoticesRepository
import com.suihan74.satena2.NoticesRepositoryImpl
import com.suihan74.satena2.model.AppDatabase
import com.suihan74.satena2.model.dataStore.BrowserPreferences
import com.suihan74.satena2.model.dataStore.Preferences
import com.suihan74.satena2.model.dataStore.browserPreferencesDataStore
import com.suihan74.satena2.model.dataStore.preferencesDataStore
import com.suihan74.satena2.model.migrate
import com.suihan74.satena2.model.theme.ThemePreset
import com.suihan74.satena2.model.theme.default.DefaultThemePresetDark
import com.suihan74.satena2.model.theme.default.DefaultThemePresetExDark
import com.suihan74.satena2.model.theme.default.DefaultThemePresetLight
import com.suihan74.satena2.scene.browser.HistoryRepository
import com.suihan74.satena2.scene.browser.HistoryRepositoryImpl
import com.suihan74.satena2.scene.entries.EntriesRepository
import com.suihan74.satena2.scene.entries.EntriesRepositoryImpl
import com.suihan74.satena2.scene.preferences.PreferencesRepository
import com.suihan74.satena2.scene.preferences.PreferencesRepositoryImpl
import com.suihan74.satena2.scene.preferences.page.accounts.hatena.HatenaAccountRepository
import com.suihan74.satena2.scene.preferences.page.accounts.hatena.HatenaAccountRepositoryImpl
import com.suihan74.satena2.scene.preferences.page.accounts.mastodon.MastodonAccountRepository
import com.suihan74.satena2.scene.preferences.page.accounts.mastodon.MastodonAccountRepositoryImpl
import com.suihan74.satena2.scene.preferences.page.accounts.misskey.MisskeyAccountRepository
import com.suihan74.satena2.scene.preferences.page.accounts.misskey.MisskeyAccountRepositoryImpl
import com.suihan74.satena2.scene.preferences.page.ngWords.NgWordsRepository
import com.suihan74.satena2.scene.preferences.page.ngWords.NgWordsRepositoryImpl
import com.suihan74.satena2.scene.preferences.page.userLabel.UserLabelRepository
import com.suihan74.satena2.scene.preferences.page.userLabel.UserLabelRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {
    private const val APP_DATABASE_FILE_NAME = "satena_db"

    private var appDatabase : AppDatabase? = null

    // ------ //

    /**
     * [Application]のインスタンス自体を供給
     */
    @Provides
    fun provideApplicationInstance(@ApplicationContext context: Context) = context as Application

    // ------ //

    /**
     * アプリ設定データストア
     */
    @Provides
    fun providePreferencesDataStore(
        @ApplicationContext context: Context
    ) : DataStore<Preferences> = context.preferencesDataStore

    /**
     * ブラウザ設定データストア
     */
    @Provides
    fun provideBrowserPreferencesDataStore(
        @ApplicationContext context: Context
    ) : DataStore<BrowserPreferences> = context.browserPreferencesDataStore

    // ------ //

    /**
     * [AppDatabase]
     */
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context) : AppDatabase {
        return appDatabase ?: let {
            val db = Room.databaseBuilder(context, AppDatabase::class.java, APP_DATABASE_FILE_NAME)
                .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
                .migrate()
                .build()
            runBlocking {
                val dao = db.themeDao()
                // アプリテーマ
                if (dao.exists(ThemePreset.CURRENT_THEME_ID) == null) {
                    dao.__insert(DefaultThemePresetLight.copy(id = ThemePreset.CURRENT_THEME_ID, name = ""))
                }
                if (dao.exists("Light") == null) {
                    dao.insert(DefaultThemePresetLight)
                }
                if (dao.exists("Dark") == null) {
                    dao.insert(DefaultThemePresetDark)
                }
                if (dao.exists("ExDark") == null) {
                    dao.insert(DefaultThemePresetExDark)
                }
            }
            appDatabase = db
            db
        }
    }

    // ------ //

    /**
     * [PreferencesRepository]
     */
    @Provides
    @Singleton
    fun providePreferencesRepository(@ApplicationContext context: Context) : PreferencesRepository =
        PreferencesRepositoryImpl(
            application = context as Application,
            dataStore = providePreferencesDataStore(context)
        )

    private var hatenaAccountRepository : HatenaAccountRepository? = null

    /**
     * [NoticesRepository]
     */
    @Provides
    @Singleton
    fun provideNoticesRepository(@ApplicationContext context: Context) : NoticesRepository =
        NoticesRepositoryImpl(
            application = context as Application,
            dataStore = providePreferencesDataStore(context)
        )

    /**
     * [HatenaAccountRepository]
     */
    @Provides
    @Singleton
    fun provideHatenaAccountRepository(@ApplicationContext context: Context) : HatenaAccountRepository =
        hatenaAccountRepository ?: HatenaAccountRepositoryImpl(
            application = context as Application,
            dataStore = providePreferencesDataStore(context)
        ).also { hatenaAccountRepository = it }

    /**
     * [MastodonAccountRepository]
     */
    @Provides
    @Singleton
    fun provideMastodonAccountRepository(@ApplicationContext context: Context) : MastodonAccountRepository =
        MastodonAccountRepositoryImpl(
            application = context as Application,
            dataStore = providePreferencesDataStore(context)
        )

    /**
     * [MisskeyAccountRepository]
     */
    @Provides
    @Singleton
    fun provideMisskeyAccountRepository(@ApplicationContext context: Context) : MisskeyAccountRepository =
        MisskeyAccountRepositoryImpl(
            application = context as Application,
            dataStore = providePreferencesDataStore(context)
        )

    @Provides
    @Singleton
    fun provideEntriesRepository(@ApplicationContext context: Context) : EntriesRepository =
        EntriesRepositoryImpl(
            appDatabase = provideAppDatabase(context),
            hatenaRepo = provideHatenaAccountRepository(context),
            dataStore = providePreferencesDataStore(context)
        )

    /**
     * [NgSettingRepository]
     */
    @Provides
    @Singleton
    fun provideNgSettingsRepository(@ApplicationContext context: Context) : NgWordsRepository =
        NgWordsRepositoryImpl(
            provideAppDatabase(context)
        )

    /**
     * [HistoryRepository]
     */
    @Provides
    @Singleton
    fun provideBrowserHistoryRepository(@ApplicationContext context: Context) : HistoryRepository =
        HistoryRepositoryImpl(
            provideAppDatabase(context)
        )

    /**
     * [UserLabelRepository]
     */
    @Provides
    @Singleton
    fun provideUserLabelRepository(@ApplicationContext context: Context) : UserLabelRepository =
        UserLabelRepositoryImpl(
            provideAppDatabase(context)
        )

    // ------ //

    val Context.appDatabase : AppDatabase
        get() = provideAppDatabase(this.applicationContext)
}
