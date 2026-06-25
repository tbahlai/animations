package com.tsynytsyna.animations

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.tsynytsyna.animations.glass_shatter.GlassShatterEffectDemo
import com.tsynytsyna.animations.ui.theme.AnimationsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AnimationsTheme {
                Scaffold { innerPadding ->
                    GlassShatterEffectDemo(Modifier.padding(innerPadding))
                }
            }
        }
    }
}
