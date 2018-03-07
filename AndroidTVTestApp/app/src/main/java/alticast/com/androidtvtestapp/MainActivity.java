/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package alticast.com.androidtvtestapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Xml;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
 * MainActivity class that loads {@link MainFragment}.
 */
public class MainActivity extends Activity {
    private static final int REQUEST_CODE = 0;

    private ListView lsvTest;
    private int numOfPassTC, numOfFailTC;
    private int numOfTestedTC = 2;

    private List<TestCase> listTestedTC;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lsvTest = (ListView) findViewById(R.id.lsvTest);
        String[] listOfTest = getResources().getStringArray(R.array.list_of_test_case);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listOfTest);
        lsvTest.setAdapter(adapter);

        lsvTest.setOnItemClickListener(new ItemClickListener());

        Button btnExport = findViewById(R.id.btnExport);
        btnExport.setOnClickListener(new btnExportOnClickListener());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
            }
        }

        listTestedTC = new ArrayList<TestCase>();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        TestCase testCase = new TestCase();
        if (requestCode == TestCase.SCAN_TEST) {
            numOfTestedTC++;
            if (resultCode == RESULT_OK) {
                boolean isSuccess = data.getBooleanExtra(TestCase.TEST_RESULT, false);
                if (isSuccess) {
                    testCase.setTestResult(true);
                    lsvTest.getChildAt(requestCode).setBackground(getResources().getDrawable(R.drawable.success_row_background));
                } else {
                    testCase.setTestResult(false);
                    lsvTest.getChildAt(requestCode).setBackground(getResources().getDrawable(R.drawable.fail_row_background));
                }
                testCase.setName(data.getStringExtra(TestCase.TC_NAME));
            }
        }
        listTestedTC.add(testCase);
    }

    private class ItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            Intent intent = null;
            switch (position) {
                case TestCase.SCAN_TEST:
                    intent = new Intent(getApplication(), ScanActivity.class);
                    startActivityForResult(intent, TestCase.SCAN_TEST);
                    break;
                case TestCase.RECORDING_TEST:
                    break;
                case TestCase.BLOCKING_PORT_TEST:
                    break;
                default:
                    break;
            }
        }
    }

    private class btnExportOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            File newXmlFile = new File("/storage/emulated/0/androidtvtest.xml");
            if (!newXmlFile.exists()) {
                try {
                    newXmlFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(newXmlFile);
            } catch (FileNotFoundException e) {
                TLog.e(this, e.toString());
            }

            XmlSerializer xmlSerializer = Xml.newSerializer();

            try {
                xmlSerializer.setOutput(fileOutputStream, "UTF-8");
                xmlSerializer.startDocument(null, Boolean.valueOf(false));
                xmlSerializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
                xmlSerializer.startTag(null, "Result");
                xmlSerializer.attribute(null, "suite_name", "TCTS_VERIFIER");
                xmlSerializer.attribute(null, "report_version", "1.0");

                xmlSerializer.startTag(null, "Build");
                xmlSerializer.attribute(null, "build_device", "hgs1000s");
                xmlSerializer.endTag(null, "Build");

                xmlSerializer.startTag(null, "Module");
                xmlSerializer.attribute(null, "name", "TCTSVerifier");

                xmlSerializer.startTag(null, "TestCase");
                for (int i = 0; i < listTestedTC.size(); i++) {
                    TestCase tc = listTestedTC.get(i);
                    if(tc.getTestResult()) {
                        numOfPassTC++;
                    } else {
                        numOfFailTC++;
                    }
                    xmlSerializer.startTag(null, "Test");
                    xmlSerializer.attribute(null, "result",
                            tc.getTestResult()?"pass":"fail");
                    xmlSerializer.attribute(null, "name", tc.getName());
                    xmlSerializer.endTag(null, "Test");
                }
                xmlSerializer.endTag(null, "TestCase");

                xmlSerializer.endTag(null, "Module");

                xmlSerializer.startTag(null, "Summary");
                xmlSerializer.attribute(null, "pass", String.valueOf(numOfPassTC));
                xmlSerializer.attribute(null, "fail", String.valueOf(numOfFailTC));
                xmlSerializer.endTag(null, "Summary");

                xmlSerializer.endTag(null, "Result");
                xmlSerializer.endDocument();
                xmlSerializer.flush();
            } catch (IOException e) {
                TLog.e(this, e.toString());
            }

            Toast.makeText(getApplicationContext(), "Result saved in " + newXmlFile.getPath(),
                    Toast.LENGTH_SHORT).show();
        }
    }
}
