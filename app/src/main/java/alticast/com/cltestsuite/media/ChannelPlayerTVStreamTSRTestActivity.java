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

package alticast.com.cltestsuite.media;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import af.channel.Channel;
import af.channel.ChannelManager;
import af.media.ChannelEventListener;
import af.media.ChannelPlayer;
import af.media.MediaEventListener;
import af.media.MediaPlayerFactory;
import af.media.VideoPlaneControl;
import af.resource.NoAvailableResourceException;
import af.resource.ResourceClient;
import alticast.com.cltestsuite.MainActivity;
import alticast.com.cltestsuite.R;
import alticast.com.cltestsuite.utils.TLog;
import alticast.com.cltestsuite.utils.TestCase;

public class ChannelPlayerTVStreamTSRTestActivity extends Activity {
    Intent intent;
    ChannelPlayer channelPlayer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_player_tvstream_live);

        intent = new Intent();
        intent.putExtra("RESULT", TestCase.FAIL);
        setResult(MediaTest.CHANNEL_PLAYER_TVSTREAM_LIVE, intent);

        LinearLayout groupView = (LinearLayout) findViewById(R.id.channel_player_tvstream_tsr_layout);
        channelPlayer = MediaPlayerFactory.getInstance().
                createChannelPlayer(this, groupView, VideoPlaneControl.VIDEO_PLANE_MAIN, new ResourceClient() {
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
                TLog.e(ChannelPlayerTVStreamTSRTestActivity.this, "onError");
            }

            @Override
            public void onPrepared() {
                TLog.i(ChannelPlayerTVStreamTSRTestActivity.this, "onPrepared");
            }

            @Override
            public void onStopped() {
                TLog.i(ChannelPlayerTVStreamTSRTestActivity.this, "onStopped");
            }

            @Override
            public void onCompletion() {

            }

            @Override
            public void onBeginning() {
                TLog.i(ChannelPlayerTVStreamTSRTestActivity.this, "onBeginning");
            }

            @Override
            public void onRateChanged(float v) {
                TLog.i(ChannelPlayerTVStreamTSRTestActivity.this, "onRateChanged");
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

        channelPlayer.setChannelEventListener(new ChannelEventListener() {
            @Override
            public void onChannelEvent(int eventCode, String channelUri) {
                TLog.i(ChannelPlayerTVStreamTSRTestActivity.this, "eventCode :" + eventCode + ", channelUri " + channelUri);
            }
        });

        Channel[] channels = ChannelManager.getInstance().getChannelList(ChannelManager.CHANNEL_LIST_ALL);
        Channel playChannel = null;
        if (channels.length > 0) {
            playChannel = channels[0];
        } else {
            TLog.e(this, "Empty channel list");
            MainActivity.mediaTestCaseList.get(MediaTest.CHANNEL_PLAYER_TVSTREAM_LIVE).setFailedReason("Empty channel list");
            finish();
        }

        channelPlayer.setDataSource(playChannel.getUri());
        try {
            channelPlayer.start();
            TLog.i(this, "Media Player start");
        } catch (NoAvailableResourceException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        channelPlayer.stop();
    }
}
