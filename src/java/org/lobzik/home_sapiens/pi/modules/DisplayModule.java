/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi.modules;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;
import org.lobzik.home_sapiens.pi.AppData;
import org.lobzik.home_sapiens.pi.event.Event;

/**
 *
 * @author lobzik
 */
import javax.imageio.ImageIO;
import org.apache.log4j.Appender;
import org.lobzik.home_sapiens.pi.BoxCommonData;
import org.lobzik.home_sapiens.pi.ConnJDBCAppender;
import org.lobzik.home_sapiens.pi.WebNotification;
import org.lobzik.home_sapiens.pi.event.EventManager;
import org.lobzik.tools.Tools;

public class DisplayModule implements Module {

    public final String MODULE_NAME = this.getClass().getSimpleName();
    private static DisplayModule instance = null;

    private static final String TMP_FILE = "i.tmp";
    private static final String IMG_FILE = "i.png";
    private static final String SYMLINK1 = "a.png";
    private static final String SYMLINK2 = "b.png";

    private static Logger log = null;
    private static final String PREFIX = "/usr/bin/sudo";
    private static final String FBI_COMMAND = "/usr/bin/fbi";
    private static final String LN_COMMAND = "/bin/ln";
    private FbiRunner fbiRunner = null;

    private DisplayModule() { //singleton
    }

    public static DisplayModule getInstance() {
        if (instance == null) {
            instance = new DisplayModule(); //lazy init
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
            draw();
            EventManager.subscribeForEventType(this, Event.Type.TIMER_EVENT);
            EventManager.subscribeForEventType(this, Event.Type.SYSTEM_EVENT);
            
            fbiRunner = new FbiRunner();
            fbiRunner.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleEvent(Event e) {
        if ((e.type == Event.Type.TIMER_EVENT || e.type == Event.Type.SYSTEM_EVENT) && e.name.equals("update_display")) {
            draw();
            //TODO мама, это только для отладки
            WebNotification wn = new WebNotification(WebNotification.Severity.ALERT, "INTERNAL_TEMP", "Быстрый рост температуры", new Date(System.currentTimeMillis() - 1800000), new Date());
            HashMap data = new HashMap();
            data.put("WebNotification", wn);
            Event reaction = new Event ("web_notification", data, Event.Type.REACTION_EVENT);
            AppData.eventManager.newEvent(reaction);
        }
    }

    private void draw() {
        try {
            log.debug("Drawing img for screen");
            Graphics g = null;
            BufferedImage img = null;
            BufferedImage temperatureImg = null;

            try {
                img = ImageIO.read(new File(AppData.getGraphicsWorkDir().getAbsolutePath() + File.separator + "screen.jpg"));
                temperatureImg = ImageIO.read(new File(AppData.getGraphicsWorkDir().getAbsolutePath() + File.separator + "temperature.png"));
            } catch (IOException e) {
            }

            g = img.getGraphics();
            g.setColor(new Color(0, 0, 0));

            g.setFont(new Font("Tahoma", Font.PLAIN, 110));
            Date d = new Date();
            if (BoxCommonData.TEST_MODE) {
                g.drawString("TEST", 100, 180);

            } else {
                g.drawString(Tools.getFormatedDate(d, "HH:mm"), 100, 180);
                
                
                //alert
                g.setColor(new Color(243, 67, 54));
                g.fillRect(0, 251, 480, 320);
                //g.drawLine(20, 20, 360, 20);
                g.setColor(new Color(0, 0, 0));

                g.drawImage(temperatureImg, 25, 270, null);

                g.setColor(new Color(255, 255, 255));
                g.setFont(new Font("Tahoma", Font.PLAIN, 20));
                g.drawString(Tools.getFormatedDate(d, "HH:mm"), 55, 290);
            }


            File file = new File(AppData.getGraphicsWorkDir().getAbsolutePath() + File.separator + TMP_FILE);
            FileOutputStream fos = new FileOutputStream(file);
            java.nio.channels.FileLock lock = fos.getChannel().lock();
            try {
                ImageIO.write(img, "png", fos);
                fos.flush();

            } finally {
                lock.release();
            }
            fos.close();
            Path src = Paths.get(AppData.getGraphicsWorkDir().getAbsolutePath() + File.separator + TMP_FILE);
            Path dst = Paths.get(AppData.getGraphicsWorkDir().getAbsolutePath() + File.separator + IMG_FILE);

            Files.move(src, dst, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);

        } catch (Exception e) {
            log.error("Error " + e.getMessage());
        }

    }

    public static void finish() {
        instance.fbiRunner.finish();
    }

    public static class FbiRunner extends Thread {

        private OutputStreamWriter osr = null;
        private Process process = null;
        private boolean run = true;

        public void finish() {
            log.info("Exiting process");
            try {
                run = false;
                osr.write("q");
                osr.flush();
                process.destroyForcibly();
            } catch (Exception e) {
            }
        }

        @Override
        public void run() {
            try {
                //creating file
                List<String> command = new LinkedList();
                log.debug("Started, drawing image");
                instance.draw();
                String[] env = {"aaa=bbb", "ccc=ddd"};
                String imgFile = AppData.getGraphicsWorkDir().getAbsolutePath() + File.separator + IMG_FILE;
                String symLink1 = AppData.getGraphicsWorkDir().getAbsolutePath() + File.separator + SYMLINK1;
                String symLink2 = AppData.getGraphicsWorkDir().getAbsolutePath() + File.separator + SYMLINK2;
                command.clear();
                command.add(LN_COMMAND);
                command.add("-sf");
                command.add(imgFile);
                command.add(symLink1);

                log.debug("Creating symlink1");

                File workdir = AppData.getGraphicsWorkDir();
                Runtime runtime = Runtime.getRuntime();
                process = runtime.exec(command.toArray(new String[command.size()]), env, workdir);
                process.waitFor();
                int exitValue = process.exitValue();
                if (exitValue != 0) {
                    log.error("error creating symlink1, exit status= " + exitValue);
                    return;
                }

                command.clear();
                command.add(LN_COMMAND);
                command.add("-sf");
                command.add(imgFile);
                command.add(symLink2);

                log.debug("Creating symlink2");

                process = runtime.exec(command.toArray(new String[command.size()]), env, workdir);
                process.waitFor();
                exitValue = process.exitValue();
                if (exitValue != 0) {
                    log.error("error creating symlink2, exit status= " + exitValue);
                    return;
                }

                command.clear();
                command.add(PREFIX);
                command.add(FBI_COMMAND);
                command.add(imgFile);
                command.add(symLink1);
                command.add(symLink2);
                command.add("-a");
                command.add("-noverbose");
                command.add("-t");
                command.add("1");
                command.add("-cachemem");
                command.add("0");
                command.add("-T");
                command.add("1");

                log.info("Running FBI");
                while (run) {
                    try {
                        exitValue = 0;
                        process = runtime.exec(command.toArray(new String[command.size()]), env, workdir);
                        //osr = new OutputStreamWriter(process.getOutputStream());
                        process.waitFor();
                        log.info("FBI exited");
                        exitValue = process.exitValue();

                    } catch (Throwable e) {
                        log.error(e.getMessage());
                    }
                    if (exitValue != 0) {
                        log.debug("error in FBI, exit status= " + exitValue);
                        Thread.sleep(10000);
                    } else {
                        break;
                    }
                }

            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }
}
