/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi.modules;

import org.lobzik.home_sapiens.pi.event.Event;

/**
 * Implement internal logics of events and data handling
 * @author lobzik
 */
public class LogicsModule implements Module {

    public final String MODULE_NAME = this.getClass().getSimpleName();
        @Override
    public String getModuleName() {
        return MODULE_NAME;
    }

    @Override
    public void start() {

    }

    @Override
    public void handleEvent(Event e) {
    }

    public static void finish() {

    }
    
}
