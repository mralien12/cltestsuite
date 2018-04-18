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
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;

import alticast.com.cltestsuite.channelbuilder.ScanTest;
import alticast.com.cltestsuite.utils.TLog;

public class MainActivity extends Activity {

    private ListView lvScanTest, lvChannelTest, lvDVRTest, lvEPGTest, lvMediaTest, lvSFTest;
    private Button btn_test_all, btn_show_result, btn_export_result;
    private Button btn_all_scan_test, btn_all_channel_test, btn_all_dvr_test, btn_all_epg_test, btn_all_media_test, btn_all_sf_test;

    private String [] arrScanTest, arrChannelTest, arrDVRTest, arrEPGTest, arrMediaTest, arrSFTest;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addControll();
        addEvent();
    }

    private void addEvent() {
        // ListView Event
        ListUtils.setDynamicHeight(lvScanTest);
        ListUtils.setDynamicHeight(lvChannelTest);
        ListUtils.setDynamicHeight(lvDVRTest);
        ListUtils.setDynamicHeight(lvEPGTest);
        ListUtils.setDynamicHeight(lvMediaTest);
        ListUtils.setDynamicHeight(lvSFTest);

        // Top Button Event
        btn_test_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testAllFunction();
            }
        });

        btn_show_result.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        btn_export_result.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        // List Button Event
        btn_all_scan_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        btn_all_channel_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        btn_all_dvr_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        btn_all_epg_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        btn_all_media_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        btn_all_sf_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void testAllFunction() {
    }

    private void addControll() {
        // Add ListView
        arrScanTest = getResources().getStringArray(R.array.lvScanTest);
        arrChannelTest = getResources().getStringArray(R.array.lvChannelTest);
        arrDVRTest = getResources().getStringArray(R.array.lvDVRTest);
        arrEPGTest = getResources().getStringArray(R.array.lvEPGTest);
        arrMediaTest = getResources().getStringArray(R.array.lvMediaTest);
        arrSFTest = getResources().getStringArray(R.array.lvSFTest);

        lvScanTest = findViewById(R.id.lvScanTest);
        lvChannelTest = findViewById(R.id.lvChannelTest);
        lvDVRTest = findViewById(R.id.lvDVRTest);
        lvEPGTest = findViewById(R.id.lvEPGTest);
        lvMediaTest = findViewById(R.id.lvMediaTest);
        lvSFTest = findViewById(R.id.lvSFTest);

        lvScanTest.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,arrScanTest));
        lvChannelTest.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,arrChannelTest));
        lvDVRTest.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,arrDVRTest));
        lvEPGTest.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,arrEPGTest));
        lvMediaTest.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,arrMediaTest));
        lvSFTest.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,arrSFTest));

        // Add Top Button
        btn_test_all = findViewById(R.id.btn_test_all);
        btn_show_result = findViewById(R.id.btn_show_result);
        btn_export_result = findViewById(R.id.btn_export_result);

        // Add List Button
        btn_all_scan_test = findViewById(R.id.btn_all_scan_test);
        btn_all_channel_test = findViewById(R.id.btn_all_channel_test);
        btn_all_dvr_test = findViewById(R.id.btn_all_dvr_test);
        btn_all_epg_test = findViewById(R.id.btn_all_epg_test);
        btn_all_media_test = findViewById(R.id.btn_all_media_test);
        btn_all_sf_test = findViewById(R.id.btn_all_sf_test);
    }

    public static class ListUtils {
        public static void setDynamicHeight(ListView mListView) {
            ListAdapter mListAdapter = mListView.getAdapter();
            if (mListAdapter == null) {
                // when adapter is null
                return;
            }
            int height = 0;
            int desiredWidth = View.MeasureSpec.makeMeasureSpec(mListView.getWidth(), View.MeasureSpec.UNSPECIFIED);
            for (int i = 0; i < mListAdapter.getCount(); i++) {
                View listItem = mListAdapter.getView(i, null, mListView);
                listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
                height += listItem.getMeasuredHeight();
            }
            ViewGroup.LayoutParams params = mListView.getLayoutParams();
            params.height = height + (mListView.getDividerHeight() * (mListAdapter.getCount() - 1));
            mListView.setLayoutParams(params);
            mListView.requestLayout();
        }
    }
}
