package de.ams.techday.aionmobileimagegeneration.imagegeneration.presentation

//import com.google.mediapipe.tasks.vision.imagegenerator.ImageGenerator
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.ams.techday.aionmobileimagegeneration.imagegeneration.generator.ConfigurableImageGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class ImageGeneratorViewModel(
    private var helper: ConfigurableImageGenerator? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val MODEL_PATH = "/data/local/tmp/image_generator/bins/"

    fun updateDisplayIteration(displayIteration: Int) {
        _uiState.update { it.copy(displayIteration = displayIteration) }
    }

    fun updateDisplayOptions(displayOptions: DisplayOptions) {
        _uiState.update { it.copy(displayOptions = displayOptions) }
    }

    fun updatePrompt(prompt: String) {
        _uiState.update { it.copy(prompt = prompt) }
    }

    fun updateIteration(iteration: Int?) {
        _uiState.update { it.copy(iteration = iteration) }
    }

    fun updateSeed(seed: Int?) {
        _uiState.update { it.copy(seed = seed) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearGenerateTime() {
        _uiState.update { it.copy(generateTime = null) }
    }

    fun clearInitializedTime() {
        _uiState.update { it.copy(initializedTime = null) }
    }

    fun initializeImageGenerator() {
        val displayIteration = _uiState.value.displayIteration
        val displayOptions = _uiState.value.displayOptions
        try {
            if (displayIteration == 0 && displayOptions == DisplayOptions.ITERATION) {
                _uiState.update { it.copy(error = "Display iteration cannot be empty") }
                throw IllegalStateException("Invalid display options")
                return
            }

            _uiState.update { it.copy(isInitializing = true) }
            viewModelScope.launch(Dispatchers.IO) {
                delay(500)
                val startTime = System.currentTimeMillis()
                helper?.initializeGenerator(MODEL_PATH)
                    _uiState.update {
                        it.copy(
                            initialized = true,
                            isInitializing = false,
                            initializedTime = System.currentTimeMillis() - startTime,
                        )
                    }
            }

        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    error = e.message
                        ?: "Error initializing image generation model",
                )
            }
        }
    }

    fun generateImage() {
        val prompt = _uiState.value.prompt
        val iteration = _uiState.value.iteration
        val seed = _uiState.value.seed
        val displayIteration = _uiState.value.displayIteration
        var isDisplayStep = false

        if (prompt.isEmpty()) {
            _uiState.update { it.copy(error = "Prompt cannot be empty") }
            return
        }
        if (iteration == null) {
            _uiState.update { it.copy(error = "Iteration cannot be empty") }
            return
        }
        if (seed == null) {
            _uiState.update { it.copy(error = "Seed cannot be empty") }
            return
        }

        _uiState.update {
            it.copy(
                generatingMessage = "Generating...", isGenerating = true
            )
        }

        // Generate with iterations
        viewModelScope.launch(Dispatchers.IO) {
            val startTime = System.currentTimeMillis()
            // if display option is final, use generate method, else use execute method

            _uiState.update { it.copy(
                isGenerating = true,
                generatingMessage = "Generating Image. Please wait .... "
            ) }
            delay(500)
            if (uiState.value.displayOptions == DisplayOptions.FINAL) {
                val result = helper?.generate(prompt, iteration, seed)
                _uiState.update {
                    it.copy(outputBitmap = result)
                }
            } else {
                helper?.setInput(prompt, iteration, seed)
                for (step in 0 until iteration) {
                    isDisplayStep =
                        (displayIteration > 0 && ((step + 1) % displayIteration == 0))
                    val result = helper?.execute(isDisplayStep)

                    if (isDisplayStep) {
                        _uiState.update {
                            it.copy(
                                outputBitmap = result,
                                generatingMessage = "Generating... (${step + 1}/$iteration)",
                            )
                        }
                    }
                }
            }
            _uiState.update {
                it.copy(
                    isGenerating = false,
                    generatingMessage = "Generate",
                    generateTime = System.currentTimeMillis() - startTime,
                )
            }
        }
    }

    fun closeGenerator() {
        helper?.close()
    }
}

data class UiState(
    val error: String? = null,
    val outputBitmap: Bitmap? = null,
    val displayOptions: DisplayOptions = DisplayOptions.FINAL,
    val displayIteration: Int = 5,
    val prompt: String = "",
    val iteration: Int? = null,
    val seed: Int? = null,
    val initialized: Boolean = false,
    val initializedDisplayIteration: Int? = null,
    val isGenerating: Boolean = false,
    val isInitializing: Boolean = false,
    val generatingMessage: String = "",
    val generateTime: Long? = null,
    val initializedTime: Long? = null
)

enum class DisplayOptions {
    ITERATION, FINAL
}


//@HiltViewModel
//class ImageGeneratorViewModel @Inject constructor(
//
//) : ViewModel() {
//
////    private val MODEL_PATH = "/data/local/tmp/image_generator/bins/"
//    private val MODEL_PATH = "/data/local/tmp/llm/gemma-2b-it-gpu-int4.bin"
//
//    private val _state = MutableStateFlow(ImageGeneratorViewState())
//    val state = _state
//
//    private var imageGenerator: ConfigurableImageGenerator? = null
//
//    fun updateDisplayIteration(displayIteration: Int?) {
//        state.update {
//            it.copy(
//                displayIteration = displayIteration
//            )
//        }
//    }
//
//    fun updateSeed(seed: Seed) {
//        state.update {
//            it.copy(seed = seed)
//        }
//    }
//
//    fun updatePrompt(prompt: Prompt) {
//        state.update {
//            it.copy(
//                prompt = prompt
//            )
//        }
//    }
//
//    fun updateIteration(iteration: Iteration) {
//        state.update {
//            it.copy(
//                iteration = iteration
//            )
//        }
//    }
//
//    fun updateDisplayOptions(displayOptions: DisplayOptions) {
//        state.update {
//            it.copy(
//                displayOptions = displayOptions
//            )
//        }
//    }
//
//    fun initializeImageGenerator() {
//        val displayIteration = _state.value.displayIteration
//        val displayOptions = _state.value.displayOptions
//        try {
//            if (displayIteration == null && displayOptions == DisplayOptions.ITERATION) {
//                _state.update { it.copy(error = "Display iteration cannot be empty") }
//                return
//            }
//
//            _state.update { it.copy(isInitializing = true) }
//            val mainLooper = Looper.getMainLooper()
//            GlobalScope.launch {
//                val startTime = System.currentTimeMillis()
//                imageGenerator?.initializeGenerator(MODEL_PATH)
//                Handler(mainLooper).post {
//                    _state.update {
//                        it.copy(
//                            initialized = true,
//                            isInitializing = false,
//                            initializedTime = System.currentTimeMillis() - startTime,
//                        )
//                    }
//                }
//            }
//
//        } catch (e: Exception) {
//            _state.update {
//                it.copy(
//                    error = e.message
//                        ?: "Error initializing image generation model",
//                )
//            }
//        }
//    }
//
//    fun createImageGenerator(context: Context) {
//        imageGenerator = ConfigurableImageGenerator(context)
//    }
//
//    fun generateImage() {
//
//        val prompt = state.value.prompt ?: ""
//        val iteration = state.value.iteration
//        val seed = state.value.seed
//        val displayIteration = state.value.displayIteration ?: 0
//        var isDisplayStep = false
//
//        // validate required inputs
//        when {
//            prompt.isEmpty() -> _state.update { it.copy(prompt = "Prompt must not be empty") }
//            iteration == null -> _state.update { it.copy(prompt = "Iteration must not be empty") }
//            seed == null -> _state.update { it.copy(prompt = "Seed must not be empty") }
//        }
//
//        viewModelScope.launch {
//
//            // TODO FIX THIS!
//            val nonNullIteration = iteration ?: 0
//            val nonNullSeed = seed ?: 0
//
//
//            val generateTime = measureTimeMillis {
//
//                when (state.value.displayOptions) {
//                    DisplayOptions.FINAL -> {
//                        // generate without showing iterations
////                        val result = imageGenerator?.generate(prompt, nonNullIteration, nonNullSeed)
////                        _state.update {
////                            it.copy(
////                                output = result
////                            )
////                        }
//                    }
//
//                    DisplayOptions.ITERATION -> {
//                        imageGenerator?.setInput(prompt, nonNullIteration, nonNullSeed)
//
//                        (0 until nonNullIteration).forEach { step ->
//                            isDisplayStep =
//                                (displayIteration > 0 && ((step + 1) % displayIteration == 0))
////                            val result = imageGenerator?.execute(isDisplayStep)
////
////                            if (isDisplayStep) {
////                                _state.update {
////                                    it.copy(
////                                        output = result,
////                                        processingMessage = "Processing...(${step + 1}/$iteration)"
////                                    )
////                                }
////                            }
//                        }
//
//                    }
//                }
//            }
//            _state.update {
//                it.copy(
//                    isGenerating = false,
//                    generatingMessage = "Generate",
//                    generateTime = generateTime
//                )
//            }
//        }
//    }
//
//    fun closeImageGenerator() {
////        imageGenerator?.close()
//    }
//
//}

//data class ImageGeneratorViewState(
//    val error: String? = null,
//    val outputBitmap: Bitmap? = null,
//    val displayOptions: DisplayOptions = DisplayOptions.FINAL,
//    val displayIteration: Int? = null,
//    val prompt: String = "",
//    val iteration: Int? = null,
//    val seed: Int? = null,
//    val initialized: Boolean = false,
//    val initializedDisplayIteration: Int? = null,
//    val isGenerating: Boolean = false,
//    val isInitializing: Boolean = false,
//    val generatingMessage: String = "",
//    val generateTime: Long? = null,
//    val initializedTime: Long? = null
//)

////data class ImageGeneratorViewState(
////    val output: Bitmap? = null,
////    val displayIteration: Int? = null,
////    val displayOptions: DisplayOptions = DisplayOptions.ITERATION,
////    val seed: Seed? = 0,
////    val iteration: Iteration? = 0,
////    val prompt: Prompt? = "",
////    val isGenerating: Boolean = false,
////
////    // metrics
////    val generateTime: Long = 0L,
////    val processingMessage: String = ""
////)
//
//enum class DisplayOptions {
//    ITERATION,
//    FINAL
//}