package alticast.com.cltestsuite.utils;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import alticast.com.cltestsuite.R;

public class ShowResultActivity extends Activity implements Serializable {

    private String name, resultDetail;
    private boolean successTestCase;
    private List<ShowResultActivity> lsTestCase;
    private ShowResultActivity showResultActivity;
    private ArrayList<ShowResultActivity> showResultActivities;
    private Button btnAllResults, btnSuccess, btnFail;
    private TextView txtHeader;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getResultDetail() {
        return resultDetail;
    }

    public void setResultDetail(String resultTC) {
        this.resultDetail = resultTC;
    }

    @Override
    public String toString() {
        if (successTestCase == true) {
            return "Testcase : " + name + '\n' + "Result : SUCCESS";
        }else
        {
            return "Testcase : " + name + '\n' + "Result : FAIL - " + resultDetail;
        }
    }

    public boolean isSuccessTestCase() {
        return successTestCase;
    }

    public void setSuccessTestCase(boolean successTestCase) {
        this.successTestCase = successTestCase;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_result);

        addControl();
        showAllResults();
        addEvent();
    }

    private void addEvent() {
        btnAllResults.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAllResults();
            }
        });

        btnSuccess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSuccessFilter();
            }
        });

        btnFail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFailFilter();
            }
        });
    }

    private void addControl() {
        btnAllResults = findViewById(R.id.btn_all_result);
        btnSuccess = findViewById(R.id.btn_success_result);
        btnFail = findViewById(R.id.btn_fail_result);
        txtHeader = findViewById(R.id.txt_header);
    }

    private void showSuccessFilter() {
        txtHeader.setText("Success Filter");

        lsTestCase = new ArrayList<ShowResultActivity>();
        lsTestCase = (List<ShowResultActivity>) getIntent().getSerializableExtra("TestCaseList");
        showResultActivities = new ArrayList<ShowResultActivity>();

        if (lsTestCase.size() > 0) {
            for (ShowResultActivity tc : lsTestCase)
            {
                if (tc.successTestCase == true) {
                    showResultActivity = new ShowResultActivity();
                    showResultActivity.setName(tc.getName());
                    showResultActivity.setResultDetail(tc.getResultDetail());
                    showResultActivity.setSuccessTestCase(tc.isSuccessTestCase());

                    showResultActivities.add(showResultActivity);
                }
            }
        }
        ListView listView = findViewById(R.id.lsResult);
        ArrayAdapter adapter = new ArrayAdapter<ShowResultActivity>(ShowResultActivity.this, android.R.layout.simple_list_item_1, showResultActivities){
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);
                int textColor = (showResultActivities.get(position).successTestCase == false) ? R.color.fail_row_background : R.color.success_row_background;
                textView.setTextColor(ShowResultActivity.this.getResources().getColor(textColor));

                return textView;
            }
        };

        Toast.makeText(ShowResultActivity.this, "Success Testcase Size: "+showResultActivities.size(), Toast.LENGTH_SHORT).show();
        listView.setAdapter(adapter);
    }

    private void showFailFilter() {
        txtHeader.setText("Fail Filter");

        lsTestCase = new ArrayList<ShowResultActivity>();
        lsTestCase = (List<ShowResultActivity>) getIntent().getSerializableExtra("TestCaseList");
        showResultActivities = new ArrayList<ShowResultActivity>();

        if (lsTestCase.size() > 0) {
            for (ShowResultActivity tc : lsTestCase)
            {
                if (tc.successTestCase == false) {
                    showResultActivity = new ShowResultActivity();
                    showResultActivity.setName(tc.getName());
                    showResultActivity.setResultDetail(tc.getResultDetail());
                    showResultActivity.setSuccessTestCase(tc.isSuccessTestCase());

                    showResultActivities.add(showResultActivity);
                }
            }
        }
        ListView listView = findViewById(R.id.lsResult);
        ArrayAdapter adapter = new ArrayAdapter<ShowResultActivity>(ShowResultActivity.this, android.R.layout.simple_list_item_1, showResultActivities){
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);
                int textColor = (showResultActivities.get(position).successTestCase == false) ? R.color.fail_row_background : R.color.success_row_background;
                textView.setTextColor(ShowResultActivity.this.getResources().getColor(textColor));

                return textView;
            }
        };

        Toast.makeText(ShowResultActivity.this, "Fail Testcase Size: "+showResultActivities.size(), Toast.LENGTH_SHORT).show();
        listView.setAdapter(adapter);
    }

    private void showAllResults(){
        txtHeader.setText("Test Results");

        lsTestCase = new ArrayList<ShowResultActivity>();
        lsTestCase = (List<ShowResultActivity>) getIntent().getSerializableExtra("TestCaseList");
        showResultActivities = new ArrayList<ShowResultActivity>();

        Toast.makeText(ShowResultActivity.this, "All Testcase Size: "+lsTestCase.size(), Toast.LENGTH_SHORT).show();

        if (lsTestCase.size() > 0) {
            for (ShowResultActivity tc : lsTestCase)
            {
                showResultActivity = new ShowResultActivity();
                showResultActivity.setName(tc.getName());
                showResultActivity.setResultDetail(tc.getResultDetail());
                showResultActivity.setSuccessTestCase(tc.isSuccessTestCase());

                showResultActivities.add(showResultActivity);
            }
        }
        ListView listView = findViewById(R.id.lsResult);
        ArrayAdapter adapter = new ArrayAdapter<ShowResultActivity>(ShowResultActivity.this, android.R.layout.simple_list_item_1, showResultActivities){
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);
                int textColor = (showResultActivities.get(position).successTestCase == false) ? R.color.fail_row_background : R.color.success_row_background;
                textView.setTextColor(ShowResultActivity.this.getResources().getColor(textColor));

                return textView;
            }
        };
        listView.setAdapter(adapter);
    }
}
