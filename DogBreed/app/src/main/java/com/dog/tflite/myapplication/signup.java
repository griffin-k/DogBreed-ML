package com.dog.tflite.myapplication;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class signup extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        EditText usernameEditText = findViewById(R.id.username);
        EditText emailEditText = findViewById(R.id.email);
        EditText passwordEditText = findViewById(R.id.password);
        Button signupButton = findViewById(R.id.loginButton);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString().trim();
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                // Check if fields are empty
                if (username.isEmpty()) {
                    Toast.makeText(signup.this, "Username cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (email.isEmpty()) {
                    Toast.makeText(signup.this, "Email cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.isEmpty()) {
                    Toast.makeText(signup.this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Add more validation if needed (e.g., email format, password strength)

                // If all fields are valid, navigate to the next activity
                Intent intent = new Intent(signup.this, SelectModel.class);
                startActivity(intent);
            }
        });
    }
}
