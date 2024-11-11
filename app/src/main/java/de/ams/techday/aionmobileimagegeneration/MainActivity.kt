package de.ams.techday.aionmobileimagegeneration

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import de.ams.techday.aionmobileimagegeneration.imagegeneration.generator.ConfigurableImageGenerator
import de.ams.techday.aionmobileimagegeneration.imagegeneration.presentation.ImageGeneratorViewModel
import de.ams.techday.aionmobileimagegeneration.imagegeneration.ui.ImageGeneratorScreen
import de.ams.techday.aionmobileimagegeneration.ui.theme.AiOnMobileImageGenerationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AiOnMobileImageGenerationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ImageGeneratorScreen(
                        viewModel = ImageGeneratorViewModel(
                            ConfigurableImageGenerator(LocalContext.current.applicationContext)
                        ),
                        paddingValues = innerPadding
                    )
                }
            }
        }
    }
}