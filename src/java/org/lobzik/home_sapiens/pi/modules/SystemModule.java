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
import java.util.HashMap;
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
public class SystemModule implements Module {

    public final String MODULE_NAME = this.getClass().getSimpleName();
    private static SystemModule instance = null;
    private Process process = null;
    private static Logger log = null;
    private static final String PREFIX = "/usr/bin/sudo";
    private static final String SHUTDOWN_COMMAND = "halt";
    private static final String SHUTDOWN_SUFFIX = "-p";

    private SystemModule() { //singleton
    }

    public static SystemModule getInstance() {
        if (instance == null) {
            instance = new SystemModule(); //lazy init
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
            EventManager.subscribeForEventType(this, Event.Type.SYSTEM_EVENT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void shutdown() {
        try {

            
            String[] env = {"aaa=bbb", "ccc=ddd"};

            String[] args = {PREFIX, SHUTDOWN_COMMAND, SHUTDOWN_SUFFIX};
            File workdir = AppData.getSoundWorkDir();
            Runtime runtime = Runtime.getRuntime();
            log.info("Shutting down system");/*
            process = runtime.exec(args, env, workdir);

            StringBuilder output = new StringBuilder();
            StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), output);
            StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), output);
            errorGobbler.start();
            outputGobbler.start();
            process.waitFor();
            int exitValue = process.exitValue();
            //log.debug(StreamGobbler.getAllOutput());

            if (exitValue != 0) {
                log.error("Error executing, exit status: " + exitValue);
            }*/
        } catch (Exception e) {
            log.error("Error " + e.getMessage());
        }

    }

    @Override
    public void handleEvent(Event e) {
        if (e.type == Event.Type.SYSTEM_EVENT && e.name.equals("shutdown")) {
            shutdown();
        }
    }

    public static void finish() {

    }

}
