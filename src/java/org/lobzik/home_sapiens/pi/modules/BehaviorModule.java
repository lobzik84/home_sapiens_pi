/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi.modules;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.lobzik.home_sapiens.entity.Measurement;
import org.lobzik.home_sapiens.entity.Parameter;
import org.lobzik.home_sapiens.pi.behavior.Action;
import org.lobzik.home_sapiens.pi.AppData;
import static org.lobzik.home_sapiens.pi.AppData.measurementsCache;
import org.lobzik.home_sapiens.pi.BoxCommonData;
import org.lobzik.home_sapiens.pi.BoxMode;
import org.lobzik.home_sapiens.pi.BoxSettingsAPI;
import org.lobzik.home_sapiens.pi.behavior.Condition;
import org.lobzik.home_sapiens.pi.ConnJDBCAppender;
import org.lobzik.home_sapiens.pi.behavior.Notification;
import org.lobzik.home_sapiens.pi.event.Event;
import org.lobzik.home_sapiens.pi.event.EventManager;
import org.lobzik.tools.Tools;
import org.lobzik.tools.db.mysql.DBSelect;
import org.lobzik.tools.db.mysql.DBTools;

/**
 * Implement internal logics of events and data handling
 *
 * @author lobzik
 */
public class BehaviorModule implements Module {

    public final String MODULE_NAME = this.getClass().getSimpleName();
    private static Logger log = null;
    private static BehaviorModule instance = null;
    private static List<Condition> conditions = null;

    private BehaviorModule() { //singleton
    }

    public static BehaviorModule getInstance() {

        if (instance == null) {
            instance = new BehaviorModule(); //lazy init
            log = Logger.getLogger(instance.MODULE_NAME);

            Appender appender = ConnJDBCAppender.getAppenderInstance(AppData.dataSource, instance.MODULE_NAME);
            log.addAppender(appender);

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

            try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {
                String sSQL = "SELECT * FROM conditions";
                List<HashMap> conditionsList = DBSelect.getRows(sSQL, conn);
                conditions = new ArrayList();
                if (conditionsList.size() > 0) {
                    for (HashMap c : conditionsList) {
                        Condition cond = new Condition(
                                Tools.parseInt(c.get("id"), 0),
                                Tools.parseInt(c.get("parameter_id"), 0),
                                Tools.getStringValue(c.get("alias"), ""),
                                Tools.getStringValue(c.get("name"), ""),
                                Tools.parseInt(c.get("state"), 0));
                        conditions.add(cond);
                    }
                }

            } catch (Exception ee) {
                ee.printStackTrace();
            }

            try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {
                String sSQL = "SELECT * FROM actions where enabled=1 order by condition_id";
                List<HashMap> actionsList = DBSelect.getRows(sSQL, conn);
                if (actionsList.size() > 0) {
                    for (HashMap a : actionsList) {
                        int conditionId = Tools.parseInt(a.get("condition_id"), 0);
                        if (conditionId > 0) {
                            Action act = new Action(
                                    Tools.parseInt(a.get("id"), 0),
                                    Tools.getStringValue(a.get("alias"), ""),
                                    Tools.getStringValue(a.get("module"), ""),
                                    (String) a.get("event_name"),
                                    (String) a.get("notification_text"),
                                    Notification.Severity.valueOf(Tools.getStringValue(a.get("severity"), "")),
                                    a.get("box_mode") != null ? BoxMode.MODE.valueOf(Tools.getStringValue(a.get("box_mode"), "")) : null,
                                    (Integer) a.get("condition_state")
                            );
                            Condition cond = getConditionById(conditions, conditionId);

                            cond.addAction(act);
                        }
                    }
                }

            } catch (Exception ee) {
                ee.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleEvent(Event e) {
        switch (e.type) {
            case SYSTEM_EVENT:
                switch (e.name) {
                    case "compare_images":
                        VideoRecModule.compareImages();
                        break;
                    case "shutdown":

                        String cause = "";
                        if (e.data != null && e.data.get("cause") != null) {
                            cause += ": " + (String) e.data.get("cause");
                        }
                        Condition c = getConditionByAlias("SHUTDOWN");
                        runActions(c, cause, "");

                        break;
                    case "location_detected":
                        c = getConditionByAlias("LOCATION_DETECTED");
                        runActions(c, e.data.toString(), "");
                        break;
                    case "forecast_loaded":
                        c = getConditionByAlias("FORECAST_LOADED");
                        runActions(c, "", "");
                        break;
                    case "tunnel_connected":
                        c = getConditionByAlias("TUNNEL_CONNECTED");
                        runActions(c, "", "");
                        break;
                    case "tunnel_connection_lost":
                        c = getConditionByAlias("TUNNEL_CONNECTION_LOST");
                        runActions(c, "", "");
                        break;
                    case "user_logged_in":
                        c = getConditionByAlias("USER_LOGGED_IN");
                        String msg = "";
                        if (e.data != null) {
                            String authType = (String) e.data.get("auth_type");
                            String IP = Tools.getStringValue(e.data.get("ip"), "");
                            if (authType != null) {
                                switch (authType) {
                                    case "local_RSA":
                                        msg = "из локальной сети, IP " + IP;
                                        break;
                                    case "local_SRP":
                                        msg = "из локальной сети по паролю, IP " + IP;
                                        break;
                                    case "remote_RSA":
                                        msg = "через Интернет, IP " + IP;
                                        break;
                                    case "remote_SRP":
                                        msg = "через Интернет по паролю, IP " + IP;
                                        break;

                                }
                            }
                        }
                        runActions(c, msg, "");
                        break;
                    case "user_registered":
                        c = getConditionByAlias("USER_REGISTERED");
                        runActions(c, BoxCommonData.BOX_ID + "", "");
                        break;
                        
                    case "user_password_updated":
                        c = getConditionByAlias("USER_PASSWORD_UPDATED");
                        runActions(c, "", "");
                        break;
                        
                    case "statistics_sent":
                        c = getConditionByAlias("STATISTICS_SENT");
                        runActions(c, "", "");
                        break;
                }

            case SYSTEM_MODE_CHANGED:
                Condition c = getConditionByAlias("BOX_MODE_CHANGED");
                if (BoxMode.isArmed()) {
                    if (c.state != 1) {
                        runActions(1, c, "", "");
                        c.setState(1);
                    }

                } else if (BoxMode.isIdle()) {
                    if (c.state != 0) {
                        runActions(0, c, "", "");
                        c.setState(0);
                    }

                }

                break;

            case PARAMETER_UPDATED:

                try {

                    if (e.data.get("parameter") != null) {
                        Parameter p = (Parameter) e.data.get("parameter");
                        switch (p.getAlias()) {
                            case "VAC_SENSOR":
                                parameterVAC_SENSORActions(e);
                                break;
                            case "DOOR_SENSOR":
                                parameterDOOR_SENSORActions(e);
                                break;
                            case "WET_SENSOR":
                                parameterWET_SENSORActions(e);
                                break;
                            case "BATT_CHARGE":
                                parameterBATT_CHARGEActions(e);
                                break;
                            case "BATT_TEMP":
                                parameterBATT_TEMPActions(e);
                                break;
                            case "INTERNAL_TEMP":
                                parameterINTERNAL_TEMPActions(e);
                                break;
                            case "INTERNAL_HUMIDITY":
                                parameterINTERNAL_HUMIDITYActions(e);
                                break;
                            case "GAS_SENSOR":
                                parameterGAS_SENSORActions(e);
                                break;
                            case "PIR_SENSOR":
                                parameterPIR_SENSORActions(e);
                                break;
                            case "MIC_NOISE":
                                parameterMIC_NOISEActions(e);
                                break;
                            case "LUMIOSITY":
                                parameterLUMIOSITYActions(e);
                                break;
                            case "NIGHTTIME":
                                parameterNIGHTTIMEActions(e);
                                break;
                        }
                    }
                } catch (Exception EE) {
                    EE.printStackTrace();
                    log.error(EE.toString());
                }

                break;
        }
    }

    public static void finish() {

    }

    public static Condition getConditionById(List<Condition> conditions, int conditionId) {
        Condition result = null;
        for (Condition c : conditions) {
            if (c.id == conditionId) {
                result = c;
            }
        }
        return result;
    }

    public static Condition getConditionByParameterAndState(List<Condition> conditions, int parameterId) {
        Condition result = null;
        for (Condition c : conditions) {
            if (c.parameterId == parameterId) {
                result = c;
            }
        }
        return result;
    }

    public static Condition getConditionByAlias(String alias) {
        Condition result = null;
        for (Condition c : conditions) {
            if (c.alias != null && c.alias.equals(alias)) {
                result = c;
            }
        }
        return result;
    }

    private void parameterMIC_NOISEActions(Event e) { //Датчик движения

        Parameter p = (Parameter) e.data.get("parameter");
        Measurement m = (Measurement) e.data.get("measurement");
        //работает более 10 сек.
        Condition c = getConditionByAlias("MIC_NOISE_ALARM");
        int VACTimeout = 10; //секунд
        int transferTrueCount = measurementsCache.getTransferTrueCountFrom(p, System.currentTimeMillis() - 1000 * VACTimeout);

        if (m.getBooleanValue() && transferTrueCount != 0) {
            if (BoxMode.isArmed()) {
                p.setState(Parameter.State.ALARM);
            }
            triggerState(1, c, m, p);
        } else {
            p.setState(Parameter.State.OK);
        }
        if (!m.getBooleanValue() && transferTrueCount == 0) {
            triggerState(0, c, m, p);
        }
    }

    private void parameterPIR_SENSORActions(Event e) { //Датчик движения

        Parameter p = (Parameter) e.data.get("parameter");
        Measurement m = (Measurement) e.data.get("measurement");
        //работает более 15 сек.
        Condition c = getConditionByAlias("PIR_SENSOR_ALARM");
        int PIRTimeout = 10; //секунд
        int transferTrueCount = measurementsCache.getTransferTrueCountFrom(p, System.currentTimeMillis() - 1000 * PIRTimeout);

        if (m.getBooleanValue() && transferTrueCount == 0) {
            if (BoxMode.isArmed()) {
                p.setState(Parameter.State.ALARM);
            }
            triggerState(1, c, m, p);
        } else {
            p.setState(Parameter.State.OK);
        }
        if (!m.getBooleanValue() && transferTrueCount == 0) {
            triggerState(0, c, m, p);
        }

        c = getConditionByAlias("LUMIOSITY_DARK");
        boolean dark = c.state == 1;

        boolean lampPIRSensorScript = "true".equalsIgnoreCase(BoxSettingsAPI.get("Lamp1PIRSensorScript"));
        p = AppData.parametersStorage.getParameterByAlias("LAMP_1");
        c = getConditionByAlias("LAMP1_PIR_SCRIPT");
        if (dark && m.getBooleanValue()) {
            triggerState(1, c, m, p, lampPIRSensorScript);
        }
        if (!m.getBooleanValue() && transferTrueCount == 0) {
            triggerState(0, c, m, p, lampPIRSensorScript);
        }

        lampPIRSensorScript = "true".equalsIgnoreCase(BoxSettingsAPI.get("Lamp2PIRSensorScript"));
        p = AppData.parametersStorage.getParameterByAlias("LAMP_2");
        c = getConditionByAlias("LAMP2_PIR_SCRIPT");
        if (dark && m.getBooleanValue()) {
            triggerState(1, c, m, p, lampPIRSensorScript);
        }
        if (!m.getBooleanValue() && transferTrueCount == 0) {
            triggerState(0, c, m, p, lampPIRSensorScript);
        }

    }

    private void parameterGAS_SENSORActions(Event e) { //Gas Sensor
        //Сработал датчик газа
        Condition c = getConditionByAlias("GAS_SENSOR_ALARM");
        Parameter p = (Parameter) e.data.get("parameter");
        Measurement m = (Measurement) e.data.get("measurement");
        int GASTimeout = 30; //секунд
        int transferTrueCount = measurementsCache.getTransferTrueCountFrom(p, System.currentTimeMillis() - 1000 * GASTimeout);

        if (m.getBooleanValue()) {
            p.setState(Parameter.State.ALARM);
            triggerState(1, c, m, p);
        } else if (transferTrueCount == 0) { //иначе валится шквал
            p.setState(Parameter.State.OK);
            triggerState(0, c, m, p);
        }
    }

    private void parameterINTERNAL_HUMIDITYActions(Event e) { //Датчик влажности

        Parameter p = (Parameter) e.data.get("parameter");
        Measurement m = (Measurement) e.data.get("measurement");

        //Вышла и более 5 минут подряд находится вне установленных пределов
        Condition c = getConditionByAlias("INTERNAL_HUMIDITY_OUT_OF_BOUNDS");
        int timeout = 5; //минут
        Measurement mMax = measurementsCache.getMaxMeasurementFrom(p, System.currentTimeMillis() - 1000 * 60 * timeout);
        Measurement mMin = measurementsCache.getMinMeasurementFrom(p, System.currentTimeMillis() - 1000 * 60 * timeout);
        if (mMax != null && mMin != null) {
            if ((mMax.getDoubleValue() < BoxSettingsAPI.getDouble("InHumAlertMin")) || (mMin.getDoubleValue() > BoxSettingsAPI.getDouble("InHumAlertMax"))) {
                triggerState(1, c, m, p);
            } else {
                triggerState(0, c, m, p);
            }
        }
        if ((m.getDoubleValue() > BoxSettingsAPI.getDouble("InHumAlertMin")) && m.getDoubleValue() < BoxSettingsAPI.getDouble("InHumAlertMax")) {
            p.setState(Parameter.State.OK);
        } else {
            p.setState(Parameter.State.ALARM);
        }

    }

    private void parameterINTERNAL_TEMPActions(Event e) { //Датчик температуры

        Parameter p = (Parameter) e.data.get("parameter");
        Measurement m = (Measurement) e.data.get("measurement");

        //не работает 
        int timeout = 5; //минут
        Condition c = getConditionByAlias("INTERNAL_TEMP_SENSOR_FAILURE");
        Measurement mMin = measurementsCache.getMinMeasurementFrom(p, System.currentTimeMillis() - 1000 * 60 * timeout);
        if (mMin == null) {

            triggerState(1, c, m, p);
        } else {
            triggerState(0, c, m, p);
        }

        //Температура воздуха снижается быстрее, чем на 5 градусов в час
        c = getConditionByAlias("INTERNAL_TEMP_FAST_FALLING");
        timeout = 60; //минут
        Measurement mMax = measurementsCache.getMaxMeasurementFrom(p, System.currentTimeMillis() - 1000 * 60 * timeout);
        mMin = measurementsCache.getMinMeasurementFrom(p, System.currentTimeMillis() - 1000 * 60 * timeout);
        if (mMax != null && mMin != null) {
            if ((mMax.getDoubleValue() - mMin.getDoubleValue() > 5) && (mMax.getTime() < mMin.getTime())) {
                triggerState(1, c, m, p);
            } else {
                triggerState(0, c, m, p);
            }
        }

        //Температура воздуха Растет быстрее чем на 5 градусов за 10 минут
        c = getConditionByAlias("INTERNAL_TEMP_FAST_RISING");
        timeout = 10; //минут
        mMax = measurementsCache.getMaxMeasurementFrom(p, System.currentTimeMillis() - 1000 * 60 * timeout);
        mMin = measurementsCache.getMinMeasurementFrom(p, System.currentTimeMillis() - 1000 * 60 * timeout);
        if (mMax != null && mMin != null) {
            if ((mMax.getDoubleValue() - mMin.getDoubleValue() > 5) && (mMax.getTime() > mMin.getTime())) {
                triggerState(1, c, m, p);
            } else {
                triggerState(0, c, m, p);
            }
        }

        //Вышла и более 5 минут подряд находится вне установленных пределов
        c = getConditionByAlias("INTERNAL_TEMP_OUT_OF_BOUNDS");
        timeout = 5; //минут
        mMax = measurementsCache.getMaxMeasurementFrom(p, System.currentTimeMillis() - 1000 * 60 * timeout);
        mMin = measurementsCache.getMinMeasurementFrom(p, System.currentTimeMillis() - 1000 * 60 * timeout);
        if (mMax != null && mMin != null) {
            if ((mMax.getDoubleValue() < BoxSettingsAPI.getDouble("InTempAlertMin")) || (mMin.getDoubleValue() > BoxSettingsAPI.getDouble("InTempAlertMax"))) {
                triggerState(1, c, m, p);
            } else {
                triggerState(0, c, m, p);
            }
        }

        if ((m.getDoubleValue() > BoxSettingsAPI.getDouble("InTempAlertMax")) || m.getDoubleValue() < BoxSettingsAPI.getDouble("InTempAlertMin")) {
            p.setState(Parameter.State.ALARM);
        } else {
            p.setState(Parameter.State.OK);
        }
    }

    private void parameterBATT_TEMPActions(Event e) { //Перегрев аккумулятора
        //Перегрев аккумулятора
        int timeout = 1; //Минута
        Condition c = getConditionByAlias("BATT_TEMP_OVERHEAT");

        Parameter p = (Parameter) e.data.get("parameter");
        Measurement m = (Measurement) e.data.get("measurement");
        Measurement mMax = measurementsCache.getMaxMeasurementFrom(p, System.currentTimeMillis() - 1000 * 60 * timeout);
        Measurement mMin = measurementsCache.getMinMeasurementFrom(p, System.currentTimeMillis() - 1000 * 60 * timeout);

        if (mMin.getDoubleValue() > BoxSettingsAPI.getDouble("VBatTempAlertMax")) {
            triggerState(1, c, m, p);
            if (p.getState() != Parameter.State.ALARM) {
                p.setState(Parameter.State.ALARM);
                HashMap data = new HashMap();
                data.put("uart_command", "charge=off"); //disable charging if battery overheated
                Event reaction = new Event("internal_uart_command", data, Event.Type.USER_ACTION);
                AppData.eventManager.newEvent(reaction);
            }
        } else if (mMax.getDoubleValue() < BoxSettingsAPI.getDouble("VBatTempAlertMax")) {
            triggerState(0, c, m, p);
            if (p.getState() != Parameter.State.OK) {
                p.setState(Parameter.State.OK);
                HashMap data = new HashMap();
                data.put("uart_command", "charge=on"); //enable if ok
                Event reaction = new Event("internal_uart_command", data, Event.Type.USER_ACTION);
                AppData.eventManager.newEvent(reaction);
            }
        }
    }

    private void parameterLUMIOSITYActions(Event e) { //Перегрев аккумулятора
        //освещённость
        int timeout = 5; //Минут
        double hysterezis = 1.2;
        Condition c = getConditionByAlias("LUMIOSITY_DARK");

        Parameter p = (Parameter) e.data.get("parameter");
        Measurement m = (Measurement) e.data.get("measurement");
        Measurement mMax = measurementsCache.getMaxMeasurementFrom(p, System.currentTimeMillis() - 1000 * 60 * timeout);
        Measurement mMin = measurementsCache.getMinMeasurementFrom(p, System.currentTimeMillis() - 1000 * 60 * timeout);

        if (mMin.getDoubleValue() > BoxSettingsAPI.getDouble("LumiosityDarkLevel") * hysterezis) {
            triggerState(0, c, m, p);
        } else if (mMax.getDoubleValue() < BoxSettingsAPI.getDouble("LumiosityDarkLevel")) {
            triggerState(1, c, m, p);
        }
    }

    private void parameterBATT_CHARGEActions(Event e) { //Door Sensor
        //Заряд аккумуляторов меньше 30%

        int timeout = 1; //Минута

        Parameter p = (Parameter) e.data.get("parameter");
        Measurement m = (Measurement) e.data.get("measurement");

        Measurement mMax = measurementsCache.getMaxMeasurementFrom(p, System.currentTimeMillis() - 1000 * 60 * timeout);
        Measurement mMin = measurementsCache.getMinMeasurementFrom(p, System.currentTimeMillis() - 1000 * 60 * timeout);
        //Parameter chargeEnabledP = AppData.parametersStorage.getParameter(AppData.parametersStorage.resolveAlias("CHARGE_ENABLED"));
        // boolean chargeEnabled = measurementsCache.getLastMeasurement(chargeEnabledP).getBooleanValue();
        Condition c = getConditionByAlias("BAT_CHARGE_LESS_30");

        if (mMax.getIntegerValue() < BoxSettingsAPI.getDouble("ChargeAlertCritical")) {
            triggerState(1, c, m, p);
        } else if (mMin.getIntegerValue() > BoxSettingsAPI.getDouble("ChargeAlertCritical")) {
            triggerState(0, c, m, p);
        }
        //между 30 и 50
        c = getConditionByAlias("BAT_CHARGE_BETWEEN_30_50");

        if (mMax.getIntegerValue() < BoxSettingsAPI.getDouble("ChargeAlertMinor") && mMin.getIntegerValue() > BoxSettingsAPI.getDouble("ChargeAlertCritical")) {
            triggerState(1, c, m, p);
        } else if (mMin.getIntegerValue() > BoxSettingsAPI.getDouble("ChargeAlertMinor") || mMax.getIntegerValue() < BoxSettingsAPI.getDouble("ChargeAlertCritical")) {
            triggerState(0, c, m, p);
        }
    }

    private void parameterNIGHTTIMEActions(Event e) {

        Condition c = getConditionByAlias("NIGHTTIME_IS_NIGHT");
        Parameter p = (Parameter) e.data.get("parameter");
        Measurement m = (Measurement) e.data.get("measurement");

        if (m.getBooleanValue()) {

            triggerState(1, c, m, p);
        } else {
            triggerState(0, c, m, p);
        }

        boolean lampDarkSensorScript = "true".equalsIgnoreCase(BoxSettingsAPI.get("Lamp1DarkSensorScript"));
        c = getConditionByAlias("LAMP1_NIGHT_SCRIPT");
        if (m.getBooleanValue()) {
            triggerState(1, c, m, p, lampDarkSensorScript);
        } else {
            triggerState(0, c, m, p, lampDarkSensorScript);
        }

        lampDarkSensorScript = "true".equalsIgnoreCase(BoxSettingsAPI.get("Lamp2DarkSensorScript"));
        c = getConditionByAlias("LAMP2_NIGHT_SCRIPT");
        if (m.getBooleanValue()) {
            triggerState(1, c, m, p, lampDarkSensorScript);
        } else {
            triggerState(0, c, m, p, lampDarkSensorScript);
        }
    }

    private void parameterDOOR_SENSORActions(Event e) { //Door Sensor
        //Сработал датчик открывания двери
        Condition c = getConditionByAlias("DOOR_SENSOR_OPEN");
        Parameter p = (Parameter) e.data.get("parameter");
        Measurement m = (Measurement) e.data.get("measurement");

        if (m.getBooleanValue()) {
            if (BoxMode.isArmed()) {
                p.setState(Parameter.State.ALARM);
            }
            triggerState(1, c, m, p);
        } else {
            p.setState(Parameter.State.OK);
        }
        if (!m.getBooleanValue()) {
            triggerState(0, c, m, p);
        }

    }

    private void parameterWET_SENSORActions(Event e) { //Wet Sensor
        //Сработал датчик открывания двери
        Condition c = getConditionByAlias("WET_SENSOR_ALARM");
        Parameter p = (Parameter) e.data.get("parameter");
        Measurement m = (Measurement) e.data.get("measurement");

        if (m.getBooleanValue()) {
            p.setState(Parameter.State.ALARM);

            triggerState(1, c, m, p);
        } else {
            p.setState(Parameter.State.OK);

            triggerState(0, c, m, p);
        }

    }

    private void parameterVAC_SENSORActions(Event e) { //AC Voltage

        int VACTimeout = 5; //минут
        Parameter p = (Parameter) e.data.get("parameter");

        //последние 5 минут нет электричества
        Condition c = getConditionByAlias("VAC_SENSOR_POWER_LOSS");
        Measurement mMax = measurementsCache.getMaxMeasurementFrom(p, System.currentTimeMillis() - 1000 * 60 * VACTimeout);
        Measurement mMin = measurementsCache.getMinMeasurementFrom(p, System.currentTimeMillis() - 1000 * 60 * VACTimeout);
        if (mMax != null && mMin != null) {
            if (mMax.getDoubleValue() < BoxSettingsAPI.getDouble("VACAlertMin")) {
                triggerState(1, c, mMax, p);
            } else if (mMax.getDoubleValue() < BoxSettingsAPI.getDouble("VACAlertMax") && mMin.getDoubleValue() > BoxSettingsAPI.getDouble("VACAlertMin")) {
                triggerState(0, c, mMax, p);
            }
        }
        //Опасный для электроприборов скачок напряжения электросети
        Measurement m = measurementsCache.getLastMeasurement(p);
        c = getConditionByAlias("VAC_SENSOR_UNSTABLE");
        if (m.getDoubleValue() > BoxSettingsAPI.getDouble("VACAlertMax") || m.getDoubleValue() < BoxSettingsAPI.getDouble("VACAlertMin")) {

            triggerState(1, c, m, p);
            if (p.getState() != Parameter.State.ALARM) {
                p.setState(Parameter.State.ALARM);
                HashMap data = new HashMap();
                data.put("uart_command", "charge=off"); //disable charging if power is NOT ok
                Event reaction = new Event("internal_uart_command", data, Event.Type.USER_ACTION);
                AppData.eventManager.newEvent(reaction);
            }

        } else {
            triggerState(0, c, m, p);
            if (p.getState() != Parameter.State.OK) {
                p.setState(Parameter.State.OK);
                HashMap data = new HashMap();
                data.put("uart_command", "charge=on"); //enable charging if power is ok
                Event reaction = new Event("internal_uart_command", data, Event.Type.USER_ACTION);
                AppData.eventManager.newEvent(reaction);
            }
        }
    }

    private void triggerState(int newState, Condition c, Measurement m, Parameter p) {
        triggerState(newState, c, m, p, true);
    }

    private void triggerState(int newState, Condition c, Measurement m, Parameter p, boolean doRunActions) {
        if (c == null || c.state == newState) {
            return;
        }
        c.setState(newState);
        if (doRunActions) {
            runActions(newState, c, m.toStringValue(), p.getAlias());
        }
    }

    private void runActions(Condition c, String value, String parameterAlias) {
        runActions(null, c, value, parameterAlias);
    }

    private void runActions(Integer newState, Condition c, String value, String parameterAlias) {

        for (Action a : c.actions) {
            if (a.boxMode != null && !a.boxMode.toString().equals(BoxMode.string())) {
                continue;//только те actions, что актуальны для текщего режима, остальное пропускаем
            }
            if (newState != null && a.conditionState != null && !a.conditionState.equals(newState)) {
                continue; //только те actions, что актуальны для текущего состояния, остальное пропускаем
            }

            HashMap data = new HashMap();
            if (a.notificationText != null) {
                String message = a.notificationText.replaceAll("%VALUE%", value);
                Notification n = new Notification(a.severity, parameterAlias, message, new Date(), null, c.alias, c.state);
                data.put("Notification", n);

            }
            Event reaction = new Event(a.eventName, data, Event.Type.BEHAVIOR_EVENT, a.module);
            AppData.eventManager.newEvent(reaction);
        }
    }

}
