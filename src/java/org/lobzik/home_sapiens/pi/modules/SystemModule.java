/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi.modules;

import java.io.File;
import java.util.HashMap;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.lobzik.home_sapiens.pi.AppData;
import org.lobzik.home_sapiens.pi.ConnJDBCAppender;
import org.lobzik.home_sapiens.pi.event.Event;
import org.lobzik.home_sapiens.pi.event.EventManager;
import org.lobzik.tools.Tools;

/**
 *
 * @author lobzik
 */
public class SystemModule implements Module {

    public final String MODULE_NAME = this.getClass().getSimpleName();
    private static SystemModule instance = null;
    private Process process = null;
    private static Logger log = null;
    private static final String PREFIX = "/usr/bin/sudo";
    private static final String SHUTDOWN_COMMAND = "halt";
    private static final String SHUTDOWN_SUFFIX = "-p";
    private static final int SHUTDOWN_TIMEOUT = 30; //seconds for halt procedure

    private SystemModule() { //singleton
    }

    public static SystemModule getInstance() {
        if (instance == null) {
            instance = new SystemModule(); //lazy init
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
            EventManager.subscribeForEventType(this, Event.Type.SYSTEM_EVENT);
            EventManager.subscribeForEventType(this, Event.Type.BEHAVIOR_EVENT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleEvent(Event e) {
        if (e.name.equals("shutdown")) {
            try {

                new Thread() {
                    @Override
                    public void run() {
                        try {
                            if (e.type == Event.Type.BEHAVIOR_EVENT) {
                                Thread.sleep(30000);//чтобы успели разлететься смс-ки и остальное
                            }
                            HashMap data = new HashMap();
                            data.put("uart_command", "poweroff=" + SHUTDOWN_TIMEOUT); //timer for SHUTDOWN_TIMEOUT secs
                            Event e = new Event("internal_uart_command", data, Event.Type.USER_ACTION);
                            AppData.eventManager.lockForEvent(e, this);
                            log.info("Now shutting down");
                            Tools.sysExec("sudo halt -p", new File("/"));
                        } catch (Exception e) {
                            log.error(e.getMessage());
                        }
                    }
                }.start();
            } catch (Exception ee) {
                log.error("Error " + ee.getMessage());
            }
        }
    }

    public static void finish() {

    }

}
