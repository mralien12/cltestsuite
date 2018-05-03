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

import af.channel.Channel;
import af.channel.ChannelManager;
import af.media.Tuner;
import af.media.TunerFactory;
import af.mpeg.section.SectionFilter;
import af.mpeg.section.SectionFilterEvent;
import af.mpeg.section.SectionFilterGroup;
import af.mpeg.section.SectionFilterListener;
import af.mpeg.section.TableSectionFilter;
import af.resource.ResourceClient;
import alticast.com.cltestsuite.utils.TLog;

public class SectionFilterTest {

    private static SectionFilterTest instance = null;
    private final static int NUMBER_OF_SECTION_FILTER = 1;
    private static SectionFilterGroup sectionFilterGroup;
    private static Channel[] channels;
    private static Tuner singleTuner;
    private static boolean onTuneEvent;
    private static int sourceId;

    public SectionFilterTest() {
    }

    public static synchronized SectionFilterTest getInstance () {
        if (instance == null) {
            instance = new SectionFilterTest();

            sectionFilterGroup = new SectionFilterGroup(NUMBER_OF_SECTION_FILTER);
            channels = ChannelManager.getInstance().getChannelList(ChannelManager.CHANNEL_LIST_ALL);
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
        }

        return instance;
    }

    public void sectionFitlerEvent (){
        TableSectionFilter tableSectionFilter = sectionFilterGroup.newTableSectionFilter();
        /* Set timeout for section filter to 10s */
        tableSectionFilter.setTimeOut(20000);

        SectionFilterListener sectionFilterListener = new SectionFilterListener() {
            @Override
            public void onSectionFilterEvent(SectionFilterEvent sectionFilterEvent) {

                TableSectionFilter recievesf = (TableSectionFilter) sectionFilterEvent.getSource();
            }
        };
    }
}
