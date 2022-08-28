package com.painttools.drawart.simple.well.drawartdonate;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.painttools.drawart.simple.well.BuildConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class DrawArtProUtil {

    public void success() {
    }

    public static synchronized DrawArtProUtil inst() {
        if (instance == null) {
            instance = new DrawArtProUtil();
        }
        return instance;
    }

    private DrawArtProUtil() {
    }

    public static String getPack(int number) {
        return BuildConfig.DEBUG ? PRO_PAINT_SIMPLE_1_TEST : PREFIX + number;
    }

    public void initBill(Context context) {
        initBill(context, null);
    }

    private final PurchasesUpdatedListener updater = (billingResult, purchases) -> {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                && purchases != null) {
            for (int i = 0; i < purchases.size(); i++) {
                handlePurchase(purchases.get(i));
            }
        }
    };


    private void onBillingConnected(@NonNull BillingResult billingResult) {
        if (billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK) {
            if (stateListener != null) {
                stateListener.onBillingSetupFinished(billingResult);
            }
            return;
        }
        Log.i("pro_paint_simple: ", PRO_PAINT_SIMPLE_1_1 + " " + PRO_PAINT_SIMPLE_1_2 + " " + PRO_PAINT_SIMPLE_1_3 + " " + PRO_PAINT_SIMPLE_1_4);
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(Arrays.asList(PRO_PAINT_SIMPLE_1_1, PRO_PAINT_SIMPLE_1_2, PRO_PAINT_SIMPLE_1_3, PRO_PAINT_SIMPLE_1_4))
                .setType(BillingClient.SkuType.INAPP);
        billing.querySkuDetailsAsync(params.build(),
                (billingResult12, skuDetailsList) -> {
                    DrawArtProUtil.this.skus = skuDetailsList;
                    for (SkuDetails skuDetails : skus) {
                        Log.i("pro_paint_simple:", " " + skuDetails.getSku());
                    }
                    DrawArtProLog.debug(billingResult12.getResponseCode());
                    if (stateListener != null) {
                        stateListener.onBillingSetupFinished(billingResult);
                    }
                });
    }


    private Purchase get(String productId) {
        try {
            Purchase.PurchasesResult purchasesResult = billing.queryPurchases(BillingClient.SkuType.INAPP);
            for (Purchase purchase :
                    Objects.requireNonNull(purchasesResult.getPurchasesList())) {
                if (purchase.getSkus().contains(productId)) return purchase;
            }
        } catch (Exception exception) {
            DrawArtProLog.error(exception);
        }
        return null;
    }

    public static void init(Context context) {
        DrawArtProUtil.inst().initBill(context);
    }

    public void initBill(final Context context, BillingClientStateListener cal) {
        billing = BillingClient.newBuilder(context)
                .setListener(updater)
                .enablePendingPurchases()
                .build();
        this.stateListener = cal;
        billing.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                DrawArtProLog.debug(billingResult.getResponseCode());
                onBillingConnected(billingResult);
            }

            @Override
            public void onBillingServiceDisconnected() {
                DrawArtProLog.debug();
                if (cal != null) {
                    cal.onBillingServiceDisconnected();
                }
            }
        });
    }


    private void consume(Purchase purchase) {
        if (purchase != null) {
            ConsumeParams consumeParams = ConsumeParams.newBuilder().setPurchaseToken(purchase.getPurchaseToken()).build();
            billing.consumeAsync(consumeParams, (billingResult, s) -> {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    fetch(null);
                }
            });
        }
    }

    public void consume(String productId) {
        Purchase purchase = get(productId);
        consume(purchase);
    }


    void handlePurchase(Purchase purchase) {
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            consume(purchase);
            if (purchase.isAcknowledged()) {
                return;
            }

            AcknowledgePurchaseParams acknowledgePurchaseParams =
                    AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(purchase.getPurchaseToken())
                            .build();
            billing.acknowledgePurchase(acknowledgePurchaseParams, billingResult -> {
                success();
            });
        }
    }


    public void fetch(DrawArtProCallback callback) {
        billing.queryPurchasesAsync(BillingClient.SkuType.INAPP, (billingResult, list) -> {
            try {
                boolean purchased = false;
                for (Purchase purchase : Objects.requireNonNull(list)) {
                    if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                        purchased = true;
                    }
                }
                if (!purchased) {
                    billing.queryPurchasesAsync(BillingClient.SkuType.SUBS, (billingResult1, list1) -> {
                        boolean subscribed = false;
                        for (Purchase purchase : Objects.requireNonNull(list1)) {
                            if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                                subscribed = true;
                            }
                        }
                        if (callback != null) {
                            if (subscribed) {
                                callback.purchased();
                            } else {
                                callback.notPurchase();
                            }
                        }
                    });
                    return;
                }
                if (callback != null) {
                    callback.purchased();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private SkuDetails get(List<SkuDetails> skuDetailsListSUB, String productId) {
        try {
            for (SkuDetails skuDetails : skuDetailsListSUB) {
                if (skuDetails.getSku().equals(productId)) {
                    return skuDetails;
                }
            }
        } catch (Exception e) {
            DrawArtProLog.error(e);
        }
        return null;
    }


    public void purchase(Activity activity, String productId) {
        try {
            DrawArtProLog.debug(productId);
            if (billing == null) {
                initBill(activity);
            }
            SkuDetails skuDetails = get(skus, productId);
            BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                    .setSkuDetails(skuDetails)
                    .build();
            billing.launchBillingFlow(activity, billingFlowParams);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }


    public String price(String productId) {
        Log.i("pro_paint_simple: ", productId);
        String defaults = "0";
        if (billing == null || !billing.isReady()) {
            return defaults;
        }
        for (SkuDetails skuDetails : skus) {
            if (skuDetails.getSku().equals(productId)) {
                return skuDetails.getPrice();
            }
        }
        return defaults;
    }

    public static final String PRO_PAINT_SIMPLE_1_TEST = "android.test.purchased";
    public static final String PREFIX = BuildConfig.DEBUG ? PRO_PAINT_SIMPLE_1_TEST : "pro_paint_simple_";

    public static final String PRO_PAINT_SIMPLE_1_1 = BuildConfig.DEBUG ? PRO_PAINT_SIMPLE_1_TEST : PREFIX + "1";
    public static final String PRO_PAINT_SIMPLE_1_2 = BuildConfig.DEBUG ? PRO_PAINT_SIMPLE_1_TEST : PREFIX + "2";
    public static final String PRO_PAINT_SIMPLE_1_3 = BuildConfig.DEBUG ? PRO_PAINT_SIMPLE_1_TEST : PREFIX + "3";

    private BillingClient billing;
    private BillingClientStateListener stateListener;
    private static DrawArtProUtil instance;
    private List<SkuDetails> skus = new ArrayList<>();

    public static final String PRO_PAINT_SIMPLE_1_4 = BuildConfig.DEBUG ? PRO_PAINT_SIMPLE_1_TEST : PREFIX + "4";

}
