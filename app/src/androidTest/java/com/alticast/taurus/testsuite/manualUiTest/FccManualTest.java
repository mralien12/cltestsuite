package com.alticast.taurus.testsuite.manualUiTest;
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


import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiScrollable;
import android.support.test.uiautomator.UiSelector;
import android.support.test.uiautomator.Until;
import android.view.KeyEvent;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class FccManualTest {
    private UiDevice mDevice;
    private static final int TIMEOUT = 10000;
    private static final String OPEN_PACKAGE = "com.mxtech.videopalyer.ad"; //com.google.android.youtube.tv
    private static final String OPEN_DESC = "altiViewTaurus";

    @Before
    public void setUp() throws RemoteException {
        //Wake up devices
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        mDevice.wakeUp();
        mDevice.waitForIdle(TIMEOUT);

        //Start from the home screen
        mDevice.pressHome();
        assertThat(mDevice, notNullValue());

        //Check Power already ON
        assertThat("Power must ON",mDevice.isScreenOn(), is(true));

        //Wait for the app to appear
        final String launcherPackage = mDevice.getLauncherPackageName();
        assertThat(launcherPackage, notNullValue());
        mDevice.wait(Until.hasObject(By.pkg(launcherPackage).depth(0)),
                TIMEOUT);
    }

    //Demo
    @Test
    public void testFccByKeyCode() throws InterruptedException {
        //Simulate a short press on the HOME button.
        mDevice.pressHome();

        //Open App by Intent
        //openApp(OPEN_PACKAGE);

        //Open App by Content Description
        UiObject appView = mDevice.findObject(new UiSelector().descriptionMatches(OPEN_DESC));
        try {
            appView.click();
        } catch (UiObjectNotFoundException e) {
            e.printStackTrace();
        }


        mDevice.pressKeyCode(KeyEvent.KEYCODE_ENTER);
        TimeUnit.SECONDS.sleep(5);

        //assertThat("Wrong Package : "+mDevice.getCurrentPackageName(),mDevice.getCurrentPackageName(), is(OPEN_DESC));

        //Scrolling
        /*
        UiScrollable appsList=new UiScrollable(new UiSelector().scrollable(true));
        appsList.setAsHorizontalList();

        try {
            UiObject vilibraApp = appsList.getChildByText(new UiSelector().descriptionContains("altiViewTaurus"),"altiViewTaurus");
        } catch (UiObjectNotFoundException e) {
            e.printStackTrace();
        }
        */

        //Change Channel by KeyCode
        mDevice.pressKeyCode(KeyEvent.KEYCODE_0);
        mDevice.pressKeyCode(KeyEvent.KEYCODE_0);
        mDevice.pressKeyCode(KeyEvent.KEYCODE_0);
        mDevice.pressKeyCode(KeyEvent.KEYCODE_4);
        mDevice.pressKeyCode(KeyEvent.KEYCODE_ENTER);
    }

    //Open package by using Intent
    private void openApp(String packageName) {
        Context context = InstrumentationRegistry.getInstrumentation().getContext();
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
}
