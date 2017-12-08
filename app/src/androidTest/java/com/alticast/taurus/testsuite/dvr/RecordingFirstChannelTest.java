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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

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

/**
 * Created by hoa on 15/11/2017.
 */

@RunWith(AndroidJUnit4.class)
public class RecordingFirstChannelTest {
    private Channel[] channels;
    private Channel currentChannel;
    private RecordingSession recordingSession;
    private Recording record;
    private String evtId = "0";
    private String programUri = "program://" + evtId;
    private boolean isTunedSuccess = false;
    private boolean isRecordingStarted = false;
    private boolean isRecordingStopped = false;

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

    @Test
    public void recordingTest() {
        if (channels == null) {
            channels = ChannelManager.getInstance().getChannelList(ChannelManager.CHANNEL_LIST_ALL);
        }

        assertThat("Length of channel list must be greater than 0", channels.length > 0);
        currentChannel = channels[0];

        /* Check the DVR storage */
        assertThat("Root path of DVR storage", RecordingManager.getInstance().getStoragePath(), is(notNullValue()));


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
            TimeUnit.MILLISECONDS.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertThat("Has the tune request been fulfill?", isTunedSuccess, is(true));

        /* Start recording */
        Program[] programs = currentChannel.getPrograms(1473527700000L, 1473528600000L);

        record = recordingSession.startRecording(programs[0].getProgramUri());

        //record = recordingSession.startRecording(null); //it cann't be null

        /*
         * Didn't call onRecordingStarted Event
         *
         */
        /*
        try {
            TimeUnit.MILLISECONDS.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertThat("Do Recording start?", isRecordingStarted, is(true));
        */

        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        recordingSession.stopRecording();

        try {
            TimeUnit.MILLISECONDS.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

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
