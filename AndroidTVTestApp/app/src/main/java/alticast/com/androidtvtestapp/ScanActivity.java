package alticast.com.androidtvtestapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

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

public class ScanActivity extends Activity {
    private static final String SAT_NAME = "ASTRA_1";
    private static final int LNB_FREQ = 9750;
    private static final int FREQ = 11362;
    private static final int SYMBOL_RATE = 22000;
    private static final int POLARIZATION = 1;
    private static final int TIMEOUT = 30;

    private boolean ret;
    private ProgressBar pgbScan;
    private ListView lsvChannel;
    private Button btnScan, btnSave;
    private ImageButton btnSuccess, btnHelp, btnFail;

    private ArrayAdapter<String> channel_adapter;

    private String TestcaseName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        btnScan = (Button) findViewById(R.id.btnScan);
        btnScan.setOnClickListener(new btnScanOnClickListener());

        pgbScan = (ProgressBar) findViewById(R.id.pgbScan);
        lsvChannel = (ListView) findViewById(R.id.lsvChannel);
        btnSave = (Button) findViewById(R.id.btnSave);
        btnSave.setEnabled(false);
        btnSave.setOnClickListener(new btnSaveOnClickListener());

        btnSuccess = (ImageButton) findViewById(R.id.btnSuccess);
        btnSuccess.setEnabled(false);
        btnSuccess.setOnClickListener(new btnSuccessOnClickListener());

        btnFail = (ImageButton) findViewById(R.id.btnFail);
        btnFail.setOnClickListener(new btnFailOnClickListener());

        btnHelp = findViewById(R.id.btnHelp);
        btnHelp.setOnClickListener(new btnHelpOnClickListener());

        TestcaseName = this.getClass().getName();
    }

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

    private RScanParam rs;
    private void scanSatellite(RScanParam rScanParam){
        final Channel[] channels = null;
        rs = rScanParam;

        ScanManager.getInstance().setEventListener(new ScanEventListener() {
            @Override
            public void notify(RScanEventObject rScanEventObject) {
                TLog.i(this, "scanSatellite() notifyScanEvent");
            }

            @Override
            public void notifyScanSaveResultFinished() {
                TLog.i(this, "scanSatellite() notifyScanSaveResultFinished");
            }

            @Override
            public void selectConflictedChannelRegion(int i, RScanConflictRegion[] rScanConflictRegions) {

            }
        });

        Thread thrScan = new Thread(new Runnable() {

            @Override
            public void run() {
                TLog.i(this, "Start Scan");
                ret = ScanManager.getInstance().startScan(rs);
                if (!ret) {
                    TLog.e(this, "Start Scan Failed");
                    return;
                }

                int progress = 0;
                for (int i = 0;i < TIMEOUT; i++) {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    progress += 100 / TIMEOUT;
                    pgbScan.setProgress(progress);
                    if (i == TIMEOUT - 1){
                        pgbScan.setProgress(100);
                    }
                }
                ret = ScanManager.getInstance().stopScan();
                if (!ret) {
                    TLog.e(this, "Stop Scan Failed");
                    return;
                }

                final Channel[] channels = ChannelManager.getInstance().getChannelList(ChannelManager.CHANNEL_LIST_ALL);
                final String[] channel_name = new String[channels.length];
                for (int i = 0;i < channels.length; i++) {
                    channel_name[i] = channels[i].getName();
                }
                TLog.i(this, "TV channel list: " + channel_name.length);
                channel_adapter = new ArrayAdapter<String>(ScanActivity.this, android.R.layout.simple_list_item_1, channel_name);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        lsvChannel.setAdapter(channel_adapter);
                        if (channel_name.length >0) {
                            btnSave.setEnabled(true);
                        }
                    }
                });
            }
        });

        thrScan.start();
    }

    private class btnScanOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            RScanParam rScanParam  = null;

            rScanParam = generateParam(SAT_NAME, LNB_FREQ, FREQ);
            scanSatellite(rScanParam);
        }
    }

    private class btnSaveOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            ret = ScanManager.getInstance().saveResult();
            if (!ret) {
                TLog.e(this, "Save error");
                return;
            }
            Toast.makeText(getApplicationContext(), "Save successfully", Toast.LENGTH_SHORT).show();
            btnSuccess.setEnabled(true);
        }
    }

    private class btnSuccessOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent();
            intent.putExtra(TestCase.TEST_RESULT, true);
            intent.putExtra(TestCase.TC_NAME, TestcaseName);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    private class btnFailOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent();
            intent.putExtra(TestCase.TEST_RESULT, false);
            intent.putExtra(TestCase.TC_NAME, TestcaseName);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    private class btnHelpOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            //TODO Describe detail scan test case
            Toast.makeText(getApplicationContext(), "This button will show detail about test case",
                    Toast.LENGTH_SHORT).show();
        }
    }
}



