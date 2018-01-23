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

package com.alticast.taurus.testsuite.channelbuilder;

import com.alticast.af.builder.RAntennaInfoDiseqc;
import com.alticast.af.builder.RAntennaInfoLnb;
import com.alticast.af.builder.RScanConflictRegion;
import com.alticast.af.builder.RScanEventObject;
import com.alticast.af.builder.RScanParam;
import com.alticast.af.builder.RTuneParamSat;
import com.alticast.af.builder.RTuneTpInfoSat;
import com.alticast.taurus.testsuite.util.TLog;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import af.builder.ScanEventListener;
import af.builder.ScanManager;
import af.channel.Channel;
import af.channel.ChannelManager;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Created by tk.hoa on 1/11/18.
 */

public class ScanSateliteTest{
    private static final String SAT_NAME = "ASTRA_1";
    private static final int LNB_FREQ = 9750;
    private static final int FREQ = 11362;
    private static final int SYMBOL_RATE = 22000;
    private static final int POLARIZATION = 1;

    private boolean isScanSuccess;
    private boolean isSaveResultSuccess;

    private static RScanParam rScanParam;

    @BeforeClass
    public static void setUp(){
        // Generate rScanParam
        List<RTuneParamSat> item = new ArrayList();

        RAntennaInfoLnb rAntennaInfoLnb = new RAntennaInfoLnb(-1, SAT_NAME, LNB_FREQ, 0);
        RAntennaInfoDiseqc diseqc = new RAntennaInfoDiseqc(-1, SAT_NAME, 0, 0, 0, 1, 1);
        List<RTuneTpInfoSat> tpList = new ArrayList();
        RTuneTpInfoSat rTuneTpInfoSat = new RTuneTpInfoSat(FREQ, SYMBOL_RATE, POLARIZATION, 1, 1, 2, 0);
        tpList.add(rTuneTpInfoSat);

        RTuneParamSat rTuneParamSat = new RTuneParamSat(0, rAntennaInfoLnb, diseqc, tpList);
        item.add(rTuneParamSat);

        rScanParam = new RScanParam(item, 1, 1, false, 0);
    }

    @Test
    public void scanSaveTest(){
        Channel[] channels = null;

        ScanManager.getInstance().setEventListener(new ScanEventListener() {
            @Override
            public void notify(RScanEventObject rScanEventObject) {
                TLog.i(this, "scanSaveTest(): notifyScanEvent");
                isScanSuccess = true;
            }

            @Override
            public void notifyScanSaveResultFinished() {
                TLog.i(this, "scanSaveTest(): notifyScanSaveResultFinished");
                isSaveResultSuccess = true;
            }

            @Override
            public void selectConflictedChannelRegion(int i, RScanConflictRegion[] rScanConflictRegions) {

            }
        });

        /* 1. Start scanning test */
            isScanSuccess = false;
            assertThat("scanSaveTest(): Start scanning", ScanManager.getInstance().startScan(rScanParam), is(true));

        /* Wait few seconds for scan progress */
            try {
                TimeUnit.SECONDS.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            assertThat("scanSaveTest(): Is startScan success?", isScanSuccess, is(true));

        /* 2. Save result test */
            assertThat("scanSaveTest(): Save scan result", ScanManager.getInstance().saveResult(), is(true));

        /* Delay few miliseconds for save result notify callback */
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            assertThat("scanSaveTest(): Is save scan result success?", isSaveResultSuccess, is(true));

        /* 3. Stop scanning test */
            assertThat("scanSaveTest(): Stop scanning", ScanManager.getInstance().stopScan(), is(true));

        /* 4. Check channel list */
            channels = ChannelManager.getInstance().getChannelList(ChannelManager.CHANNEL_LIST_ALL);

        assertThat("scanSaveTest(): Is channel build success?", channels.length > 0);
        TLog.i(this, "scanSaveTest() successfully");
    }

    @Test
    public void pauseResumeTest(){
        Channel[] channels = null;

        ScanManager.getInstance().setEventListener(new ScanEventListener() {
            @Override
            public void notify(RScanEventObject rScanEventObject) {
                TLog.i(this, "pauseResumeTest(): notifyScanEvent");
                isScanSuccess = true;
            }

            @Override
            public void notifyScanSaveResultFinished() {
            }

            @Override
            public void selectConflictedChannelRegion(int i, RScanConflictRegion[] rScanConflictRegions) {

            }
        });

        /* 1. Start scanning test */
        isScanSuccess = false;
        assertThat("pauseResumeTest(): Start scanning", ScanManager.getInstance().startScan(rScanParam), is(true));

        /* Wait few seconds for scan progress */
        try {
            TimeUnit.SECONDS.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertThat("pauseResumeTest(): Is startScan success?", isScanSuccess, is(true));

        /* 2. Pause scanning */
        assertThat("pauseResumeTest(): Pause scanning", ScanManager.getInstance().pauseScan(), is(true));

        /* Wait few seconds. And then resume scanning */
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /* 3. Resume scanning */
        assertThat("pauseResumeTest(): Resume Scanning", ScanManager.getInstance().resumeScan(), is(true));

         /* 4. Stop scanning*/
        assertThat("pauseResumeTest(): Stop scanning", ScanManager.getInstance().stopScan(), is(true));

        /* 5. Check channel list */
        channels = ChannelManager.getInstance().getChannelList(ChannelManager.CHANNEL_LIST_ALL);
        assertThat("pauseResumeTest(): Is channel build success?", channels.length > 0);

        TLog.i(this, "pauseResumeTest() successfully");
    }

    @Test
    public void pauseScanTest(){
        ScanManager.getInstance().setEventListener(new ScanEventListener() {
            @Override
            public void notify(RScanEventObject rScanEventObject) {
                TLog.i(this, "pauseScanTest(): notifyScanEvent");
                isScanSuccess = true;
            }

            @Override
            public void notifyScanSaveResultFinished() {
            }

            @Override
            public void selectConflictedChannelRegion(int i, RScanConflictRegion[] rScanConflictRegions) {

            }
        });

        /* 1. Pause scanning */
        assertThat("pauseScanTest(): Pause scanning", ScanManager.getInstance().pauseScan(), is(true));

        /* 2. Start scanning test */
        isScanSuccess = false;
        assertThat("pauseScanTest(): Start scanning", ScanManager.getInstance().startScan(rScanParam), is(true));

        /* Wait few seconds for scan progress */
        try {
            TimeUnit.SECONDS.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertThat("pauseScanTest(): Scanning will fail while pausing. Is scanning success?", isScanSuccess, is(false));

        /* 3. Resume scanning */
        assertThat("pauseScanTest(): Resume Scanning", ScanManager.getInstance().resumeScan(), is(true));

    }
}

