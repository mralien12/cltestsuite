package com.alticast.taurus.testsuite.mpeg;
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

import com.alticast.taurus.testsuite.util.TLog;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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

public class SectionFilterExceptionTest {

    private final static int NUMBER_OF_SECTION_FILTER = 1;
    private final int PMT_PID = 0x13EC;
    private final int PAT_PID = 0x00;
    private final int PAT_TABLE_ID = 0x00;
    private final int PMT_TABLE_ID = 0x02;

    private static Channel[] channels;
    private static Channel currentChannel;
    private static SectionFilterGroup sectionFilterGroup;
    private static boolean onTuneEvent;
    private static Tuner singleTuner;
    private static int sourceId;
    private static boolean isSFSuccess;
    private static TableSectionFilter tableSectionFilter;
    private static SectionFilterListener myListener;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @BeforeClass
    public static void setUp() {
        TLog.i("SetUp", "SetUp SectionfilterGroup #" + NUMBER_OF_SECTION_FILTER);
        sectionFilterGroup = new SectionFilterGroup(NUMBER_OF_SECTION_FILTER);

        TLog.i("Setup", "Check Channels");
        if (channels == null) {
            channels = ChannelManager.getInstance().getChannelList(ChannelManager.CHANNEL_LIST_ALL);
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
                TLog.i(this, "singleTest: onTuningEvent [ " + i + "] [" + s + "]");
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
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertThat("Is onTuningEvent() callback invoked?", onTuneEvent, is(true));

        tableSectionFilter = sectionFilterGroup.newTableSectionFilter();

        /* Set timeout for section filter to 10s */
        //tableSectionFilter.setTimeOut(20000);

        myListener = new SectionFilterListener() {
            @Override
            public void onSectionFilterEvent(SectionFilterEvent event) {
                TLog.i(this, "onSectionFilterEvent() [" + event + "]");
                if (event instanceof TimeOutEvent) {
                    TLog.i(this, "Section filter operations time out");
                } else if (event instanceof IncompleteFilteringEvent) {
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
                            for (int j = 0; j < seclen; j++) {
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
        sectionFilterGroup.setSourceId(sourceId);
    }

    //Demo
    @Test
    public void startFilteringExceptionTest() throws FilterResourceException, IllegalFilterDefinitionException, ConnectionLostException {
        exception.expect(ConnectionLostException.class);
        TLog.i(this, "Set the parameters on the filter");
        tableSectionFilter.startFiltering(null, PMT_PID, PMT_TABLE_ID);
        System.out.println("ExpectedException");
        try {
            TimeUnit.SECONDS.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
