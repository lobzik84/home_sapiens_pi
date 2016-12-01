/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi.modules;

import java.net.URL;
import java.sql.Connection;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import javax.naming.Context;
import javax.naming.InitialContext;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.lobzik.home_sapiens.pi.AppData;
import org.lobzik.home_sapiens.pi.BoxCommonData;
import org.lobzik.home_sapiens.pi.BoxSettingsAPI;
import org.lobzik.home_sapiens.pi.ConnJDBCAppender;
import org.lobzik.home_sapiens.pi.behavior.Notification;
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

    public String getBoxSessionKey() {
        return client.getBoxSessionKey();
    }

    @Override
    public void run() {
        setName(this.getClass().getSimpleName() + "-Thread");
        log.info("Starting " + getName());
        EventManager.subscribeForEventType(this, Event.Type.SYSTEM_EVENT);
        EventManager.subscribeForEventType(this, Event.Type.BEHAVIOR_EVENT);
        EventManager.subscribeForEventType(this, Event.Type.TIMER_EVENT);
        while (run) {
            if (client == null || !client.isConnected() || System.currentTimeMillis() - client.getLastDataRecieved() > 3 * WS_CHECK_PERIOD) {
                if (client != null && client.isConnected()) {
                    log.info("WS Client Timeout. Disconnecting...");
                    client.disconnect();
                }
                log.info("Connecting to " + BoxCommonData.TUNNEL_SERVER_URL);
                try {
                    Context initCtx = new InitialContext();
                    Context envCtx = (Context) initCtx.lookup("java:comp/env");
                    client = new TunnelClient(BoxCommonData.TUNNEL_SERVER_URL, log, envCtx);
                } catch (Exception e) {
                    log.error("Error while ws connecting: " + e.getMessage());
                    if (client != null) {
                        client.disconnect();
                    }
                    client = null;
                    //e.printStackTrace();
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
                Event ev = new Event("tunnel_connection_lost", null, Event.Type.SYSTEM_EVENT);
                AppData.eventManager.newEvent(ev);
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
                    case "tunnel_connected":
                        try {
                            log.info("Users sync");
                            uploadUnsyncedUsers();
                        } catch (Exception ee) {
                            log.error("Error on user sync: " + ee.getMessage());
                        }
                        break;
                }
                break;
            case BEHAVIOR_EVENT:
                if (e.name.equals("send_email")) {
                    if (client != null && client.isConnected()) {
                        try {
                            JSONObject json = new JSONObject();
                            json.put("box_id", BoxCommonData.BOX_ID);
                            json.put("action", "send_email");
                            json.put("mail_to", e.data.get("mail_to"));
                            json.put("mail_text", e.data.get("mail_text"));
                            json.put("mail_subject", e.data.get("mail_subject"));
                            log.debug("Sending email to " + e.data.get("mail_to"));
                            client.sendMessage(json);
                        } catch (Exception ee) {
                            log.debug("Error on send msg: " + ee.getMessage());
                        }
                    }

                } else if (e.name.equals("send_email_notification")) {
                    String email = BoxSettingsAPI.get("NotificationsEmail");
                    if (client != null && client.isConnected() && email != null && email.indexOf("@") > 0) {
                        try {
                            Notification n = (Notification) e.data.get("Notification");
                            AppData.emailNotification.put(n.id, n);
                            //get HTML
                            String html = n.text;
                            String severity = "";
                            switch (n.severity) {
                                case ALARM:
                                    severity = "Тревога";
                                    break;
                                case ALERT:
                                    severity = "Предупреждение";
                                    break;
                                case OK:
                                    severity = "ОК";
                                    break;
                                case INFO:
                                    severity = "Уведомление";
                                    break;
                            }
                            try {
                                html = Tools.getFromUrl(new URL(AppData.getLocalUrlContPath() + "/email/notification.jsp?id=" + n.id));
                            } catch (Exception ee) {
                            }
                            JSONObject json = new JSONObject();
                            json.put("box_id", BoxCommonData.BOX_ID);
                            json.put("action", "send_email");
                            json.put("mail_to", email);
                            json.put("mail_text", html); //TODO render JSP template
                            json.put("mail_subject", "Управдом " + BoxSettingsAPI.get("BoxName") + ". " + severity);
                            log.debug("Sending email to " + email);
                            client.sendMessage(json);
                        } catch (Exception ee) {
                            log.debug("Error on send msg: " + ee.getMessage());
                        }
                    }

                }
                break;
            case TIMER_EVENT:
                if (e.name.equals("send_statistics")) {
                    
                    Calendar c = new GregorianCalendar();
                    boolean doSendStat = (c.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) && ("true".equals(BoxSettingsAPI.get("StatToEmailScript")));
                    String email = BoxSettingsAPI.get("NotificationsEmail");
                    if (doSendStat && client != null && client.isConnected() && email != null && email.indexOf("@") > 0) {
                        try {
                            //get HTML
                            long to = System.currentTimeMillis();
                            long from = to - 7 * 24 * 3600 * 1000L;

                            String dateFrom = Tools.getFormatedDate(new Date(from), "dd.MM.YYYY");
                            String dateTo = Tools.getFormatedDate(new Date(to), "dd.MM.YYYY");
                            String url = AppData.getLocalUrlContPath() + "/email/statistics.jsp?moduleName=LogModule";
                            url += "&from=" + from;
                            url += "&to=" + to;
                            String html = Tools.getFromUrl(new URL(url));

                            JSONObject json = new JSONObject();
                            json.put("box_id", BoxCommonData.BOX_ID);
                            json.put("action", "send_email");
                            json.put("mail_to", email);
                            json.put("mail_text", html); //TODO render JSP template
                            json.put("mail_subject", "Управдом - " + BoxSettingsAPI.get("BoxName") + ". Отчет за период: " + dateFrom + " - " + dateTo);
                            log.debug("Sending email to " + email);
                            client.sendMessage(json);
                        } catch (Exception ee) {
                            log.debug("Error on send msg: " + ee.getMessage());
                        }
                    }
                    break;
                }

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
