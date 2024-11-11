package de.ams.techday.aionmobileimagegeneration.imagegeneration.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.ams.techday.aionmobileimagegeneration.imagegeneration.presentation.DisplayOptions
import de.ams.techday.aionmobileimagegeneration.imagegeneration.presentation.ImageGeneratorViewModel
import de.ams.techday.aionmobileimagegeneration.imagegeneration.presentation.UiState
import de.ams.techday.aionmobileimagegeneration.ui.theme.AiOnMobileImageGenerationTheme
import kotlin.random.Random

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
        viewModel::updateDisplayOptions,
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
    updateDisplayOptions: (DisplayOptions) -> Unit,
    updateIteration: (Int) -> Unit,
    updateSeed: (Int) -> Unit,

    ) {

    Column(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()
    ) {
        when {
            uiState.isInitializing -> {
                IsInitializingSection()
            }

            uiState.initialized.not() -> {
                InitializeSection(
                    onInitializeClick = {
                        initializeImageGenerator()
                    },
                    onSelectedDisplayOptions = { selection ->
                        updateDisplayOptions(selection)
                    },
                    isEnabled = (uiState.displayOptions == DisplayOptions.FINAL || (uiState.displayOptions == DisplayOptions.ITERATION && uiState.displayIteration != 0)) && !uiState.isInitializing
                )
            }

            uiState.initialized -> {
                GenerateSection(
                    prompt = uiState.prompt,
                    iteration = uiState.iteration,
                    seed = uiState.seed,
                    onGenerateClick = { generateImage() },
                    onRandomSeedClick = { updateSeed(Random.nextInt(0, 9999999)) },
                    onPromptChange = { updatePrompt(it) },
                    onIterationChange = {
                        if(it.isNotEmpty()){
                            updateIteration(it.toInt())
                        }
                    },
                    onSeedChange = { updateSeed(it.toInt()) },
                    isEnabled = uiState.prompt.isNotEmpty() && uiState.iteration != null && uiState.seed != null
                )
            }
        }

        // Output Image
        OutputSection(uiState)
    }
}

@Composable
fun OutputSection(uiState: UiState) {
    uiState.outputBitmap?.let {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(16.dp)
        ) {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "Generated Image"
            )

            Text(text = "Inference time: ${uiState.generateTime} ms")
        }
    } ?: if (uiState.isGenerating && uiState.generatingMessage.isNotEmpty()) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text("Model is generating Image")
            CircularProgressIndicator()
        }
    } else {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text("")
        }
    }
}

@Composable
fun IsInitializingSection() {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Initializing Model. Please be patient....")
        Spacer(modifier = Modifier.height(16.dp))
        CircularProgressIndicator()
    }
}

@Composable
fun InitializeSection(
    onInitializeClick: () -> Unit = {},
    isEnabled: Boolean = false,
    onSelectedDisplayOptions: (DisplayOptions) -> Unit = {}
) {

    val radioOptions = listOf("Display Result", "Display Iterations")
    val (selectedOption, onOptionSelected) = remember {
        mutableStateOf(radioOptions[0])
    }

    val mapStringToDisplayOption = { text: String ->
        when (text) {
            "Display Result" -> DisplayOptions.FINAL
            "Display Iterations" -> DisplayOptions.ITERATION
            else -> DisplayOptions.FINAL.also {
                println("Setting Display Options to FINAL")
            }
        }
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
                            onSelectedDisplayOptions(
                                mapStringToDisplayOption(text)
                            )
                        }
                    )
                    .padding(16.dp)
            ) {
                RadioButton(
                    selected = selectedOption == text,
                    onClick = {
                        onOptionSelected(text)
                        onSelectedDisplayOptions(
                            mapStringToDisplayOption(text)
                        )
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
    }
}


@Composable
fun GenerateSection(
    prompt: String,
    iteration: Int?,
    seed: Int?,
    onGenerateClick: () -> Unit = {},
    onRandomSeedClick: () -> Unit = {},
    onPromptChange: (String) -> Unit = {},
    onIterationChange: (String) -> Unit = {},
    onSeedChange: (String) -> Unit = {},
    isEnabled: Boolean
) {

    val focusRequester = remember {
        FocusRequester()
    }

    Column(
        modifier = Modifier
            .padding(8.dp)
    ) {
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
        Row(
            modifier = Modifier
                .padding(8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = seed?.toString() ?: "",
                onValueChange = onSeedChange,
                label = { Text("Seed") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
            )
            Spacer(modifier = Modifier.width(16.dp))
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
                onGenerateClick()
            },
            enabled = isEnabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Generate")
        }
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
            {},
            {}
        )
    }
}

@Composable
@Preview(showBackground = true)
fun ImageGenerateSectionPreview() {
    AiOnMobileImageGenerationTheme {
        GenerateSection(
            prompt = "A prompt",
            iteration = 5,
            seed = 23456,
            isEnabled = true,
        )
    }
}

@Composable
@Preview(showBackground = true)
fun IsInitializingSectionPreview() {
    AiOnMobileImageGenerationTheme {
        IsInitializingSection()
    }
}

@Composable
@Preview(showBackground = true)
fun OutputSectionPreview() {
    AiOnMobileImageGenerationTheme {
        OutputSection(
            UiState(
                isGenerating = true,
                generatingMessage = "Image is generating"
            )
        )
    }
}