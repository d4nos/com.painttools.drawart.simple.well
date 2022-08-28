package com.painttools.drawart.simple.well.drawartdonate;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;

import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.painttools.drawart.simple.well.R;

import java.util.Locale;


public class DrawArtProActivity extends DrawArtProBase {

    public static void open(Context context) {
        context.startActivity(new Intent(context, DrawArtProActivity.class));
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        DrawArtProUtil.inst().initBill(this);
        setTitle("アプリに寄付する");
        initBilling();

        findViewById(R.id.bt_close).setOnClickListener(v -> {
            finish();
        });
        toolbar();

    }

    private void initBilling() {
        DrawArtProUtil.inst().initBill(this, new BillingClientStateListener() {
            @Override
            public void onBillingServiceDisconnected() {
            }

            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                runOnUiThread(() -> initViews());
            }
        });
    }

    private void toolbar() {
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private View getView(String viewIdString) {
        return findViewById(getResources().getIdentifier(viewIdString, "id", getPackageName()));
    }

    private void initViews() {
        for (int i = 1; i <= 4; i++) {
            try {
                View layoutPack = getView("layout_drawart_pro_" + i);
                String packString = DrawArtProUtil.getPack(i);
                Log.i("pro_paint_simple:", packString);
                layoutPack.setSelected(true);
                layoutPack.setOnClickListener(view -> {
                    DrawArtProUtil util = DrawArtProUtil.inst();
                    util.consume(packString);
                    util.purchase(this, packString);
                });

                String titleViewIdString = String.format(Locale.US, "tv_drawart_pro_%d_price", i);
                TextView tvPrice = (TextView) getView(titleViewIdString);
                tvPrice.setSelected(layoutPack.isSelected());
                String price = DrawArtProUtil.inst().price(packString);
                tvPrice.setText(price);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected int onLayout() {
        return R.layout.drawart_pro;
    }
}
