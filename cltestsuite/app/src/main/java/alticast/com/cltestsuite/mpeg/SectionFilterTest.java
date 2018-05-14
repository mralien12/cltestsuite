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

package alticast.com.cltestsuite.mpeg;

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
import af.mpeg.section.SectionFilter;
import af.mpeg.section.SectionFilterEvent;
import af.mpeg.section.SectionFilterGroup;
import af.mpeg.section.SectionFilterListener;
import af.mpeg.section.TableSectionFilter;
import af.mpeg.section.TimeOutEvent;
import af.mpeg.section.VersionChangeDetectedEvent;
import af.resource.NoAvailableResourceException;
import af.resource.ResourceClient;
import alticast.com.cltestsuite.MainActivity;
import alticast.com.cltestsuite.utils.TLog;
import alticast.com.cltestsuite.utils.TestCase;

public class SectionFilterTest {

    public static final int SF_EXCEPTION = 0;
    public static final int SF_EVENT = 1;
    private static int ret;

    private static SectionFilterTest instance = null;
    private final static int NUMBER_OF_SECTION_FILTER = 1;
    private static SectionFilterGroup sectionFilterGroup;
    private static Channel[] channels;
    private static Channel currentChannel;
    private static Tuner singleTuner;
    private static int sourceId;
    private static String errorLog;

    private final int PMT_PID = 0x13EC;
    private final int PMT_TABLE_ID = 0x02;

    public SectionFilterTest() {
    }

    public static synchronized SectionFilterTest getInstance () {

        errorLog = null;

        if (instance == null) {
            instance = new SectionFilterTest();

            sectionFilterGroup = new SectionFilterGroup(NUMBER_OF_SECTION_FILTER);

            if (channels == null) {
                channels = ChannelManager.getInstance().getChannelList(ChannelManager.CHANNEL_LIST_ALL);
            }

            if (channels.length <= 0)
            {
                errorLog = "Empty channel list";
            }else
            {
                currentChannel = channels[0];
            }

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
                    TLog.i(this, "singleTest: onTuningEvent [ " + i + "] [" + s +"]" );
                    if (i == 1) {
                        sourceId = singleTuner.getSourceId();
                    }
                }
            });

            if (errorLog == null) {
                try {
                    singleTuner.tune(currentChannel.getUri());
                } catch (NoAvailableResourceException e) {
                    errorLog = "Tuning not success";
                    e.printStackTrace();
                }

                try {
                    TimeUnit.MILLISECONDS.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        return instance;
    }

    public int sectionFilterException (){
        ret = TestCase.FAIL;


        return ret;
    }

    public int sectionFilterEvent (){
        ret = TestCase.FAIL;

        if (errorLog == null) {
            TableSectionFilter tableSectionFilter = sectionFilterGroup.newTableSectionFilter();
            /* Set timeout for section filter to 20s */
            tableSectionFilter.setTimeOut(20000);

            SectionFilterListener sectionFilterListener = new SectionFilterListener() {
                @Override
                public void onSectionFilterEvent(SectionFilterEvent sectionFilterEvent) {

                    TLog.i(this, "onSectionFilterEvent() [" + sectionFilterEvent + "]");

                    if (sectionFilterEvent instanceof TimeOutEvent) {
                        TLog.i(this, "Section filter operations time out");
                        errorLog = "Section filter operations time out";
                    } else if(sectionFilterEvent instanceof IncompleteFilteringEvent) {
                        TLog.i(this, "Filter parameters is incorrect");
                        errorLog = "Filter parameters is incorrect";
                    } else if (sectionFilterEvent instanceof VersionChangeDetectedEvent) {
                        TLog.i(this, "Section filter operations is different from earlier sections!!");
                        errorLog = "Section filter operations is different from earlier sections!!";
                    } else if(sectionFilterEvent instanceof SectionAvailableEvent){
                        TLog.i(this, "Section filter operations is successful!!");
                        errorLog = "Section filter operations is successful!!";
                    }
                    else if (sectionFilterEvent instanceof EndOfFilteringEvent) {
                        TLog.i(this, "Section filter operations is EndOfFilteringEvent");
                        errorLog = "Section filter operations is EndOfFilteringEvent";

                        TableSectionFilter recievesf = (TableSectionFilter) sectionFilterEvent.getSource();

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
                }
            };

            tableSectionFilter.setSectionFilterListener(sectionFilterListener);

            TLog.i(this, "setSourceId [" +sourceId +"]" );
            sectionFilterGroup.setSourceId(sourceId);

            try {
                TLog.i(this, "Set the parameters on the filter");
                tableSectionFilter.startFiltering(null, PMT_PID, PMT_TABLE_ID);
            } catch (FilterResourceException e) {
                errorLog = "Filter Resource Exception";
                e.printStackTrace();
            } catch (ConnectionLostException e) {
                errorLog = "Connection Lost Exception";
                e.printStackTrace();
            } catch (IllegalFilterDefinitionException e) {
                errorLog = "Illegal Filter Definition Exception";
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
                errorLog = "No Available Resource Exception";
                e.printStackTrace();
            }

            /* Wait few miniseconds for section filtering */
            try {
                TimeUnit.MILLISECONDS.sleep(30000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            MainActivity.sfTestCaseList.get(SF_EVENT).setFailedReason(errorLog);
        } else {
            MainActivity.sfTestCaseList.get(SF_EVENT).setFailedReason(errorLog);
        }

        if (errorLog == null)
            ret = TestCase.SUCCESS;
        return ret;
    }
}
