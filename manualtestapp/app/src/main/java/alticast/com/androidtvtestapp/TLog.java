package alticast.com.androidtvtestapp;

import android.util.Log;

/*
 *  Copyright (c) 2018 Alticast Corp.
 *  All rights reserved. http://www.alticast.com/
 *
 *  This software is the confidential and proprietary information of
 *  Alticast Corp. ("Confidential Information"). You shall not
 *  disclose such Confidential Information and shall use it only in
 *  accordance with the terms of the license agreement you entered into
 *  with Alticast.
 */
public class TLog {
    public static void i(Object obj, String msg) {
        String TAG = obj.getClass().getSimpleName();
        Log.i(TAG, msg);
    }

    public static void w(Object obj, String msg) {
        String TAG = obj.getClass().getSimpleName();
        Log.w(TAG, msg);
    }

    public static void d(Object obj, String msg) {
        String TAG = obj.getClass().getSimpleName();
        Log.d(TAG, msg);
    }

    public static void e(Object obj, String msg) {
        String TAG = obj.getClass().getSimpleName();
        Log.e(TAG, msg);
    }
}
