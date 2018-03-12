/*
 *  Copyright (c) 2017 Alticast Corp.
 *  All rights reserved. http://www.alticast.com/
 *
 *  This software is the confidential and proprietary information of
 *  Alticast Corp. ("Confidential Information"). You shall not
 *  disclose such Confidential Information and shall use it only in
 *  accordance with the terms of the license agreement you entered into
 *  with Alticast.
 */

package com.alticast.taurus.testsuite.util;

import android.util.Log;

/**
 * Created by hoa on 15/11/2017.
 */

public class TLog {
    private static final String LOG_TAG = "[TCTS#";

    public static void i(Object o, String message) {
        Log.i(LOG_TAG + o.getClass().getName() + "]", message);
    }

    public static void e(Object o, String message) {
        Log.e(LOG_TAG + o.getClass().getName() + "]", message);
    }
}
