/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.lobzik.home_sapiens.pi.modules.InternalSensorsModule;
import org.lobzik.tools.Tools;

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
           // System.setProperty("gnu.io.rxtx.LibraryLoader", "true");
            
            //AppData.tunnel.connect();
            //TODO start modules
           AppData.internalSensorsModule.start();
            

        } catch (Exception ex) {
            Logger.getLogger(AppListener.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            //AppData.tunnel.disconnect();
            AppData.internalSensorsModule.finish(); //only static methods works!!
            

        } catch (Exception ex) {
            Logger.getLogger(AppListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
