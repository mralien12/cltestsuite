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

package alticast.com.cltestsuite.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class Util {
    public static String getExternalStorage() {
        String pathOfStorage = null;
        List<String> listOfStorage = new ArrayList<String>();

        BufferedReader buf_reader = null;
        try {
            buf_reader = new BufferedReader(new FileReader("/proc/mounts"));
            String line;

            while ((line = buf_reader.readLine()) != null) {
                if (line.contains("/mnt/media_rw") || line.contains("/mnt/expand")) {
                    StringTokenizer tokens = new StringTokenizer(line, " ");
                    String unused = tokens.nextToken(); // device
                    String mount_point = tokens.nextToken(); // mount point/
                    listOfStorage.add(mount_point + "/");
                }
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (buf_reader != null) {
                try {
                    buf_reader.close();
                } catch (IOException ex) {
                }
            }
        }

        if (listOfStorage.size() > 0) {
            pathOfStorage = listOfStorage.get(0);
        }
        return pathOfStorage;
    }
}
