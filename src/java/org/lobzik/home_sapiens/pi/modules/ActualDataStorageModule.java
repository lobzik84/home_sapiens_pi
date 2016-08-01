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
        

        EventManager.subscribeForEventType(this, Event.Type.PARAMETER_UPDATED);
    }

    @Override
    public synchronized void handleEvent(Event e) {
        if (e.type == Event.Type.PARAMETER_UPDATED) {
            Measurement measurement = (Measurement)e.data.get("measurement");
            Parameter p = measurement.getParameter();
            Measurement prevMeasurement =  AppData.measurementsCache.getLastMeasurement(p);
            AppData.measurementsCache.add(measurement);
            if (prevMeasurement == null ||  prevMeasurement.toStringValue() == null || !prevMeasurement.toStringValue().equals(measurement.toStringValue())) {
                Event newE = new Event(e.name, e.data, Event.Type.PARAMETER_CHANGED);
                AppData.eventManager.newEvent(newE);
            }
        }
    }



    public static void finish() {

    }
}
