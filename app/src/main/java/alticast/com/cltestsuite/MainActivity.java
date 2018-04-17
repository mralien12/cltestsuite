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

package alticast.com.cltestsuite;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import alticast.com.cltestsuite.channelbuilder.ScanTest;
import alticast.com.cltestsuite.utils.TLog;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnScanTest = (Button) findViewById(R.id.scan_test_button);
        btnScanTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean ret  = ScanTest.getInstance().SCA_NotifyAfterCompletingScan();
                TLog.i(this, "ret: " + ret);
            }
        });

        btnScanTest.performClick();

//        ScanTest.getInstance().SCA_NofifyScanSaveResultFinished();
    }
}
