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
            case SYSTEM_MODE_CHANGED:
                if (BoxMode.isArmed()) {
                    HashMap data = new HashMap();

                    Notification n = new Notification(Notification.Severity.INFO, null, "Включен режим Охрана", new Date(), null);
                    data.put("Notification", n);

                    Event reaction = new Event("log_record", data, Event.Type.BEHAVIOR_EVENT, "LogModule");
                    AppData.eventManager.newEvent(reaction);
                } else if (BoxMode.isIdle()) {
                    HashMap data = new HashMap();

                    Notification n = new Notification(Notification.Severity.INFO, null, "Включен режим Хозяин Дома", new Date(), null);
                    data.put("Notification", n);

                    Event reaction = new Event("log_record", data, Event.Type.BEHAVIOR_EVENT, "LogModule");
                    AppData.eventManager.newEvent(reaction);

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

    public void parameterMIC_NOISEActions(Event e) { //Датчик движения

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

    public void parameterPIR_SENSORActions(Event e) { //Датчик движения

        Parameter p = (Parameter) e.data.get("parameter");
        Measurement m = (Measurement) e.data.get("measurement");
        //работает более 15 сек.
        Condition c = getConditionByAlias("PIR_SENSOR_ALARM");
        int PIRTimeout = 15; //секунд
        int transferTrueCount = measurementsCache.getTransferTrueCountFrom(p, System.currentTimeMillis() - 1000 * PIRTimeout);

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

    public void parameterGAS_SENSORActions(Event e) { //Gas Sensor
        //Сработал датчик газа
        Condition c = getConditionByAlias("GAS_SENSOR_ALARM");
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

    public void parameterINTERNAL_HUMIDITYActions(Event e) { //Датчик влажности
/*
        String alias = null;
        Parameter p = (Parameter) e.data.get("parameter");
        Measurement m = (Measurement) e.data.get("measurement");

        //Вышла и более 5 минут подряд находится вне установленных пределов
        alias = "INTERNAL_HUMIDITY_OUT_OF_BOUNDS";
        int VACTimeout = 5; //минут
        Measurement mMax = measurementsCache.getMaxMeasurementFrom(p, System.currentTimeMillis() - 1000 * 60 * VACTimeout);
        Measurement mMin = measurementsCache.getMinMeasurementFrom(p, System.currentTimeMillis() - 1000 * 60 * VACTimeout);
        if (mMax != null && mMin != null) {
            if ((mMax.getDoubleValue() < BoxSettingsAPI.getDouble("InHumAlertMin")) || (mMin.getDoubleValue() > BoxSettingsAPI.getDouble("InHumAlertMax"))) {
                Condition c = getConditionByAlias(alias);
                if (c.state == 0) {
                    c.setState(1);
                    runStandardActions(c, m, p);
                }
                c = getConditionByAlias("INTERNAL_HUMIDITY_BACK_TO_NORMAL");
                c.setState(0);
            } else {
                Condition c = getConditionByAlias(alias);
                if (c.state == 1) {
                    c.setState(0);
                    c = getConditionByAlias("INTERNAL_HUMIDITY_BACK_TO_NORMAL");
                    if (c.state == 0) {
                        c.setState(1);
                        runStandardActions(c, m, p);
                    }
                }
            }
        }
         */
    }

    public void parameterINTERNAL_TEMPActions(Event e) { //Датчик температуры
//явно всё плохо, надо внимательно разбираться
        /*   String alias = null;
        Parameter p = (Parameter) e.data.get("parameter");
        Measurement m = (Measurement) e.data.get("measurement");

        //не работает 
        int VACTimeout = 5; //минут
        alias = "INTERNAL_TEMP_SENSOR_FAILURE";
        Measurement mMin = measurementsCache.getMinMeasurementFrom(p, System.currentTimeMillis() - 1000 * 60 * VACTimeout);
        if (mMin == null) {
            Condition c = getConditionByAlias(alias);
            if (c.state == 0) {
                c.setState(1);
                runStandardActions(c, m, p);
            }
            c = getConditionByAlias("VAC_SENSOR_POWER_RECOVERED");
            c.setState(0);
        } else {
            Condition c = getConditionByAlias(alias);
            c.setState(0);
        }
        //очистка
        if (mMin != null) {
            if (getConditionByAlias(alias + "_ARMED").state == 1) {
                getConditionByAlias(alias + "_ARMED").state = 0;
            }
            if (getConditionByAlias(alias + "_IDLE").state == 1) {
                getConditionByAlias(alias + "_IDLE").state = 0;
            }
            Condition c = getConditionByAlias("VAC_SENSOR_POWER_RECOVERED");
            if (c.state == 0) {
                c.setState(1);
                runStandardActions(c, m, p);
            }

            c = getConditionByAlias("VAC_SENSOR_POWER_RECOVERED" + (BoxMode.isArmed() ? "_IDLE" : "_ARMED"));
            c.setState(1);
        }

        //Температура воздуха снижается быстрее, чем на 5 градусов в час
        alias = "INTERNAL_TEMP_FAST_FALLING";
        VACTimeout = 60; //минут
        Measurement mMax = measurementsCache.getMaxMeasurementFrom(p, System.currentTimeMillis() - 1000 * 60 * VACTimeout);
        mMin = measurementsCache.getMinMeasurementFrom(p, System.currentTimeMillis() - 1000 * 60 * VACTimeout);
        if (mMax != null && mMin != null) {
            if ((mMax.getDoubleValue() - mMin.getDoubleValue() > 5) && (mMax.getTime() < mMin.getTime())) {
                Condition c = getConditionByAlias(alias);
                if (c.state == 0) {
                    c.setState(1);
                    runStandardActions(c, m, p);
                }
            } else {
                Condition c = getConditionByAlias(alias);
                if (c.state == 1) {
                    c.setState(0);
                    c = getConditionByAlias("VAC_SENSOR_POWER_RECOVERED");
                    if (c.state == 0) {
                        c.setState(1);
                        runStandardActions(c, m, p);
                    }
                }
            }
        }

        //Температура воздуха Растет быстрее чем на 5 градусов за 10 минут
        alias = "INTERNAL_TEMP_FAST_RISING";
        VACTimeout = 10; //минут
        mMax = measurementsCache.getMaxMeasurementFrom(p, System.currentTimeMillis() - 1000 * 60 * VACTimeout);
        mMin = measurementsCache.getMinMeasurementFrom(p, System.currentTimeMillis() - 1000 * 60 * VACTimeout);
        if (mMax != null && mMin != null) {
            if ((mMax.getDoubleValue() - mMin.getDoubleValue() > 5) && (mMax.getTime() > mMin.getTime())) {
                Condition c = getConditionByAlias(alias);
                if (c.state == 0) {
                    c.setState(1);
                    runStandardActions(c, m, p);
                }
            } else {
                Condition c = getConditionByAlias(alias);
                if (c.state == 1) {
                    c.setState(0);
                    c = getConditionByAlias("VAC_SENSOR_POWER_RECOVERED");
                    if (c.state == 0) {
                        c.setState(1);
                        runStandardActions(c, m, p);
                    }
                }
            }
        }

        //Вышла и более 5 минут подряд находится вне установленных пределов
        alias = "INTERNAL_TEMP_OUT_OF_BOUNDS";
        VACTimeout = 5; //минут
        mMax = measurementsCache.getMaxMeasurementFrom(p, System.currentTimeMillis() - 1000 * 60 * VACTimeout);
        mMin = measurementsCache.getMinMeasurementFrom(p, System.currentTimeMillis() - 1000 * 60 * VACTimeout);
        if (mMax != null && mMin != null) {
            if ((mMax.getDoubleValue() < BoxSettingsAPI.getDouble("InTempAlertMin")) || (mMin.getDoubleValue() > BoxSettingsAPI.getDouble("InTempAlertMax"))) {
                Condition c = getConditionByAlias(alias);
                if (c.state == 0) {
                    c.setState(1);
                    runStandardActions(c, m, p);
                }
            } else {
                Condition c = getConditionByAlias(alias);
                if (c.state == 1) {
                    c.setState(0);
                    c = getConditionByAlias("VAC_SENSOR_POWER_RECOVERED");
                    if (c.state == 0) {
                        c.setState(1);
                        runStandardActions(c, m, p);
                    }
                }
            }
        }
         */
    }

    public void parameterBATT_TEMPActions(Event e) { //Перегрев аккумулятора
        //Перегрев аккумулятора
        /*
        String alias = "BATT_TEMP_OVERHEAT";
        Parameter p = (Parameter) e.data.get("parameter");
        Measurement m = (Measurement) e.data.get("measurement");
        //Parameter chargeEnabledP = AppData.parametersStorage.getParameter(AppData.parametersStorage.resolveAlias("CHARGE_ENABLED"));
        //boolean chargeEnabled = measurementsCache.getLastMeasurement(chargeEnabledP).getBooleanValue();

        if (m.getDoubleValue() > BoxSettingsAPI.getDouble("VBatTempAlertMax")) {
            Condition c = getConditionByAlias(alias);
            if (c.state == 0) {
                c.setState(1);
                runStandardActions(c, m, p);
            }
        } else {
            Condition c = getConditionByAlias(alias);
            c.setState(0);
        }*/
    }

    public void parameterBATT_CHARGEActions(Event e) { //Door Sensor
        /* //Заряд аккумуляторов меньше 30%
        String alias = "BAT_CHARGE_LESS_30";
        Parameter p = (Parameter) e.data.get("parameter");
        Measurement m = (Measurement) e.data.get("measurement");
        Parameter chargeEnabledP = AppData.parametersStorage.getParameter(AppData.parametersStorage.resolveAlias("CHARGE_ENABLED"));
        boolean chargeEnabled = measurementsCache.getLastMeasurement(chargeEnabledP).getBooleanValue();

        if (m.getIntegerValue() < BoxSettingsAPI.getDouble("VBatAlertCritical") && !chargeEnabled) {
            Condition c = getConditionByAlias(alias);
            if (c.state == 0) {
                c.setState(1);
                runStandardActions(c, m, p);
            }
            c = getConditionByAlias("BAT_CHARGE_NORMAL");
            c.setState(0);

        }

        //Заряд аккумуляторов < 50% и > 30%
        alias = "BAT_CHARGE_BETWEEN_30_50";
        if (m.getIntegerValue() >= BoxSettingsAPI.getDouble("VBatAlertCritical") && m.getIntegerValue() < BoxSettingsAPI.getDouble("VBatAlertMinor") && !chargeEnabled) {
            Condition c = getConditionByAlias(alias);
            if (c.state == 0) {
                c.setState(1);
                runStandardActions(c, m, p);
            }
            c = getConditionByAlias("BAT_CHARGE_NORMAL");
            c.setState(0);

        }

        if (chargeEnabled) {
            Condition c = getConditionByAlias("BAT_CHARGE_LESS_30");
            c.setState(0);
            c = getConditionByAlias("BAT_CHARGE_BETWEEN_30_50");
            c.setState(0);

        }
         */
    }

    public void parameterDOOR_SENSORActions(Event e) { //Door Sensor
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

    public void parameterVAC_SENSORActions(Event e) { //AC Voltage

        int VACTimeout = 5; //минут
        Parameter p = (Parameter) e.data.get("parameter");

        //последние 5 минут нет электричества
        Condition c = getConditionByAlias("VAC_SENSOR_POWER_LOSS");
        Measurement mMax = measurementsCache.getMaxMeasurementFrom(p, System.currentTimeMillis() - 1000 * 60 * VACTimeout);
        Measurement mMin = measurementsCache.getMinMeasurementFrom(p, System.currentTimeMillis() - 1000 * 60 * VACTimeout);
        if (mMax != null && mMin != null) {
            if (mMax.getDoubleValue() < BoxSettingsAPI.getDouble("VACAlertMin")) {
                triggerState(1, c, mMax, p);
            } else {
                triggerState(0, c, mMax, p);
            }
        }

        //напряжение более 5 минут в норме после отказа
        c = getConditionByAlias("VAC_SENSOR_POWER_RECOVERED");
        if (mMax != null && mMin != null) {
            if (mMax.getDoubleValue() < BoxSettingsAPI.getDouble("VACAlertMax") && mMin.getDoubleValue() > BoxSettingsAPI.getDouble("VACAlertMin")) {
                triggerState(1, c, mMax, p);
            } else {
                triggerState(0, c, mMax, p);
            }
        }
        //Опасный для электроприборов скачок напряжения электросети
        Measurement m = measurementsCache.getLastMeasurement(p);
        c = getConditionByAlias("VAC_SENSOR_UNSTABLE");
        if (m.getDoubleValue() > BoxSettingsAPI.getDouble("VACAlertMax") || m.getDoubleValue() < BoxSettingsAPI.getDouble("VACAlertMin")) {
            p.setState(Parameter.State.ALARM);
            triggerState(1, c, m, p);
        } else {
            p.setState(Parameter.State.OK);
            triggerState(0, c, m, p);
        }
    }

    public void triggerState(int newState, Condition c, Measurement m, Parameter p) {
        if (c.state == newState) {
            return;
        }
        c.setState(newState);
        for (Action a : c.actions) {
            if (a.boxMode != null && !a.boxMode.toString().equals(BoxMode.string())) {
                continue;//только те actions, что актуальны для текщего режима, остальное пропускаем
            }
            if (a.conditionState != null && (int) a.conditionState != newState) {
                continue; //только те actions, что актуальны для текущего состояния, остальное пропускаем
            }
            HashMap data = new HashMap();
            if (a.notificationText != null) {
                String message = a.notificationText.replaceAll("%VALUE%", m.toStringValue());
                Notification n = new Notification(a.severity, p.getAlias(), message, new Date(), null, c.alias, newState);
                data.put("Notification", n);

            }
            Event reaction = new Event(a.eventName, data, Event.Type.BEHAVIOR_EVENT, a.module);
            AppData.eventManager.newEvent(reaction);
        }
    }

}
