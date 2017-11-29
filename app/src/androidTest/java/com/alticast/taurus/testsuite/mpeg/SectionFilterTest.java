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

package com.alticast.taurus.testsuite.mpeg;

import android.support.test.runner.AndroidJUnit4;

import com.alticast.taurus.testsuite.util.TLog;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import af.channel.Channel;
import af.channel.ChannelManager;
import af.mpeg.section.ConnectionLostException;
import af.mpeg.section.FilterResourceException;
import af.mpeg.section.FilteringInterruptedException;
import af.mpeg.section.IllegalFilterDefinitionException;
import af.mpeg.section.IncompleteFilteringEvent;
import af.mpeg.section.Section;
import af.mpeg.section.SectionAvailableEvent;
import af.mpeg.section.SectionFilterEvent;
import af.mpeg.section.SectionFilterGroup;
import af.mpeg.section.SectionFilterListener;
import af.mpeg.section.TableSectionFilter;
import af.mpeg.section.TimeOutEvent;
import af.resource.NoAvailableResourceException;
import af.resource.ResourceClient;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by hoa on 22/11/2017.
 */

@RunWith(AndroidJUnit4.class)
public class SectionFilterTest {
    private final static int NUMBER_OF_SECTION_FILTER = 1;
    private final int STREAM_ID = 1019;     // ???
    private final int TABLE_ID = 0x00;      // CAT table id

    private Channel[] channels;
    private Channel currentChannel;
    private static SectionFilterGroup sectionFilterGroup;
    @BeforeClass
    public static void setUp(){
        sectionFilterGroup = new SectionFilterGroup(NUMBER_OF_SECTION_FILTER);
    }

    @Test(timeout = 300000)      // Set timeout for test to 5 minutes
    public void sectionFilterTest(){

        if (channels == null) {
            channels = ChannelManager.getInstance().getChannelList(ChannelManager.CHANNEL_LIST_ALL);
        }

        assertThat("Length of channel list must be greater than 0", channels.length > 0);
        currentChannel = channels[0];


        TableSectionFilter tableSectionFilter = sectionFilterGroup.newTableSectionFilter();

        /* Set timeout for section filter to 10s */
        //tableSectionFilter.setTimeOut(10000);

        SectionFilterListener myListener = new SectionFilterListener() {
            @Override
            public void onSectionFilterEvent(SectionFilterEvent event) {
                TLog.i(this, "onSectionFilterEvent()");
                if (event instanceof TimeOutEvent){
                    TLog.i(this, "Section filter operations time out");
                } else if(event instanceof IncompleteFilteringEvent) {
                    TLog.i(this, "Filter parameters is incorrect");
                } else if (event instanceof SectionAvailableEvent) {
                    TLog.i(this, "Section filter operations is successful");
                    TableSectionFilter tblSectionFilter = (TableSectionFilter) event.getSource();
                    try {
                        Section[] sections = tblSectionFilter.getSections();
                    } catch (FilteringInterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        tableSectionFilter.setSectionFilterListener(myListener);

        try {
            TLog.i(this, "Set the parameters on the filter");
            tableSectionFilter.startFiltering(null, STREAM_ID, TABLE_ID);
        } catch (FilterResourceException e) {
            e.printStackTrace();
        } catch (ConnectionLostException e) {
            e.printStackTrace();
        } catch (IllegalFilterDefinitionException e) {
            e.printStackTrace();
        }

        try {
            sectionFilterGroup.attach(currentChannel.getUri(), new ResourceClient() {
                @Override
                public String getName() {
                    return null;
                }

                @Override
                public String getDescription() {
                    return null;
                }
            });
            TLog.i(this, "Attach the section filter");
        } catch (NoAvailableResourceException e) {
            e.printStackTrace();
        }

        while (true);
    }

    @AfterClass
    public static void tearDown(){
        sectionFilterGroup.detach();
    }
}
