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

package alticast.com.cltestsuite.channelmanager;

import java.util.concurrent.TimeUnit;

import af.channel.Channel;
import af.channel.ChannelManager;
import af.media.Tuner;
import af.media.TunerFactory;
import af.resource.NoAvailableResourceException;
import af.resource.ResourceClient;
import alticast.com.cltestsuite.MainActivity;
import alticast.com.cltestsuite.utils.TLog;
import alticast.com.cltestsuite.utils.TestCase;
import alticast.com.cltestsuite.utils.Util;

public class ChannelListTest {
    public static final int CHANNEL_LIST_UPDATED = 0;
    public static final int CHANNEL_CALLBACK_ON_CHANNEL_DATA_RECEIVED = 1;
    private static ChannelListTest instance;
    private int ret;

    private ChannelListTest() {
    }

    public static synchronized ChannelListTest getInstance() {
        if (instance == null) {
            instance = new ChannelListTest();
        }

        return instance;
    }

    public int CHL_OnChannelListUpdated() {
        ret = TestCase.FAIL;
        Tuner firstTuner;
        Channel[] channels = ChannelManager.getInstance().getChannelList(ChannelManager.CHANNEL_LIST_ALL);
        if (channels.length <= 0) {
            TLog.e(this, "CHL_OnChannelListUpdated: Empty channel list");
            MainActivity.channelTestCaseList.get(ChannelListTest.CHANNEL_LIST_UPDATED).
                    setFailedReason("Channel list is empty");
            return TestCase.FAIL;
        }

        Channel testChannel = channels[Util.getTestChannelNumber()];
        firstTuner = TunerFactory.getInstance().createTuner(new ResourceClient() {
            @Override
            public String getName() {
                return null;
            }

            @Override
            public String getDescription() {
                return null;
            }
        });

        firstTuner.setTuneEventListener(new Tuner.TunerEventListener() {
            @Override
            public void onTuningEvent(int i, String s) {
                TLog.i(this, "CHL_OnChannelListUpdated: onTune Event");
                ret = TestCase.SUCCESS;
            }
        });

        try {
            if (!firstTuner.tune(testChannel.getUri())) {
                TLog.e(this, "CHL_OnChannelListUpdated: Failed to tune");
                MainActivity.channelTestCaseList.get(ChannelListTest.CHANNEL_LIST_UPDATED).
                        setFailedReason("Failed to tune first channel");
                return TestCase.FAIL;
            }
        } catch (NoAvailableResourceException e) {
            e.printStackTrace();
        }

        /* Wait for involing callback */
        try {
            TimeUnit.MILLISECONDS.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        firstTuner.release();
        if (ret == TestCase.FAIL) {
            TLog.e(this, "CHL_OnChannelListUpdated: onTuningEvent failed");
            MainActivity.channelTestCaseList.get(ChannelListTest.CHANNEL_LIST_UPDATED).
                    setFailedReason("onTuningEvent callback is not invoked");
        }
        return ret;
    }

    /* ChannelsCallback.onChannelDataReceived: currently not supported. */
    public int CHL_ChannelCallbackOnChannelDataReceived() {
        return TestCase.SUCCESS;
    }
}
