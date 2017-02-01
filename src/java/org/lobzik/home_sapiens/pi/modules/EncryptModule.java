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
import org.lobzik.home_sapiens.pi.event.Event;
import org.lobzik.home_sapiens.pi.event.EventManager;
import org.lobzik.tools.Tools;
import java.security.SecureRandom;

/**
 *
 * @author Nikolas
 */
public class EncryptModule implements Module {
        public final String MODULE_NAME = this.getClass().getSimpleName();
    private static EncryptModule instance = null;
    private static Logger log = null;
    
    //для записи лога по reaction_events
    public static EncryptModule getInstance() {
        if (instance == null) {
            instance = new EncryptModule(); //lazy init
            log = Logger.getLogger(instance.MODULE_NAME);
            Appender appender = ConnJDBCAppender.getAppenderInstance(AppData.dataSource, instance.MODULE_NAME);
            log.addAppender(appender);
        }
        return instance;
    }

    private EncryptModule() {

    }

    @Override
    public String getModuleName() {
        return MODULE_NAME;
    }

    @Override
    public void start() {
        try {
            EventManager.subscribeForEventType(this, Event.Type.BEHAVIOR_EVENT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleEvent(Event e) {
        if (e.type == Event.Type.BEHAVIOR_EVENT && e.name.equals("do_backup")) {
            try {
                log.info("Creating database backup");
                if (!TunnelClientModule.getInstance().tunnelIsUp()) {
                    throw new Exception ("No server link");
                }
                String boxSessionKey = TunnelClientModule.getInstance().getBoxSessionKey();

                new Thread() {
                    @Override
                    public void run() {
                        try {
                            Tools.sysExec("mysqldump", AppData.getCaptureWorkDir()); //todo
                            // encrypt and upload to server
                            log.info("Backup created");
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

    public String generateUid(){
        String uid_alphabet = "1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String uid = "";
        int uid_length = 16;
        for (int i = 0; i < uid_length; i++) {
            int rnd = (int)(Math.random() *  uid_alphabet.length());
            String ch = uid_alphabet.substring(rnd ,1);
            uid += ch;
        }
    return uid;
    }
    
}
