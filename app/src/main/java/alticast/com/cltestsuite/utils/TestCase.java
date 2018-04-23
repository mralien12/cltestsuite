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
    public static final int NOT_TEST = 0;
    public static final int TEST_RUNNING = 1;
    public static final int TEST_DONE = 2;

    public static final int FAIL = 1;
    public static final int SUCCESS = 2;

    private String name;
    private int status;
    private int result;


    public TestCase(String name) {
        this.name = name;
        this.status = NOT_TEST;
        this.result = NOT_TEST;
    }

    public String getName() {
        return name;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }
}
