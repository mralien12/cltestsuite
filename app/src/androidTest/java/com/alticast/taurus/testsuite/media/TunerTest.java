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
package com.alticast.taurus.testsuite.media;

import android.support.test.runner.AndroidJUnit4;

import com.alticast.af.tpinfomanager.RSatTpInfo;
import com.alticast.taurus.testsuite.util.TLog;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import af.channel.Channel;
import af.channel.ChannelManager;
import af.media.Tuner;
import af.media.TunerFactory;
import af.resource.NoAvailableResourceException;
import af.resource.ResourceClient;
import af.transponder.TransponderInfoSat;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(AndroidJUnit4.class)
public class TunerTest {

    private boolean onTuneEvent;
    private static Channel[] channels;

    @BeforeClass
    public static void setUp(){
        if (channels == null) {
            channels = ChannelManager.getInstance().getChannelList(ChannelManager.CHANNEL_LIST_ALL);
        }
    }

    @Test
    public void singletunerTest(){
        Channel singleChannel;
        Tuner singleTuner;
        assertThat("Length of channel list must be greater than 0", channels.length > 0);
        singleChannel = channels[0];
        singleTuner = TunerFactory.getInstance().createTuner(new ResourceClient() {
            @Override
            public String getName() {
                return null;
            }

            @Override
            public String getDescription() {
                return null;
            }
        });

        singleTuner.setTuneEventListener(new Tuner.TunerEventListener() {
            @Override
            public void onTuningEvent(int i, String s) {
                onTuneEvent = true;
                TLog.i(this, "singleTest: onTuningEvent");
            }
        });

        onTuneEvent = false;
        try {
            assertThat("Is single tuner tune successfully?", singleTuner.tune(singleChannel.getUri()), is(true));
        } catch (NoAvailableResourceException e) {
            e.printStackTrace();
        }

        /* Wait few miniseconds for invoking callback */
        try {
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        assertThat("Is onTuningEvent() callback invoked?", onTuneEvent, is(true));


        singleTuner.release();
        TLog.i(this, "single tuner test is successful");
    }

    @Test
    public void multipleTunerTest(){
        int maxOfChannel;
        Tuner[] tuners;

        assertThat("Length of channel list must be greater than 0", channels.length > 0);
        maxOfChannel = channels.length;

        tuners = new Tuner[maxOfChannel];
        for(int i = 0;i < maxOfChannel; i++){
            tuners[i] = TunerFactory.getInstance().createTuner(new ResourceClient() {
                @Override
                public String getName() {
                    return null;
                }

                @Override
                public String getDescription() {
                    return null;
                }
            });

            tuners[i].setTuneEventListener(new Tuner.TunerEventListener() {
                @Override
                public void onTuningEvent(int i, String s) {
                    onTuneEvent = true;
                    TLog.i(this, "multipleTest: onTuningEvent " + Integer.toString(i) + ", " + s);
                }
            });

            onTuneEvent = false;
            try {
                assertThat("Is tuner " + Integer.toString(i) + " tuned successful?",
                        tuners[i].tune(channels[i].getUri()));
            } catch (NoAvailableResourceException e) {
                e.printStackTrace();
            }

            /* Wait few miniseconds for invoking callback */
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            assertThat("Is onTuningEvent() callback for tuner " + Integer.toString(i) + " called?",
                    onTuneEvent, is(true));
        }

        for(int i = 0;i < maxOfChannel; i++){
            tuners[i].release();
        }

        TLog.i(this, "multiple test is successful");
    }

    @Test
    public void tuneFakeChannelTest(){
        Tuner tuner;
        String channelUri = "tv://0";

        tuner = TunerFactory.getInstance().createTuner(new ResourceClient() {
            @Override
            public String getName() {
                return null;
            }

            @Override
            public String getDescription() {
                return null;
            }
        });
        tuner.setTuneEventListener(new Tuner.TunerEventListener() {
            @Override
            public void onTuningEvent(int i, String s) {
                TLog.i(this, "tuneFakeChannelTest: onTuningEvent");
            }
        });
        try {
            assertThat("tuneFakeChannelTest: Is tuner tuned successfull?", tuner.tune(channelUri), is(false));
        } catch (NoAvailableResourceException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void transponderTunerTest(){
        int tsId = 0x3fb;
        int netId = 1;
        int onId = 0x1;
        int typeSat = TransponderInfoSat.TYPESAT_BS;
        boolean userAdded = false;
        int tsSpec = TransponderInfoSat.SPEC_DVBS;
        int modulation = TransponderInfoSat.MOUDLATION_QPSK;
        int frequency = 10729;
        int polarization = TransponderInfoSat.POLARIZATION_LEFT;
        int symbolrate = 22000;
        int coderate = TransponderInfoSat.CODERATE_AUTO;
        int rolloff = TransponderInfoSat.ROLL_OFF_020;

        RSatTpInfo rSatTpInfo = new RSatTpInfo(tsId, netId, onId, typeSat, userAdded, tsSpec,
                modulation, frequency, polarization, symbolrate, coderate, rolloff);
        TransponderInfoSat infoSat = new TransponderInfoSat(rSatTpInfo);

        Tuner tuner = TunerFactory.getInstance().createTuner(new ResourceClient() {
            @Override
            public String getName() {
                return null;
            }

            @Override
            public String getDescription() {
                return null;
            }
        });

        tuner.setTuneEventListener(new Tuner.TunerEventListener() {
            @Override
            public void onTuningEvent(int i, String s) {
                onTuneEvent = true;
                TLog.i(this, "transponderTuneTest: onTuningEvent");
            }
        });

        onTuneEvent = false;
        try {
            tuner.tune(infoSat);
        } catch (NoAvailableResourceException e) {
            e.printStackTrace();
        }

        assertThat("Is tuner tuned successful?", onTuneEvent, is(true));
    }
}
