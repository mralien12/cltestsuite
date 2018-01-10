package com.alticast.taurus.testsuite.media;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.alticast.taurus.testsuite.util.TLog;

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
import af.epg.Program;
import af.media.MediaPlayer;
import af.media.MediaPlayerFactory;
import af.resource.NoAvailableResourceException;
import af.resource.ResourceClient;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by st8018 on 1/10/18.
 */

@RunWith(AndroidJUnit4.class)
public class MediaPlayerTest {
    private static Channel[] channels;
    private static MediaPlayer mediaPlayer = null;
    private static Context instrumentationCtx;

    @BeforeClass
    public static void setUpClass() {
        instrumentationCtx = InstrumentationRegistry.getContext();

        if (channels == null) {
            channels = ChannelManager.getInstance().getChannelList(ChannelManager.CHANNEL_LIST_ALL);
        }

        assertThat("Length of channel list must be greater than 0", channels.length > 0);
    }

    @Test
    public void singleChannelTest(){
        Channel singleChannel;

        singleChannel = channels[0];
        mediaPlayer = MediaPlayerFactory.getInstance().createMediaPlayer(instrumentationCtx , null, 0,new ResourceClient() {
            @Override
            public String getName() {
                return null;
            }

            @Override
            public String getDescription() {
                return null;
            }
        });

        TLog.i(this, "mediaPlayer " + mediaPlayer);

        TLog.i(this, "setDataSource()" + singleChannel.getUri());
        mediaPlayer.setDataSource(singleChannel.getUri());

        try {
            TLog.i(this, "changeChannel()");
            mediaPlayer.start();
        } catch (NoAvailableResourceException e) {
            e.printStackTrace();
        }

        Program[] programs;

        do {
            try {
                TLog.i(this, "Wait for create program datas()");
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            programs = singleChannel.getPrograms(1473527700000L, 1473528600000L);

        } while(programs.length <= 0);

        mediaPlayer.stop();
    }
}
