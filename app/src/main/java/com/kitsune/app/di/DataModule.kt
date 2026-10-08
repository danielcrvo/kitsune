package com.kitsune.app.di

import com.kitsune.app.data.download.InMemoryDownloadQueueRepository
import com.kitsune.app.data.download.ServiceDownloadScheduler
import com.kitsune.app.data.engine.YtDlpMediaEngine
import com.kitsune.app.data.media.MediaStoreExporter
import com.kitsune.app.data.media.MediaStoreLibraryRepository
import com.kitsune.app.data.network.ConnectivityNetworkMonitor
import com.kitsune.app.data.preferences.DataStorePreferencesRepository
import com.kitsune.app.data.update.GitHubAppUpdateRepository
import com.kitsune.app.domain.repository.AppUpdateRepository
import com.kitsune.app.domain.repository.DownloadQueueRepository
import com.kitsune.app.domain.repository.DownloadScheduler
import com.kitsune.app.domain.repository.MediaEngine
import com.kitsune.app.domain.repository.MediaExporter
import com.kitsune.app.domain.repository.MediaLibraryRepository
import com.kitsune.app.domain.repository.NetworkMonitor
import com.kitsune.app.domain.repository.PreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    abstract fun bindPreferencesRepository(impl: DataStorePreferencesRepository): PreferencesRepository

    @Binds
    abstract fun bindMediaEngine(impl: YtDlpMediaEngine): MediaEngine

    @Binds
    abstract fun bindMediaExporter(impl: MediaStoreExporter): MediaExporter

    @Binds
    abstract fun bindMediaLibraryRepository(impl: MediaStoreLibraryRepository): MediaLibraryRepository

    @Binds
    abstract fun bindDownloadQueueRepository(impl: InMemoryDownloadQueueRepository): DownloadQueueRepository

    @Binds
    abstract fun bindDownloadScheduler(impl: ServiceDownloadScheduler): DownloadScheduler

    @Binds
    abstract fun bindAppUpdateRepository(impl: GitHubAppUpdateRepository): AppUpdateRepository

    @Binds
    abstract fun bindNetworkMonitor(impl: ConnectivityNetworkMonitor): NetworkMonitor
}
