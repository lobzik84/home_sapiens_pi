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
            String publicKey = new String(Files.readAllBytes(Paths.get(CommonData.PUBLIC_KEY_FILE)), "UTF-8");
            AppData.settings.put("public_key", publicKey);
            Properties hostapdConf = new Properties();
            hostapdConf.load(new FileInputStream(CommonData.HOSTAPD_CONFIG_FILE));
            String ssid = hostapdConf.getProperty("ssid");
            String wpa_psk = hostapdConf.getProperty("wpa_passphrase");
            AppData.settings.put("ssid", ssid);
            AppData.settings.put("wpa_psk", wpa_psk);
            AppData.tunnel.connect(CommonData.TUNNEL_SERVER_URL);
            //TODO start modules

        } catch (Exception ex) {
            Logger.getLogger(AppListener.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            AppData.tunnel.disconnect();

        } catch (Exception ex) {
            Logger.getLogger(AppListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private int readIdFile() throws Exception {
        if (!(new File(CommonData.BOX_ID_FILE)).exists()) {
            throw new Exception("No ID file found!! Register device first");
        }
        Properties props = new Properties();
        props.load(new FileInputStream(CommonData.BOX_ID_FILE));
        int boxId = Tools.parseInt(props.getProperty("box_id"), 0);
        if (boxId > 0) {
            return boxId;
        } else {
            throw new Exception("Invalid ID file!");
        }
    }
}
