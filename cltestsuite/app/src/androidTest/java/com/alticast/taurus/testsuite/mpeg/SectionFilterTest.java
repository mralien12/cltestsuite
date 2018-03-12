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

import java.util.concurrent.TimeUnit;

import af.channel.Channel;
import af.channel.ChannelManager;
import af.media.Tuner;
import af.media.TunerFactory;
import af.mpeg.section.ConnectionLostException;
import af.mpeg.section.EndOfFilteringEvent;
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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by hoa on 22/11/2017.
 */

@RunWith(AndroidJUnit4.class)
public class SectionFilterTest {

    private final static int NUMBER_OF_SECTION_FILTER = 1;
    private final int PMT_PID = 0x13EC;
    private final int PAT_PID= 0x00;
    private final int PAT_TABLE_ID = 0x00;
    private final int PMT_TABLE_ID = 0x02;

    private static  Channel[] channels;
    private static Channel currentChannel;
    private static SectionFilterGroup sectionFilterGroup;
    private static boolean onTuneEvent;
    private static Tuner singleTuner;
    private static int sourceId;
    private boolean isSFSuccess;
    private boolean isTimeoutEvent;

    @BeforeClass
    public static void setUp(){
        TLog.i("SetUp", "SetUp SectionfilterGroup #" + NUMBER_OF_SECTION_FILTER );
        sectionFilterGroup = new SectionFilterGroup(NUMBER_OF_SECTION_FILTER);

        TLog.i("Setup", "Check Channels" );
        if (channels == null) {
            channels = ChannelManager.getInstance().getChannelList(ChannelManager.CHANNEL_LIST_ALL);
        }

        if (channels.length <= 0) {
            TLog.e("SetUp", "setUp() Channel list is empty. Please check channelbuilder test suite");
        }
        assertThat("Length of channel list must be greater than 0", channels.length > 0);
        currentChannel = channels[0];

        singleTuner = TunerFactory.getInstance().createTuner(new ResourceClient() {
            @Override
            public String getName() {
                return null;
            }

            @Override
            public String getDescription() {
                return null;
            }
        });

        singleTuner.setTuneEventListener(new Tuner.TunerEventListener() {
            @Override
            public void onTuningEvent(int i, String s) {
                onTuneEvent = true;
                TLog.i(this, "singleTest: onTuningEvent [ " + i + "] [" + s +"]" );
                if (i == 1) {
                    sourceId = singleTuner.getSourceId();
                }
            }
        });

        onTuneEvent = false;
        try {
            assertThat("Is single tuner tune successfully?", singleTuner.tune(currentChannel.getUri()), is(true));
        } catch (NoAvailableResourceException e) {
            e.printStackTrace();
        }

        /* Wait few miniseconds for invoking callback */
        try {
            TimeUnit.MILLISECONDS.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (!onTuneEvent) {
            TLog.e("SetUp" , "setUp() Tuning callback failed, please check tuner test case");
        }
        assertThat("Is onTuningEvent() callback invoked?", onTuneEvent, is(true));
    }

   @Test
    public void sectionFilterTest(){
        TLog.i(this, "Start SF test 30 Sec" );

        TableSectionFilter tableSectionFilter = sectionFilterGroup.newTableSectionFilter();

        /* Set timeout for section filter to 10s */
        //tableSectionFilter.setTimeOut(20000);

        SectionFilterListener myListener = new SectionFilterListener() {
            @Override
            public void onSectionFilterEvent(SectionFilterEvent event) {
                TLog.i(this, "onSectionFilterEvent() [" + event + "]");
                if (event instanceof TimeOutEvent){
                    TLog.i(this, "Section filter operations time out");
                } else if(event instanceof IncompleteFilteringEvent) {
                    TLog.i(this, "Filter parameters is incorrect");
                } else if (event instanceof SectionAvailableEvent) {
                    TLog.i(this, "Section filter operations is successful. do nothing!!");
                } else if (event instanceof EndOfFilteringEvent) {
                    TLog.i(this, "Section filter operations is EndOfFilteringEvent");
                    TableSectionFilter recievesf = (TableSectionFilter) event.getSource();

                    try {
                        Section currentsection[] = recievesf.getSections();

                        int seclen = currentsection.length;
                        try {
                            for(int j = 0 ; j < seclen ; j ++) {
                                byte[] sectiondata = currentsection[j].getData();
                                int dataLen = sectiondata.length;
                                TLog.i(this, "section data [" + dataLen + "]");
                                if (dataLen >= 20) {
                                    dataLen = 20;
                                    TLog.i(this, "section data [" + dataLen + "]. just print out 20 bytes.");
                                }
                                StringBuffer result = new StringBuffer();
                                result.append("Section Data [" + j + "]");
                                for (int i = 0; i < dataLen; i++) {
                                    result.append(" [" + String.format("%02x", sectiondata[i]) + "]");
                                }
                                TLog.i(this, result.toString());
                                currentsection[j].setEmpty();
                            }
                        } catch (Exception e1) {
                            TLog.i(this, "Exception");
                            e1.printStackTrace();
                        }
                        //don't stop any sectionfilter.

                    } catch (FilteringInterruptedException e) {
                        TLog.i(this, "FilteringInterruptedException");
                        e.printStackTrace();
                    }
                }
                isSFSuccess = true;
            }
        };

        tableSectionFilter.setSectionFilterListener(myListener);

       TLog.i(this, "setSourceId [" +sourceId +"]" );
       sectionFilterGroup.setSourceId(sourceId);

        try {
            TLog.i(this, "Set the parameters on the filter");
            tableSectionFilter.startFiltering(null, PMT_PID, PMT_TABLE_ID);
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

       /* Wait few miniseconds for section filtering */
       try {
           TimeUnit.MILLISECONDS.sleep(30000);
       } catch (InterruptedException e) {
           e.printStackTrace();
       }
       assertThat("Is section filter successful?", isSFSuccess, is(true));
    }

    @Test
    public void testTimeoutEvent(){
        long timeout = 10000;
        SectionFilterGroup sectionFilterGroup = new SectionFilterGroup(NUMBER_OF_SECTION_FILTER);
        TableSectionFilter tableSectionFilter = sectionFilterGroup.newTableSectionFilter();
        tableSectionFilter.setTimeOut(timeout);
        SectionFilterListener sectionFilterListener = new SectionFilterListener() {
            @Override
            public void onSectionFilterEvent(SectionFilterEvent sectionFilterEvent) {
                TLog.i(this, "testTimeoutEvent() [" + sectionFilterEvent + "]");
                if (sectionFilterEvent instanceof TimeOutEvent){
                    isTimeoutEvent = true;
                }
            }
        };

        tableSectionFilter.setSectionFilterListener(sectionFilterListener);
        sectionFilterGroup.setSourceId(sourceId);

        TLog.i(this, "testTimeoutEvent() start");
        isTimeoutEvent = false;
        try {
            tableSectionFilter.startFiltering(null, PMT_PID, -1);
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
        } catch (NoAvailableResourceException e) {
            e.printStackTrace();
        }

        /* Wait few seconds for timeout event happens */
        try {
            TimeUnit.MILLISECONDS.sleep(timeout + 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (!isTimeoutEvent) {
            TLog.e(this, "testTimeoutEvent() failed");
        } else {
            TLog.i(this, "testTimeoutEvent() success");
        }
        assertThat("Does timeout event happen?", isTimeoutEvent, is(true));
        sectionFilterGroup.detach();

    }

    @AfterClass
    public static void tearDown(){
        sectionFilterGroup.detach();
    }
}
