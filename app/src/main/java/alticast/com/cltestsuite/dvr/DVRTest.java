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

package alticast.com.cltestsuite.dvr;

import android.app.ProgressDialog;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.alticast.af.builder.RAntennaInfoDiseqc;
import com.alticast.af.builder.RAntennaInfoLnb;
import com.alticast.af.builder.RScanConflictRegion;
import com.alticast.af.builder.RScanEventObject;
import com.alticast.af.builder.RScanParam;
import com.alticast.af.builder.RTuneParamSat;
import com.alticast.af.builder.RTuneTpInfoSat;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import af.builder.ScanEventListener;
import af.builder.ScanManager;
import af.channel.Channel;
import af.channel.ChannelManager;
import af.dvr.Recording;
import af.dvr.RecordingManager;
import af.dvr.RecordingSession;
import af.dvr.RecordingSessionCallback;
import af.epg.Program;
import af.resource.NoAvailableResourceException;
import af.resource.ResourceClient;

public class DVRTest {
    private Channel[] channels;
    private Channel currentChannel;
    private RecordingSession recordingSession;
    private int timeChecking;
    private boolean returnResult, isTunedSuccess, isRecordingStopped, isRecordingStarted, isRightTime;
    private Recording record;

    public DVRTest() {
    }

    public boolean onRecordingReceived(){
        return true;
    }

    public boolean stateListenerOnStated(){
        return true;
    }

    public boolean stateListenerOnStoped(){
        return true;
    }

    public boolean recordingSessionCallback() {
        returnResult = false;
        isTunedSuccess = false;
        isRecordingStopped = false;
        isRecordingStarted = false;
        isRightTime = false;
        timeChecking = 10;

        //
        ScanResult();
        //

        List<String> list = new ArrayList<String>();
        BufferedReader buf_reader = null;
        try {
            buf_reader = new BufferedReader(new FileReader("/proc/mounts"));
            String line;

            while ((line = buf_reader.readLine()) != null) {
                if (line.contains("/mnt/media_rw") || line.contains("/mnt/expand")) {
                    StringTokenizer tokens = new StringTokenizer(line, " ");
                    String unused = tokens.nextToken(); // device
                    String mount_point = tokens.nextToken(); // mount point/
                    list.add(mount_point + "/");
                }
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (buf_reader != null) {
                try {
                    buf_reader.close();
                } catch (IOException ex) {
                }
            }
        }

        if (list.size() > 0) {
            String dvr_storage_path = list.get(0);
            RecordingManager.getInstance().start(dvr_storage_path);
        }

        if (channels == null) {
            channels = ChannelManager.getInstance().getChannelList(ChannelManager.CHANNEL_LIST_ALL);
        }

        if (channels.length > 0) {
            currentChannel = channels[0];
            if (RecordingManager.getInstance().getStoragePath() != null) {
                RecordingManager.getInstance().start("");

                recordingSession = RecordingManager.getInstance().createRecordingSession(new RecordingSessionCallback() {
                    @Override
                    public void onError(short i) {
                        isTunedSuccess = false;
                    }

                    @Override
                    public void onRecordingStopped() {
                        isRecordingStopped = true;
                    }

                    @Override
                    public void onRecordingStarted(String s) {
                        isRecordingStarted = true;
                    }

                    @Override
                    public void onTuned() {
                        isTunedSuccess = true;
                    }
                }, new ResourceClient() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public String getDescription() {
                        return null;
                    }
                });

                try {
                    recordingSession.tune(currentChannel.getUri());
                } catch (NoAvailableResourceException e) {
//                    returnResult = false;
                    e.printStackTrace();
                }

                try {
                    TimeUnit.MILLISECONDS.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
            }
        } else {
        }

        Program[] programs = currentChannel.getPrograms(1473527700000L, 1473528600000L);
        if (programs.length > 0) {
            record = recordingSession.startRecording(programs[0].getProgramUri());
            try {
                TimeUnit.SECONDS.sleep(timeChecking);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            recordingSession.stopRecording();
            if (timeChecking - 2 <= record.getDuration() && record.getDuration() <= timeChecking + 2)
                isRightTime = true;
            if (!isRightTime) {
            }

            recordingSession.release();
            RecordingManager.getInstance().stop();
        } else {
        }

        if (isTunedSuccess == true && isRecordingStarted == true && isRecordingStopped == true)
            returnResult = true;

        Log.d("LEHAI","isTunedSuccess: "+isTunedSuccess+"--isRecordingStarted: "
                +isRecordingStarted+"--isRecordingStopped: "+isRecordingStopped+"--isTunedSuccess: "+isTunedSuccess);

        return returnResult;
    }

    // Addition DEMO Scan
    private static final String SAT_NAME = "ASTRA_1";
    private static final int LNB_FREQ = 9750;
    private static final int FREQ = 11362;
    private static final int SYMBOL_RATE = 22000;
    private static final int POLARIZATION = 1;
    private static final int TIMEOUT = 30;
    private RScanParam rs;
    private boolean ret;
    private ProgressBar pgbScan;

    private RScanParam generateParam(String satName, int lnb_freq, int transponder_freq){
        // Generate rScanParam
        RScanParam rScanParam = null;
        List<RTuneParamSat> item = new ArrayList();

        RAntennaInfoLnb rAntennaInfoLnb = new RAntennaInfoLnb(-1, satName, lnb_freq, 0);
        RAntennaInfoDiseqc diseqc = new RAntennaInfoDiseqc(-1, satName, 0, 0, 0, 1, 1);
        List<RTuneTpInfoSat> tpList = new ArrayList();
        RTuneTpInfoSat rTuneTpInfoSat = new RTuneTpInfoSat(transponder_freq, SYMBOL_RATE, POLARIZATION, 1, 1, 2, 0);
        tpList.add(rTuneTpInfoSat);

        RTuneParamSat rTuneParamSat = new RTuneParamSat(0, rAntennaInfoLnb, diseqc, tpList);
        item.add(rTuneParamSat);

        rScanParam = new RScanParam(item, 1, 1, false, 0);
        return rScanParam;
    }

    private void scanSatellite(RScanParam rScanParam){
        final Channel[] channels = null;
        rs = rScanParam;

        ScanManager.getInstance().setEventListener(new ScanEventListener() {
            @Override
            public void notify(RScanEventObject rScanEventObject) {
            }

            @Override
            public void notifyScanSaveResultFinished() {
            }

            @Override
            public void selectConflictedChannelRegion(int i, RScanConflictRegion[] rScanConflictRegions) {

            }
        });

        Thread thrScan = new Thread(new Runnable() {

            @Override
            public void run() {
                ret = ScanManager.getInstance().startScan(rs);
                if (!ret) {
                    return;
                }

                ret = ScanManager.getInstance().stopScan();
                if (!ret) {
                    return;
                }

                final Channel[] channels = ChannelManager.getInstance().getChannelList(ChannelManager.CHANNEL_LIST_ALL);
                final String[] channel_name = new String[channels.length];
                for (int i = 0;i < channels.length; i++) {
                    channel_name[i] = channels[i].getName();
                }
            }
        });

        thrScan.start();
    }

    public boolean ScanResult (){
        boolean result = true;

        RScanParam rScanParam  = null;
        rScanParam = generateParam(SAT_NAME, LNB_FREQ, FREQ);
        scanSatellite(rScanParam);

        ret = ScanManager.getInstance().saveResult();
        return result;
    }
}
