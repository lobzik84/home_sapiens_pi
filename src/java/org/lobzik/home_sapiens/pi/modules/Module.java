/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi.modules;

import org.lobzik.home_sapiens.pi.event.Event;

/**
 *
 * @author lobzik
 */
public interface Module {
    
    public String getModuleName();
    public void handleEvent(Event e);
    public void start();
    public static void finish() {};
    
}
