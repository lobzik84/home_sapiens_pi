/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi.modules;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.lobzik.home_sapiens.pi.AppData;
import org.lobzik.home_sapiens.pi.ConnJDBCAppender;
import org.lobzik.home_sapiens.pi.event.Event;
import org.lobzik.home_sapiens.pi.event.EventManager;
import org.lobzik.tools.StreamGobbler;

/**
 *
 * @author lobzik
 */
public class VideoModule implements Module {

    public final String MODULE_NAME = this.getClass().getSimpleName();
    private static VideoModule instance = null;
    private static final int[] ZM_MONITOR_IDS = {1, 2};
    public static final String[] IMAGE_FILES = {"Monitor1.jpg", "Monitor2.jpg"};
    private Process process = null;
    private static Logger log = null;
    private static final String PREFIX = "/usr/bin/sudo";
    private static final String COMMAND = "/usr/bin/zmu";

    private VideoModule() { //singleton
    }

    public static VideoModule getInstance() {
        if (instance == null) {
            instance = new VideoModule(); //lazy init
            log = Logger.getLogger(instance.MODULE_NAME);
            Appender appender = ConnJDBCAppender.getAppenderInstance(AppData.dataSource, instance.MODULE_NAME);
            log.addAppender(appender);
        }
        return instance;
    }

    @Override
    public String getModuleName() {
        return MODULE_NAME;
    }

    @Override
    public void start() {
        try {
            EventManager.subscribeForEventType(this, Event.Type.TIMER_EVENT);
            EventManager.subscribeForEventType(this, Event.Type.USER_ACTION);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void capture(String camId) {
        try {
            String[] env = {"aaa=bbb", "ccc=ddd"};

            String[] args = {PREFIX, COMMAND, "-m", camId, "-i", "-v"};
            File workdir = AppData.getCaptureWorkDir();
            Runtime runtime = Runtime.getRuntime();
            long before = System.currentTimeMillis();
            log.debug("Capturing monitorId = " + camId);
            process = runtime.exec(args, env, workdir);

            StringBuilder output = new StringBuilder();
            StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), output);
            StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), output);
            errorGobbler.start();
            outputGobbler.start();
            process.waitFor();
            int exitValue = process.exitValue();
            log.debug(output);

            if (exitValue != 0) {
                log.error("Error executing, exit status: " + exitValue);
            }
        } catch (Exception e) {
            log.error("Error " + e.getMessage());
        }

    }

    @Override
    public void handleEvent(Event e) {
        /*if (e.type == Event.Type.TIMER_EVENT && e.name.equals("internal_sensors_poll")) {
           for (int monitorId: ZM_MONITOR_IDS) {
               capture(monitorId + "");
           }
        }
        else*/ if (e.type == Event.Type.USER_ACTION && e.name.equals("get_capture")) {
            for (int monitorId : ZM_MONITOR_IDS) {
                capture(monitorId + "");
            }
        }
    }

    public static void finish() {

    }

}
