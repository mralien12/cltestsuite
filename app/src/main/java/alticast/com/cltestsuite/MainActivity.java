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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import alticast.com.cltestsuite.channelbuilder.ScanTest;
import alticast.com.cltestsuite.dvr.DVRTest;
import alticast.com.cltestsuite.utils.TestCase;
import alticast.com.cltestsuite.utils.TestCaseAdapter;

public class MainActivity extends Activity {

    private ListView lvScanTest, lvChannelTest, lvDVRTest, lvEpgTest, lvMediaTest, lvSFTest;
    private Button btnTestAll, btnShowResult, btnExportResult;
    private Button btnAllScanTest, btnAllChannelTest, btnAllDvrTest, btnAllEpgTest, btnAllMediaTest, btnAllSfTest;

    private String[] arrScanTest, arrChannelTest, arrDVRTest, arrEPGTest, arrMediaTest, arrSFTest;
    private int ret;

    private Thread threadScan, threadAllScanTest, threadAllDvrTest;
    private List<TestCase> scanTestCaseList, dvrTestCaseList;
    private TestCaseAdapter scanTestAdapter, dvrTestAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addControll();
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

        lvDVRTest.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                testDVREventListener(position);
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

                            scanTestCaseList.get(ScanTest.NOTTITY).setStatus(TestCase.TEST_RUNNING);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    scanTestAdapter.notifyDataSetChanged();
                                }
                            });
                            int retNotify = ScanTest.getInstance().SCA_Notify();
                            scanTestCaseList.get(ScanTest.NOTTITY).setResult(retNotify);
                            scanTestCaseList.get(ScanTest.NOTTITY).setStatus(TestCase.TEST_DONE);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    scanTestAdapter.notifyDataSetChanged();
                                }
                            });

                            scanTestCaseList.get(ScanTest.NOTIFY_SCAN_SAVE_RESULT_FINISH).setStatus(TestCase.TEST_RUNNING);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    scanTestAdapter.notifyDataSetChanged();
                                }
                            });
                            int retNofifySave = ScanTest.getInstance().SCA_NofifyScanSaveResultFinished();
                            scanTestCaseList.get(ScanTest.NOTIFY_SCAN_SAVE_RESULT_FINISH).setResult(retNofifySave);
                            scanTestCaseList.get(ScanTest.NOTIFY_SCAN_SAVE_RESULT_FINISH).setStatus(TestCase.TEST_DONE);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    scanTestAdapter.notifyDataSetChanged();
                                }
                            });

                            scanTestCaseList.get(ScanTest.SELECT_CONFLICT_CHANNEL_REGION).setStatus(TestCase.TEST_RUNNING);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    scanTestAdapter.notifyDataSetChanged();
                                }
                            });
                            int retConflict = ScanTest.getInstance().SCA_SelectConflictedChannelRegion();
                            scanTestCaseList.get(ScanTest.SELECT_CONFLICT_CHANNEL_REGION).setResult(retConflict);
                            scanTestCaseList.get(ScanTest.SELECT_CONFLICT_CHANNEL_REGION).setStatus(TestCase.TEST_DONE);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    scanTestAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    });
                    threadAllScanTest.start();
                }
            }
        });

        btnAllChannelTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        btnAllDvrTest.setOnClickListener(new View.OnClickListener() {
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

                            dvrTestCaseList.get(DVRTest.ON_RECORDING_RECEIVED).setStatus(TestCase.TEST_RUNNING);
                            runOnUiThread( new Runnable() {
                                @Override
                                public void run() {
                                    dvrTestAdapter.notifyDataSetChanged();
                                }
                            });
                            int retOnRecordingReceived = DVRTest.getInstance().onRecordingReceived();
                            dvrTestCaseList.get(DVRTest.ON_RECORDING_RECEIVED).setResult(retOnRecordingReceived);
                            dvrTestCaseList.get(DVRTest.ON_RECORDING_RECEIVED).setStatus(TestCase.TEST_DONE);
                            runOnUiThread( new Runnable() {
                                @Override
                                public void run() {
                                    dvrTestAdapter.notifyDataSetChanged();
                                }
                            });

                            dvrTestCaseList.get(DVRTest.STATE_LISTENER_ON_STATED).setStatus(TestCase.TEST_RUNNING);
                            runOnUiThread( new Runnable() {
                                @Override
                                public void run() {
                                    dvrTestAdapter.notifyDataSetChanged();
                                }
                            });
                            int retStateListenerOnStated = DVRTest.getInstance().stateListenerOnStated();
                            dvrTestCaseList.get(DVRTest.STATE_LISTENER_ON_STATED).setResult(retStateListenerOnStated);
                            dvrTestCaseList.get(DVRTest.STATE_LISTENER_ON_STATED).setStatus(TestCase.TEST_DONE);
                            runOnUiThread( new Runnable() {
                                @Override
                                public void run() {
                                    dvrTestAdapter.notifyDataSetChanged();
                                }
                            });

                            dvrTestCaseList.get(DVRTest.STATE_LISTENER_ON_STOPED).setStatus(TestCase.TEST_RUNNING);
                            runOnUiThread( new Runnable() {
                                @Override
                                public void run() {
                                    dvrTestAdapter.notifyDataSetChanged();
                                }
                            });
                            int retStateListenerOnStoped = DVRTest.getInstance().stateListenerOnStoped();
                            dvrTestCaseList.get(DVRTest.STATE_LISTENER_ON_STOPED).setResult(retStateListenerOnStoped);
                            dvrTestCaseList.get(DVRTest.STATE_LISTENER_ON_STOPED).setStatus(TestCase.TEST_DONE);
                            runOnUiThread( new Runnable() {
                                @Override
                                public void run() {
                                    dvrTestAdapter.notifyDataSetChanged();
                                }
                            });

                            dvrTestCaseList.get(DVRTest.RECORDING_SESSION_CALLBACK).setStatus(TestCase.TEST_RUNNING);
                            runOnUiThread( new Runnable() {
                                @Override
                                public void run() {
                                    dvrTestAdapter.notifyDataSetChanged();
                                }
                            });
                            int retRecordingSessionCallback = DVRTest.getInstance().recordingSessionCallback();
                            dvrTestCaseList.get(DVRTest.RECORDING_SESSION_CALLBACK).setResult(retRecordingSessionCallback);
                            dvrTestCaseList.get(DVRTest.RECORDING_SESSION_CALLBACK).setStatus(TestCase.TEST_DONE);
                            runOnUiThread( new Runnable() {
                                @Override
                                public void run() {
                                    dvrTestAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    });
                    threadAllDvrTest.start();
                }
            }
        });

        btnAllEpgTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        btnAllMediaTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        btnAllSfTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void testAllFunction() {
    }

    private void addControll() {
        // Add ListView
        arrScanTest = getResources().getStringArray(R.array.scan_test_list);
        arrChannelTest = getResources().getStringArray(R.array.channel_test_list);
        arrDVRTest = getResources().getStringArray(R.array.dvr_test_list);
        arrEPGTest = getResources().getStringArray(R.array.epg_test_list);
        arrMediaTest = getResources().getStringArray(R.array.media_test_list);
        arrSFTest = getResources().getStringArray(R.array.sf_test_list);

        lvScanTest = findViewById(R.id.scan_test_list_view);
        lvChannelTest = findViewById(R.id.channel_test_list_view);
        lvDVRTest = findViewById(R.id.dvr_test_list_view);
        lvEpgTest = findViewById(R.id.epg_test_list_view);
        lvMediaTest = findViewById(R.id.media_test_list_view);
        lvSFTest = findViewById(R.id.sf_test_list_view);

        /*01 Add test case for scan testsuite */
        scanTestCaseList = new ArrayList<TestCase>();
        for (int testCase = 0; testCase < arrScanTest.length; testCase++) {
            scanTestCaseList.add(new TestCase(arrScanTest[testCase]));
        }
        scanTestAdapter = new TestCaseAdapter(this, scanTestCaseList);
        lvScanTest.setAdapter(scanTestAdapter);

        //03 Add list DVR testcase
        dvrTestCaseList = new ArrayList<TestCase>();
        for (int testCase = 0; testCase < arrDVRTest.length; testCase++) {
            dvrTestCaseList.add(new TestCase(arrDVRTest[testCase]));
        }
        dvrTestAdapter = new TestCaseAdapter(this, dvrTestCaseList);
        lvDVRTest.setAdapter(dvrTestAdapter);


        lvChannelTest.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrChannelTest));
        lvEpgTest.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrEPGTest));
        lvMediaTest.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrMediaTest));
        lvSFTest.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrSFTest));

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
                    case DVRTest.STATE_LISTENER_ON_STOPED:
                        ret = DVRTest.getInstance().stateListenerOnStoped();
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
                        scanTestAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
        Toast.makeText(getApplicationContext(), "Testcase " + arrDVRTest[position], Toast.LENGTH_LONG);

        dvrTestCaseList.get(position).setStatus(TestCase.NOT_TEST);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dvrTestAdapter.notifyDataSetChanged();
            }
        });
        threadScan.start();

    }
}
