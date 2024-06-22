package com.dog.tflite.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class custom_model extends AppCompatActivity {

    private ImageView imageView;
    private CardView resultCardView;
    private TextView textView;
    private EditText editTextInput;
    private Button buttonImageRecognition;
    private Bitmap selectedImageBitmap;

    private static final int PICK_IMAGE_REQUEST = 1;
    private Executor mainExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_model);

        // Initialize views
        imageView = findViewById(R.id.imageView);
        resultCardView = findViewById(R.id.resultCardView);
        textView = findViewById(R.id.textView);
        editTextInput = findViewById(R.id.editTextInput);
        buttonImageRecognition = findViewById(R.id.buttonImageRecognition);

        mainExecutor = Executors.newSingleThreadExecutor();

        // Example: Set an image (you would typically load from gallery or camera)
        imageView.setOnClickListener(v -> openGallery());

        // Set the button initially disabled
        buttonImageRecognition.setEnabled(false);

        // Enable the button once an image is selected
        imageView.setOnClickListener(v -> openGallery());
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            try {
                // Get the image from data
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                // Set the bitmap to ImageView
                imageView.setImageBitmap(bitmap);
                selectedImageBitmap = bitmap;

                // Enable the button once an image is selected
                buttonImageRecognition.setEnabled(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void buttonImageRecognitionGemini(View view) {
        if (selectedImageBitmap != null) {
            // Disable the button while the request is being processed
            buttonImageRecognition.setEnabled(false);

            // Clear input field
            editTextInput.setText("");

            GenerativeModel generativeModel = new GenerativeModel("gemini-pro-vision", "AIzaSyAVremZ8j3CWnUxBwZ8jZQUpaY16EB68cY");
            GenerativeModelFutures model = GenerativeModelFutures.from(generativeModel);

            String userInput = editTextInput.getText().toString().trim();

            Content content = new Content.Builder()
                    .addText(userInput + "you act like a dog Breed classification model you only respond related to Dogs if any other question or image Ask to You  have to responde like Please ask my about Dog or Provide me the DOg image and your response must be Short and 2 to point maximum 2 to 3 lines ")
                    .addImage(selectedImageBitmap)
                    .build();

            // Show the initial message
            textView.setText("Please wait a while, the model is analyzing...");

            // Delayed updates to textView
            mainExecutor.execute(() -> {
                try {
                    Thread.sleep(1000);
                    runOnUiThread(() -> textView.setText("Model is predicting..."));

                    Thread.sleep(1000);
                    runOnUiThread(() -> textView.setText("Model is generating response..."));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

            ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
            Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
                @Override
                public void onSuccess(GenerateContentResponse result) {
                    String resultText = result.getText();
                    runOnUiThread(() -> {
                        textView.setText(resultText);
                        buttonImageRecognition.setEnabled(true);
                    });
                }

                @Override
                public void onFailure(Throwable t) {
                    runOnUiThread(() -> {
                        textView.setText(t.toString());
                        buttonImageRecognition.setEnabled(true);
                    });
                }
            }, mainExecutor);
        } else {
            Toast.makeText(this, "Please select an image from the gallery first.", Toast.LENGTH_SHORT).show();
        }
    }
}
