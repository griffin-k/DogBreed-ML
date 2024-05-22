package com.fypsolutions.tflite.myapplication;




import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class login extends AppCompatActivity {

    private static final String VALID_USERNAME = "waqas@zoofari.com";
    private static final String VALID_PASSWORD = "zoofari@123";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EditText usernameEditText = findViewById(R.id.username);
        EditText passwordEditText = findViewById(R.id.password);
        Button loginButton = findViewById(R.id.loginButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                // Check if fields are empty
                if (username.isEmpty()) {
                    Toast.makeText(login.this, "Username cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.isEmpty()) {
                    Toast.makeText(login.this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Validate credentials
                if (username.equals(VALID_USERNAME) && password.equals(VALID_PASSWORD)) {
                    // If login is successful, navigate to the next activity
                    Intent intent = new Intent(login.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    // Show error message
                    Toast.makeText(login.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
