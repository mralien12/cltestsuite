package com.alticast.taurus.testsuite.suite;

import com.alticast.taurus.testsuite.BlockingListeningPortTest;
import com.alticast.taurus.testsuite.RecordingFirstChannelTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Created by hoa on 01/11/2017.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({RecordingFirstChannelTest.class, BlockingListeningPortTest.class})
public class UnitTestSuite {

}
