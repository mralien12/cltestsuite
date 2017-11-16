package com.alticast.taurus.testsuite.util;

import android.util.Log;

/**
 * Created by hoa on 15/11/2017.
 */

public class TLog {
    private static final String LOG_TAG = "[CL_TEST_KIT#";
    public static void i(Object o, String message) {
        Log.i(LOG_TAG + o.getClass().getName() + "]", message);
    }

    public static void e(Object o, String message) {
        Log.e(LOG_TAG + o.getClass().getName() + "]", message);
    }
}
