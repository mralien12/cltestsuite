package alticast.com.androidtvtestapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import af.channel.Channel;
import af.channel.ChannelManager;
import af.dvr.Recording;
import af.dvr.RecordingManager;
import af.dvr.RecordingSession;
import af.dvr.RecordingSessionCallback;
import af.epg.Program;
import af.resource.NoAvailableResourceException;
import af.resource.ResourceClient;

public class RecordingActivity extends Activity {

    private Channel[] channels;
    private Channel currentChannel;
    private RecordingSession recordingSession;
    private Button btn_scan_buffer, btn_create_channel, btn_recording;
    private ImageButton btn_success, btn_false;
    private Toast toast;
    private int timeChecking;
    private boolean btn_true = true;
    private boolean isTunedSuccess, isRecordingStopped, isRecordingStarted, isRightTime;
    private Recording record;
    private ProgressDialog mProgressDialog;
    private String TestcaseName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording);

        btn_scan_buffer = findViewById(R.id.btn_scan_buffer);
        btn_create_channel = findViewById(R.id.btn_create_channel);
        btn_create_channel.setEnabled(false);
        btn_recording = findViewById(R.id.btn_recording);
        btn_recording.setEnabled(false);

        btn_success = (ImageButton) findViewById(R.id.btnSuccess);
        btn_success.setEnabled(false);
        btn_false = (ImageButton) findViewById(R.id.btnFail);
        TestcaseName = this.getClass().getName();

        btn_scan_buffer.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_true = true;

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
                    btn_true = false;
                    ex.printStackTrace();
                    toast = Toast.makeText(RecordingActivity.this, "FAIL: File Not Found Exception", Toast.LENGTH_SHORT);
                    toast.show();
                } catch (IOException ex) {
                    btn_true = false;
                    ex.printStackTrace();
                    toast = Toast.makeText(RecordingActivity.this, "FAIL: IO Exception", Toast.LENGTH_SHORT);
                    toast.show();
                } finally {
                    if (buf_reader != null) {
                        try {
                            buf_reader.close();
                        } catch (IOException ex) {
                            btn_true = false;
                            toast = Toast.makeText(RecordingActivity.this, "FAIL: IO Exception", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }
                }

                if (list.size() > 0) {
                    String dvr_storage_path = list.get(0);
                    RecordingManager.getInstance().start(dvr_storage_path);
                }

                if (btn_true) {
                    btn_create_channel.setEnabled(true);
                }
            }
        });

        btn_create_channel.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_true = true;

                isTunedSuccess = false;
                isRecordingStopped = false;
                isRecordingStarted = false;
                isRightTime = false;

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
                            btn_true = false;
                            e.printStackTrace();
                            toast = Toast.makeText(RecordingActivity.this, "FAIL: No Available Resource Exception", Toast.LENGTH_SHORT);
                            toast.show();
                        }

                        try {
                            TimeUnit.MILLISECONDS.sleep(2000);
                        } catch (InterruptedException e) {
                            btn_true = false;
                            e.printStackTrace();
                            toast = Toast.makeText(RecordingActivity.this, "FAIL: Interrupted Exception", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    } else {
                        btn_true = false;
                        Log.e("HAI", "+RecordingManager.getInstance().getStoragePath()");
                        toast = Toast.makeText(RecordingActivity.this, "FAIL: Do not have root path of DVR storage", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                } else {
                    btn_true = false;
                    toast = Toast.makeText(RecordingActivity.this, "FAIL: No Channel", Toast.LENGTH_SHORT);
                    toast.show();
                }

                if (isTunedSuccess) {
                    if (btn_true)
                        btn_recording.setEnabled(true);
                }
            }
        });

        btn_recording.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_true = true;
                timeChecking = 30;

                mProgressDialog = new ProgressDialog(v.getContext());
                mProgressDialog.setMessage("Loading ...");
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mProgressDialog.show();

                Program[] programs = currentChannel.getPrograms(1473527700000L, 1473528600000L);
                if (programs.length > 0) {
                    record = recordingSession.startRecording(programs[0].getProgramUri());
                    try {
                        TimeUnit.SECONDS.sleep(timeChecking);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        toast = Toast.makeText(RecordingActivity.this, "Interrupted Exception", Toast.LENGTH_SHORT);
                        toast.show();
                        btn_true = false;
                    }

                    recordingSession.stopRecording();
                    if (timeChecking - 2 <= record.getDuration() && record.getDuration() <= timeChecking + 2)
                        isRightTime = true;
                    if (!isRightTime) {
                        btn_true = false;
                    }

                    if (btn_true)
                        btn_success.setEnabled(true);
                    recordingSession.release();
                    RecordingManager.getInstance().stop();
                } else {
                    toast = Toast.makeText(RecordingActivity.this, "Programs must be greater than 0!", Toast.LENGTH_SHORT);
                    toast.show();
                    btn_true = false;
                }
                mProgressDialog.dismiss();
            }
        });

        btn_success.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra(TestCase.TEST_RESULT, true);
                intent.putExtra(TestCase.TC_NAME, TestcaseName);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        btn_false.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra(TestCase.TEST_RESULT, false);
                intent.putExtra(TestCase.TC_NAME, TestcaseName);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }
}
