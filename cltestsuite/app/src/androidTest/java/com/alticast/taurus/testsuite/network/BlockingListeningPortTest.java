/*
 *  Copyright (c) 2017 Alticast Corp.
 *  All rights reserved. http://www.alticast.com/
 *
 *  This software is the confidential and proprietary information of
 *  Alticast Corp. ("Confidential Information"). You shall not
 *  disclose such Confidential Information and shall use it only in
 *  accordance with the terms of the license agreement you entered into
 *  with Alticast.
 */

package com.alticast.taurus.testsuite.network;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.alticast.taurus.testsuite.MainActivity;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by hoa on 02/11/2017.
 */
@RunWith(AndroidJUnit4.class)
public class BlockingListeningPortTest {
    @ClassRule
    public static ActivityTestRule<MainActivity> mActivity = new ActivityTestRule<>(MainActivity.class);
    static ListeningPortsTest listeningPortsTest;


    @BeforeClass
    public static void setUp() {
        listeningPortsTest = new ListeningPortsTest(mActivity.getActivity().getApplicationContext());
    }

    @Test
    public void listeningLoopbackUdpPorts() {
        try {
            listeningPortsTest.testNoListeningLoopbackUdpPorts();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void remotelyAccessibleListeningTcp6Port() {
        try {
            listeningPortsTest.testNoRemotelyAccessibleListeningTcp6Ports();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void remotelyAccessibleListeningTcpPorts() {
        try {
            listeningPortsTest.testNoRemotelyAccessibleListeningTcpPorts();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void remotelyAccessibleListeingUdp6Ports() {
        try {
            listeningPortsTest.testNoRemotelyAccessibleListeningUdp6Ports();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void remotelyAccessibleListengUdpPorts() {
        try {
            listeningPortsTest.testNoRemotelyAccessibleListeningUdpPorts();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
