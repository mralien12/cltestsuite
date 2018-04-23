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
    public static final int FAIL = 1;
    public static final int SUCCESS = 2;

    private String name;
    private int status;

    public TestCase(String name) {
        this.name = name;
        this.status = NOT_TEST;
    }

    public TestCase(String name, int status) {
        this.name = name;
        this.status = status;
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
}
