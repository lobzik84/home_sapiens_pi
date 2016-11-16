/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi.modules;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
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

    private static final Stack<WebNotification> notifications = new Stack();
    private static final int MAX_STACK_SIZE = 10;

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
            EventManager.subscribeForEventType(this, Event.Type.REACTION_EVENT);
            fbiRunner = new FbiRunner();
            fbiRunner.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleEvent(Event e) {
        switch (e.type) {
            case TIMER_EVENT:
            case SYSTEM_EVENT:
                if (e.name.equals("update_display")) {
                    draw();
                }
                break;

            case REACTION_EVENT:
                if (e.name.equals("display_notification")) {
                    WebNotification n = (WebNotification) e.data.get("DisplayNotification");
                    if (n != null) {
                        while (notifications.size() > MAX_STACK_SIZE) {
                            notifications.remove(notifications.size() - 1);
                        }
                        notifications.push(n);
                        draw();
                    }
                } else if (e.name.equals("delete_display_notification")) {
                    String conditionAlias = (String)e.data.get("ConditionAlias");
                    if (conditionAlias != null) {
                        int index = -1;
                        
                        for (int i=0; i< notifications.size(); i++){
                            WebNotification n = notifications.get(i);
                            if (n.conditionAlias.equals(conditionAlias)) {
                                index = i;
                                break;
                            }
                        }
                        if (index >= 0) {
                            notifications.remove(index);
                        }
                        draw();
                    }
                }
                break;
        }

    }

    private void draw() {
        try {
            log.debug("Drawing img for screen");
            Graphics g = null;
            BufferedImage img = null;
            Image iconImg = null;
            if (BoxCommonData.TEST_MODE) {
                g.drawString("TEST", 100, 180);

            } else {
                List<WebNotification> lwn = WebNotificationsModule.getNotifications();
                WebNotification notif = null;
                //TODO будет Behavior - просто поставим реакцию и присвоим notif что надо
                /*
                for (int i = lwn.size() - 1; i >= 0; i--) {
                    WebNotification wn = lwn.get(i);
                    switch (wn.severity) {
                        case ALARM:
                        case ALERT:
                            if (notif == null) {
                                notif = wn;
                            }

                            break;

                    }
                }*/
                if (!notifications.isEmpty()) {
                    notif = notifications.peek();
                }

                try {
                    img = ImageIO.read(new File(AppData.getGraphicsWorkDir().getAbsolutePath() + File.separator + "screen.jpg"));
                    if (notif != null) {
                        switch (notif.parameterAlias) {

                            case "CHARGE":
                            case "DOOR_SENSOR":
                            case "GAS_SENSOR":
                            case "INTERNAL_HUMIDITY":
                            case "INTERNAL_TEMP":
                            case "PIR_SENSOR":
                            case "VAC_SENSOR":
                            case "WET_SENSOR":

                                iconImg = ImageIO.read(new File(AppData.getGraphicsWorkDir().getAbsolutePath() + File.separator + notif.parameterAlias + ".png"));
                                //File iconFile = new File(AppData.getGraphicsWorkDir().getAbsolutePath() + File.separator + notif.parameterAlias + ".svg");
                                //iconImg = transcodeSVGDocument(iconFile.toURI().toURL(), 21, 30);
                                break;

                        }

                        if (iconImg == null) {
                            iconImg = ImageIO.read(new File(AppData.getGraphicsWorkDir().getAbsolutePath() + File.separator + "default_icon.png"));
                        }
                    }
                } catch (IOException e) {
                }

                g = img.getGraphics();
                int timeY = 215;
                if (notif != null) {
                    //alert
                    switch (notif.severity) {
                        case ALARM:
                            g.setColor(new Color(244, 67, 54));
                            break;
                        case ALERT:
                            g.setColor(new Color(255, 152, 1));
                            break;
                        default:
                            g.setColor(new Color(96, 125, 39));
                            break;
                    }

                    g.fillRect(0, 251, 480, 320);
                    g.setColor(new Color(0, 0, 0));

                    if (iconImg != null) {
                        //Image iconResized = createResizedCopy(iconImg, 21, 30, true);
                        g.drawImage(iconImg, 25, 270, 21, 30, null);
                        // g.dispose();
                        //g.drawImage(iconResized, 25, 270, null);
                    }

                    g.setColor(new Color(255, 255, 255));
                    g.setFont(new Font("Tahoma", Font.PLAIN, 18));
                    g.drawString(Tools.getFormatedDate(notif.startDate, "HH:mm"), 55, 290);
                    g.drawString(notif.text, 125, 290);

                    timeY = 180;
                }
                //TIME
                g.setColor(new Color(0, 0, 0));
                g.setFont(new Font("Tahoma", Font.PLAIN, 110));
                g.drawString(Tools.getFormatedDate(new Date(), "HH:mm"), 100, timeY);
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

    /*
    public static Image transcodeSVGDocument( URL url, int x, int y ){
    // Create a PNG transcoder.
    Transcoder t = new PNGTranscoder();

    // Set the transcoding hints.
    t.addTranscodingHint( PNGTranscoder.KEY_WIDTH,  new Float(x) );
    t.addTranscodingHint( PNGTranscoder.KEY_HEIGHT, new Float(y) );

    // Create the transcoder input.
    TranscoderInput input = new TranscoderInput( url.toString() );

    ByteArrayOutputStream ostream = null;
    try {
        // Create the transcoder output.
        ostream = new ByteArrayOutputStream();
        TranscoderOutput output = new TranscoderOutput( ostream );

        // Save the image.
        t.transcode( input, output );

        // Flush and close the stream.
        ostream.flush();
        ostream.close();
    } catch( Exception ex ){
        ex.printStackTrace();
    }

    // Convert the byte stream into an image.
    byte[] imgData = ostream.toByteArray();
    Image img = Toolkit.getDefaultToolkit().createImage( imgData );

    // Return the newly rendered image.
    return img;
}
     */
    private static BufferedImage createResizedCopy(Image originalImage,
            int scaledWidth, int scaledHeight,
            boolean preserveAlpha) {
        System.out.println("resizing...");
        int imageType = preserveAlpha ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage scaledBI = new BufferedImage(scaledWidth, scaledHeight, imageType);
        Graphics2D g = scaledBI.createGraphics();
        if (preserveAlpha) {
            g.setComposite(AlphaComposite.Src);
        }
        g.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null);
        g.dispose();
        return scaledBI;
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
