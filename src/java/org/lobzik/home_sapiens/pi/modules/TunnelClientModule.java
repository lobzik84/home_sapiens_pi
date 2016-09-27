/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi.modules;

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.lobzik.home_sapiens.pi.AppData;
import org.lobzik.home_sapiens.pi.BoxCommonData;
import org.lobzik.home_sapiens.pi.ConnJDBCAppender;
import org.lobzik.home_sapiens.pi.event.Event;
import org.lobzik.home_sapiens.pi.tunnel.client.TunnelClient;

/**
 *
 * @author lobzik
 */
public class TunnelClientModule extends Thread implements Module {

    public final String MODULE_NAME = this.getClass().getSimpleName();
    private static TunnelClientModule instance = null;
    private static TunnelClient client = null;
    private static final long WS_CHECK_PERIOD = 20 * 1000l;
    private static Logger log = null;
    private static boolean run = true;

    private TunnelClientModule() { //singleton
    }

    public static TunnelClientModule getInstance() {
        if (instance == null) {
            instance = new TunnelClientModule(); //lazy init
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
    public void run() {
        setName(this.getClass().getSimpleName() + "-Thread");
        log.info("Starting " + getName());
        while (run) {
            if (client == null || !client.isConnected() || System.currentTimeMillis() - client.getLastDataRecieved() > 3 * WS_CHECK_PERIOD) {
                if (client != null && client.isConnected()) {
                    log.info("WS Client Timeout. Disconnecting...");
                    client.disconnect();
                }
                log.info("Connecting to " + BoxCommonData.TUNNEL_SERVER_URL);
                try {
                client = new TunnelClient(BoxCommonData.TUNNEL_SERVER_URL);
                }
                catch (Exception e)
                {
                    log.error("Error while ws connecting: " + e.getMessage());
                }
            }
            synchronized (this) {
                try {
                    wait(WS_CHECK_PERIOD);
                } catch (Exception e) {
                }
            }
            if (client!=null && !client.isConnected())
                log.info("WS Client Disconnected.");
            if (run) {
                if (client!=null && client.isConnected() && System.currentTimeMillis() - client.getLastDataRecieved() >= WS_CHECK_PERIOD) {
                    try {
                        log.debug("No data for long time. Sending tt");
                        client.sendMessage("tt");
                    } catch (Exception e) {
                    }
                }
            }

        }

    }

    @Override
    public void handleEvent(Event e) {
    }

    public static void finish() {
        run = false;
        client.disconnect();
        synchronized (instance) {
            instance.notify();
        }
    }
}
