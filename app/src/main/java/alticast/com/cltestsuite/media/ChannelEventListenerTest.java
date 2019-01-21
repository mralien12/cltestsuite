package alticast.com.cltestsuite.media;

import android.content.Context;

import af.channel.Channel;
import af.channel.ChannelManager;
import af.media.ChannelPlayer;
import af.media.LiveEventListener;
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

public class ChannelEventListenerTest {
    private static ChannelEventListenerTest instance;
    private Context context;
    private int ret;

    private ChannelEventListenerTest(Context context) {
        this.context  = context;
    }

    public static synchronized ChannelEventListenerTest getInstance(Context context) {
        if (instance == null) {
            instance = new ChannelEventListenerTest(context);
        }

        return instance;
    }

    public int onChannelEvent() {
        ret = TestCase.FAIL;
        ChannelPlayer channelPlayer;
        channelPlayer = MediaPlayerFactory.getInstance().createChannelPlayer(context, null, VideoPlaneControl.VIDEO_PLANE_MAIN, new ResourceClient() {
            @Override
            public String getName() {
                return null;
            }

            @Override
            public String getDescription() {
                return null;
            }
        });;

      channelPlayer.setEventListener(new LiveEventListener() {
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

          @Override
          public void onError(int i, String s) {

          }

          @Override
          public void onPrepared() {

          }

          @Override
          public void onStopped() {

          }

          @Override
          public void onCompletion() {

          }

          @Override
          public void onBeginning() {

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
          public void onAudioTrack(MediaPlayer.AudioTrack[] audioTracks) {

          }
      });

        channelPlayer.setChannelEventListener(new af.media.ChannelEventListener() {
            @Override
            public void onChannelEvent(int eventCode, String channelUri) {
                switch (eventCode) {
                    case  TUNING_FAILED:
                        break;
                    case TUNED_AWAY:
                        break;
                    case STOPPED_BY_USER:
                        break;
                    case STOPPED_BY_SYSTEM:
                        break;
                    case PARENTAL_CONTROL_BLOCKED:
                        break;
                    case OUT_OF_RESOURCE:
                        break;
                    case NORMAL_CONTENT:
                        ret = TestCase.SUCCESS;
                        break;
                    case CONTENT_NOT_FOUND:
                        break;
                    case CA_REFUSAL:
                        break;
                    default:
                }
            }
        });

        Channel[] channels = ChannelManager.getInstance().getChannelList(ChannelManager.CHANNEL_LIST_ALL);
        Channel playChannel = null;
        if (channels.length > 0) {
            playChannel = channels[0];
        } else {
            TLog.e(this, "Empty channel list");
            MainActivity.mediaTestCaseList.get(MediaTest.CHANNEL_EVENT_LISTENER).setFailedReason("Empty channel list");
        }
        channelPlayer.setDataSource(playChannel.getUri());

        try {
            channelPlayer.start();
        } catch (NoAvailableResourceException e) {
            e.printStackTrace();
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        channelPlayer.stop();
        return ret;
    }
}
