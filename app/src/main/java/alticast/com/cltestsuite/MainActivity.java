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
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import alticast.com.cltestsuite.channelbuilder.ScanTest;
import alticast.com.cltestsuite.channelmanager.ChannelListTest;
import alticast.com.cltestsuite.dvr.DVRTest;
import alticast.com.cltestsuite.media.ChannelPlayerTVStreamLiveTestActivity;
import alticast.com.cltestsuite.media.ChannelPlayerTVStreamTSRTestActivity;
import alticast.com.cltestsuite.media.MediaTest;
import alticast.com.cltestsuite.mpeg.SectionFilterTest;
import alticast.com.cltestsuite.utils.ShowResultActivity;
import alticast.com.cltestsuite.utils.TestCase;
import alticast.com.cltestsuite.utils.TestCaseAdapter;

public class MainActivity extends Activity {
    private static final int DELAY_UPDATE_UI = 500;   /* miliseconds */

    private ListView lvScanTest, lvChannelTest, lvDVRTest, lvEpgTest, lvMediaTest, lvMediaEventListenerTest, lvSFTest;
    private Button btnTestAll, btnShowResult, btnExportResult;
    private Button btnAllScanTest, btnAllChannelTest, btnAllDvrTest, btnAllEpgTest, btnAllMediaTest, btnAllSfTest;

    private String[] arrScanTest, arrChannelTest, arrDVRTest, arrEPGTest, arrMediaTest, arrMediaEventListenerTest, arrSFTest;
    private int ret;

    private Thread threadScan, threadAllScanTest, threadAllChannelTest, threadAllDvrTest;
    public static List<TestCase> scanTestCaseList, channelTestCaseList, dvrTestCaseList;
    public static List<TestCase> epgTestCaseList, mediaTestCaseList, mediaEventListenerTestCaseList, sfTestCaseList;
    private TestCaseAdapter scanTestAdapter, channelTestAdapter, dvrTestAdapter;
    private TestCaseAdapter epgTestAdapter, mediaTestAdaper, mediaEventListenerTestAdapter, sfTestAdapter;
    private List<ShowResultActivity> listTestedTC;
    private ShowResultActivity showResultActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addControl();
        addEvent();
        addListener();
    }

    private void addListener() {
        lvScanTest.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                testScanEventListener(position);
            }
        });

        lvChannelTest.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                testChannelListListener(position);
            }
        });

        lvDVRTest.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                testDVREventListener(position);
            }
        });

        lvMediaTest.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                testMediaListener(position);
            }
        });

        lvMediaEventListenerTest.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            }
        });

        lvSFTest.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                testSfEventListener (position);
            }
        });
    }

    private void addEvent() {
        // ListView Event
        ListUtils.setDynamicHeight(lvScanTest);
        ListUtils.setDynamicHeight(lvChannelTest);
        ListUtils.setDynamicHeight(lvDVRTest);
        ListUtils.setDynamicHeight(lvEpgTest);
        ListUtils.setDynamicHeight(lvMediaTest);
        ListUtils.setDynamicHeight(lvMediaEventListenerTest);
        ListUtils.setDynamicHeight(lvSFTest);

        // Top Button Event
        btnTestAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testAllFunction();
            }
        });

        btnShowResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listTestedTC = new ArrayList<ShowResultActivity>();

                // 1. Scan Test
                for (TestCase testCase : scanTestCaseList) {
                    if (testCase.getResult() == TestCase.FAIL) {
                        showResultActivity = new ShowResultActivity();
                        showResultActivity.setName(testCase.getName());
                        showResultActivity.setResultDetail(testCase.getFailedReason());
                        showResultActivity.setSuccessTestCase(false);
                        listTestedTC.add(showResultActivity);
                    } else if (testCase.getResult() == TestCase.SUCCESS) {
                        showResultActivity = new ShowResultActivity();
                        showResultActivity.setName(testCase.getName());
                        showResultActivity.setResultDetail(testCase.getFailedReason());
                        showResultActivity.setSuccessTestCase(true);
                        listTestedTC.add(showResultActivity);
                    }
                }

                // 2. Channel List Test
                for (TestCase testCase : channelTestCaseList) {
                    if (testCase.getResult() == TestCase.FAIL) {
                        showResultActivity = new ShowResultActivity();
                        showResultActivity.setName(testCase.getName());
                        showResultActivity.setResultDetail(testCase.getFailedReason());
                        showResultActivity.setSuccessTestCase(false);
                        listTestedTC.add(showResultActivity);
                    } else if (testCase.getResult() == TestCase.SUCCESS) {
                        showResultActivity = new ShowResultActivity();
                        showResultActivity.setName(testCase.getName());
                        showResultActivity.setResultDetail(testCase.getFailedReason());
                        showResultActivity.setSuccessTestCase(true);
                        listTestedTC.add(showResultActivity);
                    }
                }

                // 3. DVR Test
                for (TestCase testCase : dvrTestCaseList) {
                    if (testCase.getResult() == TestCase.FAIL) {
                        showResultActivity = new ShowResultActivity();
                        showResultActivity.setName(testCase.getName());
                        showResultActivity.setResultDetail(testCase.getFailedReason());
                        showResultActivity.setSuccessTestCase(false);
                        listTestedTC.add(showResultActivity);
                    } else if (testCase.getResult() == TestCase.SUCCESS) {
                        showResultActivity = new ShowResultActivity();
                        showResultActivity.setName(testCase.getName());
                        showResultActivity.setResultDetail(testCase.getFailedReason());
                        showResultActivity.setSuccessTestCase(true);
                        listTestedTC.add(showResultActivity);
                    }
                }

                // 4. EPG Test
                for (TestCase testCase : epgTestCaseList) {
                    if (testCase.getResult() == TestCase.FAIL) {
                        showResultActivity = new ShowResultActivity();
                        showResultActivity.setName(testCase.getName());
                        showResultActivity.setResultDetail(testCase.getFailedReason());
                        showResultActivity.setSuccessTestCase(false);
                        listTestedTC.add(showResultActivity);
                    } else if (testCase.getResult() == TestCase.SUCCESS) {
                        showResultActivity = new ShowResultActivity();
                        showResultActivity.setName(testCase.getName());
                        showResultActivity.setResultDetail(testCase.getFailedReason());
                        showResultActivity.setSuccessTestCase(true);
                        listTestedTC.add(showResultActivity);
                    }
                }

                // 5. Media Test
                for (TestCase testCase : mediaTestCaseList) {
                    if (testCase.getResult() == TestCase.FAIL) {
                        showResultActivity = new ShowResultActivity();
                        showResultActivity.setName(testCase.getName());
                        showResultActivity.setResultDetail(testCase.getFailedReason());
                        showResultActivity.setSuccessTestCase(false);
                        listTestedTC.add(showResultActivity);
                    } else if (testCase.getResult() == TestCase.SUCCESS) {
                        showResultActivity = new ShowResultActivity();
                        showResultActivity.setName(testCase.getName());
                        showResultActivity.setResultDetail(testCase.getFailedReason());
                        showResultActivity.setSuccessTestCase(true);
                        listTestedTC.add(showResultActivity);
                    }
                }

                // 6. Section Filter Test
                for (TestCase testCase : sfTestCaseList) {
                    if (testCase.getResult() == TestCase.FAIL) {
                        showResultActivity = new ShowResultActivity();
                        showResultActivity.setName(testCase.getName());
                        showResultActivity.setResultDetail(testCase.getFailedReason());
                        showResultActivity.setSuccessTestCase(false);
                        listTestedTC.add(showResultActivity);
                    } else if (testCase.getResult() == TestCase.SUCCESS) {
                        showResultActivity = new ShowResultActivity();
                        showResultActivity.setName(testCase.getName());
                        showResultActivity.setResultDetail(testCase.getFailedReason());
                        showResultActivity.setSuccessTestCase(true);
                        listTestedTC.add(showResultActivity);
                    }
                }

                Intent intent = new Intent(MainActivity.this, ShowResultActivity.class);
                intent.putExtra("TestCaseList", (Serializable) listTestedTC);
                startActivity(intent);
            }
        });

        btnExportResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        // List Button Event
        btnAllScanTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (threadAllScanTest != null) {
                    if (threadAllScanTest.isAlive()) {
                        Toast.makeText(MainActivity.this, "Scan Test is running...", Toast.LENGTH_LONG).show();
                        return;
                    } else {
                        threadAllScanTest.start();
                    }
                } else {
                    threadAllScanTest = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    for (int pos = 0; pos < arrScanTest.length; pos++) {
                                        scanTestCaseList.get(pos).setResult(TestCase.NOT_TEST);
                                        scanTestAdapter.notifyDataSetChanged();
                                    }
                                }
                            });

                            /* Wait few miliseconds for updating UI */
                            try {
                                Thread.sleep(DELAY_UPDATE_UI);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            for (int testCase = 0; testCase < scanTestCaseList.size(); testCase++) {
                                int ret = TestCase.FAIL;
                                scanTestCaseList.get(testCase).setStatus(TestCase.TEST_RUNNING);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        scanTestAdapter.notifyDataSetChanged();
                                    }
                                });

                                switch (testCase) {
                                    case ScanTest.NOTTITY:
                                        ret = ScanTest.getInstance().SCA_Notify();
                                        break;
                                    case ScanTest.NOTIFY_SCAN_SAVE_RESULT_FINISH:
                                        ret = ScanTest.getInstance().SCA_NofifyScanSaveResultFinished();
                                        break;
                                    case ScanTest.SELECT_CONFLICT_CHANNEL_REGION:
                                        ret = ScanTest.getInstance().SCA_SelectConflictedChannelRegion();
                                        break;
                                    default:
                                }

                                scanTestCaseList.get(testCase).setResult(ret);
                                scanTestCaseList.get(testCase).setStatus(TestCase.TEST_DONE);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        scanTestAdapter.notifyDataSetChanged();
                                    }
                                });
                            }
                        }
                    });
                    threadAllScanTest.start();
                }
            }
        });

        btnAllChannelTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (threadAllChannelTest != null) {
                    if (threadAllChannelTest.isAlive()) {
                        Toast.makeText(MainActivity.this, "Channel List Test is running...", Toast.LENGTH_LONG).show();
                        return;
                    } else {
                        threadAllChannelTest.start();
                    }
                } else {
                    threadAllChannelTest = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            int ret = TestCase.FAIL;
                            for (int testCase = 0; testCase < channelTestCaseList.size(); testCase++) {
                                channelTestCaseList.get(testCase).setResult(TestCase.NOT_TEST);
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    channelTestAdapter.notifyDataSetChanged();
                                }
                            });

                            /* Wait few miliseconds for updating UI */
                            try {
                                Thread.sleep(DELAY_UPDATE_UI);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            for (int testCase = 0; testCase < channelTestCaseList.size(); testCase++) {
                                channelTestCaseList.get(testCase).setStatus(TestCase.TEST_RUNNING);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        channelTestAdapter.notifyDataSetChanged();
                                    }
                                });

                                switch (testCase) {
                                    case ChannelListTest.CHANNEL_LIST_UPDATED:
                                        ret = ChannelListTest.getInstance().CHL_OnChannelListUpdated();
                                        break;
                                    case ChannelListTest.CHANNEL_CALLBACK_ON_CHANNEL_DATA_RECEIVED:
                                        ret = ChannelListTest.getInstance().CHL_ChannelCallbackOnChannelDataReceived();
                                        break;
                                    default:
                                }

                                channelTestCaseList.get(testCase).setResult(ret);
                                channelTestCaseList.get(testCase).setStatus(TestCase.TEST_DONE);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        channelTestAdapter.notifyDataSetChanged();
                                    }
                                });
                            }
                        }
                    });
                    threadAllChannelTest.start();
                }
            }
        });

        btnAllDvrTest.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View v) {
                if (threadAllDvrTest != null) {
                    if (threadAllDvrTest.isAlive()) {
                        Toast.makeText(MainActivity.this, "DVR Test is running...", Toast.LENGTH_LONG).show();
                        return;
                    } else {
                        threadAllDvrTest.start();
                    }
                } else {
                    threadAllDvrTest = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    for (int pos = 0; pos < arrDVRTest.length; pos++) {
                                        dvrTestCaseList.get(pos).setResult(TestCase.NOT_TEST);
                                        dvrTestAdapter.notifyDataSetChanged();
                                    }
                                }
                            });
                            /* Wait few miliseconds for updating UI */
                            try {
                                Thread.sleep(DELAY_UPDATE_UI);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            for (int testCase = 0; testCase < dvrTestCaseList.size(); testCase++) {
                                int ret = TestCase.FAIL;
                                dvrTestCaseList.get(testCase).setStatus(TestCase.TEST_RUNNING);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        dvrTestAdapter.notifyDataSetChanged();
                                    }
                                });

                                switch (testCase) {
                                    case DVRTest.ON_RECORDING_RECEIVED:
                                        ret = DVRTest.getInstance().onRecordingReceived();
                                        break;
                                    case DVRTest.STATE_LISTENER_ON_STATED:
                                        ret = DVRTest.getInstance().stateListenerOnStated();
                                        break;
                                    case DVRTest.STATE_LISTENER_ON_STOPPED:
                                        ret = DVRTest.getInstance().stateListenerOnStopped();
                                        break;
                                    case DVRTest.RECORDING_SESSION_CALLBACK:
                                        ret = DVRTest.getInstance().recordingSessionCallback();
                                        break;
                                    default:
                                }

                                dvrTestCaseList.get(testCase).setResult(ret);
                                dvrTestCaseList.get(testCase).setStatus(TestCase.TEST_DONE);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        dvrTestAdapter.notifyDataSetChanged();
                                    }
                                });
                            }
                        }
                    });
                    threadAllDvrTest.start();
                }
            }
        });

        btnAllEpgTest.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View v) {

            }
        });

        btnAllMediaTest.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View v) {

            }
        });

        btnAllSfTest.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void testAllFunction() {
    }

    private void addControl() {
        // Add ListView
        arrScanTest = getResources().getStringArray(R.array.scan_test_list);
        arrChannelTest = getResources().getStringArray(R.array.channel_test_list);
        arrDVRTest = getResources().getStringArray(R.array.dvr_test_list);
        arrEPGTest = getResources().getStringArray(R.array.epg_test_list);
        arrMediaTest = getResources().getStringArray(R.array.media_test_list);
        arrMediaEventListenerTest = getResources().getStringArray(R.array.media_event_listener_test);
        arrSFTest = getResources().getStringArray(R.array.sf_test_list);

        lvScanTest = findViewById(R.id.scan_test_list_view);
        lvChannelTest = findViewById(R.id.channel_test_list_view);
        lvDVRTest = findViewById(R.id.dvr_test_list_view);
        lvEpgTest = findViewById(R.id.epg_test_list_view);
        lvMediaTest = findViewById(R.id.media_test_list_view);
        lvMediaEventListenerTest = findViewById(R.id.media_event_listener_test_list_view);
        lvSFTest = findViewById(R.id.sf_test_list_view);

        /* Add test case for scan testsuite */
        scanTestCaseList = new ArrayList<TestCase>();
        for (int testCase = 0; testCase < arrScanTest.length; testCase++) {
            scanTestCaseList.add(new TestCase(arrScanTest[testCase]));
        }
        scanTestAdapter = new TestCaseAdapter(this, scanTestCaseList);
        lvScanTest.setAdapter(scanTestAdapter);

        /* Add test case for channel testsuite */
        channelTestCaseList = new ArrayList<TestCase>();
        for (int testCase = 0; testCase < arrChannelTest.length; testCase++) {
            channelTestCaseList.add(new TestCase(arrChannelTest[testCase]));
        }
        channelTestAdapter = new TestCaseAdapter(this, channelTestCaseList);
        lvChannelTest.setAdapter(channelTestAdapter);


        /* Add test case for dvr testsuite */
        dvrTestCaseList = new ArrayList<TestCase>();
        for (int testCase = 0; testCase < arrDVRTest.length; testCase++) {
            dvrTestCaseList.add(new TestCase(arrDVRTest[testCase]));
        }
        dvrTestAdapter = new TestCaseAdapter(this, dvrTestCaseList);
        lvDVRTest.setAdapter(dvrTestAdapter);

        /* Add test case for epg testsuite */
        epgTestCaseList = new ArrayList<TestCase>();
        for (int testCase = 0; testCase < arrEPGTest.length; testCase++) {
            epgTestCaseList.add(new TestCase(arrEPGTest[testCase]));
        }
        epgTestAdapter = new TestCaseAdapter(this, epgTestCaseList);
        lvEpgTest.setAdapter(epgTestAdapter);

        /* Add test case for media testsuite */
        mediaTestCaseList = new ArrayList<TestCase>();
        for (int testCase = 0; testCase < arrMediaTest.length; testCase++) {
            mediaTestCaseList.add(new TestCase(arrMediaTest[testCase]));
        }
        mediaTestAdaper = new TestCaseAdapter(this, mediaTestCaseList);
        lvMediaTest.setAdapter(mediaTestAdaper);

        /* Add test case for media event listener testsuite */
        mediaEventListenerTestCaseList = new ArrayList<TestCase>();
        for (int testCase = 0; testCase < arrMediaEventListenerTest.length; testCase++) {
            mediaEventListenerTestCaseList.add(new TestCase(arrMediaEventListenerTest[testCase]));
        }
        mediaEventListenerTestAdapter = new TestCaseAdapter(this, mediaEventListenerTestCaseList);
        lvMediaEventListenerTest.setAdapter(mediaEventListenerTestAdapter);

        /* Add test case for SF testsuite */
        sfTestCaseList = new ArrayList<TestCase>();
        for (int testCase = 0; testCase < arrSFTest.length; testCase++) {
            sfTestCaseList.add(new TestCase(arrSFTest[testCase]));
        }
        sfTestAdapter = new TestCaseAdapter(this, sfTestCaseList);
        lvSFTest.setAdapter(sfTestAdapter);

        // Add Top Button
        btnTestAll = findViewById(R.id.btn_test_all);
        btnShowResult = findViewById(R.id.btn_show_result);
        btnExportResult = findViewById(R.id.btn_export_result);

        // Add List Button
        btnAllScanTest = findViewById(R.id.btn_all_scan_test);
        btnAllChannelTest = findViewById(R.id.btn_all_channel_test);
        btnAllDvrTest = findViewById(R.id.btn_all_dvr_test);
        btnAllEpgTest = findViewById(R.id.btn_all_epg_test);
        btnAllMediaTest = findViewById(R.id.btn_all_media_test);
        btnAllSfTest = findViewById(R.id.btn_all_sf_test);
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

    public void testScanEventListener(final int position) {
        if (threadScan != null) {
            if (threadScan.isAlive()) {
                Toast.makeText(getApplicationContext(), "Wait for current test case finish", Toast.LENGTH_LONG).show();
                return;

            }
        }

        threadScan = new Thread(new Runnable() {
            @Override
            public void run() {
                ret = TestCase.FAIL;
                scanTestCaseList.get(position).setStatus(TestCase.TEST_RUNNING);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        scanTestAdapter.notifyDataSetChanged();
                    }
                });
                switch (position) {
                    case ScanTest.NOTTITY:
                        ret = ScanTest.getInstance().SCA_Notify();
                        break;
                    case ScanTest.NOTIFY_SCAN_SAVE_RESULT_FINISH:
                        ret = ScanTest.getInstance().SCA_NofifyScanSaveResultFinished();
                        break;
                    case ScanTest.SELECT_CONFLICT_CHANNEL_REGION:
                        ret = ScanTest.getInstance().SCA_SelectConflictedChannelRegion();
                        break;
                    default:
                }

                scanTestCaseList.get(position).setResult(ret);
                scanTestCaseList.get(position).setStatus(TestCase.TEST_DONE);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        scanTestAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
        Toast.makeText(getApplicationContext(), "Testcase " + arrScanTest[position], Toast.LENGTH_SHORT).show();

        scanTestCaseList.get(position).setStatus(TestCase.NOT_TEST);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                scanTestAdapter.notifyDataSetChanged();
            }
        });
        threadScan.start();
    }

    public void testChannelListListener(final int position) {
        ret = TestCase.FAIL;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                channelTestCaseList.get(position).setStatus(TestCase.TEST_RUNNING);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        channelTestAdapter.notifyDataSetChanged();
                    }
                });
                switch (position) {
                    case ChannelListTest.CHANNEL_LIST_UPDATED:
                        ret = ChannelListTest.getInstance().CHL_OnChannelListUpdated();
                        break;
                    case ChannelListTest.CHANNEL_CALLBACK_ON_CHANNEL_DATA_RECEIVED:
                        ret = ChannelListTest.getInstance().CHL_ChannelCallbackOnChannelDataReceived();
                        break;
                    default:
                }
                channelTestCaseList.get(position).setResult(ret);
                channelTestCaseList.get(position).setStatus(TestCase.TEST_DONE);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        channelTestAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
        thread.start();

    }

    public void testDVREventListener(final int position) {
        if (threadScan != null) {
            if (threadScan.isAlive()) {
                Toast.makeText(getApplicationContext(), "Wait for current test case finish", Toast.LENGTH_LONG).show();
                return;
            }
        }
        threadScan = new Thread(new Runnable() {
            @Override
            public void run() {
                ret = TestCase.FAIL;
                dvrTestCaseList.get(position).setStatus(TestCase.TEST_RUNNING);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dvrTestAdapter.notifyDataSetChanged();
                    }
                });

                switch (position) {
                    case DVRTest.ON_RECORDING_RECEIVED:
                        ret = DVRTest.getInstance().onRecordingReceived();
                        break;
                    case DVRTest.STATE_LISTENER_ON_STATED:
                        ret = DVRTest.getInstance().stateListenerOnStated();
                        break;
                    case DVRTest.STATE_LISTENER_ON_STOPPED:
                        ret = DVRTest.getInstance().stateListenerOnStopped();
                        break;
                    case DVRTest.RECORDING_SESSION_CALLBACK:
                        ret = DVRTest.getInstance().recordingSessionCallback();
                        break;
                    default:
                        break;
                }

                dvrTestCaseList.get(position).setResult(ret);
                dvrTestCaseList.get(position).setStatus(TestCase.TEST_DONE);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dvrTestAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
        Toast.makeText(getApplicationContext(), "Testcase " + arrDVRTest[position], Toast.LENGTH_LONG).show();

        dvrTestCaseList.get(position).setStatus(TestCase.NOT_TEST);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dvrTestAdapter.notifyDataSetChanged();
            }
        });
        threadScan.start();
    }

    public void testMediaListener(final int position) {
        Intent intent;
        switch (position) {
            case MediaTest.CHANNEL_PLAYER_TVSTREAM_LIVE:
                intent = new Intent(getBaseContext(), ChannelPlayerTVStreamLiveTestActivity.class);
                startActivityForResult(intent, MediaTest.CHANNEL_PLAYER_TVSTREAM_LIVE_REQUEST_CODE);
                break;
            case MediaTest.CHANNEL_PLAYER_TVSTREAM_TSR:
                intent = new Intent(getBaseContext(), ChannelPlayerTVStreamTSRTestActivity.class);
                startActivityForResult(intent, MediaTest.CHANNEL_PLAYER_TVSTREAM_TSR_REQUEST_CODE);
                break;
            default:
        }
    }

    public void testSfEventListener(final int position) {
        if (threadScan != null) {
            if (threadScan.isAlive()) {
                Toast.makeText(getApplicationContext(), "Wait for current test case finish", Toast.LENGTH_LONG).show();
                return;

            }
        }

        threadScan = new Thread(new Runnable() {
            @Override
            public void run() {
                ret = TestCase.FAIL;
                sfTestCaseList.get(position).setStatus(TestCase.TEST_RUNNING);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        sfTestAdapter.notifyDataSetChanged();
                    }
                });
                switch (position) {
                    case SectionFilterTest.SF_EXCEPTION:
                        break;
                    case SectionFilterTest.SF_EVENT:
                        ret = SectionFilterTest.getInstance().sectionFitlerEvent();
                        break;
                    default:
                }

                sfTestCaseList.get(position).setResult(ret);
                sfTestCaseList.get(position).setStatus(TestCase.TEST_DONE);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        sfTestAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
        Toast.makeText(getApplicationContext(), "Testcase " + arrSFTest[position], Toast.LENGTH_SHORT).show();

        sfTestCaseList.get(position).setStatus(TestCase.NOT_TEST);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                sfTestAdapter.notifyDataSetChanged();
            }
        });
        threadScan.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MediaTest.CHANNEL_PLAYER_TVSTREAM_LIVE_REQUEST_CODE) {
            int result = data.getIntExtra("RESULT", TestCase.NOT_TEST);

            mediaTestCaseList.get(MediaTest.CHANNEL_PLAYER_TVSTREAM_LIVE).setResult(result);
            mediaTestCaseList.get(MediaTest.CHANNEL_PLAYER_TVSTREAM_LIVE).setStatus(TestCase.TEST_DONE);
            mediaTestAdaper.notifyDataSetChanged();

        }
    }
}
