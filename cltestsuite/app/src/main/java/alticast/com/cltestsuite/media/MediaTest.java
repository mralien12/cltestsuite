package alticast.com.cltestsuite.media;

import alticast.com.cltestsuite.utils.TestCase;

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
public class MediaTest {
    public static final int CHANNEL_PLAYER_TVSTREAM_LIVE = 0;
    public static final int CHANNEL_PLAYER_TVSTREAM_TSR = 1;
    public static final int CHANNEL_EVENT_LISTENER = 2;
    public static final int CAPTION_CONTROLLER_EVENT_LISTENER_ON_DETACH = 3;

    public static final int CHANNEL_PLAYER_TVSTREAM_LIVE_REQUEST_CODE = 100;
    public static final int CHANNEL_PLAYER_TVSTREAM_TSR_REQUEST_CODE = 101;
    private static MediaTest instance;

    private MediaTest() {};

    public static synchronized MediaTest getInstance() {
        if (instance == null) {
            instance = new MediaTest();
        }

        return instance;
    }

    public int captionControllerEventListenerOnDetach() {
        return TestCase.FAIL;
    }

}
