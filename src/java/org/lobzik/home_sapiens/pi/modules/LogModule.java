/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi.modules;

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.lobzik.home_sapiens.pi.AppData;
import org.lobzik.home_sapiens.pi.ConnJDBCAppender;
import org.lobzik.home_sapiens.pi.behavior.Notification;
import org.lobzik.home_sapiens.pi.event.Event;
import org.lobzik.home_sapiens.pi.event.EventManager;

/**
 *
 * @author lobzik
 */
public class LogModule implements Module {

    public final String MODULE_NAME = this.getClass().getSimpleName();
    private static LogModule instance = null;
    private static Logger log = null;

    //для записи лога по reaction_events
    public static LogModule getInstance() {
        if (instance == null) {
            instance = new LogModule(); //lazy init
            log = Logger.getLogger(instance.MODULE_NAME);
            Appender appender = ConnJDBCAppender.getAppenderInstance(AppData.dataSource, instance.MODULE_NAME);
            log.addAppender(appender);
        }
        return instance;
    }

    private LogModule() {

    }

    @Override
    public String getModuleName() {
        return MODULE_NAME;
    }

    @Override
    public void start() {
        try {
            actionLog(Notification.Severity.INFO, "Система запущена", null);
            EventManager.subscribeForEventType(this, Event.Type.BEHAVIOR_EVENT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleEvent(Event e) {
        if (e.type == Event.Type.BEHAVIOR_EVENT && e.name.equals("log_record")) {
            Notification n = (Notification) e.data.get("Notification");
            if (n != null) {
                actionLog(n.severity, n.text, n.parameterAlias);
            }
        }
    }

    public static void actionLog(Notification.Severity severity, String mess, String alias) {
        String message;
        if (alias != null && alias.length() > 0) {
            message = "ALIAS:" + alias + ": " + mess;
        } else {
            message = mess;
        }
        switch (severity) {
            case INFO:
                log.info(message);
                break;

            case OK:
                log.warn(message);
                break;

            case ALERT:
                log.error(message);
                break;

            case ALARM:
                log.fatal(message);
                break;
        }
    }

}
