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

package alticast.com.cltestsuite.utils;

public class TestCase {
    private String name;
    private boolean testResult;

    public TestCase(String name) {
        this.name = name;
    }

    public TestCase(String name, boolean testResult) {
        this.name = name;
        this.testResult = testResult;
    }

    public String getName() {
        return name;
    }

    public boolean isTestResult() {
        return testResult;
    }

    public void setTestResult(boolean testResult) {
        this.testResult = testResult;
    }
}
