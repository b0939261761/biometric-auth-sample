package com.biometric.auth.sample;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.biometric.auth.BiometricCallback;
import com.biometric.auth.BiometricManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {


    private class BiometricManagerCallback implements BiometricCallback {

        @Override
        public void onAuthenticationFailed() {
            Toast.makeText(getApplicationContext(), getString(R.string.biometric_failure), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onAuthenticationSuccessful() {
            Toast.makeText(getApplicationContext(), getString(R.string.biometric_success), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
            Toast.makeText(getApplicationContext(), helpString, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onAuthenticationError(int errorCode, CharSequence errString) {
            Toast.makeText(getApplicationContext(), errString, Toast.LENGTH_LONG).show();
        }
    }


    private Button button;
    private TextView textView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.textView);
        int err = BiometricManager.checkFingerprint(this);
        textView.setText(String.valueOf(err));

        button = findViewById(R.id.btn_authenticate);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BiometricManagerCallback biometricManagerCallback = new BiometricManagerCallback();
                BiometricManager biometricManager = new BiometricManager(MainActivity.this)
                    .setTitle("FingerPrint to \"Open\"")
                    .setDescription("Use your fingerprint to open")
                    .setNegativeButtonText("Use code");
                biometricManager.authenticate(biometricManagerCallback);
            }
        });
    }
}
