package com.dog.tflite.myapplication;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class SelectModel extends AppCompatActivity {

    private Button tensorflowButton, geminiVisionButton, customModelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_model);

        tensorflowButton = findViewById(R.id.tensorflow_lite);
        geminiVisionButton = findViewById(R.id.gemini_vision);
        customModelButton = findViewById(R.id.custom_model);

        tensorflowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleTensorFlowClick();
            }
        });

        geminiVisionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleGeminiVisionClick();
            }
        });

        customModelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleCustomModelClick();
            }
        });
    }

    private void handleTensorFlowClick() {

        Intent intent = new Intent(SelectModel.this, tensor_flow.class);
        startActivity(intent);
    }

    private void handleGeminiVisionClick() {

        Intent intent = new Intent(SelectModel.this, gemini_vision.class);
        startActivity(intent);
    }

    private void handleCustomModelClick() {

        Intent intent = new Intent(SelectModel.this, custom_model.class);
        startActivity(intent);
    }
}
