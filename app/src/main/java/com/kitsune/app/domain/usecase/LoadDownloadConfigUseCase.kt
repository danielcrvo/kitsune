package com.kitsune.app.domain.usecase

import com.kitsune.app.domain.model.DownloadConfig
import com.kitsune.app.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class LoadDownloadConfigUseCase @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) {

    suspend operator fun invoke(): DownloadConfig {
        val savedConfig = preferencesRepository.downloadConfig.first()
        val savedMode = preferencesRepository.downloadMode.first()
        return savedConfig.withMode(savedMode)
    }
}
