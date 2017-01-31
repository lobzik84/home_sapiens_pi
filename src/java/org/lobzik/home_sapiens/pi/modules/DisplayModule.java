/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi.modules;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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
import org.lobzik.home_sapiens.entity.Measurement;
import org.lobzik.home_sapiens.entity.Parameter;
import static org.lobzik.home_sapiens.pi.AppData.measurementsCache;
import org.lobzik.home_sapiens.pi.BoxCommonData;
import org.lobzik.home_sapiens.pi.BoxMode;
import org.lobzik.home_sapiens.pi.BoxSettingsAPI;
import org.lobzik.home_sapiens.pi.ConnJDBCAppender;
import org.lobzik.home_sapiens.pi.behavior.Notification;
import org.lobzik.home_sapiens.pi.event.EventManager;
import org.lobzik.home_sapiens.pi.weather.entity.Forecast;
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

    private static final long MAXTIMEDIFF = 12 * 3600 * 1000l; //forecast older then 12 h не катит
    private static final Stack<Notification> notifications = new Stack();
    private static final int MAX_STACK_SIZE = 10;

    private static final long NEXTFORECASTTIMEDIFF = 12 * 3600 * 1000l; //прогноз на через 12 часов
    private static List<Forecast> forecasts = null;

    private static final Color DAY_FONT_COLOR = new Color(255, 255, 255);
    private static final Color NIGHT_FONT_COLOR = new Color(180, 180, 180);

    private static final Font FONT_SMALL = new Font("Roboto Regular", Font.BOLD, 20);
    private static final Font FONT_SMALL_FOR = new Font("Roboto Regular", Font.BOLD, 22);
    private static final Font NOTIFICATION_FONT = new Font("Roboto Regular", Font.PLAIN, 15);
    private static final int LUM_SENSOR_TIMEOUT = 3; // для датчика освещённости
    private static final double LUM_SENSOR_HYSTEREZIS = 1.5;// для датчика освещённости

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
            EventManager.subscribeForEventType(this, Event.Type.BEHAVIOR_EVENT);
            EventManager.subscribeForEventType(this, Event.Type.SYSTEM_MODE_CHANGED);

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
                } else if (e.name.equals("forecast_loaded")) {
                    forecasts = (List<Forecast>) e.data.get("forecast");
                    draw();
                }
                break;

            case SYSTEM_MODE_CHANGED:
                draw();
                break;

            case BEHAVIOR_EVENT:
                if (e.name.equals("display_notification")) {
                    Notification n = (Notification) e.data.get("Notification");
                    //String conditionAlias = (String) e.data.get("ConditionAlias");
                    //Integer state = (Integer)e.data.get("ConditionState");
                    if (n != null && n.conditionState != null && n.conditionState == 1) {
                        while (notifications.size() > MAX_STACK_SIZE) {
                            notifications.remove(notifications.size() - 1);
                        }
                        notifications.push(n);
                        draw();
                    } else if (n != null) {
                        int index = -1;

                        for (int i = 0; i < notifications.size(); i++) {
                            Notification d = notifications.get(i);
                            if (d.conditionAlias.equals(n.conditionAlias)) {
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
            if (BoxMode.isArmed()) {
                img = ImageIO.read(new File(AppData.getGraphicsWorkDir().getAbsolutePath() + File.separator + "background_armed.jpg"));
                // g = img.getGraphics();

            } else {
                if (BoxCommonData.TEST_MODE) {
                    img = ImageIO.read(new File(AppData.getGraphicsWorkDir().getAbsolutePath() + File.separator + "background_night.jpg"));
                    g = img.getGraphics();
                    g.setColor(new Color(255, 255, 255));
                    g.setFont(new Font("Roboto Regular", Font.BOLD, 81));
                    g.drawString("TEST", 100, 180);

                } else {
                    Parameter p = AppData.parametersStorage.getParameterByAlias("MODEM_RSSI");
                    Measurement m = AppData.measurementsCache.getLastMeasurement(p);

                    int rssi = -101;//сигнал сети, дБ. <100 нет фишек, 100<rssi<90 одна фишка, 90<rssi<80 две фишки, 80<rssi<70 три фишки, >70 четыре фишки
                    if (m != null) {
                        rssi = (int) (double) m.getDoubleValue();
                    }

                    boolean nightTime = false; //ночью true, при этом ночной фон и иконки погоды ночные!

                    p = AppData.parametersStorage.getParameterByAlias("NIGHTTIME");
                    m = AppData.measurementsCache.getLastMeasurement(p);
                    if (m != null) {
                        nightTime = m.getBooleanValue();
                    }
                    p = AppData.parametersStorage.getParameterByAlias("LUMIOSITY");

                    Measurement mMax = measurementsCache.getMaxMeasurementFrom(p, System.currentTimeMillis() - 1000 * 60 * LUM_SENSOR_TIMEOUT);
                    Measurement mMin = measurementsCache.getMinMeasurementFrom(p, System.currentTimeMillis() - 1000 * 60 * LUM_SENSOR_TIMEOUT);

                    boolean darkRoom = false;
                    if (mMin != null && mMax != null) {
                        if (mMin.getDoubleValue() > BoxSettingsAPI.getDouble("LumiosityDarkLevel") * LUM_SENSOR_HYSTEREZIS) {
                            darkRoom = false;
                        } else if (mMax.getDoubleValue() < BoxSettingsAPI.getDouble("LumiosityDarkLevel")) {
                            darkRoom = true;
                        }
                    }

                    long forecastForTime = System.currentTimeMillis() + NEXTFORECASTTIMEDIFF;
                    Forecast next = null;
                    Forecast actual = null;
                    if (forecasts != null) {

                        long diff = System.currentTimeMillis();
                        for (Forecast f : forecasts) {
                            //searching for closest forecast

                            long timeDiff = f.getTime().getTime() - forecastForTime;
                            if (timeDiff < 0) {
                                timeDiff = timeDiff * -1;
                            }
                            if (timeDiff < MAXTIMEDIFF && timeDiff < diff) {
                                diff = timeDiff;
                                next = f;
                            }
                        }

                        diff = System.currentTimeMillis();
                        for (Forecast f : forecasts) {
                            //searching for closest forecast

                            long timeDiff = f.getTime().getTime() - System.currentTimeMillis();
                            if (timeDiff < 0) {
                                timeDiff = timeDiff * -1;
                            }
                            if (timeDiff < MAXTIMEDIFF && timeDiff < diff) {
                                diff = timeDiff;
                                actual = f;
                            }
                        }
                    }
                    Double outsideTempNow = null; //если null - не рисуем,это прогноз на +12 часов
                    Integer cloudsNow = null;//если null - не рисуем, это прогноз на +12 часов                
                    Double rainNow = null;
                    
                    if (actual != null) {
                        outsideTempNow = actual.getTemperature();
                        cloudsNow = actual.getClouds();
                        rainNow = actual.getPrecipitation();
                    }
                    
                    Double outsideTempNext = null; //если null - не рисуем,это прогноз на +12 часов
                    Integer cloudsNext = null;//если null - не рисуем, это прогноз на +12 часов                
                    Double rainNext = null;

                    if (next != null) {
                        outsideTempNext = next.getTemperature();
                        cloudsNext = next.getClouds();
                        rainNext = next.getPrecipitation();
                    }

                    String modemMode = "4G";//Режим сети. приедет от модема

                    String[] nextForecastFor = {"вечер", "завтра"}; //если текущее время до 12.00 дня - пишем прогноз на "вечер", если после - на "завтра". для случая, когда рисуем прогноз на вечер - берём ночные иконки!
                    String[] weekDays = {"воскресенье", "понедельник", "вторник", "среда", "четверг", "пятница", "суббота"};
                    String[] yearMonths = {"января", "февраля", "марта", "апреля", "мая", "июня", "июля", "августа", "сентября", "октября", "ноября", "декабря"};

                    Color fontColor = DAY_FONT_COLOR;
                    //как выбрать иконку для погоды? всего 7 вариантов png
                    Notification notif = null;

                    if (!notifications.isEmpty()) {
                        notif = notifications.peek();
                    }

                    try {
                        if (darkRoom) {
                            fontColor = NIGHT_FONT_COLOR;
                            img = ImageIO.read(new File(AppData.getGraphicsWorkDir().getAbsolutePath() + File.separator + "background_night.jpg"));
                        } else {
                            fontColor = DAY_FONT_COLOR;
                            img = ImageIO.read(new File(AppData.getGraphicsWorkDir().getAbsolutePath() + File.separator + "background_day.jpg"));
                        }

                        if (notif != null) {
                            try {
                                iconImg = ImageIO.read(new File(AppData.getGraphicsWorkDir().getAbsolutePath() + File.separator + notif.parameterAlias + ".png"));
                            } catch (Exception e) {
                            }

                            if (iconImg == null) {
                                iconImg = ImageIO.read(new File(AppData.getGraphicsWorkDir().getAbsolutePath() + File.separator + "DEFAULT.png"));
                            }
                        }
                    } catch (IOException e) {
                    }

                    g = img.getGraphics();

                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(
                            RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

                    // You can also enable antialiasing for text:
                    g2d.setRenderingHint(
                            RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                    if (notif != null) {
                        //alert
                        switch (notif.severity) {
                            case INFO:
                                g.setColor(new Color(96, 125, 139));
                                break;
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

                        g.fillRect(0, 266, 480, 320);
                        g.setColor(new Color(0, 0, 0));

                        if (iconImg != null) {

                            g.drawImage(iconImg, 10, 280, 24, 24, null);
                        }

                        g.setColor(DAY_FONT_COLOR);
                        g.setFont(NOTIFICATION_FONT);
                        g.drawString(Tools.getFormatedDate(notif.startDate, "HH:mm"), 45, 290);
                        g.drawString(notif.text, 45, 306);

                    }
                    //TIME
                    g.setColor(fontColor);
                    g.setFont(new Font("Roboto Regular", Font.BOLD, 81));
                    g.drawString(Tools.getFormatedDate(new Date(), "HH:mm"), 15, 169);

                    //Date
                    String dateString = weekDays[new GregorianCalendar().get(Calendar.DAY_OF_WEEK) - 1];
                    dateString += ", " + Tools.getFormatedDate(new Date(), "dd") + " " + yearMonths[new GregorianCalendar().get(Calendar.MONTH)];
                    g.setColor(fontColor);
                    g.setFont(FONT_SMALL);
                    g.drawString(dateString, 17, 205);

                    //Modem
                    g.setColor(fontColor);
                    g.setFont(new Font("Roboto Regular", Font.BOLD, 16));
                    g.drawString(modemMode, 425, 40);

                    //rssi
                    g.setColor(new Color(255, 255, 255, 80));
                    g.fillRect(456, 36, 3, 4);
                    g.fillRect(460, 32, 3, 8);
                    g.fillRect(464, 28, 3, 12);
                    g.fillRect(468, 24, 3, 16);

                    //int rssi = -75;//сигнал сети, дБ. <100 нет фишек, 100<rssi<90 одна фишка, 90<rssi<80 две фишки, 80<rssi<70 три фишки, >70 четыре фишки
                    if (rssi <= -100) {
                        g2d.setColor(new Color(255, 0, 0));
                        g2d.setStroke(new BasicStroke(2.0f));
                        g2d.drawOval(456, 25, 14, 14);
                        g2d.drawLine(458, 28, 468, 36);
                    }
                    g.setColor(fontColor);
                    if (rssi > -100) {
                        g.fillRect(456, 36, 3, 4);
                    }
                    if (rssi > -90) {
                        g.fillRect(460, 32, 3, 8);
                    }
                    if (rssi > -80) {
                        g.fillRect(464, 28, 3, 12);
                    }
                    if (rssi > -70) {
                        g.fillRect(468, 24, 3, 16);
                    }

                    if (outsideTempNext != null && cloudsNext != null) {
                        g.setColor(fontColor);
                        g.setFont(FONT_SMALL_FOR);
                        g.drawString(nextForecastFor[(new GregorianCalendar().get(Calendar.HOUR_OF_DAY)) < 12 ? 0 : 1], 306, 205);

                        g.setFont(FONT_SMALL_FOR);
                        if (outsideTempNext < 0) {
                            g.drawString("-", 431, 205);
                        }

                        g.drawString(Math.abs(outsideTempNext.intValue()) + "°", 440, 205);

                        String imgName = "weather-";
                        if (rainNext == null || rainNext <= 0.05) {
                            if (cloudsNext <= 20) {
                                imgName += (new GregorianCalendar().get(Calendar.HOUR_OF_DAY)) < 12 ? "night" : "sun";
                            } else if (cloudsNext > 20 && cloudsNext < 70) {
                                imgName += ((new GregorianCalendar().get(Calendar.HOUR_OF_DAY)) < 12 ? "night" : "sun") + "-cloudly";
                            } else if (cloudsNext >= 70) {
                                imgName += "cloudly";
                            }
                        } else {
                            imgName += outsideTempNext > 0 ? "rain" : "snow";
                        }

                        Image icn = null;
                        try {
                            icn = ImageIO.read(new File(AppData.getGraphicsWorkDir().getAbsolutePath() + File.separator + imgName + ".png"));
                        } catch (Exception e) {
                        }
                        if (icn != null) {
                            int pHeight = 24;
                            int w = icn.getWidth(null);
                            int h = icn.getHeight(null);
                            g.drawImage(icn, 406 - (w * pHeight / h) / 2, 188, w * pHeight / h, pHeight, null);
                        }
                    }

                    if (outsideTempNow != null && cloudsNow != null) {
                        g.setFont(new Font("Roboto Regular", Font.BOLD, 42));
                        if (outsideTempNow < 0) {
                            g.drawString("-", 393, 136);
                        }

                        g.drawString(Math.abs(outsideTempNow.intValue()) + "°", 411, 136);

                        String imgName = "weather-";
                        if (rainNow == null || rainNow <= 0.05) {
                            if (cloudsNow <= 20) {
                                imgName += nightTime ? "night" : "sun";
                            } else if (cloudsNow > 20 && cloudsNext < 70) {
                                imgName += nightTime ? "night" : "sun" + "-cloudly";
                            } else if (cloudsNow >= 70) {
                                imgName += "cloudly";
                            }
                        } else {
                            imgName += outsideTempNow > 0 ? "rain" : "snow";
                        }
                        Image icn = null;
                        try {
                            icn = ImageIO.read(new File(AppData.getGraphicsWorkDir().getAbsolutePath() + File.separator + imgName + ".png"));
                        } catch (Exception e) {
                        }

                        if (icn != null) {
                            int pHeight = 60;
                            int w = icn.getWidth(null);
                            int h = icn.getHeight(null);
                            g.drawImage(icn, 336 - (w * pHeight / h) / 2, 109, w * pHeight / h, pHeight, null);
                        }
                    }
                }
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
    private static BufferedImage createResizedCopy(Image originalImage, int scaledWidth, int scaledHeight, boolean preserveAlpha) {
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
