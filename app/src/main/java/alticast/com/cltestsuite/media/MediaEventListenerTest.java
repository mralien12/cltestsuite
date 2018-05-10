package alticast.com.cltestsuite.media;

import android.content.Context;
import android.view.ViewGroup;

import af.dvr.Recording;
import af.dvr.RecordingManager;
import af.media.MediaEventListener;
import af.media.MediaPlayer;
import af.media.MediaPlayerFactory;
import af.media.VideoPlaneControl;
import af.resource.NoAvailableResourceException;
import af.resource.ResourceClient;
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
    private static MediaEventListenerTest instance;
    private static MediaPlayer mediaPlayer;
    int ret= TestCase.FAIL;

    private MediaEventListenerTest(Context context, ViewGroup viewGroup){
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayerFactory.getInstance()
                    .createMediaPlayer(context, viewGroup, VideoPlaneControl.VIDEO_PLANE_MAIN, new ResourceClient() {
                        @Override
                        public String getName() {
                            return null;
                        }

                        @Override
                        public String getDescription() {
                            return null;
                        }
                    });
            mediaPlayer.setEventListener(new MediaEventListener() {
                @Override
                public void onError(int i, String s) {
                    TLog.i(this, "onError");
                }

                @Override
                public void onPrepared() {
                    TLog.i(this, "onPrepared");
                }

                @Override
                public void onStopped() {
                    TLog.i(this, "onStopped");
                }

                @Override
                public void onCompletion() {

                }

                @Override
                public void onBeginning() {
                    TLog.i(this, "onBeginning");
                    ret = TestCase.SUCCESS;
                }

                @Override
                public void onRateChanged(float v) {

                }

                @Override
                public void onBufferingUpdate(int i) {

                }

                @Override
                public void onMessage(String s, String s1) {

                }

                @Override
                public void onSubtitleDisplay(int i, int i1) {

                }

                @Override
                public void onSubtitleClear() {

                }

                @Override
                public void onSubtitleImage(int i, int i1, int i2, int i3, int i4, byte[] bytes) {

                }

                @Override
                public void onSubtitleUpdate() {

                }

                @Override
                public void onSubtitle(boolean b, SubtitleInfo[] subtitleInfos) {

                }

                @Override
                public void onTeletext(boolean b, TeletextInfo[] teletextInfos) {

                }
            });
        }
    }

    public static synchronized MediaEventListenerTest getInstance(Context context, ViewGroup viewGroup) {
        if (instance == null) {
            instance = new MediaEventListenerTest(context, viewGroup);
        }

        return instance;
    }

    public int onBeginingTest(){
        int ret = TestCase.FAIL;

        Recording[] recordings = RecordingManager.getInstance().getRecordings();
        if (recordings.length <= 0) {
            TLog.e(this, "Recording list is empty");
            return ret;
        }
        final Recording testRecording = recordings[0];

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                mediaPlayer.setDataSource(testRecording.getUri());
                try {
                    mediaPlayer.start();
                    mediaPlayer.setRate(-1);

                } catch (NoAvailableResourceException e) {
                    e.printStackTrace();
                }
            }
        });

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (ret == TestCase.FAIL) {
            TLog.e(this, "onBeginning is not invoked");
        }
        return ret;
    }
}
