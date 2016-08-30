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
import java.sql.Connection;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.lobzik.home_sapiens.pi.AppData;
import org.lobzik.home_sapiens.pi.ConnJDBCAppender;
import org.lobzik.home_sapiens.pi.event.Event;
import org.lobzik.home_sapiens.pi.event.EventManager;

/**
 *
 * @author lobzik
 */
public class SpeakerModule implements Module {

    public final String MODULE_NAME = this.getClass().getSimpleName();
    private static SpeakerModule instance = null;
    private Process process = null;
    private static Logger log = null;
    private static final String PREFIX = "/usr/bin/sudo";
    private static final String COMMAND = "/usr/bin/aplay";
    
    private SpeakerModule() { //singleton
    }

    public static SpeakerModule getInstance() {
        if (instance == null) {
            instance = new SpeakerModule(); //lazy init
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
            EventManager.subscribeForEventType(this, Event.Type.USER_ACTION);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void play(String file) {
        try {
            String[] env = {"aaa=bbb", "ccc=ddd"};

            String[] args = {PREFIX, COMMAND, file};
            File workdir = AppData.getSoundWorkDir();
            Runtime runtime = Runtime.getRuntime();
            long before = System.currentTimeMillis();
            log.debug("Playing " + file + " at " + workdir);
            process = runtime.exec(args, env, workdir);
            
            StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream());
            StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream());
            errorGobbler.start();
            outputGobbler.start();
            process.waitFor();
            int exitValue = process.exitValue();
            //log.debug(StreamGobbler.getAllOutput());
            StreamGobbler.clearOutput();
            if (exitValue != 0) {
                log.error("Error executing, exit status: " + exitValue);
            }
        } catch (Exception e) {
            log.error("Error " + e.getMessage());
        }

    }

    @Override
    public void handleEvent(Event e) {
        if (e.type == Event.Type.USER_ACTION && e.name.equals("play_sound")) {
            play(AppData.getSoundWorkDir().getAbsolutePath() + File.separator + (String)e.data.get("sound_file"));
        }
    }

    public static void finish() {

    }

    public static class StreamGobbler extends Thread {

        InputStream is;
        private static StringBuilder output = new StringBuilder();

        public StreamGobbler(InputStream is) {
            this.is = is;
        }

        @Override
        public void run() {
            try {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line = null;
                while ((line = br.readLine()) != null) {
                    output.append(line).append("\n");
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

        public static String getAllOutput() {
            return output.toString();
        }

        public static void clearOutput() {
            output = new StringBuilder();
        }
    }
}
