/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


package org.lobzik.home_sapiens.tunnel.client;

import java.util.HashMap;
import java.util.Map;


/**
 *
 * @author lobzik
 */
public class TunnelClient {
    private static TunnelClient instance = null; 
    private static final HashMap dataMap = new HashMap();
            
    public static final int NOT_CONNECTED_STATE = -1;
    public static final int CONNECTING_STATE = 0;
    public static final int CONNECTED_STATE = 1;
    
    private int state = NOT_CONNECTED_STATE;
    
    private TunnelClient(Map dataMap) { //singleton
        this.dataMap.putAll(dataMap);
    }
    
    public static TunnelClient getInstance(Map dataMap) {
        if (instance == null) 
            instance = new TunnelClient(dataMap); //lazy init
        return instance;
    }
    
    public void connect() {
        //TODO authenticate on server
        state = CONNECTING_STATE;
    }
    
    public void disconnect() {
        //TODO authenticate on server
        state = NOT_CONNECTED_STATE;
    }
    
    
    public void stop() {
        
    }
    
    public int getState() {
        return state;
    }
            
    
}
