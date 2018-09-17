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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

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
import com.alticast.af.dvr.RRecordingSessionCallback;

public class DVRTest {
    public static final int ON_RECORDING_RECEIVED = 0;
    public static final int STATE_LISTENER_ON_STATED = 1;
    public static final int STATE_LISTENER_ON_STOPPED = 2;
    public static final int RECORDING_SESSION_CALLBACK = 3;
    public static int timeChecking = 30;

    private Channel[] channels;
    private Channel currentChannel;
    private RecordingSession recordingSession;
    private static boolean isTunedSuccess, isRecordingStopped, isRecordingStarted, isRightTime;
    private Recording record;
    private static DVRTest instance = null;
    private static int ret;
    private static int retStart, retStop;
    private static String devices;
    private static int count = 0;
    private static String errorLog;

    private static RecordingManager recordingManager;

    protected DVRTest() {
    }

    public static synchronized DVRTest getInstance() {
        retStart = retStop = TestCase.FAIL;
        errorLog = null;

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
//        List<String> list = new ArrayList<String>();
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
            errorLog = "File not found";
            e.printStackTrace();
        } catch (IOException e) {
            TLog.e(this, "stateListenerOnStated: IOException");
            errorLog = "IO Exception, Check your USB connection";
            e.printStackTrace();
        } finally {
            if (buf_reader != null) {
                try {
                    buf_reader.close();
                } catch (IOException ex) {
                    TLog.e(this, "stateListenerOnStated: Read Buffer error");
                    errorLog = "stateListenerOnStated: Read Buffer error";
                }
            }
        }

        if (devices != null) {
            recordingManager.getInstance().start(devices + "/");
        }else {
            TLog.e(this, "stateListenerOnStated: Not found storage device");
            if (errorLog == null)
            {
                errorLog = "Not found storage device";
            }
        }

        /* Delay few milliseconds for save result*/
        try {
            TimeUnit.MILLISECONDS.sleep(1000);
        } catch (InterruptedException e) {
            TLog.e(this, "stateListenerOnStated: InterruptedException");
            e.printStackTrace();
        }

        MainActivity.dvrTestCaseList.get(DVRTest.STATE_LISTENER_ON_STATED).setFailedReason(errorLog);
        return retStart;
    }

    public int stateListenerOnStopped() {
        recordingManager.getInstance().stop();

        /* Delay few milliseconds for save result*/
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

        ScanTest.getInstance().SCA_NotifyScanSaveResultFinished();
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
                        TLog.e(this, "RecordingSessionCallback onError");
                        if (i == RRecordingSessionCallback.RECORDING_ERROR_UNKNOWN){
                            TLog.e(this, "Recording error unknown");
                            if (errorLog == null)
                                errorLog = "The requested operation cannot be completed due to a undefined problem";
                        } else if (i == RRecordingSessionCallback.RECORDING_ERROR_INSUFFICIENT_SPACE){
                            TLog.e(this, "Recording error insufficient space");
                            if (errorLog == null)
                                errorLog = "Recording cannot proceed due to insufficient storage space";
                        } else if (i == RRecordingSessionCallback.RECORDING_ERROR_NO_AVAILABLE_RESOURCE){
                            TLog.e(this, "Recording error no available resource");
                            if (errorLog == null)
                                errorLog = "Recording cannot proceed because a requested recording resource was not able to be allocated";
                        } else if (i == RRecordingSessionCallback.RECORDING_ERROR_DISK_ERROR){
                            TLog.e(this, "Recording error disk error");
                            if (errorLog == null)
                                errorLog = "Indicates disk error";
                        } else if (i == RRecordingSessionCallback.RECORDING_ERROR_WRITE_ERROR){
                            TLog.e(this, "Recording error write error");
                            if (errorLog == null)
                                errorLog = "Recording error write error";
                        }
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
                    TLog.e(this, "NoAvailableResourceException");
                    if (errorLog == null)
                        errorLog = "Resource's recording not available";
                    e.printStackTrace();
                }

                try {
                    TimeUnit.MILLISECONDS.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                /* Start recording 30s */
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
                            if (errorLog == null)
                                errorLog = "Wrong recording time";
                        }

                        recordingSession.release();
                        RecordingManager.getInstance().stop();

                        if (isTunedSuccess == true && isRecordingStopped == true && isRightTime == true)
                            ret = TestCase.SUCCESS;
                    }else {
                        TLog.e(this, "Can not catch any programs");
                        if (errorLog == null)
                            errorLog = "Can not catch any programs";
                        ret = TestCase.FAIL;
                    }
                } catch (NullPointerException e) {
                    TLog.e(this, "Null programs, check MediaPlayerTest");
                    if (errorLog == null)
                        errorLog = "Null programs";
                    ret = TestCase.FAIL;
                }

            } else {
                TLog.e(this, "recordingSessionCallback: Do not have DVR storage");
                if (errorLog == null)
                    errorLog = "Do not have DVR storage";
            }
        } else {
            TLog.e(this, "recordingSessionCallback: Do not have channel");
            errorLog = "Do not have channel";
        }

        TLog.i(this, "isTunedSuccess: " + count + "--isRecordingStarted: " + isRecordingStarted + "--isRecordingStopped: " + isRecordingStopped + "--isTunedSuccess: " + isTunedSuccess);
        MainActivity.dvrTestCaseList.get(DVRTest.RECORDING_SESSION_CALLBACK).setFailedReason(errorLog);

        return ret;
    }
}
