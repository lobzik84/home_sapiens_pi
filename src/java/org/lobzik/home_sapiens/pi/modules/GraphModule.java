/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi.modules;

import org.lobzik.home_sapiens.pi.event.Event;
import org.lobzik.home_sapiens.pi.event.EventManager;
public class GraphModule implements Module {

    public final String MODULE_NAME = this.getClass().getSimpleName();
    private static GraphModule instance = null;


    private GraphModule() { //singleton
    }

    public static GraphModule getInstance() {
        if (instance == null) {
            instance = new GraphModule(); //lazy init
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
            EventManager.subscribeForEventType(this, Event.Type.USER_ACTION);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleEvent(Event e) {
        if (e.type == Event.Type.USER_ACTION && e.name.equals("build-graph")) {
            
        }
    }
  

    public static void finish() {

    }
    
    

}