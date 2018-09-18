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
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.RelativeLayout;

import af.channel.Channel;
import af.channel.ChannelManager;
import af.media.ChannelEventListener;
import af.media.ChannelPlayer;
import af.media.LiveEventListener;
import af.media.MediaPlayer;
import af.media.MediaPlayerFactory;
import af.media.VideoPlaneControl;
import af.resource.NoAvailableResourceException;
import af.resource.ResourceClient;
import alticast.com.cltestsuite.MainActivity;
import alticast.com.cltestsuite.R;
import alticast.com.cltestsuite.utils.TLog;
import alticast.com.cltestsuite.utils.TestCase;
import alticast.com.cltestsuite.utils.Util;

public class ChannelPlayerTVStreamLiveTestActivity extends Activity {
    Intent intent;
    ChannelPlayer channelPlayer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_player_tvstream_live);

        intent = new Intent();
        intent.putExtra("RESULT", TestCase.FAIL);
        setResult(MediaTest.CHANNEL_PLAYER_TVSTREAM_LIVE, intent);

        RelativeLayout groupView = (RelativeLayout) findViewById(R.id.channel_player_live_layout);
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

        channelPlayer.setChannelEventListener(new ChannelEventListener() {
            @Override
            public void onChannelEvent(int eventCode, String channelUri) {
                TLog.i(ChannelPlayerTVStreamLiveTestActivity.this, "eventCode :" + eventCode + ", channelUri " + channelUri);
            }
        });

        Channel[] channels = ChannelManager.getInstance().getChannelList(ChannelManager.CHANNEL_LIST_ALL);
        Channel playChannel = null;
        if (channels.length > 0) {
            playChannel = channels[Util.getTestChannelNumber()];
            channelPlayer.setDataSource(playChannel.getUri());
            try {
                channelPlayer.start();
                TLog.i(this, "Media Player start");
            } catch (NoAvailableResourceException e) {
                e.printStackTrace();
            }
        } else {
            TLog.e(this, "Empty channel list");
            MainActivity.mediaTestCaseList.get(MediaTest.CHANNEL_PLAYER_TVSTREAM_LIVE).setFailedReason("Empty channel list");
            finish();
        }

        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage("Can you see the video on the screen?");

        // Add the buttons
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                channelPlayer.stop();
                intent.putExtra("RESULT", TestCase.SUCCESS);
                setResult(MediaTest.CHANNEL_PLAYER_TVSTREAM_LIVE, intent);
                finish();
            }
        });
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                channelPlayer.stop();
                intent.putExtra("RESULT", TestCase.FAIL);
                setResult(MediaTest.CHANNEL_PLAYER_TVSTREAM_LIVE, intent);
                finish();
            }
        });
        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.fastlane_background)));
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        channelPlayer.stop();
    }
}
