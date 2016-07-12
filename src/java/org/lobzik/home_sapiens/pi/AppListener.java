/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.apache.log4j.Appender;
import org.apache.log4j.AsyncAppender;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.jdbc.JDBCAppender;
import org.apache.tomcat.dbcp.dbcp.BasicDataSource;
import org.lobzik.home_sapiens.pi.modules.DBDataWriterModule;
import org.lobzik.home_sapiens.pi.modules.InternalSensorsModule;

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

        try {
            PatternLayout layout = new PatternLayout("%d{yyyy.MM.dd HH:mm:ss} %c{1} %-5p: %m%n");
            ConsoleAppender consoleAppender = new ConsoleAppender(layout);
            BasicConfigurator.configure(consoleAppender);
            log.info("Root Log init ok");
            log.info("Starting hs app. Modules start!");

           // System.setProperty("gnu.io.rxtx.LibraryLoader", "true");

            //AppData.tunnel.connect();
            //TODO start modules
            AppData.eventManager.start();
            DBDataWriterModule.getInstance().start();
            InternalSensorsModule.getInstance().start();

        } catch (Throwable ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            log.info("Context Destroyed called. Stopping application modules!");
            //AppData.tunnel.disconnect();
            InternalSensorsModule.finish(); //only static methods works!!
            DBDataWriterModule.getInstance().finish();
            AppData.eventManager.finish();
            BasicConfigurator.resetConfiguration();

        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }
}
