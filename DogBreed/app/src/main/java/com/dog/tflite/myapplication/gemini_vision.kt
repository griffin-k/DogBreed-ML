package com.dog.tflite.myapplication
import android.Manifest.permission.CAMERA
import android.Manifest.permission.RECORD_AUDIO
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Bitmap.createScaledBitmap
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.dog.tflite.myapplication.ml.MovinetA0Model
import com.vmadalin.easypermissions.EasyPermissions
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Locale


class gemini_vision : AppCompatActivity(), EasyPermissions.PermissionCallbacks {
    private val TAG: String ="MAIN_ACTIVITY_DETECTION"
    private lateinit var imageView: ImageView
    private lateinit var ll_buttons: LinearLayout
    private lateinit var btnPredict: Button
    private lateinit var btnRequest: Button
    private lateinit var tvResult: TextView
    private lateinit var classifier: Classifier
    private lateinit var videoClassifier: VClassifier


    /*



    val model = Model.newInstance(context)

    // Creates inputs for reference.
    val image = TensorImage.fromBitmap(bitmap)

    // Runs model inference and gets result.
    val outputs = model.process(image)
    val detectionResult = outputs.detectionResultList.get(0)

    // Gets result from DetectionResult.
    val location = detectionResult.locationAsRectF;
    val category = detectionResult.categoryAsString;
    val score = detectionResult.scoreAsFloat;

    // Releases model resources if no longer used.
    model.close()
     */

    private fun initControls(){
        ll_buttons = findViewById(R.id.linearlayoutButtons)
        btnPredict = findViewById(R.id.btnPredict)
        btnRequest = findViewById(R.id.btnRequest)
        imageView = findViewById(R.id.image_view)
        tvResult = findViewById(R.id.tvResult)

    }

    private  fun enableButtons(){
        ll_buttons.visibility = View.VISIBLE
        btnRequest.visibility = View.GONE
        btnPredict.visibility  = View.VISIBLE
    }
    private fun disableButtons(){
        ll_buttons.visibility = View.GONE
        btnRequest.visibility = View.VISIBLE
        btnPredict.visibility  = View.GONE
    }
    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        disableButtons()
        Toast.makeText(baseContext,"permission denied",Toast.LENGTH_SHORT).show()
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        enableButtons()
        Toast.makeText(baseContext,"permission Granted",Toast.LENGTH_SHORT).show()
    }

    private fun requestPermissions() {
        EasyPermissions.requestPermissions(
            this,
            "We need these permissions to keep functional",
            REQUEST_CODE_LOAD_IMAGE,
            CAMERA, RECORD_AUDIO
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }



    private fun hasPermissions() =
        EasyPermissions.hasPermissions(
            this,
            CAMERA,
            RECORD_AUDIO,


            )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gemini_vision)
        initClassifier();
        initVideoClassifier();
        initControls();

        if (hasPermissions()){
            enableButtons()
        }else{
            disableButtons()
        }

        btnRequest.setOnClickListener {
            requestPermissions()
        }


        val loadImageButton = findViewById<Button>(R.id.load_image_button)
        val captureImageButton = findViewById<Button>(R.id.capture_image_button)
        val recordVideoButton = findViewById<Button>(R.id.record_video_button)
        btnPredict.visibility = View.GONE
        btnPredict.setOnClickListener {

//            if(resizedBitmap==null){
//                Toast.makeText(baseContext,"No bitmap found",Toast.LENGTH_SHORT).show()
//            }else {
//                val model = MobileNetModel.newInstance(applicationContext)
//
//                // Calculate byte buffer size
//                val dataType = DataType.FLOAT32
//                val numPixels = resizedBitmap!!.width * resizedBitmap!!.height * 4
//                val bytesPerPixel = dataType.byteSize()
//                val byteBuffer = ByteBuffer.allocateDirect(numPixels * bytesPerPixel)
//
//                // Convert bitmap pixels to byte buffer
//                byteBuffer.rewind()
//                resizedBitmap!!.copyPixelsToBuffer(byteBuffer)
//
//                // Create TensorBuffer and load data
//                val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 128, 128, 3), dataType)
//                inputFeature0.loadBuffer(byteBuffer)
//
//                // Runs model inference and gets result.
//                val outputs = model.process(inputFeature0)
//                val outputFeature0 = outputs.outputFeature0AsTensorBuffer
//                print(outputFeature0)
//
//                // Releases model resources if no longer used.
//                model.close()
//            }
        }



        loadImageButton.setOnClickListener {
            // Request storage permission for loading images
            // Toast.makeText(baseContext,"wait...",Toast.LENGTH_SHORT).show();
            if(hasPermissions()){
                openGallery()
            }else{
                requestPermissions()
            }
        }

        captureImageButton.setOnClickListener {
            // Check camera permission before requesting
            /*if (checkSelfPermission(CAMERA) == PackageManager.PERMISSION_GRANTED) {
                captureImage()
            } else {
                // Request permission if not granted
                requestPermissions(
                    arrayOf(CAMERA),
                    REQUEST_CODE_CAPTURE_IMAGE
                )
            }*/

            if(hasPermissions()){
                captureImage()
            }else{
                requestPermissions()
            }
        }



        recordVideoButton.setOnClickListener {
            // Request camera and storage permission for recording video
            /*requestPermissions(
                arrayOf(
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.RECORD_AUDIO,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                REQUEST_CODE_RECORD_VIDEO
            )*/

            if(hasPermissions()){
                captureVideo()
            }else{
                requestPermissions()
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->

        val granted = permissions.all { it.value }
        if (granted) {
            Toast.makeText(baseContext, "Permission Granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(baseContext, "Permission denied", Toast.LENGTH_SHORT).show()

        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_CODE_LOAD_IMAGE)
    }

    private fun captureImage() {

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_CODE_CAPTURE_IMAGE)
    }

    private fun captureVideo() {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        startActivityForResult(intent, REQUEST_CODE_RECORD_VIDEO)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
            //val selectedImage = data.data
            val selectedImageUri = data.data
            var bitmap:Bitmap? = null
            try {
                bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    // For Android versions 28 (Pie) and above
                    val source = ImageDecoder.createSource(contentResolver, selectedImageUri!!)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    // For Android versions below 28
                    MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri)
                }
            } catch (exception: IOException) {
                Toast.makeText(this, "Can not load image!!", Toast.LENGTH_SHORT).show()
            }


            bitmap?.let {
                val output = classifier.classify(bitmap!!)
                val resultStr =
                    String.format(Locale.ENGLISH, "Breed : %s, confidence : %.2f%%", output.first, output.second * 100)
                print(resultStr)
                imageView.setImageBitmap(bitmap)
                tvResult.text = resultStr
            }


            //  runObjectDetection(bitmap)
        }

        else if (requestCode == REQUEST_CODE_CAPTURE_IMAGE && resultCode == RESULT_OK) {
            val capturedImage = data?.extras?.get("data") as Bitmap?
            if (capturedImage != null) {
                imageView.setImageBitmap(capturedImage)
                var bitmap:Bitmap? = null;
                bitmap = capturedImage
                bitmap?.let {
                    val output = classifier.classify(bitmap)
                    val resultStr =
                        String.format(Locale.ENGLISH, "Breed : %s, Confidence : %.2f%%", output.first, output.second * 100)
                    print(resultStr)
                    imageView.setImageBitmap(bitmap)
                    tvResult.text = resultStr
                }

            } else {
                // Handle potential error (e.g., image capture failed)
                Toast.makeText(this, "Failed to capture image.", Toast.LENGTH_SHORT).show()
            }
        }
        else if (requestCode == REQUEST_CODE_RECORD_VIDEO && resultCode == RESULT_OK) {
            val videoUri = data?.data
            if (videoUri != null) {
                // Extract frames from the captured video
                val frameImages = extractFrames(videoUri)
                if (frameImages.isNotEmpty()) {
                    // Combine frames into a single image with a 4x3 grid
                    val combinedImage = combineImages(frameImages)
                    imageView.setImageBitmap(combinedImage)


                    combinedImage.let {
                        val output1 = classifier.classify(frameImages[4])
//                        if(output1.first.contains("german") or output1.first.contains("malamute")){
                        val output = videoClassifier.classify(combinedImage)
                        val resultStr =
                            String.format(Locale.ENGLISH, "Breed : %s, Confidence : %.2f%%", output.first, output.second * 100)
                        print(resultStr)
                        imageView.setImageBitmap(combinedImage)
                        tvResult.text = resultStr
//                        }else{
//                            tvResult.text = "Video is only trained on German Shephered, but found ${output1.first}"
//                        }

                    }
                    Toast.makeText(this, "success to extract frames from the video.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to extract frames from the video.", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }





    private val NUM_FRAMES = 32
    private val FRAME_WIDTH = 200
    private val FRAME_HEIGHT = 200

    private fun combineImages(images: List<Bitmap>): Bitmap {
        // Combine the images into a single image with a 4x3 grid
        val combinedWidth = images[0].width * 2
        val combinedHeight = images[0].height * 2
        val combinedBitmap = Bitmap.createBitmap(combinedWidth, combinedHeight, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(combinedBitmap)

        var x = 0
        var y = 0
        for (bitmap in images) {
            canvas.drawBitmap(bitmap, x.toFloat(), y.toFloat(), null)
            x += bitmap.width
            if (x >= combinedWidth) {
                x = 0
                y += bitmap.height
            }
        }

        return combinedBitmap
    }

    private fun extractFrames(videoUri: Uri): List<Bitmap> {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(this, videoUri)

        val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0
        val interval = duration / NUM_FRAMES

        val frameImages = mutableListOf<Bitmap>()

        for (i in 0 until NUM_FRAMES) {
            val timeUs = (i * interval * 1000).coerceAtMost(duration - 1000)
            val frameBitmap = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            val resizedBitmap =
                frameBitmap?.let { Bitmap.createScaledBitmap(it, FRAME_WIDTH, FRAME_HEIGHT, false) }
            if (resizedBitmap != null) {
                frameImages.add(resizedBitmap)
            }
        }

        retriever.release()
        return frameImages
    }


    /**
     * TFLite Object Detection Function
     * ref: https://developers.google.com/codelabs/tflite-object-detection-android#4
     */

    private fun initClassifier() {


        videoClassifier = VClassifier(this, Classifier.IMAGENET_CLASSIFY_MODEL)
        try {
            videoClassifier.init()
        } catch (exception: IOException) {
            Toast.makeText(this, "Can not init Classifier!!", Toast.LENGTH_SHORT).show()
        }
    }
    private fun initVideoClassifier() {
        classifier = Classifier(this, Classifier.IMAGENET_CLASSIFY_MODEL)
        try {
            classifier.init()
        } catch (exception: IOException) {
            Toast.makeText(this, "Can not init Classifier!!", Toast.LENGTH_SHORT).show()
        }

    }


//    fun processVideoFrames43(frames: List<Bitmap>) {
//        val model = MovinetA0Model.newInstance(baseContext)
//
//        // Define the required dimensions
//        val frameCount = 43
//        val frameHeight = 172
//        val frameWidth = 172
//        val frameChannels = 3
//
//        // Create a ByteBuffer to hold the input data
//        val byteBuffer = ByteBuffer.allocateDirect(frameCount * frameHeight * frameWidth * frameChannels * 4)
//        byteBuffer.order(ByteOrder.nativeOrder())
//
//        // Resize and add each frame to the ByteBuffer
//        for (frame in frames) {
//            val resizedFrame = createScaledBitmap(frame, frameWidth, frameHeight, true)
//            val intValues = IntArray(frameWidth * frameHeight)
//            resizedFrame.getPixels(intValues, 0, frameWidth, 0, 0, frameWidth, frameHeight)
//
//            // Convert pixel values to float and add to the ByteBuffer
//            for (pixelValue in intValues) {
//                byteBuffer.putFloat((pixelValue shr 16 and 0xFF) / 255.0f)  // R
//                byteBuffer.putFloat((pixelValue shr 8 and 0xFF) / 255.0f)   // G
//                byteBuffer.putFloat((pixelValue and 0xFF) / 255.0f)         // B
//            }
//        }
//
//        // Prepare the input tensor
//        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, frameCount, frameHeight, frameWidth, frameChannels), DataType.FLOAT32)
//        inputFeature0.loadBuffer(byteBuffer)
//
//        // Create other necessary input features with dummy data
//        // Modify these as needed for your specific model requirements
//        val dummyBuffer = ByteBuffer.allocateDirect(4).apply { order(ByteOrder.nativeOrder()); putInt(0) }
//
//        val inputFeature1 = TensorBuffer.createFixedSize(intArrayOf(1, 2, 22, 22, 80), DataType.FLOAT32)
//        inputFeature1.loadBuffer(dummyBuffer)
//        // Repeat for other input features if needed, with proper shapes and data types
//
//        // Run model inference
//        val outputs = model.process(inputFeature0 )
//
//        // Get the output tensor buffers
//        val outputFeature0 = outputs.outputFeature0AsTensorBuffer
//        // Retrieve other output features if needed
//
//        // Release model resources
//        model.close()
//    }





    fun processSingleFrame(frame: Bitmap): TensorBuffer {
        // Define the required dimensions for the model input
        val frameHeight = 172
        val frameWidth = 172
        val frameChannels = 3

        // Resize the frame to the required dimensions
        val resizedFrame = createScaledBitmap(frame, frameWidth, frameHeight, true)

        // Create a ByteBuffer to hold the input data
        val byteBuffer = ByteBuffer.allocateDirect(frameHeight * frameWidth * frameChannels * 4)
        byteBuffer.order(ByteOrder.nativeOrder())

        // Get the pixel values from the bitmap and put them into the ByteBuffer
        val intValues = IntArray(frameWidth * frameHeight)
        resizedFrame.getPixels(intValues, 0, frameWidth, 0, 0, frameWidth, frameHeight)

        // Convert pixel values to float and add to the ByteBuffer
        for (pixelValue in intValues) {
            byteBuffer.putFloat((pixelValue shr 16 and 0xFF) / 255.0f)  // R
            byteBuffer.putFloat((pixelValue shr 8 and 0xFF) / 255.0f)   // G
            byteBuffer.putFloat((pixelValue and 0xFF) / 255.0f)         // B
        }

        // Create a TensorBuffer with the appropriate shape and load the ByteBuffer
        val inputFeature = TensorBuffer.createFixedSize(intArrayOf(1, frameHeight, frameWidth, frameChannels), DataType.FLOAT32)
        inputFeature.loadBuffer(byteBuffer)

        return inputFeature
    }



    fun processVideoFrames43(context: Context, frames: List<Bitmap>) {
        if (frames.size != 43) {
            throw IllegalArgumentException("The function expects exactly 43 frames")
        }

        val model = MovinetA0Model.newInstance(context)

        // Create a list to hold the input features
        val inputFeatures = mutableListOf<TensorBuffer>()

        // Process each frame and add to the list of input features
        for (frame in frames) {
            inputFeatures.add(processSingleFrame(frame))
        }

        // Create the input tensors for the model
        val inputs = inputFeatures.toTypedArray()

        // If your model accepts additional dummy inputs, initialize them here
        val dummyIntBuffer = ByteBuffer.allocateDirect(4).apply { order(ByteOrder.nativeOrder()); putInt(0) }
        val dummyFloatBuffer = ByteBuffer.allocateDirect(4).apply { order(ByteOrder.nativeOrder()); putFloat(0.0f) }



        model.close()
    }


    companion object {
        private const val REQUEST_CODE_LOAD_IMAGE = 101
        private const val REQUEST_CODE_CAPTURE_IMAGE = 102
        private const val REQUEST_CODE_RECORD_VIDEO = 103
    }
}