package de.ams.techday.aionmobileimagegeneration.imagegeneration.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.ams.techday.aionmobileimagegeneration.imagegeneration.presentation.DisplayOptions
import de.ams.techday.aionmobileimagegeneration.imagegeneration.presentation.ImageGeneratorViewModel
import de.ams.techday.aionmobileimagegeneration.imagegeneration.presentation.UiState
import de.ams.techday.aionmobileimagegeneration.ui.theme.AiOnMobileImageGenerationTheme

@Composable
fun ImageGeneratorScreen(
    viewModel: ImageGeneratorViewModel = viewModel(),
    paddingValues: PaddingValues
) {

    val viewState by viewModel.uiState.collectAsState()

    ImageGeneratorContent(
        viewState,
        paddingValues,
        viewModel::initializeImageGenerator,
        viewModel::generateImage,
        viewModel::updatePrompt,
        viewModel::updateIteration,
        viewModel::updateSeed
    )

}

@Composable
fun ImageGeneratorContent(
    uiState: UiState,
    paddingValues: PaddingValues,
    initializeImageGenerator: () -> Unit,
    generateImage: () -> Unit,
    updatePrompt: (String) -> Unit,
    updateIteration: (Int) -> Unit,
    updateSeed: (Int) -> Unit,

    ) {
    // Handle UI updates based on uiState
    LaunchedEffect(uiState) {
        // Update UI elements based on uiState changes
        // ... (Similar to the original Activity's collect block)
    }

    // Layout using Compose components
    Column(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()
    ) {
        // Initialize Section
        if (!uiState.initialized) {
            InitializeSection(
                onInitializeClick = { /*viewModel.*/initializeImageGenerator() },
                isEnabled = (uiState.displayOptions == DisplayOptions.FINAL || (uiState.displayOptions == DisplayOptions.ITERATION && uiState.displayIteration != 0)) && !uiState.isInitializing
            )
        }

        // Generate Section
        if (uiState.initialized) {
            GenerateSection(
                prompt = uiState.prompt,
                iteration = uiState.iteration,
                seed = uiState.seed,
                onGenerateClick = { /*viewModel.*/generateImage() },
                onRandomSeedClick = { /* ... */ },
                onPromptChange = { /*viewModel.*/updatePrompt(it) },
                onIterationChange = { /*viewModel.*/updateIteration(it.toInt()) },
                onSeedChange = { /*viewModel.*/updateSeed(it.toInt()) },
                isEnabled = uiState.prompt.isNotEmpty() && uiState.iteration != null && uiState.seed != null
            )
        }

//        // Display Iteration Section
//        if (uiState.displayOptions == DisplayOptions.ITERATION) {
//            DisplayIterationSection(
//                displayIteration = uiState.displayIteration,
//                onDisplayIterationChange = { viewModel.updateDisplayIteration(it) }
//            )
//        }
//
//        // Display Options
//        DisplayOptionsSection(
//            selectedOption = uiState.displayOptions,
//            onOptionSelected = { viewModel.updateDisplayOptions(it) }
//        )

        // Output Image
        uiState.outputBitmap?.let {
            Image(
//                bitmap = uiState.outputBitmap.asImageBitmap(),
                bitmap = it.asImageBitmap(),
                contentDescription = "Generated Image"
            )
        }

        // Error and Time Display
        // ... (Similar to the original Activity's error and time display)
    }

    // Side-effects for closing keyboard, setting default values, etc.
    LaunchedEffect(Unit) {
        // ... (Similar to the original Activity's onCreate and onDestroy logic)
    }
}

// Composable functions for individual sections (InitializeSection, GenerateSection, etc.)
// ... (Implement these based on the original Activity's layout and functionality)

@Composable
fun InitializeSection(
    onInitializeClick: () -> Unit,
    isEnabled: Boolean
) {

    val radioOptions = listOf("Display Result", "Display Iterations")
    val (selectedOption, onOptionSelected) = remember {
        mutableStateOf(radioOptions[0])
    }

    Column {

        radioOptions.forEach { text ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = selectedOption == text,
                        onClick = {
                            onOptionSelected(text)
                        }
                    )
                    .padding(16.dp)
            ) {
                RadioButton(
                    selected = selectedOption == text,
                    onClick = {
                        onOptionSelected(text)
                    }
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium.merge(),
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }

        Button(
            onClick = onInitializeClick,
            enabled = isEnabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Initialize")
        }
        // You can add other UI elements related to initialization here if needed
    }
}


@Composable
fun GenerateSection(
    prompt: String,
    iteration: Int?,
    seed: Int?,
    onGenerateClick: () -> Unit,
    onRandomSeedClick: () -> Unit,
    onPromptChange: (String) -> Unit,
    onIterationChange: (String) -> Unit,
    onSeedChange: (String) -> Unit,
    isEnabled: Boolean
) {

    val focusRequester = remember {
        FocusRequester()
    }

    Column {
        OutlinedTextField(
            value = prompt,
            onValueChange = onPromptChange,
            label = { Text("Prompt") },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
        )
        OutlinedTextField(
            value = iteration?.toString() ?: "",
            onValueChange = onIterationChange,
            label = { Text("Iteration") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
        )
        Row {
            OutlinedTextField(
                value = seed?.toString() ?: "",
                onValueChange = onSeedChange,
                label = { Text("Seed") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
            )
            Button(onClick = {
                focusRequester.requestFocus()
                onRandomSeedClick
            }, modifier = Modifier.weight(1f)) {
                Text("Random Seed")
            }
        }
        Button(
            onClick = {
                focusRequester.requestFocus()
                onGenerateClick
            },
            enabled = isEnabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Generate")
        }
        // You can add other UI elements related to generation here if needed
    }
}


@Composable
@Preview(showBackground = true)
fun ImageGeneratorScreenPreview() {
    AiOnMobileImageGenerationTheme {
        ImageGeneratorContent(
            UiState(),
            paddingValues = PaddingValues(8.dp),
            {},
            {},
            {},
            {},
            {}
        )
    }
}