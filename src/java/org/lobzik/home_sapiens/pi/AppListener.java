/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi;

import java.io.File;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.lobzik.home_sapiens.pi.modules.ActualDataStorageModule;
import org.lobzik.home_sapiens.pi.modules.BehaviorModule;
import org.lobzik.home_sapiens.pi.modules.DBCleanerModule;
import org.lobzik.home_sapiens.pi.modules.DBDataWriterModule;
import org.lobzik.home_sapiens.pi.modules.DisplayModule;
import org.lobzik.home_sapiens.pi.modules.InternalSensorsModule;
import org.lobzik.home_sapiens.pi.modules.SpeakerModule;
import org.lobzik.home_sapiens.pi.modules.GraphModule;
import org.lobzik.home_sapiens.pi.modules.InstinctsModule;
import org.lobzik.home_sapiens.pi.modules.MicrophoneModule;
import org.lobzik.home_sapiens.pi.modules.ModemModule;
import org.lobzik.home_sapiens.pi.modules.SystemModule;
import org.lobzik.home_sapiens.pi.modules.TestModule;
import org.lobzik.home_sapiens.pi.modules.TimerModule;
import org.lobzik.home_sapiens.pi.modules.TunnelClientModule;
import org.lobzik.home_sapiens.pi.modules.VideoModule;
import org.lobzik.home_sapiens.pi.modules.WeatherModule;
import org.lobzik.home_sapiens.pi.modules.WebNotificationsModule;

/**
 * Web application lifecycle listener.
 *
 * @author lobzik
 */
@WebListener()
public class AppListener implements ServletContextListener {

    private static final Logger log = Logger.getRootLogger();

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        AppData.init();
        try {
            PatternLayout layout = new PatternLayout("%d{yyyy.MM.dd HH:mm:ss} %c{1} %-5p: %m%n");
            ConsoleAppender consoleAppender = new ConsoleAppender(layout);
            BasicConfigurator.configure(consoleAppender);
            log.info("Root Log init ok");
            log.info("Starting hs app. Modules start!");
            BoxSettingsAPI.initBoxSettings();
            BoxMode.initFromDB();

            AppData.setSoundWorkDir(new File(sce.getServletContext().getRealPath("sounds")));
            AppData.setGraphicsWorkDir(new File(sce.getServletContext().getRealPath("img")));
            AppData.setCaptureWorkDir(new File(sce.getServletContext().getRealPath("capture")));
            
            ActualDataStorageModule.getInstance().start();
            InstinctsModule.getInstance().start();
            InternalSensorsModule.getInstance().start();
            
            if (!BoxCommonData.TEST_MODE) {
                DisplayModule.getInstance().start();
                WebNotificationsModule.getInstance().start();
                DBDataWriterModule.getInstance().start();
                TimerModule.getInstance().start();
                DBCleanerModule.getInstance().start();
                GraphModule.getInstance().start();
                BehaviorModule.getInstance().start();
                TunnelClientModule.getInstance().start();
                WeatherModule.getInstance().start();
            }
            SpeakerModule.getInstance().start();
            VideoModule.getInstance().start();
            MicrophoneModule.getInstance().start();
            SystemModule.getInstance().start();

            ModemModule.getInstance().start();
            
            
            if (BoxCommonData.TEST_MODE) {
                TestModule.getInstance().start();
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            log.info("Context Destroyed called. Stopping application modules!");
            if (!BoxCommonData.TEST_MODE) {
                BehaviorModule.finish();
                TunnelClientModule.finish();
                TimerModule.finish();
                InternalSensorsModule.finish(); //only static methods works!!
                DBDataWriterModule.finish();
                DBCleanerModule.finish();
                SpeakerModule.finish();
                GraphModule.finish();
                VideoModule.finish();
                MicrophoneModule.finish();
                SystemModule.finish();
                ModemModule.finish();
                DisplayModule.finish();
                WeatherModule.finish();
                AppData.eventManager.finish();
            }
            BasicConfigurator.resetConfiguration();

        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

}
