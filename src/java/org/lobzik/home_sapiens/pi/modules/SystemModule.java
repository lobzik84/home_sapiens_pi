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
            createRunFile();
            removeRebootFile();
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
                            } else {
                                Thread.sleep(3000);
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
        } else if (e.name.equals("modem_and_system_reboot")) {
            try {
                log.warn("Got reboot modem and system event!");
                createRebootFile();
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            log.info("Turning modem power off");
                            HashMap data = new HashMap();
                            data.put("uart_command", "modem=off");
                            Event e = new Event("internal_uart_command", data, Event.Type.USER_ACTION);
                            AppData.eventManager.lockForEvent(e, this);
                            Thread.sleep(5000);
                            log.info("Turning modem power on!");
                            data = new HashMap();
                            data.put("uart_command", "modem=on");
                            e = new Event("internal_uart_command", data, Event.Type.USER_ACTION);
                            AppData.eventManager.lockForEvent(e, this);
                            Thread.sleep(5000);
                            log.info("Now rebooting");
                            Tools.sysExec("sudo reboot", new File("/"));
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
        removeRunFile();
    }

    private static void removeRunFile() {
        try {
            Tools.sysExec("sudo rm " + AppData.RUN_FILE, new File("/"));
            log.info("Run file removed");
        } catch (Exception e) {
            log.error("Error while removing run file: " + e.getMessage());
        }
    }

    private static void createRunFile() {
        try {
            Tools.sysExec("sudo touch " + AppData.RUN_FILE, new File("/"));
            log.info("Run file created");
        } catch (Exception e) {
            log.error("Error while creating run file: " + e.getMessage());
        }
    }

    private static void removeRebootFile() {
        try {
            Tools.sysExec("sudo rm " + AppData.REBOOT_FILE, new File("/"));
            log.info("Reboot file removed");
        } catch (Exception e) {
            log.error("Error while removing reboot file: " + e.getMessage());
        }
    }

    private static void createRebootFile() {
        try {
            Tools.sysExec("sudo touch " + AppData.REBOOT_FILE, new File("/"));
            log.info("Reboot file created");
        } catch (Exception e) {
            log.error("Error while creating reboot file: " + e.getMessage());
        }
    }
    
    
}
