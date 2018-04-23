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

package alticast.com.cltestsuite.channelbuilder;

import com.alticast.af.builder.RAntennaInfoDiseqc;
import com.alticast.af.builder.RAntennaInfoLnb;
import com.alticast.af.builder.RScanConflictRegion;
import com.alticast.af.builder.RScanEventObject;
import com.alticast.af.builder.RScanParam;
import com.alticast.af.builder.RTuneParamSat;
import com.alticast.af.builder.RTuneTpInfoSat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import af.builder.ScanEventListener;
import af.builder.ScanManager;
import af.channel.Channel;
import af.channel.ChannelManager;
import alticast.com.cltestsuite.utils.TLog;
import alticast.com.cltestsuite.utils.TestCase;

public class ScanTest {
    /*
     * Define final variables corresponding to list of test case in values/strings.xml
     */
    public static final int NOTTITY = 0;
    public static final int NOTIFY_SCAN_SAVE_RESULT_FINISH = 1;
    public static final int SELECT_CONFLICT_CHANNEL_REGION = 2;

    private static final int SCAN_TIMEOUT = 10;     /* seconds */
    private static final String SAT_NAME = "ASTRA_1";
    private static final int LNB_FREQ = 9750;
    private static final int FREQ = 11362;
    private static final int SYMBOL_RATE = 22000;
    private static final int POLARIZATION = 1;

    private static ScanTest instance;
    private static RScanParam rScanParam;
    private static int ret;

    private ScanTest() {
    }

    public static synchronized ScanTest getInstance() {
        if (instance == null) {
            instance = new ScanTest();
        }

        if (rScanParam == null) {
            setUp();
        }
        return instance;
    }

    private static void setUp() {
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

    /*
     * ScanEventListener.Notify
     * A. Try to start scan.
     * B. Get the notification after completing scan, whether it’s success or failure
     */
    public int SCA_Notify() {
        ret = TestCase.FAIL;
        ScanManager.getInstance().setEventListener(new ScanEventListener() {
            @Override
            public void notify(RScanEventObject rScanEventObject) {
                TLog.i(this, "SC_Notify: Notify Event");
                ret = TestCase.SUCCESS;
            }

            @Override
            public void notifyScanSaveResultFinished() {

            }

            @Override
            public void selectConflictedChannelRegion(int i, RScanConflictRegion[] rScanConflictRegions) {

            }
        });

        if (!ScanManager.getInstance().startScan(rScanParam)) {
            TLog.e(this, "SCA_Notify: Failed to start scan");
            return TestCase.FAIL;
        }

        /* Wait few seconds for scan progress */
        try {
            TimeUnit.SECONDS.sleep(SCAN_TIMEOUT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (!ScanManager.getInstance().stopScan()) {
            TLog.e(this, "SCA_Notify: Failed to stop scan");
            return TestCase.FAIL;
        }

        if (ret == TestCase.FAIL) {
            TLog.e(this, "SCA_Notify: Failed");
        }

        return ret;
    }

    /*
    * ScanEventListener.notifyScanSaveResultFinished
    * A. Try to start scan.
    * B. Get the notification after calling the Notify. And Could get a channel list.
    */
    public int SCA_NofifyScanSaveResultFinished() {
        ret = TestCase.FAIL;
        ScanManager.getInstance().setEventListener(new ScanEventListener() {
            @Override
            public void notify(RScanEventObject rScanEventObject) {

            }

            @Override
            public void notifyScanSaveResultFinished() {
                TLog.i(this, "SCA_NofifyScanSaveResultFinished: Notify Event");
                ret = TestCase.SUCCESS;
            }

            @Override
            public void selectConflictedChannelRegion(int i, RScanConflictRegion[] rScanConflictRegions) {

            }
        });

        if (!ScanManager.getInstance().startScan(rScanParam)) {
            TLog.e(this, "SCA_NofifyScanSaveResultFinished: Failed to start scan");
            return TestCase.FAIL;
        }

        /* Wait few seconds for scan progress */
        try {
            TimeUnit.SECONDS.sleep(SCAN_TIMEOUT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (!ScanManager.getInstance().saveResult()) {
            TLog.e(this, "SCA_NofifyScanSaveResultFinished: Failed to save result");
            return TestCase.FAIL;
        }

        /* Delay few miliseconds for save result notify callback */
        try {
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (!ScanManager.getInstance().stopScan()) {
            TLog.e(this, "SCA_NofifyScanSaveResultFinished: Failed to stop scan");
            return TestCase.FAIL;
        }

        if (ret == TestCase.FAIL) {
            TLog.e(this, "SCA_NofifyScanSaveResultFinished: Failed");
            return TestCase.FAIL;
        }

        Channel[] channels = ChannelManager.getInstance().getChannelList(ChannelManager.CHANNEL_LIST_ALL);
        if (channels.length <= 0) {
            TLog.e(this, "SCA_NofifyScanSaveResultFinished: Channel list is empty");
            return TestCase.FAIL;
        }

        return ret;
    }

    /*
     * ScanEventListener.selectConflictedChannelRegion
     * A. Make a status that is using fully resource(tuner).
     * B. Try to start scan.
     * C. Get the notification.
     */
    public int SCA_SelectConflictedChannelRegion() {
        ret = TestCase.FAIL;
        //TODO Make a status  that is using fully resources (tuner)
        return ret;
    }

}



