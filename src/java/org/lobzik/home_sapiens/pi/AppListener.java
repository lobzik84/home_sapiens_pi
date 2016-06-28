/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi;

import java.io.FileInputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
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
        
        int boxId;
        try {
            boxId = readIdFile();
            AppData.settings.put("box_id", boxId);
            AppData.tunnel.connect(CommonData.TUNNEL_SERVER_URL);
            
        } catch (Exception ex) {
            Logger.getLogger(AppListener.class.getName()).log(Level.SEVERE, null, ex);
        }
        

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }

    private int readIdFile() throws Exception {
        Properties props = new Properties();
        props.load(new FileInputStream(CommonData.BOX_ID_FILE));
        int boxId = Tools.parseInt(props.getProperty("box_id"), 0);
        if (boxId > 0)
            return boxId;
        else
            throw new Exception("Invalid ID file!");
    }
}
