/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.lobzik.home_sapiens.pi.modules.DBDataWriterModule;
import org.lobzik.home_sapiens.pi.modules.InternalSensorsModule;

/**
 * Web application lifecycle listener.
 *
 * @author lobzik
 */
@WebListener()
public class AppListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        try {
            System.out.println("Starting hs app. Modules start!");
           // System.setProperty("gnu.io.rxtx.LibraryLoader", "true");

            //AppData.tunnel.connect();
            //TODO start modules
            AppData.eventManager.start();
            DBDataWriterModule.getInstance().start();
            InternalSensorsModule.getInstance().start();

        } catch (Exception ex) {
            Logger.getLogger(AppListener.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            System.out.println("Context Destroyed called. Stopping application modules!");
            //AppData.tunnel.disconnect();
            InternalSensorsModule.finish(); //only static methods works!!
            DBDataWriterModule.getInstance().finish();
            AppData.eventManager.finish();

        } catch (Exception ex) {
            Logger.getLogger(AppListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
