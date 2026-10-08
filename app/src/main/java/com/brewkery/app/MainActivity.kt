package com.brewkery.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.ui.Modifier
import com.brewkery.app.navigation.AppNavHost
import com.brewkery.app.ui.theme.BrewColors
import com.brewkery.app.ui.theme.BrewkeryTheme
import androidx.compose.foundation.background

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BrewkeryTheme {
                Box(Modifier.fillMaxSize().background(BrewColors.Background).systemBarsPadding()) {
                    AppNavHost()
                }
            }
        }
    }
}
