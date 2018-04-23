package alticast.com.cltestsuite.utils;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import alticast.com.cltestsuite.R;

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
public class TestCaseAdapter extends ArrayAdapter<TestCase> {

    public TestCaseAdapter(@NonNull Context context, @NonNull List<TestCase> testCasess) {
        super(context, 0, testCasess);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_item, parent, false);
        }
        TestCase currentTestCase = getItem(position);
        TextView tvTestCaseName = (TextView) listItemView.findViewById(R.id.test_case_name_text_view);
        tvTestCaseName.setText(currentTestCase.getName());
        LinearLayout linearLayout = (LinearLayout) listItemView.findViewById(R.id.list_item_layout);
        switch (currentTestCase.getStatus()) {
            case TestCase.NOT_TEST:
                tvTestCaseName.setTextColor(Color.WHITE);
//                linearLayout.setBackgroundColor(Color.TRANSPARENT);
                break;
            case TestCase.FAIL:
                tvTestCaseName.setTextColor(Color.RED);
//                linearLayout.setBackgroundColor(Color.RED);
                break;
            case TestCase.SUCCESS:
                tvTestCaseName.setTextColor(Color.GREEN);
//                linearLayout.setBackgroundColor(Color.GREEN);
                break;
            default:

        }
        return listItemView;
    }
}
