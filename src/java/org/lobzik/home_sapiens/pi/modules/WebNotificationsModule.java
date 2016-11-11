/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi.modules;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.lobzik.home_sapiens.pi.WebNotification;
import org.lobzik.home_sapiens.pi.AppData;
import org.lobzik.home_sapiens.pi.ConnJDBCAppender;
import org.lobzik.home_sapiens.pi.event.Event;
import org.lobzik.home_sapiens.pi.event.EventManager;
import org.lobzik.tools.Tools;

/**
 *
 * @author lobzik
 */
public class WebNotificationsModule implements Module {

    public final String MODULE_NAME = this.getClass().getSimpleName();
    private static WebNotificationsModule instance = null;
    private static Logger log = null;
    private final List<WebNotification> notifications = new LinkedList();
    
    public static final int MAX_NOTIFICATIONS = 10;
    
    
    private WebNotificationsModule() { //singleton
    }

    public static WebNotificationsModule getInstance() {
        if (instance == null) {
            instance = new WebNotificationsModule(); //lazy init
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
    public void start() {
        try {
            EventManager.subscribeForEventType(this, Event.Type.REACTION_EVENT);
            EventManager.subscribeForEventType(this, Event.Type.USER_ACTION);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleEvent(Event e) {
        switch (e.type) {
            case REACTION_EVENT:
                if (e.name.equals("web_notification")) {
                    WebNotification wn = (WebNotification) e.data.get("WebNotification");
                    if (wn != null) {
                        while (notifications.size() > MAX_NOTIFICATIONS) {
                            notifications.remove(0); //удаляем старые
                        }
                        notifications.add(wn);
                    }
                }
                break;

            case USER_ACTION:
                if (e.name.equals("delete_web_notification")) {
                    int wnId = Tools.parseInt(e.data.get("notification_id"), 0);
                    int index = -1;
                    for (int i = 0; i < notifications.size(); i++) {
                        WebNotification wn = notifications.get(i);
                        if (wnId == wn.id) {
                            index = i;
                            break;
                        }
                    }
                    if (index >= 0) {
                        notifications.remove(index);
                    }

                }
                break;
        }

    }

    public static List<WebNotification> getNotifications() {
        return instance.notifications;
    }
    
    public static void finish() {
        instance.notifications.clear();
    }

}
