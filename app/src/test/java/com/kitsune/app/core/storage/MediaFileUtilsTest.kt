package com.kitsune.app.core.storage

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class MediaFileUtilsTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun createFile(name: String, sizeBytes: Int): File {
        return tempFolder.newFile(name).apply { writeBytes(ByteArray(sizeBytes)) }
    }

    @Test
    fun sanitizeFileNamePreservesUnicodeAndRemovesIllegalCharacters() {
        assertEquals("Canção de Ninar 日本語", MediaFileUtils.sanitizeFileName("Canção de Ninar 日本語"))
        assertEquals("AC_DC _ Back in Black", MediaFileUtils.sanitizeFileName("AC/DC | Back in Black"))
        assertEquals("a b", MediaFileUtils.sanitizeFileName("  a\t\tb  "))
        assertEquals("hidden", MediaFileUtils.sanitizeFileName("..hidden.."))
    }

    @Test
    fun sanitizeFileNameTruncatesWithoutSplittingSurrogatePairs() {
        val name = "a".repeat(9) + "😀"
        val result = MediaFileUtils.sanitizeFileName(name, maxLength = 10)
        assertEquals("a".repeat(9), result)
    }

    @Test
    fun fallbackMimeTypeMatchesExtension() {
        assertEquals("audio/mpeg", MediaFileUtils.fallbackMimeType("mp3", isAudio = true))
        assertEquals("audio/mp4", MediaFileUtils.fallbackMimeType("M4A", isAudio = true))
        assertEquals("audio/ogg", MediaFileUtils.fallbackMimeType("opus", isAudio = true))
        assertEquals("audio/webm", MediaFileUtils.fallbackMimeType("webm", isAudio = true))
        assertEquals("video/webm", MediaFileUtils.fallbackMimeType("webm", isAudio = false))
        assertEquals("video/x-matroska", MediaFileUtils.fallbackMimeType("mkv", isAudio = false))
        assertEquals("video/mp4", MediaFileUtils.fallbackMimeType("unknown", isAudio = false))
    }

    @Test
    fun intermediateFilesAreDetected() {
        assertTrue(MediaFileUtils.isIntermediateFile(File("video.mp4.part")))
        assertTrue(MediaFileUtils.isIntermediateFile(File("video.f137.mp4")))
        assertTrue(MediaFileUtils.isIntermediateFile(File("video.en.vtt")))
        assertTrue(MediaFileUtils.isIntermediateFile(File("video.webp")))
        assertFalse(MediaFileUtils.isIntermediateFile(File("video.mp4")))
        assertFalse(MediaFileUtils.isIntermediateFile(File("song.mp3")))
    }

    @Test
    fun selectFinalOutputFileIgnoresPartialAndAuxiliaryFiles() {
        createFile("clip.mp4.part", 5000)
        createFile("clip.f137.mp4", 4000)
        createFile("clip.en.vtt", 3000)
        val expected = createFile("clip [abc].mp4", 1000)
        createFile("empty.mp4", 0)

        val selected = MediaFileUtils.selectFinalOutputFile(tempFolder.root.listFiles()!!.toList())
        assertEquals(expected.name, selected?.name)
    }

    @Test
    fun selectFinalOutputFileReturnsNullWhenOnlyPartialFilesExist() {
        createFile("clip.mp4.part", 5000)
        assertNull(MediaFileUtils.selectFinalOutputFile(tempFolder.root.listFiles()!!.toList()))
    }

    @Test
    fun uniqueFileAppendsCounterWhenNameIsTaken() {
        createFile("Song.mp3", 10)
        createFile("Song (1).mp3", 10)
        val result = MediaFileUtils.uniqueFile(tempFolder.root, "Song", "mp3")
        assertEquals("Song (2).mp3", result.name)
    }
}
