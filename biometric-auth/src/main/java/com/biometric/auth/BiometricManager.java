package com.biometric.auth;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.hardware.fingerprint.FingerprintManagerCompat;
import androidx.core.os.CancellationSignal;

@TargetApi(Build.VERSION_CODES.M)
public class BiometricManager {

    public static int READY = 0; // отпечатки работают
    public static int NOT_SUPPORTED = 1; // нет поддержки, Андроид ниже 6 версии
    public static int NO_FINGERPRINTS = 2; // если на устройстве нет отпечатков
    public static int NOT_REGISTER = 3; // если устройство не защищено пином, рисунком или паролем
    public static int NO_PERMISSION = 4; // нет разрешенного доступа к отпечаткам

    private static final String KEY_NAME = UUID.randomUUID().toString();

    private Cipher cipher;
    private KeyStore keyStore;
    private KeyGenerator keyGenerator;
    private FingerprintManagerCompat.CryptoObject cryptoObject;

    private Context context;
    private String title;
    private String description;
    private String negativeButtonText;

    private BiometricDialog biometricDialog;

    private CancellationSignal cancellationSignal = new CancellationSignal();

    public BiometricManager(Context context) {
        this.context = context;
    }

    public BiometricManager setTitle(@NonNull final String title) {
        this.title = title;
        return this;
    }

    public BiometricManager setDescription(@NonNull final String description) {
        this.description = description;
        return this;
    }

    public BiometricManager setNegativeButtonText(@NonNull final String negativeButtonText) {
        this.negativeButtonText = negativeButtonText;
        return this;
    }

    public static int checkFingerprint(@NonNull Context context) {
        FingerprintManagerCompat fingerprintManager = FingerprintManagerCompat.from(context);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return NOT_SUPPORTED;

        if (!fingerprintManager.isHardwareDetected()) return NO_FINGERPRINTS;

        if (!fingerprintManager.hasEnrolledFingerprints()) return NOT_REGISTER;

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) !=
            PackageManager.PERMISSION_GRANTED) return NO_PERMISSION;

        return READY;
    }

    private void closeDialog() { if(biometricDialog != null) biometricDialog.dismiss(); }

    private void successDialog() { if(biometricDialog != null) biometricDialog.successStatus(); }

    private void errorStatus() { if(biometricDialog != null)  biometricDialog.errorStatus(); }

    public void authenticate(final BiometricCallback biometricCallback) {
        generateKey();

        if(initCipher()) {

            cryptoObject = new FingerprintManagerCompat.CryptoObject(cipher);
            FingerprintManagerCompat fingerprintManagerCompat = FingerprintManagerCompat.from(context);

            fingerprintManagerCompat.authenticate(cryptoObject, 0, cancellationSignal,
                    new FingerprintManagerCompat.AuthenticationCallback() {
                        @Override
                        public void onAuthenticationError(int errMsgId, CharSequence errString) {
                            super.onAuthenticationError(errMsgId, errString);
                            if (errMsgId != FingerprintManager.FINGERPRINT_ERROR_CANCELED) {
                                biometricCallback.onAuthenticationError(errMsgId, errString);
                            }
                            closeDialog();
                        }

                        @Override
                        public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
                            super.onAuthenticationHelp(helpMsgId, helpString);
                            errorStatus();
                            biometricCallback.onAuthenticationHelp(helpMsgId, helpString);
                        }

                        @Override
                        public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
                            super.onAuthenticationSucceeded(result);
                            successDialog();
                            biometricCallback.onAuthenticationSuccessful();
                        }

                        @Override
                        public void onAuthenticationFailed() {
                            super.onAuthenticationFailed();
                            errorStatus();
                            biometricCallback.onAuthenticationFailed();
                        }
                    }, null);

            displayBiometricDialog();
        }
    }

    private void displayBiometricDialog() {
        biometricDialog = new BiometricDialog(context, cancellationSignal);
        biometricDialog.setTitle(title);
        biometricDialog.setDescription(description);
        biometricDialog.setButtonText(negativeButtonText);
        biometricDialog.show();
    }

    private void generateKey() {
        try {

            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            keyGenerator.init(new
                    KeyGenParameterSpec.Builder(KEY_NAME, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());

            keyGenerator.generateKey();

        } catch (KeyStoreException
                | NoSuchAlgorithmException
                | NoSuchProviderException
                | InvalidAlgorithmParameterException
                | CertificateException
                | IOException exc) {
            exc.printStackTrace();
        }
    }


    private boolean initCipher() {
        try {
            cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7);

        } catch (NoSuchAlgorithmException |
                NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get Cipher", e);
        }

        try {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME,
                    null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return true;


        } catch (KeyPermanentlyInvalidatedException e) {
            return false;

        } catch (KeyStoreException | CertificateException
                | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException e) {

            throw new RuntimeException("Failed to init Cipher", e);
        }
    }
}
