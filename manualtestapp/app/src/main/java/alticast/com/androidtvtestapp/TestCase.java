package alticast.com.androidtvtestapp;

import java.io.Serializable;

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
public class TestCase implements Serializable {
    public static final int SCAN_TEST = 0;
    public static final int TUNER_TEST = 1;
    public static final int SECTION_FILTER_TEST = 2;
    public static final int RECORDING_TEST = 3;
    public static final int BLOCKING_PORT_TEST = 4;

    public static final String TEST_RESULT = "result";
    public static final String TC_NAME = "TC_name";

    private String name;
    private boolean testResult;

    public boolean getTestResult() {
        return testResult;
    }

    public void setTestResult(boolean testResult) {
        this.testResult = testResult;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
