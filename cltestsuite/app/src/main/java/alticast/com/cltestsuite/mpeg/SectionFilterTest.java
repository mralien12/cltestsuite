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

import android.util.Log;

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

    public static final int TIME_OUT_EVENT = 0;
    public static final int INCOMPLETE_FILTERING_EVENT = 1;
    public static final int VERSION_CHANGE_DETECTED_EVENT = 2;
    public static final int SECTION_AVAILABLE_EVENT = 3;
    public static final int END_OF__FILTERING_EVENT = 4;

    public static final int FILTER_RESOURCE_EXCEPTION = 0;
    public static final int ILLEGAL_FILTER_DEFINITION_EXCEPTION = 1;
    public static final int CONNECTION_LOST_EXCEPTION = 2;
    public static final int INVALID_SOURCE_EXCEPTION = 3;
    public static final int NO_DATA_AVAILABLE_EXCEPTION = 4;
    public static final int FILTERING_INTERRUPTED_EXCEPTION = 5;

    public static final int TIMEOUT = 20000;
    private static int ret[];

    private static SectionFilterListener sectionFilterListener;
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

    public static synchronized SectionFilterTest getInstance() {

        errorLog = null;

        if (instance == null) {
            instance = new SectionFilterTest();

            sectionFilterGroup = new SectionFilterGroup(NUMBER_OF_SECTION_FILTER);

            if (channels == null) {
                channels = ChannelManager.getInstance().getChannelList(ChannelManager.CHANNEL_LIST_ALL);
            }

            if (channels.length <= 0) {
                errorLog = "Empty channel list";
            } else {
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
                    TLog.i(this, "singleTest: onTuningEvent [ " + i + "] [" + s + "]");
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

        sectionFilterListener = new SectionFilterListener() {
            @Override
            public void onSectionFilterEvent(SectionFilterEvent sectionFilterEvent) {

                TLog.i(this, "onSectionFilterEvent() [" + sectionFilterEvent + "]");

                if (sectionFilterEvent instanceof TimeOutEvent) {
                    TLog.i(this, "Section filter operations time out");
                    ret[TIME_OUT_EVENT] = TestCase.SUCCESS;
                } else if (sectionFilterEvent instanceof IncompleteFilteringEvent) {
                    TLog.i(this, "Filter parameters is incorrect");
                    ret[INCOMPLETE_FILTERING_EVENT] = TestCase.SUCCESS;
                } else if (sectionFilterEvent instanceof VersionChangeDetectedEvent) {
                    TLog.i(this, "Section filter operations is different from earlier sections!!");
                    ret[VERSION_CHANGE_DETECTED_EVENT] = TestCase.SUCCESS;
                } else if (sectionFilterEvent instanceof SectionAvailableEvent) {
                    TLog.i(this, "Section filter operations is successful!!");
                    ret[SECTION_AVAILABLE_EVENT] = TestCase.SUCCESS;
                } else if (sectionFilterEvent instanceof EndOfFilteringEvent) {
                    TLog.i(this, "Section filter operations is EndOfFilteringEvent");
                    ret[END_OF__FILTERING_EVENT] = TestCase.SUCCESS;
                    errorLog = "Section filter operations is EndOfFilteringEvent";
                    MainActivity.sfEventTestCaseList.get(END_OF__FILTERING_EVENT).setFailedReason(errorLog);
                }
            }
        };

        return instance;
    }

    public int sectionFilterException(int position, int size) {
        ret = new int[size];
        for (int i = 0; i < size; i++)
            ret[i] = TestCase.FAIL;

        return ret[0];
    }

    public int[] sectionFilterEvent(int size) {
        ret = new int[size];
        for (int i = 0; i < size; i++)
            ret[i] = TestCase.FAIL;

        if (errorLog == null) {
            TableSectionFilter tableSectionFilter = sectionFilterGroup.newTableSectionFilter();

            /* Set timeout for section filter*/
            tableSectionFilter.setTimeOut(TIMEOUT);

            tableSectionFilter.setSectionFilterListener(sectionFilterListener);

            TLog.i(this, "setSourceId [" + sourceId + "]");
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

            sectionFilterGroup.detach();
            //MainActivity.sfTestCaseList.get(SF_EVENT).setFailedReason(errorLog);
        } else {
            for (int position = 0; position < size; position++)
                MainActivity.sfTestCaseList.get(position).setFailedReason(errorLog);
        }

        for (int position = 0; position < size; position++) {
            if (ret[position] != TestCase.SUCCESS) {
                ret[position] = TestCase.FAIL;
                switch (position) {
                    case TIME_OUT_EVENT:
                        errorLog = "Section filter operations not time out";
                        MainActivity.sfEventTestCaseList.get(TIME_OUT_EVENT).setFailedReason(errorLog);
                        break;
                    case INCOMPLETE_FILTERING_EVENT:
                        errorLog = "Section filter started by TableSectionFilter completely defined";
                        MainActivity.sfEventTestCaseList.get(INCOMPLETE_FILTERING_EVENT).setFailedReason(errorLog);
                        break;
                    case VERSION_CHANGE_DETECTED_EVENT:
                        errorLog = "Section filter has a same version_number from earlier sections";
                        MainActivity.sfEventTestCaseList.get(VERSION_CHANGE_DETECTED_EVENT).setFailedReason(errorLog);
                        break;
                    case SECTION_AVAILABLE_EVENT:
                        errorLog = "Section filter not matching the filtering pattern";
                        MainActivity.sfEventTestCaseList.get(SECTION_AVAILABLE_EVENT).setFailedReason(errorLog);
                        break;
                    case END_OF__FILTERING_EVENT:
                        errorLog = "Section filter not ending of a filtering operation started by RingSectionFilter or TableSectionFilter";
                        MainActivity.sfEventTestCaseList.get(END_OF__FILTERING_EVENT).setFailedReason(errorLog);
                        break;
                    default:
                        break;
                }
            }
        }
        return ret;
    }
}
