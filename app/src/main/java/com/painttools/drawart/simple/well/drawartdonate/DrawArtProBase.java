package com.painttools.drawart.simple.well.drawartdonate;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public abstract class DrawArtProBase extends AppCompatActivity {
    protected abstract void initViews(Bundle savedInstanceState);

    protected abstract int onLayout();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(onLayout());
        this.initViews(savedInstanceState);
    }

}
