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

package com.alticast.taurus.testsuite.dvr;

import android.support.test.runner.AndroidJUnit4;
import com.alticast.taurus.testsuite.util.TLog;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RecordingFirstChannelMultiTimeTest {
    private Channel[] channels;
    private Channel currentChannel;
    private RecordingSession recordingSession;
    private Recording record;
    private String evtId = "0";
    private boolean isTunedSuccess = false;
    private boolean isRecordingStopped = false;
    private boolean isRecordingStarted = false;
    private boolean isRightTime = false;
    private int timeChecking;

    @BeforeClass
    public static void setUpClass() {
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

    @Before
    public void setUpChannels() {
        isTunedSuccess = false;
        isRecordingStopped = false;
        isRecordingStarted = false;
        isRightTime = false;

        if (channels == null) {
            channels = ChannelManager.getInstance().getChannelList(ChannelManager.CHANNEL_LIST_ALL);
        }
        assertThat("Length of channel list must be greater than 0", channels.length > 0);
        currentChannel = channels[0];

        /* Check the DVR storage */
        assertThat("Root path of DVR storage", RecordingManager.getInstance().getStoragePath(), is(notNullValue()));

        /**/
        RecordingManager.getInstance().start("");

        /* Create session for recording */
        recordingSession = RecordingManager.getInstance().createRecordingSession(new RecordingSessionCallback() {
            @Override
            public void onError(short i) {
                isTunedSuccess = false;
                TLog.e(this, "onError");
            }

            @Override
            public void onRecordingStopped() {
                TLog.i(this, "onRecordingStopped");
                isRecordingStopped = true;
            }

            @Override
            public void onRecordingStarted(String s) {
                TLog.i(this, "onRecordingStarted" + s);
                isRecordingStarted = true;
            }

            @Override
            public void onTuned() {
                TLog.i(this, "onTuned");
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
            e.printStackTrace();
        }

        /* onTuned() callback function need some delay to execute.
         * So we try to sleep 1-2 second(s).
         */
        try {
            TimeUnit.MILLISECONDS.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertThat("Has the tune request been fulfill?", isTunedSuccess, is(true));
    }

    @Test
    public void recording030Second() {
        timeChecking = 30;
        /* Start recording */
        Program[] programs = currentChannel.getPrograms(1473527700000L, 1473528600000L);
        assertThat("Programs must be greater than 0!! please check MediaPlayerTest", programs.length > 0);
        record = recordingSession.startRecording(programs[0].getProgramUri());

        TLog.i(this, "Start recording in 30 seconds, Channels number: "+ programs.length + " First Program duration: "+programs[0].getDuration());

        try {
            TimeUnit.SECONDS.sleep(timeChecking);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void recording060Second() {
        timeChecking = 60;
        /* Start recording */
        Program[] programs = currentChannel.getPrograms(1473527700000L, 1473528600000L);
        assertThat("Programs must be greater than 0!! please check MediaPlayerTest", programs.length > 0);
        record = recordingSession.startRecording(programs[0].getProgramUri());

        TLog.i(this, "Start recording in 60 seconds, Channels number: "+ programs.length + " First Program duration: "+programs[0].getDuration());

        try {
            TimeUnit.SECONDS.sleep(timeChecking);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //@Ignore
    @Test
    public void recording120Second() {
        timeChecking = 120;
        /* Start recording */
        Program[] programs = currentChannel.getPrograms(1473527700000L, 1473528600000L);
        assertThat("Programs must be greater than 0!! please check MediaPlayerTest", programs.length > 0);
        record = recordingSession.startRecording(programs[0].getProgramUri());

        TLog.i(this, "Start recording in 120 seconds, Channels number: "+ programs.length + " First Program duration: "+programs[0].getDuration());

        try {
            TimeUnit.SECONDS.sleep(timeChecking);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //@Ignore
    @Test
    public void recording180Second() {
        timeChecking = 180;
        /* Start recording */
        Program[] programs = currentChannel.getPrograms(1473527700000L, 1473528600000L);
        assertThat("Programs must be greater than 0!! please check MediaPlayerTest", programs.length > 0);
        record = recordingSession.startRecording(programs[0].getProgramUri());

        TLog.i(this, "Start recording in 180 seconds, Channels number: "+ programs.length + " First Program duration: "+programs[0].getDuration());

        try {
            TimeUnit.SECONDS.sleep(timeChecking);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @After
    public void stopRecord() {
        recordingSession.stopRecording();

        try {
            TimeUnit.MILLISECONDS.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        switch (timeChecking){
            case 30:
                if (timeChecking - 2 <= record.getDuration() && record.getDuration() <= timeChecking + 2)
                    isRightTime = true;
                break;
            case 60:
                if (timeChecking - 2 <= record.getDuration() && record.getDuration() <= timeChecking + 2)
                    isRightTime = true;
                break;
            case 120:
                if (timeChecking - 2 <= record.getDuration() && record.getDuration() <= timeChecking + 2)
                    isRightTime = true;
                break;
            case 180:
                if (timeChecking - 2 <= record.getDuration() && record.getDuration() <= timeChecking + 2)
                    isRightTime = true;
                break;
            default: isRightTime = false;
        }

        assertThat("Recording Time : " + record.getDuration()+" _ Expected Time : " + timeChecking, isRightTime, is(true));
        TLog.i(this, "Recording length : " + record.getDuration());
        assertThat("Do Recording stop?", isRecordingStopped, is(true));
        recordingSession.release();
        assertThat("Record is expected not null", record, is(notNullValue()));
        TLog.i(this, "Testcase completed");
    }

    @AfterClass
    public static void tearDownClass() {
        RecordingManager.getInstance().stop();
    }
}
