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

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.util.Xml;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import alticast.com.cltestsuite.channelbuilder.ScanTest;
import alticast.com.cltestsuite.channelmanager.ChannelListTest;
import alticast.com.cltestsuite.dvr.DVRTest;
import alticast.com.cltestsuite.media.ChannelEventListenerTest;
import alticast.com.cltestsuite.media.ChannelPlayerTVStreamLiveTestActivity;
import alticast.com.cltestsuite.media.MediaEventListenerTest;
import alticast.com.cltestsuite.media.MediaTest;
import alticast.com.cltestsuite.mpeg.SectionFilterTest;
import alticast.com.cltestsuite.utils.ShowResultActivity;
import alticast.com.cltestsuite.utils.TLog;
import alticast.com.cltestsuite.utils.TestCase;
import alticast.com.cltestsuite.utils.TestCaseAdapter;

public class MainActivity extends Activity {
    private static final int DELAY_UPDATE_UI = 500;   /* miliseconds */

    private ListView lvScanTest, lvChannelTest, lvDVRTest, lvEpgTest, lvMediaTest, lvMediaEventListenerTest, lvSFTest, lvSFEventTest, lvSFExceptionTest;
    private Button btnTestAll, btnShowResult, btnExportResult;
    private Button btnAllScanTest, btnAllChannelTest, btnAllDvrTest, btnAllEpgTest, btnAllMediaTest, btnAllSfTest;
    private Button btnSFEventTest, btnSFExceptionTest;
    private Button btnAllMediaEventListenerTest;

    private String[] arrScanTest, arrChannelTest, arrDVRTest, arrEPGTest, arrMediaTest, arrMediaEventListenerTest, arrSFTest, arrSFEventTest, arrSFExceptionTest;
    private int ret;

    private Thread threadScan, threadAllScanTest, threadAllChannelTest, threadAllDvrTest;
    private Thread threadChannelList, threadMediaTest, threadMediaEventListenerTest, threadAllMediaEventListenerTest;
    public static List<TestCase> scanTestCaseList, channelTestCaseList, dvrTestCaseList;
    public static List<TestCase> epgTestCaseList, mediaTestCaseList, mediaEventListenerTestCaseList, sfTestCaseList, sfEventTestCaseList, sfExceptionTestCaseList;
    private TestCaseAdapter scanTestAdapter, channelTestAdapter, dvrTestAdapter;
    private TestCaseAdapter epgTestAdapter, mediaTestAdapter, mediaEventListenerTestAdapter, sfTestAdapter, sfEventTestAdapter, sfExceptionTestAdapter;
    private List<ShowResultActivity> listTestedTC;
    private ShowResultActivity showResultActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            }
        }

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
                testMedia(position);
            }
        });

        lvMediaEventListenerTest.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                testMediaEventListener(position);
            }
        });

        lvSFEventTest.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            }
        });

        lvSFExceptionTest.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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
        ListUtils.setDynamicHeight(lvSFEventTest);
        ListUtils.setDynamicHeight(lvSFExceptionTest);

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

                // 5.1 Media Test
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

                // 5.2 Media Event Listener Test
                for (TestCase testCase : mediaEventListenerTestCaseList) {
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

                // 6.A Section Filter Event Test
                for (TestCase testCase : sfEventTestCaseList) {
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

                // 6.B Section Filter Exception Test
                for (TestCase testCase : sfExceptionTestCaseList) {
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
                exportTestResults();
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

        btnAllMediaEventListenerTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (threadAllMediaEventListenerTest != null) {
                    if (threadAllMediaEventListenerTest.isAlive()) {
                        Toast.makeText(MainActivity.this, "Media Event Listener Test is running...", Toast.LENGTH_LONG).show();
                        return;
                    } else {
                        threadAllMediaEventListenerTest.start();
                    }
                } else {
                    threadAllMediaEventListenerTest = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            int ret = TestCase.FAIL;
                            for (int testCase = 0; testCase < mediaEventListenerTestCaseList.size(); testCase++) {
                                mediaEventListenerTestCaseList.get(testCase).setResult(TestCase.NOT_TEST);
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mediaEventListenerTestAdapter.notifyDataSetChanged();
                                }
                            });

                            /* Wait few miliseconds for updating UI */
                            try {
                                Thread.sleep(DELAY_UPDATE_UI);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            for (int testCase = 0; testCase < mediaEventListenerTestCaseList.size(); testCase++) {
                                mediaEventListenerTestCaseList.get(testCase).setStatus(TestCase.TEST_RUNNING);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mediaEventListenerTestAdapter.notifyDataSetChanged();
                                    }
                                });

                                switch (testCase) {
                                    case MediaEventListenerTest.ON_BEGINNING:
                                        ret = MediaEventListenerTest.getInstance(getBaseContext()).onBeginingTest();
                                        break;
                                    case MediaEventListenerTest.ON_COMPLETION:
                                        ret = MediaEventListenerTest.getInstance(getBaseContext()).onCompletion();
                                        break;
                                    case MediaEventListenerTest.ON_ERROR:
                                        try {
                                            ret = MediaEventListenerTest.getInstance(getBaseContext()).onError();
                                        } catch (RemoteException e) {
                                            e.printStackTrace();
                                        }
                                        break;
                                    case MediaEventListenerTest.ON_PREPARED:
                                        ret = MediaEventListenerTest.getInstance(getBaseContext()).onPrepared();
                                        break;
                                    case MediaEventListenerTest.ON_RATED_CHANGE:
                                        ret = MediaEventListenerTest.getInstance(getBaseContext()).onRateChanged();
                                        break;
                                    case MediaEventListenerTest.ON_STOPPED:
                                        ret = MediaEventListenerTest.getInstance(getBaseContext()).onStopped();
                                        break;
                                    default:
                                }
                                mediaEventListenerTestCaseList.get(testCase).setResult(ret);
                                mediaEventListenerTestCaseList.get(testCase).setStatus(TestCase.TEST_DONE);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mediaEventListenerTestAdapter.notifyDataSetChanged();
                                    }
                                });
                            }

                        }
                    });
                    threadAllMediaEventListenerTest.start();
                }
            }
        });

        btnAllSfTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        btnSFEventTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkSFEventResult();
            }
        });

        btnSFExceptionTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkSFExceptionResult();
            }
        });
    }

    private void checkSFExceptionResult() {
        threadAllChannelTest = new Thread(new Runnable() {
            @Override
            public void run() {
                int ret = TestCase.FAIL;
                for (int testCase = 0; testCase < sfExceptionTestCaseList.size(); testCase++) {
                    sfExceptionTestCaseList.get(testCase).setResult(TestCase.NOT_TEST);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        sfExceptionTestAdapter.notifyDataSetChanged();
                    }
                });

                /* Wait few miliseconds for updating UI */
                try {
                    Thread.sleep(DELAY_UPDATE_UI);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                for (int testCase = 0; testCase < sfExceptionTestCaseList.size(); testCase++) {
                    sfExceptionTestCaseList.get(testCase).setStatus(TestCase.TEST_RUNNING);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            sfExceptionTestAdapter.notifyDataSetChanged();
                        }
                    });

                    switch (testCase) {
                        case SectionFilterTest.FILTER_RESOURCE_EXCEPTION:
                            ret = SectionFilterTest.getInstance().sectionFilterException(SectionFilterTest.FILTER_RESOURCE_EXCEPTION, sfExceptionTestCaseList.size());
                            break;
                        case SectionFilterTest.ILLEGAL_FILTER_DEFINITION_EXCEPTION:
                            ret = SectionFilterTest.getInstance().sectionFilterException(SectionFilterTest.ILLEGAL_FILTER_DEFINITION_EXCEPTION, sfExceptionTestCaseList.size());
                            break;
                        case SectionFilterTest.CONNECTION_LOST_EXCEPTION:
                            ret = SectionFilterTest.getInstance().sectionFilterException(SectionFilterTest.CONNECTION_LOST_EXCEPTION, sfExceptionTestCaseList.size());
                            break;
                        case SectionFilterTest.INVALID_SOURCE_EXCEPTION:
                            ret = SectionFilterTest.getInstance().sectionFilterException(SectionFilterTest.INVALID_SOURCE_EXCEPTION, sfExceptionTestCaseList.size());
                            break;
                        case SectionFilterTest.NO_DATA_AVAILABLE_EXCEPTION:
                            ret = SectionFilterTest.getInstance().sectionFilterException(SectionFilterTest.NO_DATA_AVAILABLE_EXCEPTION, sfExceptionTestCaseList.size());
                            break;
                        case SectionFilterTest.FILTERING_INTERRUPTED_EXCEPTION:
                            ret = SectionFilterTest.getInstance().sectionFilterException(SectionFilterTest.FILTERING_INTERRUPTED_EXCEPTION, sfExceptionTestCaseList.size());
                            break;
                        default:
                    }

                    sfExceptionTestCaseList.get(testCase).setResult(ret);
                    sfExceptionTestCaseList.get(testCase).setStatus(TestCase.TEST_DONE);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            sfExceptionTestAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });
        threadAllChannelTest.start();
    }

    private void checkSFEventResult() {
        threadAllChannelTest = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int testCase = 0; testCase < sfEventTestCaseList.size(); testCase++) {
                    sfEventTestCaseList.get(testCase).setResult(TestCase.NOT_TEST);
                }

                sfEventTestCaseList.get(0).setStatus(TestCase.TEST_RUNNING);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        sfEventTestAdapter.notifyDataSetChanged();
                    }
                });

                int[] positions = SectionFilterTest.getInstance().sectionFilterEvent(sfEventTestCaseList.size());
                for (int position = 0; position < sfEventTestCaseList.size(); position++) {
                    sfEventTestCaseList.get(position).setResult(positions[position]);
                    sfEventTestCaseList.get(position).setStatus(TestCase.TEST_DONE);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        sfEventTestAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
        threadAllChannelTest.start();
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
        arrSFEventTest = getResources().getStringArray(R.array.sf_event_list);
        arrSFExceptionTest = getResources().getStringArray(R.array.sf_exception_list);
        //arrSFTest = getResources().getStringArray(R.array.sf_test_list);

        lvScanTest = findViewById(R.id.scan_test_list_view);
        lvChannelTest = findViewById(R.id.channel_test_list_view);
        lvDVRTest = findViewById(R.id.dvr_test_list_view);
        lvEpgTest = findViewById(R.id.epg_test_list_view);
        lvMediaTest = findViewById(R.id.media_test_list_view);
        lvMediaEventListenerTest = findViewById(R.id.media_event_listener_test_list_view);
        lvSFEventTest = findViewById(R.id.sf_event_list_view);
        lvSFExceptionTest = findViewById(R.id.sf_exception_list_view);
        //lvSFTest = findViewById(R.id.sf_test_list_view);


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
        mediaTestAdapter = new TestCaseAdapter(this, mediaTestCaseList);
        lvMediaTest.setAdapter(mediaTestAdapter);

        /* Add test case for media event listener testsuite */
        mediaEventListenerTestCaseList = new ArrayList<TestCase>();
        for (int testCase = 0; testCase < arrMediaEventListenerTest.length; testCase++) {
            mediaEventListenerTestCaseList.add(new TestCase(arrMediaEventListenerTest[testCase]));
        }
        mediaEventListenerTestAdapter = new TestCaseAdapter(this, mediaEventListenerTestCaseList);
        lvMediaEventListenerTest.setAdapter(mediaEventListenerTestAdapter);

        /* Add test case for SF Event testsuite */
        sfEventTestCaseList = new ArrayList<TestCase>();
        for (int testCase = 0; testCase < arrSFEventTest.length; testCase++) {
            sfEventTestCaseList.add(new TestCase(arrSFEventTest[testCase]));
        }
        sfEventTestAdapter = new TestCaseAdapter(this, sfEventTestCaseList);
        lvSFEventTest.setAdapter(sfEventTestAdapter);

        /* Add test case for SF Exception testsuite */
        sfExceptionTestCaseList = new ArrayList<TestCase>();
        for (int testCase = 0; testCase < arrSFExceptionTest.length; testCase++) {
            sfExceptionTestCaseList.add(new TestCase(arrSFExceptionTest[testCase]));
        }
        sfExceptionTestAdapter = new TestCaseAdapter(this, sfExceptionTestCaseList);
        lvSFExceptionTest.setAdapter(sfExceptionTestAdapter);

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
        btnAllMediaEventListenerTest = findViewById(R.id.all_media_event_listener_button);
        btnAllSfTest = findViewById(R.id.btn_all_sf_test);
        btnSFEventTest = findViewById(R.id.btn_sf_event_test);
        btnSFExceptionTest = findViewById(R.id.btn_sf_exception_test);
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
                int ret = TestCase.FAIL;
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
        if (threadChannelList != null) {
            if (threadChannelList.isAlive()) {
                Toast.makeText(getApplicationContext(), "Wait for current test case finish", Toast.LENGTH_LONG).show();
                return;

            }
        }

        threadChannelList = new Thread(new Runnable() {
            @Override
            public void run() {
                int ret = TestCase.FAIL;
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
        threadChannelList.start();

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
        if (position == DVRTest.RECORDING_SESSION_CALLBACK) {
            Toast.makeText(getApplicationContext(), "Testcase " + arrDVRTest[position] + " recording in: " + DVRTest.timeChecking + "s", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Testcase " + arrDVRTest[position], Toast.LENGTH_LONG).show();
        }

        dvrTestCaseList.get(position).setStatus(TestCase.NOT_TEST);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dvrTestAdapter.notifyDataSetChanged();
            }
        });
        threadScan.start();
    }

    public void testMedia(final int position) {
        Intent intent;
        switch (position) {
            case MediaTest.CHANNEL_PLAYER_TVSTREAM_LIVE:
                intent = new Intent(getBaseContext(), ChannelPlayerTVStreamLiveTestActivity.class);
                startActivityForResult(intent, MediaTest.CHANNEL_PLAYER_TVSTREAM_LIVE_REQUEST_CODE);
                break;
            case MediaTest.CHANNEL_PLAYER_TVSTREAM_TSR:
//                intent = new Intent(getBaseContext(), ChannelPlayerTVStreamTSRTestActivity.class);
//                startActivityForResult(intent, MediaTest.CHANNEL_PLAYER_TVSTREAM_TSR_REQUEST_CODE);
                break;
            default:
        }

        if (position == MediaTest.CHANNEL_EVENT_LISTENER  || position == MediaTest.CAPTION_CONTROLLER_EVENT_LISTENER_ON_DETACH) {
            if (threadMediaTest != null) {
                if (threadMediaTest.isAlive()) {
                    Toast.makeText(getApplicationContext(), "Wait for current test case finish", Toast.LENGTH_LONG).show();
                    return;
                }
            }

            threadMediaTest = new Thread(new Runnable() {
                @Override
                public void run() {
                    int ret = TestCase.FAIL;

                    mediaTestCaseList.get(position).setStatus(TestCase.TEST_RUNNING);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mediaTestAdapter.notifyDataSetChanged();
                        }
                    });
                    if (position == MediaTest.CHANNEL_EVENT_LISTENER) {
                        ret = ChannelEventListenerTest.getInstance(getBaseContext()).onChannelEvent();
                    } else if (position == MediaTest.CAPTION_CONTROLLER_EVENT_LISTENER_ON_DETACH) {
                        ret = MediaTest.getInstance().captionControllerEventListenerOnDetach();
                    }


                    mediaTestCaseList.get(position).setResult(ret);
                    mediaTestCaseList.get(position).setStatus(TestCase.TEST_DONE);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mediaTestAdapter.notifyDataSetChanged();
                        }
                    });
                }
            });
            threadMediaTest.start();
        }
    }

    public void testMediaEventListener(final int position) {
        if (threadMediaEventListenerTest != null) {
            if (threadMediaEventListenerTest.isAlive()) {
                Toast.makeText(getApplicationContext(), "Wait for current test case finish", Toast.LENGTH_LONG).show();
                return;
            }
        }

        threadMediaEventListenerTest = new Thread(new Runnable() {
            @Override
            public void run() {
                int ret = TestCase.FAIL;

                mediaEventListenerTestCaseList.get(position).setStatus(TestCase.TEST_RUNNING);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mediaEventListenerTestAdapter.notifyDataSetChanged();
                    }
                });

                switch (position) {
                    case MediaEventListenerTest.ON_BEGINNING:
                        ret = MediaEventListenerTest.getInstance(getBaseContext()).onBeginingTest();
                        break;
                    case MediaEventListenerTest.ON_COMPLETION:
                        ret = MediaEventListenerTest.getInstance(getBaseContext()).onCompletion();
                        break;
                    case MediaEventListenerTest.ON_ERROR:
                        try {
                            ret = MediaEventListenerTest.getInstance(getBaseContext()).onError();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        break;
                    case MediaEventListenerTest.ON_PREPARED:
                        ret = MediaEventListenerTest.getInstance(getBaseContext()).onPrepared();
                        break;
                    case MediaEventListenerTest.ON_RATED_CHANGE:
                        ret = MediaEventListenerTest.getInstance(getBaseContext()).onRateChanged();
                        break;
                    case MediaEventListenerTest.ON_STOPPED:
                        ret = MediaEventListenerTest.getInstance(getBaseContext()).onStopped();
                        break;
                    default:
                }
                mediaEventListenerTestCaseList.get(position).setResult(ret);
                mediaEventListenerTestCaseList.get(position).setStatus(TestCase.TEST_DONE);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mediaEventListenerTestAdapter.notifyDataSetChanged();
                    }
                });

            }
        });

        threadMediaEventListenerTest.start();

    }

    public void testSfEventListener(final int position) {
    }

    public void testSfExceptionListener(final int position) {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MediaTest.CHANNEL_PLAYER_TVSTREAM_LIVE_REQUEST_CODE) {
            int result = data.getIntExtra("RESULT", TestCase.NOT_TEST);

            mediaTestCaseList.get(MediaTest.CHANNEL_PLAYER_TVSTREAM_LIVE).setResult(result);
            mediaTestCaseList.get(MediaTest.CHANNEL_PLAYER_TVSTREAM_LIVE).setStatus(TestCase.TEST_DONE);
            mediaTestAdapter.notifyDataSetChanged();

        }
    }

    private void exportTestResults() {
        int numOfPassTC = 0;
        int numOfFailTC = 0;
        File resultXmlFile = new File("/storage/emulated/0/androidtvtest.xml");

        if (!resultXmlFile.exists()) {
            try {
                resultXmlFile.createNewFile();
            } catch (IOException e) {
                TLog.e(this, "Can not create " + resultXmlFile.getPath());
                e.printStackTrace();
            }
        }

        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(resultXmlFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        XmlSerializer xmlSerializer = Xml.newSerializer();

        try {
            xmlSerializer.setOutput(fileOutputStream, "UTF-8");
            xmlSerializer.startDocument(null, false);
            xmlSerializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            xmlSerializer.processingInstruction("xml-stylesheet type=\"text/xsl\" href=\"compatibility_result.xsl\"");
            xmlSerializer.startTag(null, "Result");
            xmlSerializer.attribute(null, "suite_name", "TCTS_VERIFIER");
            xmlSerializer.attribute(null, "report_version", "1.0");

            xmlSerializer.startTag(null, "Build");
            xmlSerializer.attribute(null, "build_device", "hgs1000s");
            xmlSerializer.endTag(null, "Build");

            xmlSerializer.startTag(null, "Module");
            xmlSerializer.attribute(null, "name", "TCTSVerifier");

            xmlSerializer.startTag(null, "TestCase");

            if (listTestedTC != null) {
                for (int i = 0; i < listTestedTC.size(); i++) {
                    ShowResultActivity tc = listTestedTC.get(i);
                    if (tc.isSuccessTestCase()) {
                        numOfPassTC++;
                    } else {
                        numOfFailTC++;
                    }
                    xmlSerializer.startTag(null, "Test");
                    xmlSerializer.attribute(null, "result",
                            tc.isSuccessTestCase() ? "pass" : "fail");
                    xmlSerializer.attribute(null, "name", tc.getName());
                    xmlSerializer.endTag(null, "Test");
                }
            }

            xmlSerializer.endTag(null, "TestCase");

            xmlSerializer.endTag(null, "Module");

            xmlSerializer.startTag(null, "Summary");
            xmlSerializer.attribute(null, "pass", String.valueOf(numOfPassTC));
            xmlSerializer.attribute(null, "fail", String.valueOf(numOfFailTC));
            xmlSerializer.endTag(null, "Summary");

            xmlSerializer.endTag(null, "Result");
            xmlSerializer.endDocument();
            xmlSerializer.flush();
            fileOutputStream.close();
        } catch (IOException e) {
            TLog.e(this, e.toString());
        }

        try {
            File result_zip_file = new File("/storage/emulated/0/test_result.zip"); //TODO Add date time in filename
            FileOutputStream fos = new FileOutputStream(result_zip_file);
            ZipOutputStream zos = new ZipOutputStream(fos);

            /* Firstly, zip xml file */
            InputStream fisResultXml = new FileInputStream(resultXmlFile);
            zipFile(fisResultXml, "androidtvtest.xml", zos);
            fisResultXml.close();
            /* Secondly, zip logo.png */
            @SuppressLint("ResourceType")
            InputStream isLogoPng = getResources().openRawResource(R.drawable.logo);
            zipFile(isLogoPng, "logo.png", zos);
            isLogoPng.close();

                /* Thirdly, zip css file */
            InputStream isCssFile = getAssets().open("compatibility_result.css");
            zipFile(isCssFile, "compatibility_result.css", zos);
            isCssFile.close();

                /* Fourthly, zip xsd file */
            InputStream isXsdFile = getAssets().open("compatibility_result.xsd");
            zipFile(isXsdFile, "compatibility_result.xsd", zos);
            isXsdFile.close();

                /* Fifthly, zip xsl file */
            InputStream isXslFile = getAssets().open("compatibility_result.xsl");
            zipFile(isXslFile, "compatibility_result.xsl", zos);
            isXslFile.close();

            zos.closeEntry();
            zos.close();
            fos.close();
            resultXmlFile.delete();

            Toast.makeText(getApplicationContext(), "Result saved in " + result_zip_file.getPath(),
                    Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void zipFile(InputStream inputStream, String file_name, ZipOutputStream zos) {
        byte buf[] = new byte[1024];
        int len;
        ZipEntry ze = new ZipEntry(file_name);
        try {
            zos.putNextEntry(ze);
            while ((len = inputStream.read(buf)) > 0) {
                zos.write(buf, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
