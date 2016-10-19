/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi.modules;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.lobzik.home_sapiens.entity.Measurement;
import org.lobzik.home_sapiens.entity.Parameter;
import org.lobzik.home_sapiens.pi.AppData;
import org.lobzik.home_sapiens.pi.ConnJDBCAppender;
import org.lobzik.home_sapiens.pi.event.Event;
import org.lobzik.tools.StreamGobbler;

/**
 *
 * @author lobzik
 */
public class MicrophoneModule implements Module {

    public final String MODULE_NAME = this.getClass().getSimpleName();
    private static MicrophoneModule instance = null;

    private static final ARecordRunner arecordRunner = new ARecordRunner();
    private static final AudioAnalyzer audioAnalyzer = new AudioAnalyzer();

    private static Logger log = null;
    private static final String TMP_FILE = "tmp.wav";
    private static final String ARECORD = "/usr/bin/arecord";
    private static final AtomicBoolean doStoreAudioRecord = new AtomicBoolean(false);
    public static final int MICROPONE_REC_PERIOD = 3;//secs
    public static final int NOISE_TRIGGER_LEVEL = 1000;//ambient is about 600, max is 60000

    private static final String PARAMETER_ALIAS = "MIC_NOISE";
    private static Parameter parameter = null;

    private MicrophoneModule() { //singleton
    }

    public static MicrophoneModule getInstance() {
        if (instance == null) {
            instance = new MicrophoneModule(); //lazy init
            log = Logger.getLogger(instance.MODULE_NAME);
            Appender appender = ConnJDBCAppender.getAppenderInstance(AppData.dataSource, instance.MODULE_NAME);
            log.addAppender(appender);
            int paramId = AppData.parametersStorage.resolveAlias(PARAMETER_ALIAS);
            if (paramId > 0) {
                parameter = AppData.parametersStorage.getParameter(paramId);
            }
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

            audioAnalyzer.start();
            arecordRunner.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleEvent(Event e) {
    }

    public static void finish() {
        arecordRunner.finish();
        audioAnalyzer.finish();

    }

    private static class ARecordRunner extends Thread {

        private Process process = null;
        private boolean run = true;

        public void finish() {
            log.info("Exiting process");
            try {
                run = false;

                process.destroyForcibly();
            } catch (Exception e) {
            }
        }

        @Override
        public void run() {
            log.debug("Starting record cycle");
            List<String> command = new LinkedList();
            String tmpFile = AppData.getSoundWorkDir().getAbsolutePath() + File.separator + TMP_FILE;
            command.clear();
            command.add(ARECORD);
            command.add("-f");
            command.add("S16_LE");//Signed 16 bit Little Endian
            command.add("-c1");//??? mono?
            command.add("-r16000");//bitrate 16000 Hz
            command.add("-D");
            command.add("hw:1,0");//hardware device 1 channel 0
            command.add("-d");
            command.add("" + MICROPONE_REC_PERIOD);
            command.add(tmpFile);

            File workdir = AppData.getSoundWorkDir();
            Runtime runtime = Runtime.getRuntime();
            String[] env = {"aaa=bbb", "ccc=ddd"};

            while (run) {
                try {
                    process = runtime.exec(command.toArray(new String[command.size()]), env, workdir);
                    StringBuilder output = new StringBuilder();
                    StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), output);
                    StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), output);
                    errorGobbler.start();
                    outputGobbler.start();
                    process.waitFor();
                    int exitValue = process.exitValue();
                    if (exitValue != 0) {
                        throw new Exception("error executing arecord, status= " + exitValue + " output=" + output);
                    }
                    /*try {
                        synchronized (audioAnalyzer) {
                            audioAnalyzer.wait();
                        }
                    } catch (Exception e) {
                    }//lock analyzer JIC*/
                    Path tmp = Paths.get(AppData.getSoundWorkDir() + File.separator + TMP_FILE);
                    byte[] audioData = Files.readAllBytes(tmp);
                    audioAnalyzer.analyze(audioData);

                    Path dst = Paths.get(AppData.getGraphicsWorkDir().getAbsolutePath() + File.separator + "mic_" + System.currentTimeMillis() + ".wav");
                    if (doStoreAudioRecord.get()) {
                        log.debug("Storing record to " + dst);
                        Files.move(tmp, dst, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                    } else {
                        Files.deleteIfExists(tmp);
                    }

                } catch (Exception e) {
                    log.error(e.getMessage());
                    try {
                        Thread.sleep(60000);
                    } catch (InterruptedException ioe) {
                    }
                }
            }
        }
    }

    private static class AudioAnalyzer extends Thread {

        private boolean run = true;
        private byte[] fa;

        public void finish() {
            log.info("Exiting process");
            try {
                run = false;
                synchronized (this) {
                    notify();
                }
            } catch (Exception e) {
            }
        }

        public void analyze(byte[] audioData) {
            fa = audioData;
            synchronized (this) {
                notify();
            }
        }

        @Override
        public void run() {
            log.debug("Starting audioanalyzer");
            while (run) {
                try {
                    synchronized (this) {
                        wait();
                    }
                    //log.debug("ANALYZE THIS " + fa.length);
                    //TODO smart analyzis like  FFT
                    int startByte = 0;
                    // look for data header

                    int x = 0;
                    while (x < fa.length) {
                        if (fa[x] == 'd' && fa[x + 1] == 'a'
                                && fa[x + 2] == 't' && fa[x + 3] == 'a') {
                            startByte = x + 8;
                            break;
                        }
                        x++;
                    }

                    short[] snd = new short[fa.length / 2];

                    x = 0;
                    int length = fa.length;
                    for (int s = startByte; s < length; s = s + 2) {
                        snd[x] = (short) (fa[s + 1] * 0x100 + fa[s]);
                        x++;
                    }
                    //log.debug(snd.length);

                    long sumMax = 0;
                    long sum = 0;
                    int peakDetectorTime = snd.length / MICROPONE_REC_PERIOD;
                    int c = 0;
                    for (short b : snd) {
                        c++;
                        if (b > 0) {
                            sumMax += b;
                        } else {
                            sumMax = sumMax - b;
                        }
                        if (c >= peakDetectorTime) {
                            c = 0;
                            if (sumMax > sum) {
                                sum = sumMax;
                            }
                        }
                    }
                    int avgMax = (int) (sum / peakDetectorTime);
                    HashMap eventData = new HashMap();
                    if (avgMax >= NOISE_TRIGGER_LEVEL) {
                        log.debug("noise detected! Avg is " + avgMax);
                        if (parameter != null) {
                            Measurement m = new Measurement(parameter, true);
                            eventData.put("parameter", parameter);
                            eventData.put("measurement", m);
                            Event e = new Event("mic_noise", eventData, Event.Type.PARAMETER_UPDATED);
                            AppData.eventManager.newEvent(e);
                        }
                    } else if (parameter != null) {
                        Measurement m = new Measurement(parameter, false);
                        eventData.put("parameter", parameter);
                        eventData.put("measurement", m);
                        Event e = new Event("mic_noise", eventData, Event.Type.PARAMETER_UPDATED);
                        AppData.eventManager.newEvent(e);
                    }

                } catch (Exception e) {
                    log.error("Error in analyzer: " + e.getMessage());
                }

            }
        }
    }
}
