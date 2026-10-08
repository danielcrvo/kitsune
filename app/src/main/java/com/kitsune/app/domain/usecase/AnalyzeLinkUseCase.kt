package com.kitsune.app.domain.usecase

import com.kitsune.app.domain.link.UrlDetector
import com.kitsune.app.domain.model.LinkAnalysis
import javax.inject.Inject

class AnalyzeLinkUseCase @Inject constructor() {

    operator fun invoke(url: String): LinkAnalysis = LinkAnalysis(
        url = url,
        platform = UrlDetector.detect(url),
        isValid = UrlDetector.isValidUrl(url),
        isPlaylist = UrlDetector.isPlaylistUrl(url)
    )
}
