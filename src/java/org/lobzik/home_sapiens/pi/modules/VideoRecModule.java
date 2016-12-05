/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi.modules;

import java.io.File;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.lobzik.home_sapiens.pi.AppData;
import org.lobzik.home_sapiens.pi.ConnJDBCAppender;
import org.lobzik.home_sapiens.pi.event.Event;
import org.lobzik.home_sapiens.pi.event.EventManager;
import org.lobzik.tools.StreamGobbler;
import org.lobzik.tools.Tools;

/**
 *
 * @author lobzik
 */
public class VideoRecModule implements Module {

    public final String MODULE_NAME = this.getClass().getSimpleName();
    private static VideoRecModule instance = null;
    private static Logger log = null;
    private static final String TMP_FILE = "-out.mp4";

    private static final String PREFIX = "/usr/bin/sudo";
    private static final String COMMAND = AppData.getSoundWorkDir().getAbsolutePath() + File.separator +"video"+ File.separator + "videoCapture.sh";
    
    //для записи лога по reaction_events
    public static VideoRecModule getInstance() {
        if (instance == null) {
            instance = new VideoRecModule(); //lazy init
            log = Logger.getLogger(instance.MODULE_NAME);
            Appender appender = ConnJDBCAppender.getAppenderInstance(AppData.dataSource, instance.MODULE_NAME);
            log.addAppender(appender);
        }
        return instance;
    }

    private VideoRecModule() {

    }

    @Override
    public String getModuleName() {
        return MODULE_NAME;
    }

    @Override
    public void start() {
        try {
            EventManager.subscribeForEventType(this, Event.Type.BEHAVIOR_EVENT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleEvent(Event e) {
        if (e.type == Event.Type.BEHAVIOR_EVENT && e.name.equals("upload_video_recs")) {
            try {
                log.info("Creating video record");
                if (!TunnelClientModule.getInstance().tunnelIsUp()) {
                    throw new Exception ("No server link");
                }
                String boxSessionKey = TunnelClientModule.getInstance().getBoxSessionKey();

                new Thread() {
                    @Override
                    public void run() {
                        try {
 
                           Process process = null;
                            String[] env = {"aaa=bbb", "ccc=ddd"};

                            String[] args = {PREFIX, COMMAND, "2", "50", "2"};
                            File workdir = AppData.getCaptureWorkDir();
                            Runtime runtime = Runtime.getRuntime();
                            log.debug("Capturing monitorId = " + 1);
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
            
                            Tools.sysExec("zmu", AppData.getCaptureWorkDir()); //todo
                            //ffmpeg?
                            // encrypt and upload to server
                            log.info("Video saved");
                        } catch (Exception e) {
                            log.error(e.getMessage());
                        }
                    }
                }.start();
            } catch (Exception ee) {
                log.error("Error " + ee.getMessage());
            }
        }
    }

}
