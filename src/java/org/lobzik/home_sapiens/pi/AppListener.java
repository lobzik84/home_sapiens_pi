/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.lobzik.home_sapiens.pi.modules.ActualDataStorageModule;
import org.lobzik.home_sapiens.pi.modules.DBCleanerModule;
import org.lobzik.home_sapiens.pi.modules.DBDataWriterModule;
import org.lobzik.home_sapiens.pi.modules.InternalSensorsModule;
import org.lobzik.home_sapiens.pi.modules.SpeakerModule;
import org.lobzik.home_sapiens.pi.modules.TimerModule;

/**
 * Web application lifecycle listener.
 *
 * @author lobzik
 */
@WebListener()
public class AppListener implements ServletContextListener {

    private static Logger log = Logger.getRootLogger();

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        AppData.init();
        try {
            PatternLayout layout = new PatternLayout("%d{yyyy.MM.dd HH:mm:ss} %c{1} %-5p: %m%n");
            ConsoleAppender consoleAppender = new ConsoleAppender(layout);
            BasicConfigurator.configure(consoleAppender);
            log.info("Root Log init ok");
            log.info("Starting hs app. Modules start!");

           // System.setProperty("gnu.io.rxtx.LibraryLoader", "true");

            //AppData.tunnel.connect();
            //TODO start modules

            DBDataWriterModule.getInstance().start();
            ActualDataStorageModule.getInstance().start();
            
            InternalSensorsModule.getInstance().start();
            TimerModule.getInstance().start();
            DBCleanerModule.getInstance().start();
            SpeakerModule.getInstance().start();
            

        } catch (Throwable ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            log.info("Context Destroyed called. Stopping application modules!");
            //AppData.tunnel.disconnect();
            TimerModule.finish();
            InternalSensorsModule.finish(); //only static methods works!!
            DBDataWriterModule.finish();
            DBCleanerModule.finish();
            SpeakerModule.finish();
                    
            
            AppData.eventManager.finish();
            BasicConfigurator.resetConfiguration();

        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }
}
