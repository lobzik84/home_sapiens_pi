/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi.modules;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.lobzik.home_sapiens.pi.AppData;
import org.lobzik.home_sapiens.pi.BoxCommonData;
import org.lobzik.home_sapiens.pi.ConnJDBCAppender;
import org.lobzik.home_sapiens.pi.event.Event;
import org.lobzik.home_sapiens.pi.event.EventManager;
import org.lobzik.home_sapiens.pi.tunnel.client.TunnelClient;
import org.lobzik.tools.Tools;
import org.lobzik.tools.db.mysql.DBSelect;
import org.lobzik.tools.db.mysql.DBTools;

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
        EventManager.subscribeForEventType(this, Event.Type.REACTION_EVENT);
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
        switch (e.type) {
            case SYSTEM_EVENT:
                switch (e.name) {
                    case "msg_to_server":
                        if (client != null && client.isConnected()) {
                            try {
                                JSONObject json = (JSONObject) e.data.get("json");
                                log.debug("Sending to server " + json.toString().length() + " bytes");
                                client.sendMessage(json);
                            } catch (Exception ee) {
                                log.debug("Error on send msg: " + ee.getMessage());
                            }
                        }
                        break;

                    case "upload_user_to_server":
                        try {
                            int userId = Tools.parseInt(e.data.get("userId"), 0);
                            log.info("Uploading user id=" + userId);
                            uploadUserToServer(userId);
                        } catch (Exception ee) {
                            log.error("Error on user sync: " + ee.getMessage());
                        }

                        break;

                    case "upload_unsynced_users_to_server":
                        try {
                            log.info("Users sync");
                            uploadUnsyncedUsers();
                        } catch (Exception ee) {
                            log.error("Error on user sync: " + ee.getMessage());
                        }
                        break;
                }
                break;
            case REACTION_EVENT:
                if (e.name.equals("send_email")) {
                    if (client != null && client.isConnected()) {
                        try {
                            JSONObject json = new JSONObject();
                            json.put("box_id", BoxCommonData.BOX_ID);
                            json.put("action", "send_email");
                            json.put("mail_to", e.data.get("mail_to"));
                            json.put("mail_text", e.data.get("mail_text"));
                            json.put("mail_subject", e.data.get("mail_subject"));
                            log.debug("Sending email to " +  e.data.get("mail_to"));
                            client.sendMessage(json);
                        } catch (Exception ee) {
                            log.debug("Error on send msg: " + ee.getMessage());
                        }
                    }
                    break;
                }
                break;
        }

    }

    private void uploadUnsyncedUsers() throws Exception {

        String sSQL = "select * from users where status = 1 and synced=0";
        try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {
            List<HashMap> resList = DBSelect.getRows(sSQL, conn);
            log.info("Syncing " + resList.size() + " users");
            for (HashMap userMap : resList) {

                JSONObject json = new JSONObject();
                json.put("box_id", BoxCommonData.BOX_ID);
                json.put("action", "user_data_upload");
                for (String key : (Set<String>) userMap.keySet()) {
                    json.put(key, userMap.get(key));
                }
                client.sendMessage(json);
            }
        }
    }

    private void uploadUserToServer(int userId) throws Exception {

        String sSQL = "select * from users where status = 1 and id = " + userId;
        try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {
            List<HashMap> resList = DBSelect.getRows(sSQL, conn);
            if (resList.isEmpty()) {
                throw new Exception("User id=" + userId + " not found");
            }
            HashMap userMap = resList.get(0);
            JSONObject json = new JSONObject();
            json.put("box_id", BoxCommonData.BOX_ID);
            json.put("action", "user_data_upload");
            for (String key : (Set<String>) userMap.keySet()) {
                json.put(key, userMap.get(key));
            }
            client.sendMessage(json);

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
