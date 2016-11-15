/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi.modules;

import java.math.BigInteger;
import java.sql.Connection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.lobzik.home_sapiens.entity.Measurement;
import org.lobzik.home_sapiens.entity.Parameter;
import org.lobzik.home_sapiens.pi.AppData;
import org.lobzik.home_sapiens.pi.BoxCommonData;
import org.lobzik.home_sapiens.pi.BoxMode;
import org.lobzik.home_sapiens.pi.BoxSettingsAPI;
import org.lobzik.home_sapiens.pi.ConnJDBCAppender;
import org.lobzik.home_sapiens.pi.MeasurementsCache;
import org.lobzik.home_sapiens.pi.WebNotification;
import org.lobzik.home_sapiens.pi.event.Event;
import org.lobzik.home_sapiens.pi.event.EventManager;
import static org.lobzik.home_sapiens.pi.modules.ModemModule.test;
import org.lobzik.tools.Tools;
import org.lobzik.tools.db.mysql.DBSelect;
import org.lobzik.tools.db.mysql.DBTools;

/**
 * Implement internal logics of events and data handling
 *
 * @author lobzik
 */
public class InstinctsModule implements Module {

    public final String MODULE_NAME = this.getClass().getSimpleName();
    private static Logger log = null;
    private static InstinctsModule instance = null;

    private InstinctsModule() { //singleton
    }

    public static InstinctsModule getInstance() {

        if (instance == null) {
            instance = new InstinctsModule(); //lazy init
            log = Logger.getLogger(instance.MODULE_NAME);
            if (!test) {
                Appender appender = ConnJDBCAppender.getAppenderInstance(AppData.dataSource, instance.MODULE_NAME);
                log.addAppender(appender);
            }
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
            EventManager.subscribeForEventType(this, Event.Type.SYSTEM_EVENT);
            EventManager.subscribeForEventType(this, Event.Type.SYSTEM_MODE_CHANGED);
            EventManager.subscribeForEventType(this, Event.Type.PARAMETER_UPDATED);
            EventManager.subscribeForEventType(this, Event.Type.PARAMETER_CHANGED);
            EventManager.subscribeForEventType(this, Event.Type.USER_ACTION);

            Parameter p = AppData.parametersStorage.getParameter(AppData.parametersStorage.resolveAlias("SOCKET"));
            Measurement off = new Measurement(p, false);
            HashMap eventData = new HashMap();
            eventData.put("parameter", p);
            eventData.put("measurement", off);
            Event newE = new Event("init", eventData, Event.Type.PARAMETER_UPDATED);
            AppData.eventManager.newEvent(newE);

            p = AppData.parametersStorage.getParameter(AppData.parametersStorage.resolveAlias("LAMP_1"));
            off = new Measurement(p, false);
            eventData = new HashMap();
            eventData.put("parameter", p);
            eventData.put("measurement", off);
            newE = new Event("init", eventData, Event.Type.PARAMETER_UPDATED);
            AppData.eventManager.newEvent(newE);

            p = AppData.parametersStorage.getParameter(AppData.parametersStorage.resolveAlias("LAMP_2"));
            off = new Measurement(p, false);
            eventData = new HashMap();
            eventData.put("parameter", p);
            eventData.put("measurement", off);
            newE = new Event("init", eventData, Event.Type.PARAMETER_UPDATED);
            AppData.eventManager.newEvent(newE);

            p = AppData.parametersStorage.getParameter(AppData.parametersStorage.resolveAlias("DOOR_SENSOR"));
            off = new Measurement(p, false);
            eventData = new HashMap();
            eventData.put("parameter", p);
            eventData.put("measurement", off);
            newE = new Event("init", eventData, Event.Type.PARAMETER_UPDATED);
            AppData.eventManager.newEvent(newE);

            p = AppData.parametersStorage.getParameter(AppData.parametersStorage.resolveAlias("WET_SENSOR"));
            off = new Measurement(p, false);
            eventData = new HashMap();
            eventData.put("parameter", p);
            eventData.put("measurement", off);
            newE = new Event("init", eventData, Event.Type.PARAMETER_UPDATED);
            AppData.eventManager.newEvent(newE);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleEvent(Event e) {
        switch (e.type) {
            case SYSTEM_EVENT:
                if (e.name.equals("cellid_detected")) {
                    searchForLocationByCellId(e.data);
                }
                break;

            case PARAMETER_CHANGED:
                switch (e.name) {

                    case "user_command": //реакция на команды, для управления 433 через параметры
                        Measurement m = (Measurement) e.data.get("measurement");

                        Parameter p = (Parameter) e.data.get("parameter");
                        String alias = p.getAlias();
                        String uartCommand = "";
                        switch (alias) {
                            case "SOCKET":
                                if (m.getBooleanValue()) {
                                    uartCommand = BoxSettingsAPI.get("Socket1OnCommand433");
                                    log.info("Включена розетка");
                                    WebNotification wn = new WebNotification(WebNotification.Severity.INFO, alias, "Включена розетка", new Date(), null);
                                    HashMap data = new HashMap();
                                    data.put("WebNotification", wn);
                                    Event reaction = new Event("web_notification", data, Event.Type.REACTION_EVENT);
                                    AppData.eventManager.newEvent(reaction);

                                } else {
                                    uartCommand = BoxSettingsAPI.get("Socket1OffCommand433");
                                    log.info("Выключена розетка");
                                    WebNotification wn = new WebNotification(WebNotification.Severity.INFO, alias, "Отключена розетка", new Date(), null);
                                    HashMap data = new HashMap();
                                    data.put("WebNotification", wn);
                                    Event reaction = new Event("web_notification", data, Event.Type.REACTION_EVENT);
                                    AppData.eventManager.newEvent(reaction);
                                }
                                break;

                            case "LAMP_1":
                                if (m.getBooleanValue()) {
                                    uartCommand = BoxSettingsAPI.get("Lamp1OnCommand433");
                                    //FOR DEBUG ONLY
                                    Parameter pNightTime = AppData.parametersStorage.getParameter(AppData.parametersStorage.resolveAlias("NIGHTTIME"));
                                    Measurement isDay = new Measurement(pNightTime, false);
                                    HashMap eventData = new HashMap();
                                    eventData.put("parameter", pNightTime);
                                    eventData.put("measurement", isDay);
                                    Event newE = new Event("debug", eventData, Event.Type.PARAMETER_UPDATED);
                                    AppData.eventManager.newEvent(newE);
                                    log.info("Включена лампа 1");
                                    WebNotification wn = new WebNotification(WebNotification.Severity.INFO, alias, "Включена лампа 1", new Date(), null);
                                    HashMap data = new HashMap();
                                    data.put("WebNotification", wn);
                                    Event reaction = new Event("web_notification", data, Event.Type.REACTION_EVENT);
                                    AppData.eventManager.newEvent(reaction);

                                } else {
                                    uartCommand = BoxSettingsAPI.get("Lamp1OffCommand433");

                                    //FOR DEBUG ONLY
                                    Parameter pNightTime = AppData.parametersStorage.getParameter(AppData.parametersStorage.resolveAlias("NIGHTTIME"));
                                    Measurement isNight = new Measurement(pNightTime, true);
                                    HashMap eventData = new HashMap();
                                    eventData.put("parameter", pNightTime);
                                    eventData.put("measurement", isNight);
                                    Event newE = new Event("debug", eventData, Event.Type.PARAMETER_UPDATED);
                                    AppData.eventManager.newEvent(newE);
                                    log.info("Выключена лампа 1");
                                    WebNotification wn = new WebNotification(WebNotification.Severity.INFO, alias, "Отключена лампа 1", new Date(), null);
                                    HashMap data = new HashMap();
                                    data.put("WebNotification", wn);
                                    Event reaction = new Event("web_notification", data, Event.Type.REACTION_EVENT);
                                    AppData.eventManager.newEvent(reaction);

                                }
                                break;

                            case "LAMP_2":
                                if (m.getBooleanValue()) {
                                    uartCommand = BoxSettingsAPI.get("Lamp2OnCommand433");
                                    log.info("Включена лампа 2");
                                    WebNotification wn = new WebNotification(WebNotification.Severity.INFO, alias, "Включена лампа 2", new Date(), null);
                                    HashMap data = new HashMap();
                                    data.put("WebNotification", wn);
                                    Event reaction = new Event("web_notification", data, Event.Type.REACTION_EVENT);
                                    AppData.eventManager.newEvent(reaction);
                                } else {
                                    uartCommand = BoxSettingsAPI.get("Lamp2OffCommand433");
                                    log.info("Выключена лампа 2");
                                    WebNotification wn = new WebNotification(WebNotification.Severity.INFO, alias, "Отключена лампа 2", new Date(), null);
                                    HashMap data = new HashMap();
                                    data.put("WebNotification", wn);
                                    Event reaction = new Event("web_notification", data, Event.Type.REACTION_EVENT);
                                    AppData.eventManager.newEvent(reaction);
                                }
                                break;

                        }
                        if (uartCommand != null && uartCommand.length() > 0) {
                            HashMap data = new HashMap();
                            data.put("uart_command", "433_TX=" + uartCommand);
                            Event reaction = new Event("internal_uart_command", data, Event.Type.USER_ACTION);
                            AppData.eventManager.newEvent(reaction);
                        }
                        break;

                    default:
                        p = (Parameter) e.data.get("parameter");
                        if (p != null) {
                            m = (Measurement) e.data.get("measurement");
                            alias = p.getAlias();
                            switch (alias) {
                                case "INTERNAL_TEMP":
                                    if (m.getDoubleValue() > BoxSettingsAPI.getDouble("InTempAlertMax") || m.getDoubleValue() < BoxSettingsAPI.getDouble("InTempAlertMin")) {
                                        if (p.getState() != Parameter.State.ALARM) {
                                            p.setState(Parameter.State.ALARM);

                                            WebNotification wn = new WebNotification(WebNotification.Severity.ALARM, alias, "Комнатная температура вышла за пределы!", new Date(), null);
                                            HashMap data = new HashMap();
                                            data.put("WebNotification", wn);
                                            Event reaction = new Event("web_notification", data, Event.Type.REACTION_EVENT);
                                            AppData.eventManager.newEvent(reaction);
                                        }
                                    } else {
                                        if (p.getState() != Parameter.State.OK) {
                                            p.setState(Parameter.State.OK);

                                            WebNotification wn = new WebNotification(WebNotification.Severity.OK, alias, "Комнатная температура снова в норме", new Date(), null);
                                            HashMap data = new HashMap();
                                            data.put("WebNotification", wn);
                                            Event reaction = new Event("web_notification", data, Event.Type.REACTION_EVENT);
                                            AppData.eventManager.newEvent(reaction);
                                        }

                                    }
                                    break;
                                case "VAC_SENSOR":
                                    if (m.getDoubleValue() > BoxSettingsAPI.getDouble("VACAlertMax") || m.getDoubleValue() < BoxSettingsAPI.getDouble("VACAlertMin")) {
                                        if (p.getState() != Parameter.State.ALARM) {
                                            p.setState(Parameter.State.ALARM);
                                            log.error("Напряжение сети вне пределов, отключаем зарядку");
                                            HashMap data = new HashMap();
                                            data.put("uart_command", "charge=off"); //disable charging if power is NOT ok
                                            Event reaction = new Event("internal_uart_command", data, Event.Type.USER_ACTION);
                                            AppData.eventManager.newEvent(reaction);

                                            WebNotification wn = new WebNotification(WebNotification.Severity.ALARM, alias, "Напряжение сети вне пределов!", new Date(), null);
                                            HashMap data2 = new HashMap();
                                            data2.put("WebNotification", wn);
                                            Event reaction2 = new Event("web_notification", data2, Event.Type.REACTION_EVENT);
                                            AppData.eventManager.newEvent(reaction2);
                                        }

                                    } else {
                                        if (p.getState() != Parameter.State.OK) {
                                            p.setState(Parameter.State.OK);
                                            log.info("Включаем зарядку");
                                            HashMap data = new HashMap();
                                            data.put("uart_command", "charge=on"); //enable charging if power is ok
                                            Event reaction = new Event("internal_uart_command", data, Event.Type.USER_ACTION);
                                            AppData.eventManager.newEvent(reaction);

                                            WebNotification wn = new WebNotification(WebNotification.Severity.OK, alias, "Напряжение сети снова в норме!", new Date(), null);
                                            HashMap data2 = new HashMap();
                                            data2.put("WebNotification", wn);
                                            Event reaction2 = new Event("web_notification", data2, Event.Type.REACTION_EVENT);
                                            AppData.eventManager.newEvent(reaction2);
                                        }

                                    }

                                    break;
                                case "VBAT_SENSOR":
                                    double avgBattVoltage = AppData.measurementsCache.getLastMeasurement(p).getDoubleValue();
                                    int id = AppData.parametersStorage.resolveAlias("CHARGE_ENABLED");
                                    Parameter charge = AppData.parametersStorage.getParameter(id);
                                    if (AppData.measurementsCache.getLastMeasurement(charge).getBooleanValue()) {
                                        avgBattVoltage -= 1.3;//если зарядка
                                    }

                                    int chargePercents = 5;
                                    if (avgBattVoltage > 6.6) {
                                        chargePercents += (avgBattVoltage - 6.6) * 119; //при 7.4В будет 100%
                                    }
                                    if (chargePercents > 100) {
                                        chargePercents = 100;
                                    }
                                    Parameter chargeP = AppData.parametersStorage.getParameter(AppData.parametersStorage.resolveAlias("BATT_CHARGE"));
                                    Measurement chargeM = new Measurement(chargeP, chargePercents);
                                    HashMap eventData = new HashMap();
                                    eventData.put("parameter", chargeP);
                                    eventData.put("measurement", chargeM);
                                    Event newE = new Event("calculated", eventData, Event.Type.PARAMETER_UPDATED);

                                    AppData.eventManager.newEvent(newE);

                                    if (AppData.measurementsCache.getAvgMeasurementFrom(p, System.currentTimeMillis() - 300000).getDoubleValue() < 5.8) {
                                        log.fatal("Уровень заряда АКБ критически низок! Отключаем систему");
                                        Event shutdown = new Event("shutdown", null, Event.Type.SYSTEM_EVENT);
                                        AppData.eventManager.newEvent(shutdown);
                                    }
                                    break;

                            }
                        }
                        break;
                }
                break;

            case USER_ACTION:
                if (e.name.equals("user_command")) {
                    Map commandData = e.data;
                    if (commandData != null) {
                        for (String parName : (Set<String>) commandData.keySet()) {
                            String val = (String) commandData.get(parName);
                            int paramId = AppData.parametersStorage.resolveAlias(parName);

                            if (paramId > 0) {
                                HashMap eventData = new HashMap();
                                Parameter p = AppData.parametersStorage.getParameter(paramId);
                                Measurement m = null;
                                switch (p.getType()) {
                                    case BOOLEAN:
                                        m = new Measurement(p, Tools.parseBoolean(val, null));
                                        break;
                                    //TODO other types?
                                    //да, это единственный случай, когда юзер меняет параметр. просто это очень удобно и лучше я не придумал
                                }
                                eventData.put("parameter", p);
                                eventData.put("measurement", m);
                                Event newE = new Event("user_command", eventData, Event.Type.PARAMETER_UPDATED);

                                AppData.eventManager.newEvent(newE);

                            }
                        }
                    }
                }
                break;
        }
    }

    public static void finish() {

    }

    private void searchForLocationByCellId(Map cellIdData) {
        try {
            String lac = (String) cellIdData.get("LAC");
            String cid = (String) cellIdData.get("CID");
            BigInteger cell = new BigInteger(cid, 16);
            BigInteger area = new BigInteger(lac, 16);
            String sSQL = "select lat, lon from opencellid.megafon_ru where mcc=250 and net=2 and cell=" + cell.toString() + " and area=" + area.toString();
            try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {
                List<HashMap> resList = DBSelect.getRows(sSQL, conn);
                if (resList.isEmpty()) {
                    throw new Exception("CellId " + cid + " with lac " + lac + " не найден в базе данных");
                }
                double latitude = Tools.parseDouble(resList.get(0).get("lat"), 0);
                double longitude = Tools.parseDouble(resList.get(0).get("lon"), 0);
                log.info("Определено местоположение!" + latitude + ", " + longitude);
                HashMap eventData = new HashMap();
                eventData.put("latitude", latitude);
                eventData.put("longitude", longitude);
                Event event = new Event("location_detected", eventData, Event.Type.SYSTEM_EVENT);
                AppData.eventManager.newEvent(event);
            }

        } catch (Exception e) {
            log.error("Не удалось определить местоположение: " + e.getMessage());
        }
    }

}
