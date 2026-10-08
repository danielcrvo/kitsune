package com.kitsune.app.domain.usecase

import com.kitsune.app.domain.model.AppUpdateInfo
import com.kitsune.app.domain.repository.AppUpdateRepository
import com.kitsune.app.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CheckForAppUpdateUseCase @Inject constructor(
    private val appUpdateRepository: AppUpdateRepository,
    private val preferencesRepository: PreferencesRepository
) {

    suspend operator fun invoke(isManualCheck: Boolean): Result<AppUpdateInfo?> {
        if (!appUpdateRepository.isUpdaterEnabled) return Result.success(null)
        if (!isManualCheck && !preferencesRepository.isAutoCheckUpdates.first()) {
            return Result.success(null)
        }
        return appUpdateRepository.checkForUpdate().onSuccess { info ->
            if (info != null) appUpdateRepository.notifyUpdateAvailable(info)
        }
    }
}
