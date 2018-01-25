package com.alticast.taurus.testsuite;

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

import com.alticast.taurus.testsuite.channelbuilder.ScanSateliteTest;
import com.alticast.taurus.testsuite.dvr.RecordingFirstChannelMultiTimeTest;
import com.alticast.taurus.testsuite.dvr.RecordingMultiChannelTest;
import com.alticast.taurus.testsuite.media.MediaPlayerTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        ScanSateliteTest.class,
        MediaPlayerTest.class,
        RecordingFirstChannelMultiTimeTest.class,
        RecordingMultiChannelTest.class
})

public class RecordingTestSuite {
}
