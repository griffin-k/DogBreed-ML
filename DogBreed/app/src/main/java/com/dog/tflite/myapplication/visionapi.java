package com.dog.tflite.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

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

public class visionapi extends AppCompatActivity {
    private TextView textView;
    private ImageView imageView;
    private Bitmap selectedImageBitmap;

    private static final int PICK_IMAGE_REQUEST = 1;
    private Executor mainExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vision);

        textView = findViewById(R.id.textView);
        imageView = findViewById(R.id.imageView);

        mainExecutor = Executors.newSingleThreadExecutor();

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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void buttonImageRecognitionGemini(View view) {
        if (selectedImageBitmap != null) {
            // Clear text view
            textView.setText("");

            GenerativeModel generativeModel = new GenerativeModel("gemini-pro-vision", "AIzaSyAVremZ8j3CWnUxBwZ8jZQUpaY16EB68cY");
            GenerativeModelFutures model = GenerativeModelFutures.from(generativeModel);

            Content content = new Content.Builder()
                    .addText("Act as a Breed classifier of Only Dogs Your job is to classify the Breed of dog. You should not output anything other than the format mentioned below and if the image is not of the dog then return a short text to ask the user to upload the dog image again. You should not accept any other animal image other than the dog. Your Output should be in the format Mentioned Below\n Your Output:Breed Name: breed here \n Color: breed color here \n Confidence: How much sure you are in percentage like 90% or 60% etc.")
                    .addImage(selectedImageBitmap)
                    .build();

            // Show the initial message
            textView.setText("Please wait a while, the model is analyzing...");

            // Delayed updates to textView
            mainExecutor.execute(() -> {
                try {
                    Thread.sleep(2000);
                    runOnUiThread(() -> textView.setText("Model is predicting..."));

                    Thread.sleep(2000);
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
                    runOnUiThread(() -> textView.setText(resultText));
                }

                @Override
                public void onFailure(Throwable t) {
                    runOnUiThread(() -> textView.setText(t.toString()));
                }
            }, mainExecutor);
        } else {
            textView.setText("Please select an image from the gallery first.");
        }
    }
}
