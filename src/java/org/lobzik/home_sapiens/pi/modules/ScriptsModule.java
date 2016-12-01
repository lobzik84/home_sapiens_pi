/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi.modules;

import java.util.HashMap;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.lobzik.home_sapiens.entity.Measurement;
import org.lobzik.home_sapiens.entity.Parameter;
import org.lobzik.home_sapiens.pi.AppData;
import org.lobzik.home_sapiens.pi.BoxSettingsAPI;
import org.lobzik.home_sapiens.pi.ConnJDBCAppender;
import org.lobzik.home_sapiens.pi.behavior.Notification;
import org.lobzik.home_sapiens.pi.event.Event;
import org.lobzik.home_sapiens.pi.event.EventManager;

/**
 *
 * @author lobzik
 */
public class ScriptsModule implements Module {

    public final String MODULE_NAME = this.getClass().getSimpleName();
    private static ScriptsModule instance = null;
    private static Logger log = null;

    //для записи лога по reaction_events
    public static ScriptsModule getInstance() {
        if (instance == null) {
            instance = new ScriptsModule(); //lazy init
            log = Logger.getLogger(instance.MODULE_NAME);
            Appender appender = ConnJDBCAppender.getAppenderInstance(AppData.dataSource, instance.MODULE_NAME);
            log.addAppender(appender);
        }
        return instance;
    }

    private ScriptsModule() {

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
        if (e.type == Event.Type.BEHAVIOR_EVENT) {
            switch (e.name) {
                /*case "pir_action_on"://TODO где-то проверить на темноту?
                    if ("true".equalsIgnoreCase(BoxSettingsAPI.get("Lamp1PIRSensorScript"))) {
                        Parameter p = AppData.parametersStorage.getParameterByAlias("LAMP_1");
                        Measurement m = new Measurement(p, true);
                        HashMap eventData = new HashMap();
                        eventData.put("parameter", p);
                        eventData.put("measurement", m);
                        Event newE = new Event("script_command", eventData, Event.Type.PARAMETER_UPDATED);
                        AppData.eventManager.newEvent(newE);
                    }
                    if ("true".equalsIgnoreCase(BoxSettingsAPI.get("Lamp2PIRSensorScript"))) {
                        Parameter p = AppData.parametersStorage.getParameterByAlias("LAMP_2");
                        Measurement m = new Measurement(p, true);
                        HashMap eventData = new HashMap();
                        eventData.put("parameter", p);
                        eventData.put("measurement", m);
                        Event newE = new Event("script_command", eventData, Event.Type.PARAMETER_UPDATED);
                        AppData.eventManager.newEvent(newE);
                    }
                    break;

                case "pir_action_off":
                    if ("true".equalsIgnoreCase(BoxSettingsAPI.get("Lamp1PIRSensorScript"))) {
                        Parameter p = AppData.parametersStorage.getParameterByAlias("LAMP_1");
                        Measurement m = new Measurement(p, false);
                        HashMap eventData = new HashMap();
                        eventData.put("parameter", p);
                        eventData.put("measurement", m);
                        Event newE = new Event("script_command", eventData, Event.Type.PARAMETER_UPDATED);
                        AppData.eventManager.newEvent(newE);
                    }
                    if ("true".equalsIgnoreCase(BoxSettingsAPI.get("Lamp2PIRSensorScript"))) {
                        Parameter p = AppData.parametersStorage.getParameterByAlias("LAMP_2");
                        Measurement m = new Measurement(p, false);
                        HashMap eventData = new HashMap();
                        eventData.put("parameter", p);
                        eventData.put("measurement", m);
                        Event newE = new Event("script_command", eventData, Event.Type.PARAMETER_UPDATED);
                        AppData.eventManager.newEvent(newE);
                    }
                    break;

                case "nighttime_action_on":
                    if ("true".equalsIgnoreCase(BoxSettingsAPI.get("Lamp1DarkSensorScript"))) {
                        Parameter p = AppData.parametersStorage.getParameterByAlias("LAMP_1");
                        Measurement m = new Measurement(p, true);
                        HashMap eventData = new HashMap();
                        eventData.put("parameter", p);
                        eventData.put("measurement", m);
                        Event newE = new Event("script_command", eventData, Event.Type.PARAMETER_UPDATED);
                        AppData.eventManager.newEvent(newE);
                    }
                    if ("true".equalsIgnoreCase(BoxSettingsAPI.get("Lamp2DarkSensorScript"))) {
                        Parameter p = AppData.parametersStorage.getParameterByAlias("LAMP_2");
                        Measurement m = new Measurement(p, true);
                        HashMap eventData = new HashMap();
                        eventData.put("parameter", p);
                        eventData.put("measurement", m);
                        Event newE = new Event("script_command", eventData, Event.Type.PARAMETER_UPDATED);
                        AppData.eventManager.newEvent(newE);
                    }
                    break;
                case "nighttime_action_off":
                    if ("true".equalsIgnoreCase(BoxSettingsAPI.get("Lamp1DarkSensorScript"))) {
                        Parameter p = AppData.parametersStorage.getParameterByAlias("LAMP_1");
                        Measurement m = new Measurement(p, false);
                        HashMap eventData = new HashMap();
                        eventData.put("parameter", p);
                        eventData.put("measurement", m);
                        Event newE = new Event("script_command", eventData, Event.Type.PARAMETER_UPDATED);
                        AppData.eventManager.newEvent(newE);
                    }
                    if ("true".equalsIgnoreCase(BoxSettingsAPI.get("Lamp2DarkSensorScript"))) {
                        Parameter p = AppData.parametersStorage.getParameterByAlias("LAMP_2");
                        Measurement m = new Measurement(p, false);
                        HashMap eventData = new HashMap();
                        eventData.put("parameter", p);
                        eventData.put("measurement", m);
                        Event newE = new Event("script_command", eventData, Event.Type.PARAMETER_UPDATED);
                        AppData.eventManager.newEvent(newE);
                    }
                    break;*/
                case "lamp1_on":
                    Parameter p = AppData.parametersStorage.getParameterByAlias("LAMP_1");
                    Measurement m = new Measurement(p, true);
                    HashMap eventData = new HashMap();
                    eventData.put("parameter", p);
                    eventData.put("measurement", m);
                    Event newE = new Event("script_command", eventData, Event.Type.PARAMETER_UPDATED);
                    AppData.eventManager.newEvent(newE);
                    break;

                case "lamp1_off":
                    p = AppData.parametersStorage.getParameterByAlias("LAMP_1");
                    m = new Measurement(p, false);
                    eventData = new HashMap();
                    eventData.put("parameter", p);
                    eventData.put("measurement", m);
                    newE = new Event("script_command", eventData, Event.Type.PARAMETER_UPDATED);
                    AppData.eventManager.newEvent(newE);
                    break;
                    
                case "lamp2_on":
                    p = AppData.parametersStorage.getParameterByAlias("LAMP_2");
                    m = new Measurement(p, true);
                    eventData = new HashMap();
                    eventData.put("parameter", p);
                    eventData.put("measurement", m);
                    newE = new Event("script_command", eventData, Event.Type.PARAMETER_UPDATED);
                    AppData.eventManager.newEvent(newE);
                    break;

                case "lamp2_off":
                    p = AppData.parametersStorage.getParameterByAlias("LAMP_2");
                    m = new Measurement(p, false);
                    eventData = new HashMap();
                    eventData.put("parameter", p);
                    eventData.put("measurement", m);
                    newE = new Event("script_command", eventData, Event.Type.PARAMETER_UPDATED);
                    AppData.eventManager.newEvent(newE);
                    break;
            }
        }
    }

}
