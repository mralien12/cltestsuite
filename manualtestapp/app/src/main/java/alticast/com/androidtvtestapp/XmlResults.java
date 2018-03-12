package alticast.com.androidtvtestapp;


import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class XmlResults extends Activity {
    private String name;
    private boolean result;
    private List<TestCase> listTestedTC;
    private XmlResults xmlResult;
    private ArrayList<XmlResults> xmlResults;
    private TestCase tc;

    public void setName(String name) {
        this.name = name;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "Name: " + name + "\nResult: "+ result;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_result);
        listTestedTC = new ArrayList<TestCase>();

        listTestedTC = (List<TestCase>) getIntent().getSerializableExtra("TestCaseList");

        xmlResults = new ArrayList<XmlResults>();

        for (int item = 0; item < listTestedTC.size(); item++){
            xmlResult = new XmlResults();
            tc = (TestCase) listTestedTC.get(item);

            xmlResult.setName(tc.getName());
            xmlResult.setResult(tc.getTestResult());
            xmlResults.add(xmlResult);
        }

        ListView listView = findViewById(R.id.listResult);

        ArrayAdapter adapter = new ArrayAdapter<XmlResults>(XmlResults.this, android.R.layout.simple_list_item_1, xmlResults){
        };
        listView.setAdapter(adapter);
    }
}
