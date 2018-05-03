package alticast.com.cltestsuite.utils;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
        return "Testcase : " + name + '\n' + "Result : " + resultDetail;
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

        lsTestCase = new ArrayList<ShowResultActivity>();
        lsTestCase = (List<ShowResultActivity>) getIntent().getSerializableExtra("TestCaseList");
        showResultActivities = new ArrayList<ShowResultActivity>();

        Toast.makeText(ShowResultActivity.this, "Testcase Size: "+lsTestCase.size(), Toast.LENGTH_LONG).show();

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
