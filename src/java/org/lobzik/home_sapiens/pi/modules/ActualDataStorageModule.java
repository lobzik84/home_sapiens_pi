/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi.modules;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import org.lobzik.home_sapiens.entity.Measurement;
import org.lobzik.home_sapiens.entity.Parameter;
import org.lobzik.home_sapiens.pi.AppData;
import org.lobzik.home_sapiens.pi.event.Event;
import org.lobzik.home_sapiens.pi.event.EventManager;

/**
 *
 * @author lobzik
 */
public class ActualDataStorageModule implements Module {

    public final String MODULE_NAME = this.getClass().getSimpleName();
    private static ActualDataStorageModule instance = null;
    private static final int CACHE_SIZE = 100; 
    //private static final HashMap<Integer, List> parametersCache = new HashMap();
            
    private ActualDataStorageModule() { //singleton
    }

    public static ActualDataStorageModule getInstance() {
        if (instance == null) {
            instance = new ActualDataStorageModule(); //lazy init
        }
        return instance;
    }

    @Override
    public String getModuleName() {
        return MODULE_NAME;
    }

    @Override
    public void start() {
        for (Integer paramId: AppData.parametersStorage.getParameterIds()) {
            List<Parameter> paramHistory = new LinkedList();
            AppData.parametersCache.put(paramId, paramHistory);
            EventManager.subscribeForEventType(this, Event.Type.PARAMETER_UPDATED);
        }
    }

    @Override
    public synchronized void handleEvent(Event e) {
        if (e.type == Event.Type.PARAMETER_UPDATED) {
            Parameter parameter = (Parameter)e.data.get("parameter");
            List<Parameter> history = AppData.parametersCache.get(parameter.getId());
            history.add(parameter);
            while (history.size() > CACHE_SIZE) history.remove(0);
            AppData.parametersCache.put(parameter.getId(), history);
        }
    }



    public static void finish() {

    }
}
