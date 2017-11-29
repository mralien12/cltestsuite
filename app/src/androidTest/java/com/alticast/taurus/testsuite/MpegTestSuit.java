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

package com.alticast.taurus.testsuite;

import com.alticast.taurus.testsuite.mpeg.SectionFilterTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Created by hoa on 29/11/2017.
 */

@RunWith(Suite.class)
@Suite.SuiteClasses({
        SectionFilterTest.class,
})
public class MpegTestSuit {
}
