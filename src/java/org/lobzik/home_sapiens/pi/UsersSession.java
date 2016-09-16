/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi;

import java.util.HashMap;

/**
 *
 * @author lobzik
 */
public class UsersSession extends HashMap {
    
    private long refreshTime = System.currentTimeMillis();
    
    public UsersSession() {
        super();
        refreshTime = System.currentTimeMillis();
    }
    
    public void updateRefreshTime() {
        refreshTime = System.currentTimeMillis();
    }
    
    public long getRefreshTime() {
        return refreshTime;
    }
    

}
