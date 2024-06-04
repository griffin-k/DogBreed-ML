package com.dog.tflite.myapplication;






import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class Splash extends AppCompatActivity {

    private static final int SPLASH_DISPLAY_LENGTH = 4000; // Duration in milliseconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Delay for SPLASH_DISPLAY_LENGTH and then start the MainActivity
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                // Create an Intent that will start the MainActivity.
                Intent mainIntent = new Intent(Splash.this, option.class);
                startActivity(mainIntent);
                finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}
