/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi.modules;

import java.awt.Color;
import java.io.File;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.lobzik.home_sapiens.pi.AppData;
import org.lobzik.home_sapiens.pi.ConnJDBCAppender;
import org.lobzik.home_sapiens.pi.event.Event;
import org.lobzik.home_sapiens.pi.event.EventManager;
import org.lobzik.tools.StreamGobbler;
import org.lobzik.tools.Tools;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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
    
    
    private static final int sqS = 20;
    private static final double minDiff = 0.25;
    
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
    
    
  public static HashMap compareImages() {
      HashMap newImageHash = new HashMap();

      try{
        String basePath = AppData.getSoundWorkDir().getAbsolutePath() + File.separator +"video"+ File.separator;
        basePath = "C:\\tmp"+ File.separator;
        File firstI= new File( basePath + "image1.jpg");
        File secondI= new File(basePath + "image2.jpg");
        BufferedImage image1 = ImageIO.read(firstI);
        BufferedImage image2 = ImageIO.read(secondI);
        int differentParts= 0;

        
        for (int x = 0; x < image1.getWidth()-sqS; x=x+sqS) {
            for (int y = 0; y < image1.getHeight()-sqS; y=y+sqS) {
                
                long gs1=0;
                long gs2=0;
                /*
                long r1 =0;
                long g1 =0;
                long b1 =0;
                long r2 =0;
                long g2 =0;
                long b2 =0;
                double diffR =  ((double)r1)  / (double)r2;
                double diffG =  ((double)g1)  / (double)g2;
                double diffB =  ((double)b1)  / (double)b2;
                */
                for (int x1 = 0; x1 < sqS; x1++) {
                    for (int y1 = 0; y1 < sqS; y1++) {
                       Color c1 =  new Color(image1.getRGB(x+x1, y+y1));
                       Color c2 =  new Color(image2.getRGB(x+x1, y+y1));
                       gs1+= (c1.getRed() + c1.getGreen() + c1.getBlue())/3;
                       gs2+= (c2.getRed() + c2.getGreen() + c2.getBlue())/3;
                       /*
                       r1 += c1.getRed();
                       g1 += c1.getGreen();
                       b1 += c1.getBlue();
                       
                       r2 += c2.getRed();
                       g2 += c2.getGreen();
                       b2 += c2.getBlue();
                               */
                               
                    }
                }

                double diffGS =  ((double)gs1)  / (double)gs2;
                newImageHash.put("x"+x+"y"+y, (double)gs2);
                
                //if (diffR <1-minDiff || diffR >1+minDiff || diffG <1-minDiff || diffG >1+minDiff || diffB <1-minDiff || diffB >1+minDiff)
                if (diffGS <1-minDiff || diffGS >1+minDiff)
                {
                    for (int x1 = 0; x1 < sqS; x1++) {
                        for (int y1 = 0; y1 < sqS; y1++) {
                           if(x1==0 || y1==0 || x1==sqS-1 || y1==sqS-1)
                           {
                                image2.setRGB(x+x1, y+y1, 0);
                           }
                        }
                    }
                differentParts++;

                }
                       
//colors[x][y] = new Color(image1.getRGB(x, y));
            }
        }
                    File outputfile = new File(basePath + "image3.jpg");
                    ImageIO.write(image2, "jpg", outputfile);        
      }
      catch (Exception ee) 
      {
          System.out.printf(ee.toString());
      }
      return newImageHash;
  }
      

    public static class smuFfmpegRunner extends Thread {

        private Process process = null;
        private boolean run = true;
        private String camId = "1";

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
    }
}
