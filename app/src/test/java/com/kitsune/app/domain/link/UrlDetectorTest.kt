package com.kitsune.app.domain.link

import com.kitsune.app.domain.model.PlatformType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UrlDetectorTest {

    @Test
    fun `detecta links do YouTube corretamente`() {
        val standard = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
        val short = "https://youtu.be/dQw4w9WgXcQ"
        val shorts = "https://www.youtube.com/shorts/abcdef12345"

        assertEquals(PlatformType.YOUTUBE, UrlDetector.detect(standard))
        assertEquals(PlatformType.YOUTUBE, UrlDetector.detect(short))
        assertEquals(PlatformType.YOUTUBE, UrlDetector.detect(shorts))
    }

    @Test
    fun `detecta links do TikTok corretamente`() {
        val vmLink = "https://vm.tiktok.com/ZMh123456/"
        val standardLink = "https://www.tiktok.com/@user/video/1234567890"

        assertEquals(PlatformType.TIKTOK, UrlDetector.detect(vmLink))
        assertEquals(PlatformType.TIKTOK, UrlDetector.detect(standardLink))
    }

    @Test
    fun `detecta links do Instagram corretamente`() {
        val reel = "https://www.instagram.com/reel/C8abcde1234/"
        val post = "https://www.instagram.com/p/C8abcde1234/"

        assertEquals(PlatformType.INSTAGRAM, UrlDetector.detect(reel))
        assertEquals(PlatformType.INSTAGRAM, UrlDetector.detect(post))
    }

    @Test
    fun `detecta links do Bilibili corretamente`() {
        val bv = "https://www.bilibili.com/video/BV1xx411c7mD"
        val b23 = "https://b23.tv/abcdef1"

        assertEquals(PlatformType.BILIBILI, UrlDetector.detect(bv))
        assertEquals(PlatformType.BILIBILI, UrlDetector.detect(b23))
    }

    @Test
    fun `detecta links do Twitter e X corretamente`() {
        val twitter = "https://twitter.com/user/status/1234567890123456789"
        val x = "https://x.com/user/status/1234567890123456789"

        assertEquals(PlatformType.TWITTER, UrlDetector.detect(twitter))
        assertEquals(PlatformType.TWITTER, UrlDetector.detect(x))
    }

    @Test
    fun `detecta links do Reddit corretamente`() {
        val reddit = "https://www.reddit.com/r/videos/comments/123456/cool_video/"
        val reddIt = "https://redd.it/123456"

        assertEquals(PlatformType.REDDIT, UrlDetector.detect(reddit))
        assertEquals(PlatformType.REDDIT, UrlDetector.detect(reddIt))
    }

    @Test
    fun `detecta links do SoundCloud corretamente`() {
        val sc = "https://soundcloud.com/artist/cool-track"
        assertEquals(PlatformType.SOUNDCLOUD, UrlDetector.detect(sc))
    }

    @Test
    fun `sanitiza parametros de rastreamento de URLs`() {
        val ytWithTracking = "https://youtu.be/dQw4w9WgXcQ?si=abcdef123456&utm_source=share"
        val sanitizedYt = UrlDetector.sanitizeUrl(ytWithTracking)
        assertEquals("https://youtu.be/dQw4w9WgXcQ", sanitizedYt)

        val instaWithTracking = "https://www.instagram.com/reel/C8abcde1234/?igsh=xyz123&utm_medium=copy_link"
        val sanitizedInsta = UrlDetector.sanitizeUrl(instaWithTracking)
        assertEquals("https://www.instagram.com/reel/C8abcde1234/", sanitizedInsta)

        val xWithTracking = "https://x.com/user/status/1234567890123456789?s=20&t=abcdef"
        val sanitizedX = UrlDetector.sanitizeUrl(xWithTracking)
        assertEquals("https://x.com/user/status/1234567890123456789", sanitizedX)
    }

    @Test
    fun `sanitizacao preserva codificacao, fragmentos e timestamps`() {
        val encoded = "https://example.com/watch?q=rock%26roll&utm_campaign=x#section"
        assertEquals("https://example.com/watch?q=rock%26roll#section", UrlDetector.sanitizeUrl(encoded))

        val ytTimestamp = "https://www.youtube.com/watch?v=dQw4w9WgXcQ&t=42&si=abc"
        assertEquals("https://www.youtube.com/watch?v=dQw4w9WgXcQ&t=42", UrlDetector.sanitizeUrl(ytTimestamp))

        val genericS = "https://example.com/video?s=7"
        assertEquals(genericS, UrlDetector.sanitizeUrl(genericS))

        val noQuery = "https://youtu.be/dQw4w9WgXcQ"
        assertEquals(noQuery, UrlDetector.sanitizeUrl("  $noQuery  "))
    }

    @Test
    fun `valida URLs invalidas`() {
        assertFalse(UrlDetector.isValidUrl(""))
        assertFalse(UrlDetector.isValidUrl("   "))
        assertFalse(UrlDetector.isValidUrl("not a valid link"))
        assertTrue(UrlDetector.isValidUrl("https://example.com/video.mp4"))
    }

    @Test
    fun `detecta playlists corretamente`() {
        val ytPlaylist = "https://www.youtube.com/playlist?list=PL1234567890"
        val ytVideoInPlaylist = "https://www.youtube.com/watch?v=dQw4w9WgXcQ&list=PL1234567890"
        val soundcloudSet = "https://soundcloud.com/artist/sets/album-name"
        val normalVideo = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"

        assertTrue(UrlDetector.isPlaylistUrl(ytPlaylist))
        assertTrue(UrlDetector.isPlaylistUrl(ytVideoInPlaylist))
        assertTrue(UrlDetector.isPlaylistUrl(soundcloudSet))
        assertFalse(UrlDetector.isPlaylistUrl(normalVideo))
        assertFalse(UrlDetector.isPlaylistUrl(""))
    }
}
