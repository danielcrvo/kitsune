package com.kitsune.app.ui.screens

import android.app.Application
import org.junit.Assert.assertNotNull
import org.junit.Test

class MainViewModelConstructorTest {

    @Test
    fun mainViewModelHasApplicationConstructorForViewModelProvider() {
        val constructor = MainViewModel::class.java.getConstructor(Application::class.java)
        assertNotNull(constructor)
    }
}
