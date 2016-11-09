/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi.modules;

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.lobzik.home_sapiens.pi.AppData;
import org.lobzik.home_sapiens.pi.BoxCommonData;
import org.lobzik.home_sapiens.pi.ConnJDBCAppender;
import org.lobzik.home_sapiens.pi.event.Event;
import org.lobzik.home_sapiens.pi.event.EventManager;
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

    public boolean tunnelIsUp() {
        if (client != null && client.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void run() {
        setName(this.getClass().getSimpleName() + "-Thread");
        log.info("Starting " + getName());
        EventManager.subscribeForEventType(this, Event.Type.SYSTEM_EVENT);
        while (run) {
            if (client == null || !client.isConnected() || System.currentTimeMillis() - client.getLastDataRecieved() > 3 * WS_CHECK_PERIOD) {
                if (client != null && client.isConnected()) {
                    log.info("WS Client Timeout. Disconnecting...");
                    client.disconnect();
                }
                log.info("Connecting to " + BoxCommonData.TUNNEL_SERVER_URL);
                try {
                    client = new TunnelClient(BoxCommonData.TUNNEL_SERVER_URL, log);
                } catch (Exception e) {
                    log.error("Error while ws connecting: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            synchronized (this) {
                try {
                    wait(WS_CHECK_PERIOD);
                } catch (Exception e) {
                }
            }
            if (client != null && !client.isConnected()) {
                log.info("WS Client Disconnected.");
            }
            if (run) {
                if (client != null && client.isConnected() && System.currentTimeMillis() - client.getLastDataRecieved() >= WS_CHECK_PERIOD) {
                    try {
                        log.debug("No data for long time. Sending tt");
                        client.sendTT();
                    } catch (Exception e) {
                        log.debug("Error on tt: " + e.getMessage());
                    }
                }
            }

        }

    }

    @Override
    public void handleEvent(Event e) {
        if (e.name.equals("msg_to_server")) {
            if (client != null && client.isConnected()) {
                try {
                    JSONObject json = (JSONObject) e.data.get("json");
                    log.debug("Sending to server " + json.toString().length() + " bytes");
                    client.sendMessage(json);
                } catch (Exception ee) {
                    log.debug("Error on send msg: " + ee.getMessage());
                }
            }
        }
    }

    public static void finish() {
        run = false;
        client.disconnect();
        synchronized (instance) {
            instance.notify();
        }
    }
}
