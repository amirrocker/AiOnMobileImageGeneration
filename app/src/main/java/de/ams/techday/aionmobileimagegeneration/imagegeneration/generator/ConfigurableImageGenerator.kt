package de.ams.techday.aionmobileimagegeneration.imagegeneration.generator

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.google.mediapipe.framework.image.BitmapExtractor
import com.google.mediapipe.tasks.vision.imagegenerator.ImageGenerator
import com.google.mediapipe.tasks.vision.imagegenerator.ImageGenerator.ImageGeneratorOptions
import javax.inject.Inject

class ConfigurableImageGenerator @Inject constructor(
    val context: Context
) {

    // use hilt once it works
    private lateinit var imageGenerator: ImageGenerator

    suspend fun initializeGenerator(modelPath: Path) {
        val options = ImageGeneratorOptions.builder()
            .setImageGeneratorModelDirectory(modelPath)
            .build()
        imageGenerator = ImageGenerator.createFromOptions(context, options)
    }

    fun setInput(prompt: Prompt, iteration: Iteration, seed: Seed) {
        // pass the desired input prompt to the generator
        imageGenerator.setInputs(prompt, iteration, seed)
    }

    val width = 256
    val height = 256

    // this task happens synchronously! It must be called
    // in a coroutine or background thread
    fun generate(prompt: Prompt, iteration: Iteration, seed: Seed):Bitmap {
        val result = imageGenerator.generate(prompt, iteration, seed)
        val bitmap = BitmapExtractor.extract(result?.generatedImage())
        return bitmap
    }

    // generate with iterations
    fun execute(showResult: Boolean):Bitmap {

        val result = imageGenerator.execute(showResult)

        if(result?.generatedImage() == null) {
            return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888 )
                .apply {
                    val canvas = Canvas(this)
                    val paint = Paint()
                    paint.color = Color.WHITE
                    canvas.drawPaint(paint)
                }
        }

        val bitmap = BitmapExtractor.extract(result.generatedImage())
        return bitmap
    }

    fun close() {
        try {
            imageGenerator.close()
        } catch(e:Exception) {
            e.printStackTrace()
        }
    }

}

internal typealias Prompt = String
internal typealias Iteration = Int
internal typealias Seed = Int
internal typealias Path = String