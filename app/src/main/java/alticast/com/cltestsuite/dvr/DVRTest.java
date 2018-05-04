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

package alticast.com.cltestsuite.dvr;

import android.widget.ProgressBar;

import com.alticast.af.builder.RAntennaInfoDiseqc;
import com.alticast.af.builder.RAntennaInfoLnb;
import com.alticast.af.builder.RScanConflictRegion;
import com.alticast.af.builder.RScanEventObject;
import com.alticast.af.builder.RScanParam;
import com.alticast.af.builder.RTuneParamSat;
import com.alticast.af.builder.RTuneTpInfoSat;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import af.builder.ScanEventListener;
import af.builder.ScanManager;
import af.channel.Channel;
import af.channel.ChannelManager;
import af.dvr.Recording;
import af.dvr.RecordingManager;
import af.dvr.RecordingSession;
import af.dvr.RecordingSessionCallback;
import af.epg.Program;
import af.resource.NoAvailableResourceException;
import af.resource.ResourceClient;
import alticast.com.cltestsuite.MainActivity;
import alticast.com.cltestsuite.channelbuilder.ScanTest;
import alticast.com.cltestsuite.utils.TLog;
import alticast.com.cltestsuite.utils.TestCase;

public class DVRTest {
    public static final int ON_RECORDING_RECEIVED = 0;
    public static final int STATE_LISTENER_ON_STATED = 1;
    public static final int STATE_LISTENER_ON_STOPPED = 2;
    public static final int RECORDING_SESSION_CALLBACK = 3;

    private Channel[] channels;
    private Channel currentChannel;
    private RecordingSession recordingSession;
    private int timeChecking;
    private static boolean isTunedSuccess, isRecordingStopped, isRecordingStarted, isRightTime;
    private Recording record;
    private static DVRTest instance = null;
    private static int ret;
    private static int retStart, retStop;
    private static String devices;
    private static int count = 0;

    private static RecordingManager recordingManager;

    protected DVRTest() {
    }

    public static synchronized DVRTest getInstance() {
        retStart = retStop = TestCase.FAIL;

        ScanTest scanTest = ScanTest.getInstance();
        if (instance == null) {
            instance = new DVRTest();

            recordingManager.getInstance().setStateListener(new RecordingManager.StateListener() {
                @Override
                public void onStarted(String s) {
                    retStart = TestCase.SUCCESS;
                    retStop = TestCase.FAIL;
                }

                @Override
                public void onStopped() {
                    retStart = TestCase.FAIL;
                    retStop = TestCase.SUCCESS;
                }
            });
        }

        return instance;
    }

    // not have idea
    public int onRecordingReceived() {
        ret = TestCase.FAIL;
        return ret;
    }

    public int stateListenerOnStated() {
        devices = null;

        //add device rootpath
        List<String> list = new ArrayList<String>();
        BufferedReader buf_reader = null;
        try {
            buf_reader = new BufferedReader(new FileReader("/proc/mounts"));
            String line;

            while ((line = buf_reader.readLine()) != null) {
                if (line.contains("/mnt/media_rw")) {
                    String[] tokens = line.split(" ");
                    devices = tokens[1];
                    TLog.i(this, "rootpath: " + devices);
                }
            }
        } catch (FileNotFoundException e) {
            TLog.e(this, "stateListenerOnStated: FileNotFoundException");
            MainActivity.dvrTestCaseList.get(DVRTest.STATE_LISTENER_ON_STOPPED).setFailedReason("File not found");
            e.printStackTrace();
        } catch (IOException e) {
            TLog.e(this, "stateListenerOnStated: IOException");
            MainActivity.dvrTestCaseList.get(DVRTest.STATE_LISTENER_ON_STOPPED).setFailedReason("IO Exception, Check your USB connection");
            e.printStackTrace();
        } finally {
            if (buf_reader != null) {
                try {
                    buf_reader.close();
                } catch (IOException ex) {
                    TLog.e(this, "stateListenerOnStated: Read Buffer error");
                    MainActivity.dvrTestCaseList.get(DVRTest.STATE_LISTENER_ON_STOPPED).setFailedReason("Read Buffer error");
                }
            }
        }

        if (devices != null) {
            recordingManager.getInstance().start(devices + "/");
        }else {
            TLog.e(this, "stateListenerOnStated: Not found storage device");
            MainActivity.dvrTestCaseList.get(DVRTest.STATE_LISTENER_ON_STATED).setFailedReason("Not found storage device");
        }

        /* Delay few miliseconds for save result*/
        try {
            TimeUnit.MILLISECONDS.sleep(1000);
        } catch (InterruptedException e) {
            TLog.e(this, "stateListenerOnStated: InterruptedException");
            e.printStackTrace();
        }
        return retStart;
    }

    public int stateListenerOnStopped() {
        recordingManager.getInstance().stop();

        /* Delay few miliseconds for save result*/
        try {
            TimeUnit.MILLISECONDS.sleep(1000);
        } catch (InterruptedException e) {
            TLog.e(this, "stateListenerOnStopped: InterruptedException");
            e.printStackTrace();
        }
        return retStop;
    }

    public int recordingSessionCallback() {
        ret = TestCase.FAIL;
        isTunedSuccess = false;
        isRecordingStopped = false;
        isRecordingStarted = false;
        isRightTime = false;
        timeChecking = 30;

        //
        ScanResult();
        //

        stateListenerOnStated();
        RecordingManager.getInstance().start(devices + "/");

        if (channels == null) {
            channels = ChannelManager.getInstance().getChannelList(ChannelManager.CHANNEL_LIST_ALL);
        }

        if (channels.length > 0) {
            currentChannel = channels[0];
            if (RecordingManager.getInstance().getStoragePath() != null) {
                RecordingManager.getInstance().start("");

                recordingSession = RecordingManager.getInstance().createRecordingSession(new RecordingSessionCallback() {
                    @Override
                    public void onError(short i) {
                        isTunedSuccess = false;
                        TLog.e(this, "onError");
                    }

                    @Override
                    public void onRecordingStopped() {
                        isRecordingStopped = true;
                    }

                    @Override
                    public void onRecordingStarted(String s) {
                        isRecordingStarted = true;
                    }

                    @Override
                    public void onTuned() {
                        isTunedSuccess = true;
                    }
                }, new ResourceClient() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public String getDescription() {
                        return null;
                    }
                });

                try {
                    recordingSession.tune(currentChannel.getUri());
                } catch (NoAvailableResourceException e) {
//                    returnResult = false;
                    e.printStackTrace();
                }

                try {
                    TimeUnit.MILLISECONDS.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
            }
        } else {
            TLog.e(this, "recordingSessionCallback: Not have channel");
            MainActivity.dvrTestCaseList.get(DVRTest.RECORDING_SESSION_CALLBACK).setFailedReason("Not have channel");
        }

        try {
            Program[] programs = currentChannel.getPrograms(1473527700000L, 1473528600000L);
            if (programs.length > 0) {
                record = recordingSession.startRecording(programs[0].getProgramUri());
                try {
                    TimeUnit.SECONDS.sleep(timeChecking);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                recordingSession.stopRecording();
                if (timeChecking - 2 <= record.getDuration() && record.getDuration() <= timeChecking + 2)
                    isRightTime = true;
                if (!isRightTime) {
                    TLog.e(this, "Wrong recording time");
                }

                recordingSession.release();
                RecordingManager.getInstance().stop();

                if (isTunedSuccess == true && isRecordingStopped == true && isRightTime == true)
                    ret = TestCase.SUCCESS;
            }
        } catch (NullPointerException e) {
            TLog.e(this, "Can not catch any programs");
            MainActivity.dvrTestCaseList.get(DVRTest.RECORDING_SESSION_CALLBACK).setFailedReason("Can not catch any programs");
            ret = TestCase.FAIL;
        }

        TLog.i(this, "isTunedSuccess: " + count + "--isRecordingStarted: " + isRecordingStarted + "--isRecordingStopped: " + isRecordingStopped + "--isTunedSuccess: " + isTunedSuccess);

        return ret;
    }

    // Addition DEMO Scan
    private static final String SAT_NAME = "ASTRA_1";
    private static final int LNB_FREQ = 9750;
    private static final int FREQ = 11362;
    private static final int SYMBOL_RATE = 22000;
    private static final int POLARIZATION = 1;
    private static final int TIMEOUT = 30;
    private RScanParam rs;
    private boolean retz;
    private ProgressBar pgbScan;

    private RScanParam generateParam(String satName, int lnb_freq, int transponder_freq) {
        // Generate rScanParam
        RScanParam rScanParam = null;
        List<RTuneParamSat> item = new ArrayList();

        RAntennaInfoLnb rAntennaInfoLnb = new RAntennaInfoLnb(-1, satName, lnb_freq, 0);
        RAntennaInfoDiseqc diseqc = new RAntennaInfoDiseqc(-1, satName, 0, 0, 0, 1, 1);
        List<RTuneTpInfoSat> tpList = new ArrayList();
        RTuneTpInfoSat rTuneTpInfoSat = new RTuneTpInfoSat(transponder_freq, SYMBOL_RATE, POLARIZATION, 1, 1, 2, 0);
        tpList.add(rTuneTpInfoSat);

        RTuneParamSat rTuneParamSat = new RTuneParamSat(0, rAntennaInfoLnb, diseqc, tpList);
        item.add(rTuneParamSat);

        rScanParam = new RScanParam(item, 1, 1, false, 0);
        return rScanParam;
    }

    private void scanSatellite(RScanParam rScanParam) {
        final Channel[] channels = null;
        rs = rScanParam;

        ScanManager.getInstance().setEventListener(new ScanEventListener() {
            @Override
            public void notify(RScanEventObject rScanEventObject) {
            }

            @Override
            public void notifyScanSaveResultFinished() {
            }

            @Override
            public void selectConflictedChannelRegion(int i, RScanConflictRegion[] rScanConflictRegions) {

            }
        });

        Thread thrScan = new Thread(new Runnable() {

            @Override
            public void run() {
                retz = ScanManager.getInstance().startScan(rs);
                if (!retz) {
                    return;
                }

                retz = ScanManager.getInstance().stopScan();
                if (!retz) {
                    return;
                }

                final Channel[] channels = ChannelManager.getInstance().getChannelList(ChannelManager.CHANNEL_LIST_ALL);
                final String[] channel_name = new String[channels.length];
                for (int i = 0; i < channels.length; i++) {
                    channel_name[i] = channels[i].getName();
                }
            }
        });

        thrScan.start();
    }

    public boolean ScanResult() {
        boolean result = true;

        RScanParam rScanParam = null;
        rScanParam = generateParam(SAT_NAME, LNB_FREQ, FREQ);
        scanSatellite(rScanParam);

        retz = ScanManager.getInstance().saveResult();
        return result;
    }
}
