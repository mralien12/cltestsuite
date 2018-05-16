package alticast.com.cltestsuite.media;

import android.content.Context;
import android.os.RemoteException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import af.dvr.Recording;
import af.dvr.RecordingManager;
import af.media.ChannelPlayer;
import af.media.MediaEventListener;
import af.media.MediaPlayer;
import af.media.MediaPlayerFactory;
import af.media.VideoPlaneControl;
import af.resource.NoAvailableResourceException;
import af.resource.ResourceClient;
import alticast.com.cltestsuite.MainActivity;
import alticast.com.cltestsuite.utils.TLog;
import alticast.com.cltestsuite.utils.TestCase;

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
public class MediaEventListenerTest {
    public static final int ON_BEGINNING = 0;
    public static final int ON_COMPLETION = 1;
    public static final int ON_ERROR = 2;
    public static final int ON_PREPARED = 3;
    public static final int ON_RATED_CHANGE = 4;
    public static final int ON_STOPPED = 5;


    private static MediaEventListenerTest instance;
    private static ChannelPlayer channelPlayer;
    int retOnBeginning = TestCase.FAIL;
    int retOnCompletion = TestCase.FAIL;
    int retOnError = TestCase.FAIL;
    int retOnPrepared = TestCase.FAIL;
    int retOnRatedChanged = TestCase.FAIL;
    int retOnStopped = TestCase.FAIL;

    private MediaEventListenerTest(Context context) {
        if (channelPlayer == null) {
            channelPlayer = MediaPlayerFactory.getInstance()
                    .createChannelPlayer(context, null, VideoPlaneControl.VIDEO_PLANE_MAIN, new ResourceClient() {
                        @Override
                        public String getName() {
                            return null;
                        }

                        @Override
                        public String getDescription() {
                            return null;
                        }
                    });
            channelPlayer.setEventListener(new MediaEventListener() {
                @Override
                public void onError(int i, String s) {
                    retOnError = TestCase.SUCCESS;
                    TLog.i(MediaEventListenerTest.this, "onError");
                }

                @Override
                public void onPrepared() {
                    retOnPrepared = TestCase.SUCCESS;
                    TLog.i(MediaEventListenerTest.this, "onPrepared");
                }

                @Override
                public void onStopped() {
                    retOnStopped = TestCase.SUCCESS;
                    TLog.i(MediaEventListenerTest.this, "onStopped");
                }

                @Override
                public void onCompletion() {
                    retOnCompletion = TestCase.SUCCESS;
                    TLog.i(MediaEventListenerTest.this, "onCompletion");
                }

                @Override
                public void onBeginning() {
                    retOnBeginning = TestCase.SUCCESS;
                    TLog.i(MediaEventListenerTest.this, "onBeginning");
                }

                @Override
                public void onRateChanged(float v) {
                    retOnRatedChanged = TestCase.SUCCESS;
                    TLog.i(MediaEventListenerTest.this, "onRateChanged");
                }

                @Override
                public void onBufferingUpdate(int i) {

                }

                @Override
                public void onMessage(String s, String s1) {

                }

                @Override
                public void onAudioTrack(MediaPlayer.AudioTrack[] audioTracks) {

                }
            });
        }
    }

    public static synchronized MediaEventListenerTest getInstance(Context context) {
        if (instance == null) {
            instance = new MediaEventListenerTest(context);
        }

        return instance;
    }

    public static void setUpStorage() {
        List<String> list = new ArrayList<String>();

        BufferedReader buf_reader = null;
        try {
            buf_reader = new BufferedReader(new FileReader("/proc/mounts"));
            String line;

            while ((line = buf_reader.readLine()) != null) {
                if (line.contains("/mnt/media_rw") || line.contains("/mnt/expand")) {
                    StringTokenizer tokens = new StringTokenizer(line, " ");
                    String unused = tokens.nextToken(); // device
                    String mount_point = tokens.nextToken(); // mount point/
                    list.add(mount_point + "/");
                }
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (buf_reader != null) {
                try {
                    buf_reader.close();
                } catch (IOException ex) {
                }
            }
        }

        if (list.size() > 0) {
            String dvr_storage_path = list.get(0);
            RecordingManager.getInstance().start(dvr_storage_path);
        }
    }

    public int onBeginingTest() {
        retOnBeginning = TestCase.FAIL;

        setUpStorage();
        Recording[] recordings = RecordingManager.getInstance().getRecordings();
        if (recordings == null || recordings.length <= 0) {
            TLog.e(this, "Recording list is empty");
            MainActivity.mediaEventListenerTestCaseList.get(ON_BEGINNING).setFailedReason("Recording list is empty");
            return retOnBeginning;
        }
        final Recording testRecording = recordings[0];

        channelPlayer.setDataSource(testRecording.getUri());

        try {
            channelPlayer.start();
        } catch (NoAvailableResourceException e) {
            e.printStackTrace();
        }
        channelPlayer.setRate(-1);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        channelPlayer.stop();

        if (retOnBeginning == TestCase.FAIL) {
            TLog.e(this, "onBeginning is not invoked");
            MainActivity.mediaEventListenerTestCaseList.get(ON_BEGINNING).setFailedReason("onBeginning is not invoked");
        }
        return retOnBeginning;
    }

    public int onCompletion() {
        int rate = 1;
        retOnCompletion = TestCase.FAIL;
        Recording testRecording = null;

        setUpStorage();
        Recording[] recordings = RecordingManager.getInstance().getRecordings();
        if (recordings == null || recordings.length <= 0) {
            TLog.e(this, "Recording list is empty");
            MainActivity.mediaEventListenerTestCaseList.get(ON_COMPLETION).setFailedReason("Recording list is empty");
            return retOnCompletion;
        }
        for (int i = 0; i < recordings.length;i++) {
            if (recordings[i].getDuration() > 0) {
                testRecording = recordings[i];
                break;
            }
        }
        if (testRecording ==  null) {
            TLog.e(this, "All recordings duration is zero");
            MainActivity.mediaEventListenerTestCaseList.get(ON_COMPLETION).setFailedReason("All recordings duration is zero");
            return retOnCompletion;
        }

        channelPlayer.setDataSource(testRecording.getUri());

        try {
            channelPlayer.start();
        } catch (NoAvailableResourceException e) {
            e.printStackTrace();
        }

        channelPlayer.setRate(rate);
        TLog.i(this, "test duration will take : " + testRecording.getDuration() + " seconds");
        try {
            TimeUnit.SECONDS.sleep(testRecording.getDuration() + 1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        channelPlayer.stop();

        if (retOnCompletion == TestCase.FAIL) {
            TLog.e(this, "onCompletion is not invoked");
            MainActivity.mediaEventListenerTestCaseList.get(ON_COMPLETION).setFailedReason("onCompletion is not invoked");
        }
        return retOnCompletion;
    }

    public int onError() throws RemoteException {
        retOnError = TestCase.FAIL;

        setUpStorage();
        Recording[] recordings = RecordingManager.getInstance().getRecordings();
        if (recordings == null || recordings.length <= 0) {
            TLog.e(this, "Recording list is empty");
            MainActivity.mediaEventListenerTestCaseList.get(ON_ERROR).setFailedReason("Recording list is empty");
            return retOnError;
        }
        final Recording testRecording = recordings[0];

        channelPlayer.setDataSource(testRecording.getUri());

        try {
            channelPlayer.start();
        } catch (NoAvailableResourceException e) {
            e.printStackTrace();
        }

        /* Right after start test case, remove USB HDD to get error */
        try {
            TimeUnit.SECONDS.sleep(testRecording.getDuration() + 1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        channelPlayer.stop();

        if (retOnError == TestCase.FAIL) {
            TLog.e(this, "onError is not invoked");
            MainActivity.mediaEventListenerTestCaseList.get(ON_ERROR).setFailedReason("onError is not invoked");
        }
        return retOnError;
    }

    public int onPrepared() {
        retOnPrepared = TestCase.FAIL;

        setUpStorage();
        Recording[] recordings = RecordingManager.getInstance().getRecordings();
        if (recordings == null || recordings.length <= 0) {
            TLog.e(this, "Recording list is empty");
            MainActivity.mediaEventListenerTestCaseList.get(ON_PREPARED).setFailedReason("Recording list is empty");
            return retOnPrepared;
        }
        final Recording testRecording = recordings[0];

        channelPlayer.setDataSource(testRecording.getUri());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (retOnPrepared == TestCase.FAIL) {
            TLog.e(this, "onPrepared is not invoked");
            MainActivity.mediaEventListenerTestCaseList.get(ON_PREPARED).setFailedReason("onPrepared is not invoked");
        }
        return retOnPrepared;
    }

    public int onRateChanged() {
        retOnRatedChanged = TestCase.FAIL;

        setUpStorage();
        Recording[] recordings = RecordingManager.getInstance().getRecordings();
        if (recordings == null || recordings.length <= 0) {
            TLog.e(this, "Recording list is empty");
            MainActivity.mediaEventListenerTestCaseList.get(ON_RATED_CHANGE).setFailedReason("Recording list is empty");
            return retOnRatedChanged;
        }
        final Recording testRecording = recordings[0];

        channelPlayer.setDataSource(testRecording.getUri());

        try {
            channelPlayer.start();
        } catch (NoAvailableResourceException e) {
            e.printStackTrace();
        }

        channelPlayer.setRate(1);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        channelPlayer.stop();
        if (retOnRatedChanged == TestCase.FAIL) {
            TLog.e(this, "onRateChanged is not invoked");
            MainActivity.mediaEventListenerTestCaseList.get(ON_RATED_CHANGE).setFailedReason("onRateChanged is not invoked");
        }
        return retOnRatedChanged;
    }

    public int onStopped() {
        retOnStopped = TestCase.FAIL;

        setUpStorage();
        Recording[] recordings = RecordingManager.getInstance().getRecordings();
        if (recordings == null || recordings.length <= 0) {
            TLog.e(this, "Recording list is empty");
            MainActivity.mediaEventListenerTestCaseList.get(ON_STOPPED).setFailedReason("Recording list is empty");
            return retOnStopped;
        }
        final Recording testRecording = recordings[0];

        channelPlayer.setDataSource(testRecording.getUri());

        try {
            channelPlayer.start();
        } catch (NoAvailableResourceException e) {
            e.printStackTrace();
        }

        channelPlayer.stop();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (retOnStopped == TestCase.FAIL) {
            TLog.e(this, "onStopped is not invoked");
            MainActivity.mediaEventListenerTestCaseList.get(ON_STOPPED).setFailedReason("onStopped is not invoked");
        }
        return retOnStopped;
    }
}
