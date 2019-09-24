package com.biometric.auth;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.os.CancellationSignal;

public class BiometricDialog extends Dialog implements View.OnClickListener {

  private ImageView imgFingerprint;
  private Button btnCancel;
  private TextView itemTitle, itemDescription;

  CancellationSignal cancellationSignal;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Window window = this.getWindow();
    window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
  }

  public void successStatus() {
    imgFingerprint.setColorFilter(Color.parseColor("#0E72F0"));
    imgFingerprint.setImageResource(R.drawable.ic_check);

    Handler handler = new Handler();
    handler.postDelayed(new Runnable() {
      public void run() {
        dismiss();
      }
    },1500);
  }

  public void errorStatus() {
    final ColorFilter colorFilter = imgFingerprint.getColorFilter();
    imgFingerprint.setColorFilter(Color.parseColor("#DF5152"));

    Handler handler = new Handler();
    handler.postDelayed(new Runnable() {
      public void run() {
        imgFingerprint.setColorFilter(Color.parseColor("#0E72F0"));
      }
    },1500);
  }

  @Override
  public void dismiss() {
    super.dismiss();
    if (!cancellationSignal.isCanceled()) cancellationSignal.cancel();
  }

  public BiometricDialog(@NonNull Context context, CancellationSignal cancellationSignal) {
    super(context);
    this.cancellationSignal  = cancellationSignal;
    setDialogView();
  }

  private void setDialogView() {
    View dialog = getLayoutInflater().inflate(R.layout.view_fingerprint, null);
    setContentView(dialog);

    imgFingerprint = findViewById(R.id.img_fingerprint);
    btnCancel = findViewById(R.id.btn_cancel);
    btnCancel.setOnClickListener(this);

    itemTitle = findViewById(R.id.item_title);
    itemDescription = findViewById(R.id.item_description);
  }

  public void setTitle(String title) { itemTitle.setText(title); }

  public void setDescription(String description) { itemDescription.setText(description); }

  public void setButtonText(String negativeButtonText) { btnCancel.setText(negativeButtonText); }

  @Override
  public void onClick(View view) { dismiss(); }

}
