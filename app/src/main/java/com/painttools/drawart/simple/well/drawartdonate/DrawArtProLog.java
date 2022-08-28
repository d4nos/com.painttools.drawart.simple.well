package com.painttools.drawart.simple.well.drawartdonate;

import android.util.Log;

public class DrawArtProLog {
    private static final String TAG = "pro_paint_simple: ";


    public static void debug(int intValue) {
        Log.d(TAG, log(String.valueOf(intValue)));
    }

    private static String log(String message) {
        return getStack() + " = " + message;
    }

    public static void debug() {
        Log.i(TAG, log(""));
    }

    private static String getStack() {
        try {
            StackTraceElement[] stacktraceObj = Thread.currentThread().getStackTrace();
            StackTraceElement stackTraceElement = stacktraceObj[5];
            String className = stackTraceElement.getClassName();
            className = className.substring(className.lastIndexOf(".") + 1);
            return " [" + className + "] " + stackTraceElement.getMethodName();
        } catch (Exception e) {
            return "";
        }
    }

    public static void error(Throwable exception) {
        try {
            if (exception == null) {
                return;
            }
            try {
                Log.e(TAG, exception.getMessage());
            } catch (Exception e) {
            }
            exception.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void debug(String message) {
        Log.d(TAG, log(message));
    }


}
