package com.bseb.bsebclass12thartsobjective;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;

public class Loading extends Dialog {
    public Loading(@NonNull Context context) {
        super(context);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        setTitle(null);
        setCancelable(true);
        setOnCancelListener(null);

        View view = LayoutInflater.from(context).inflate(R.layout.loading,null);
        setContentView(view);


    }
}
