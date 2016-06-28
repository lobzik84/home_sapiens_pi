/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi.tunnel.client;

import org.apache.log4j.Logger;

/**
 *
 * @author lobzik
 */
public class TunnelClient extends Thread {

    private static TunnelClient instance = null;

    public static final int NOT_CONNECTED_STATE = -1;
    public static final int CONNECTING_STATE = 0;
    public static final int CONNECTED_STATE = 1;

    private int state = NOT_CONNECTED_STATE;
    private static long connectTries = 0;
    private static Logger log = null;
    private static boolean run = true;
    private static String  tunnelServerUrl = null;
    private static String  authToken = null;
    
    private TunnelClient() { //singleton
    }

    public static TunnelClient getInstance() {
        if (instance == null) {
            instance = new TunnelClient(); //lazy init
        }
        return instance;
    }

    public static void connect(String url) {
        //TODO authenticate on server
        tunnelServerUrl = url;
        getInstance().start();
    }

    public synchronized void run() {
        while (run) {
            try {
                connectTries++;
                state = CONNECTING_STATE;
                
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
    }

    public synchronized void disconnect() {
        log.info("Disconnecting...");
        run = false;
        state = NOT_CONNECTED_STATE;

        synchronized (this) {
            notifyAll();

        }
        log.info("Disconnected.");
    }

    public int getTunnelState() {
        return state;
    }

    public void setLogger(Logger logger) {
        log = logger;
    }

}
