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
import com.alticast.af.builder.RScanManager;
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
import alticast.com.cltestsuite.MainActivity;
import alticast.com.cltestsuite.utils.TLog;
import alticast.com.cltestsuite.utils.TestCase;
import alticast.com.cltestsuite.utils.Util;

public class ScanTest {
    /*
     * Define final variables corresponding to list of test case in values/strings.xml
     */
    public static final int NOTIFY = 0;
    public static final int NOTIFY_SCAN_SAVE_RESULT_FINISH = 1;
    public static final int SELECT_CONFLICT_CHANNEL_REGION = 2;

    private static final int SCAN_TIMEOUT = 30;     /* seconds */

    private static int uid;
    private static String satName = "";
    private static int lnbFreq;
    private static int freq;
    private static int symbolRate;

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
        String projectName = Util.getProjectName();
        if(projectName == "kbro") {
            uid = 1;
            lnbFreq = 10750;
            freq = 11900;
            symbolRate = 21300;
        } else {    // Default project is reference STB: HGS1000S
            uid = RScanManager.UID_NOT_DEFINED;
            satName = "ASTRA_1";
            lnbFreq = 9750;
            freq = 11362;
            symbolRate = 22000;
        }

        // Generate rScanParam
        List<RTuneParamSat> tuneParam = new ArrayList();

        RAntennaInfoLnb antInfo = new RAntennaInfoLnb(uid, satName, lnbFreq, RScanManager.LNB_VOLT_STD);
        RAntennaInfoDiseqc antDiseqc = new RAntennaInfoDiseqc(uid, satName, 0,
                RScanManager.LNB_VOLT_STD, RScanManager.SAT_22KTONE_AUTO,
                RScanManager.SAT_DISEQC_VER_1_0, RScanManager.SAT_DISEQC_INPUT_A);
        List<RTuneTpInfoSat> tuneTpList = new ArrayList();
        RTuneTpInfoSat rTuneTpInfoSat = new RTuneTpInfoSat(freq, symbolRate,
                RScanManager.SAT_POLAR_HOR, RScanManager.SAT_PSK_8PSK, RScanManager.SAT_TRANS_DVBS2,
                RScanManager.SAT_PILOT_AUTO, RScanManager.SAT_CODERATE_2_3);
        tuneTpList.add(rTuneTpInfoSat);

        RTuneParamSat rTuneParamSat = new RTuneParamSat(RScanManager.SAT_ANT_TYPE_LNB_ONLY,
                antInfo, antDiseqc, tuneTpList);
        tuneParam.add(rTuneParamSat);

        rScanParam = new RScanParam(tuneParam, null, null, RScanManager.SVC_SCAN_TYPE_AUTO,
                RScanManager.SVC_TYPE_TV, RScanManager.CAS_TYPE_FTA,
                false, 0, RScanManager.DELIVERY_TYPE_SAT);
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
            MainActivity.scanTestCaseList.get(ScanTest.NOTIFY).
                    setFailedReason("Failed to start scan");
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
            MainActivity.scanTestCaseList.get(ScanTest.NOTIFY).
                    setFailedReason("Failed to stop scan");
            return TestCase.FAIL;
        }

        if (ret == TestCase.FAIL) {
            TLog.e(this, "SCA_Notify: Failed");
            MainActivity.scanTestCaseList.get(ScanTest.NOTIFY).
                    setFailedReason("Notify callback is not invoked");
        }

        return ret;
    }

    /*
    * ScanEventListener.notifyScanSaveResultFinished
    * A. Try to start scan.
    * B. Get the notification after calling the Notify. And Could get a channel list.
    */
    public int SCA_NotifyScanSaveResultFinished() {
        ret = TestCase.FAIL;
        ScanManager.getInstance().setEventListener(new ScanEventListener() {
            @Override
            public void notify(RScanEventObject rScanEventObject) {

            }

            @Override
            public void notifyScanSaveResultFinished() {
                TLog.i(this, "SCA_NotifyScanSaveResultFinished: Notify Event");
                ret = TestCase.SUCCESS;
            }

            @Override
            public void selectConflictedChannelRegion(int i, RScanConflictRegion[] rScanConflictRegions) {

            }
        });

        if (!ScanManager.getInstance().startScan(rScanParam)) {
            TLog.e(this, "SCA_NotifyScanSaveResultFinished: Failed to start scan");
            MainActivity.scanTestCaseList.get(ScanTest.NOTIFY_SCAN_SAVE_RESULT_FINISH).
                    setFailedReason("Failed to start scan");
            return TestCase.FAIL;
        }

        /* Wait few seconds for scan progress */
        try {
            TimeUnit.SECONDS.sleep(SCAN_TIMEOUT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (!ScanManager.getInstance().saveResult()) {
            TLog.e(this, "SCA_NotifyScanSaveResultFinished: Failed to save result");
            MainActivity.scanTestCaseList.get(ScanTest.NOTIFY_SCAN_SAVE_RESULT_FINISH).
                    setFailedReason("Failed to save scan result after finishing scan");
            return TestCase.FAIL;
        }

        /* Delay few miliseconds for save result notify callback */
        try {
            TimeUnit.MILLISECONDS.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (!ScanManager.getInstance().stopScan()) {
            TLog.e(this, "SCA_NotifyScanSaveResultFinished: Failed to stop scan");
            MainActivity.scanTestCaseList.get(ScanTest.NOTIFY_SCAN_SAVE_RESULT_FINISH).
                    setFailedReason("Failed to stop scan");
            return TestCase.FAIL;
        }

        if (ret == TestCase.FAIL) {
            TLog.e(this, "SCA_NotifyScanSaveResultFinished: Failed");
            MainActivity.scanTestCaseList.get(ScanTest.NOTIFY_SCAN_SAVE_RESULT_FINISH).
                    setFailedReason("notifyScanSaveResultFinished callback is not invoked");
            return TestCase.FAIL;
        }

        Channel[] channels = ChannelManager.getInstance().getChannelList(ChannelManager.CHANNEL_LIST_ALL);
        if (channels.length <= 0) {
            TLog.e(this, "SCA_NotifyScanSaveResultFinished: Channel list is empty");
            MainActivity.scanTestCaseList.get(ScanTest.NOTIFY_SCAN_SAVE_RESULT_FINISH).
                    setFailedReason("Channel list is empty");
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
        /* Taurus does not support this function */
        ret = TestCase.SUCCESS;
        //TODO Make a status  that is using fully resources (tuner)
        return ret;
    }

}



