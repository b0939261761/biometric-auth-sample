package com.biometric.auth;

public interface BiometricCallback {
    void onAuthenticationFailed(); //отпечаток считался, но не распознался

    void onAuthenticationSuccessful(); //все прошло успешно

    void onAuthenticationHelp(int helpCode, CharSequence helpString); //грязные пальчики, недостаточно сильный зажим

    void onAuthenticationError(int errorCode, CharSequence errString); //несколько неудачных попыток считывания (5)

}
