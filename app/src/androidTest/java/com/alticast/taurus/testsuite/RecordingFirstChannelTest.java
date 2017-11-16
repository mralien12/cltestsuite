package com.alticast.taurus.testsuite;

import android.support.test.runner.AndroidJUnit4;

import com.alticast.taurus.testsuite.util.TLog;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import af.channel.Channel;
import af.channel.ChannelManager;
import af.dvr.Recording;
import af.dvr.RecordingManager;
import af.dvr.RecordingSession;
import af.dvr.RecordingSessionCallback;
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

    @Test
    public void recordingTest(){
        if (channels == null) {
            channels = ChannelManager.getInstance().getChannelList(ChannelManager.CHANNEL_LIST_ALL);
        }
        currentChannel = channels[0];

        /* Create session for recording */
        recordingSession = RecordingManager.getInstance().createRecordingSession(new RecordingSessionCallback() {
            @Override
            public void onError(short i) {
                TLog.e(this, "onError");
            }

            @Override
            public void onRecordingStopped() {
                TLog.i(this, "onRecordingStopped");
            }

            @Override
            public void onRecordingStarted(String s) {
                TLog.i(this, "onRecordingStarted" + s);
            }

            @Override
            public void onTuned() {
                TLog.i(this, "onTuned");
                record = recordingSession.startRecording(programUri);
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

        /* Start recording */
        try {
            recordingSession.tune(currentChannel.getUri());
        } catch (NoAvailableResourceException e) {
            e.printStackTrace();
        }

        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        recordingSession.stopRecording();
        assertThat(record, is(notNullValue()));
        TLog.i(this, "Testcase completed");
    }

}
