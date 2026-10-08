package com.kitsune.app.domain.usecase

import com.kitsune.app.domain.model.AppUpdateInfo
import com.kitsune.app.testing.FakeAppUpdateRepository
import com.kitsune.app.testing.FakePreferencesRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CheckForAppUpdateUseCaseTest {

    private val update = AppUpdateInfo(
        versionName = "1.3.0",
        releaseTitle = "v1.3.0",
        releaseNotes = "",
        downloadUrl = "https://example.com/app.apk",
        apkFileName = "app.apk",
        fileSizeBytes = 1L
    )

    @Test
    fun silentCheckRespectsAutoCheckPreference() = runTest {
        val repository = FakeAppUpdateRepository(checkResult = Result.success(update))
        val useCase = CheckForAppUpdateUseCase(repository, FakePreferencesRepository(autoCheckUpdates = false))

        assertNull(useCase(isManualCheck = false).getOrNull())
        assertEquals(0, repository.checkCount)

        assertEquals(update, useCase(isManualCheck = true).getOrNull())
        assertEquals(listOf("1.3.0"), repository.notifiedVersions)
    }

    @Test
    fun disabledUpdaterNeverChecks() = runTest {
        val repository = FakeAppUpdateRepository(isUpdaterEnabled = false, checkResult = Result.success(update))
        val useCase = CheckForAppUpdateUseCase(repository, FakePreferencesRepository())

        assertTrue(useCase(isManualCheck = true).isSuccess)
        assertNull(useCase(isManualCheck = true).getOrNull())
        assertEquals(0, repository.checkCount)
    }
}
