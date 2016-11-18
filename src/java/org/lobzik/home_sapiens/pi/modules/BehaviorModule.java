/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi.modules;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.lobzik.home_sapiens.entity.Measurement;
import org.lobzik.home_sapiens.entity.Parameter;
import org.lobzik.home_sapiens.pi.Action;
import org.lobzik.home_sapiens.pi.AppData;
import static org.lobzik.home_sapiens.pi.AppData.measurementsCache;
import org.lobzik.home_sapiens.pi.BoxCommonData;
import org.lobzik.home_sapiens.pi.BoxMode;
import org.lobzik.home_sapiens.pi.BoxMode.MODE;
import org.lobzik.home_sapiens.pi.BoxSettingsAPI;
import org.lobzik.home_sapiens.pi.Condition;
import org.lobzik.home_sapiens.pi.ConnJDBCAppender;
import org.lobzik.home_sapiens.pi.MeasurementsCache;
import org.lobzik.home_sapiens.pi.WebNotification;
import org.lobzik.home_sapiens.pi.event.Event;
import org.lobzik.home_sapiens.pi.event.EventManager;
import static org.lobzik.home_sapiens.pi.modules.ModemModule.STATUS_NEW;
import static org.lobzik.home_sapiens.pi.modules.ModemModule.test;
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
    private static Connection conn = null;
    private static String mobileNumber = null;
    private static String email = null;
    private static List<Condition> conditions = null;

    private BehaviorModule() { //singleton
    }

    public static BehaviorModule getInstance() {

        if (instance == null) {
            instance = new BehaviorModule(); //lazy init
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

            if (test) {
                conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/hs?useUnicode=true&amp;characterEncoding=utf8&user=hsuser&password=hspass");
            } else {
                conn = DBTools.openConnection(BoxCommonData.dataSourceName);
            }

            try {
                String sSQL = "SELECT * FROM users";
                List<HashMap> userData = DBSelect.getRows(sSQL, conn);
                if (userData.size() > 0) {
                    HashMap ud = userData.get(0);
                    mobileNumber = Tools.getStringValue(ud.get("login"), "").replace("(", "").replace(")", "").replaceAll("-", "");
                    email = Tools.getStringValue(ud.get("email"), "");
                }

            } catch (Exception ee) {
                ee.printStackTrace();
            }

            try {
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
                                MODE.valueOf(Tools.getStringValue(c.get("box_mode"), "")),
                                Tools.parseInt(c.get("state"), 0));
                        conditions.add(cond);
                    }
                }

            } catch (Exception ee) {
                ee.printStackTrace();
            }

            try {
                String sSQL = "SELECT * FROM actions order by condition_id";
                List<HashMap> actionsList = DBSelect.getRows(sSQL, conn);
                if (actionsList.size() > 0) {
                    for (HashMap a : actionsList) {
                        int conditionId = Tools.parseInt(a.get("condition_id"), 0);
                        if (conditionId > 0) {
                            Action act = new Action(
                                    Tools.parseInt(a.get("id"), 0),
                                    Tools.getStringValue(a.get("alias"), ""),
                                    Tools.getStringValue(a.get("module"), ""),
                                    Tools.getStringValue(a.get("data"), ""),
                                    WebNotification.Severity.valueOf(Tools.getStringValue(a.get("severity"), ""))
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
                    log.warn("Включен режим Охрана");
                } else if (BoxMode.isIdle()) {
                    log.warn("Включен режим Хозяин Дома");
                }

                break;

            case PARAMETER_UPDATED:
                switch (e.name) {
                    case "mic_noise": //just for debug of microphone module, to be removed
                        Measurement m = (Measurement) e.data.get("measurement");
                        HashMap data = new HashMap();
                        if (m.getBooleanValue()) {
                            data.put("uart_command", "led1=on");
                        } else {
                            data.put("uart_command", "led1=off");
                        }
                        Event reaction = new Event("internal_uart_command", data, Event.Type.USER_ACTION);
                        AppData.eventManager.newEvent(reaction);
                        break;

                }

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
                                //parameterBATT_TEMPActions(e);
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

    public static void actionLog(WebNotification.Severity severity, String message) {
        switch (severity) {
            case INFO:
                log.info(message);
                break;

            case OK:
                log.warn(message);
                break;

            case ALERT:
                log.error(message);
                break;

            case ALARM:
                log.fatal(message);
                break;
        }
    }

    public static void actionSMS(WebNotification.Severity severity, String message) {
        HashMap data = new HashMap();
        data.put("message", message);
        data.put("recipient", mobileNumber);
        Event e = new Event("send_sms", data, Event.Type.USER_ACTION);
        AppData.eventManager.newEvent(e);
    }

    public static void actionEmail(WebNotification.Severity severity, String message) {
        HashMap data = new HashMap();
        data.put("message", message);
        data.put("recipient", email);
        Event e = new Event("send_email", data, Event.Type.USER_ACTION);
        AppData.eventManager.newEvent(e);
    }

    public static void actionDisplay(WebNotification.Severity severity, String message, Parameter p, Condition c, Action a) {

        /*
        HashMap data = new HashMap();
         data.put("message", message);
         Event e = new Event("update_display", data, Event.Type.SYSTEM_EVENT);
         AppData.eventManager.newEvent(e);
         */
        WebNotification dn = new WebNotification(a.severity, p.getAlias(), message, new Date(), null, c.getAlias());
        HashMap data3 = new HashMap();
        data3.put("DisplayNotification", dn);
        data3.put("ConditionAlias", c.getAlias());
        Event reaction3 = new Event((c.state == 0 ? "delete_" : "") + "display_notification", data3, Event.Type.REACTION_EVENT);
        AppData.eventManager.newEvent(reaction3);

        /*
        HashMap data3 = new HashMap();
        data3.put("ConditionAlias", "VAC_SENSOR_OUT_RANGE");
        Event reaction3 = new Event("delete_display_notification", data3, Event.Type.REACTION_EVENT);
        AppData.eventManager.newEvent(reaction3);
         */
    }

    public static void actionWebNotify(WebNotification.Severity severity, String message, String parameterAlias) {

        WebNotification wn = new WebNotification(WebNotification.Severity.ALERT, parameterAlias, message, new Date(), new Date(System.currentTimeMillis() + 1800000));
        HashMap data = new HashMap();
        data.put("WebNotification", wn);
        Event reaction = new Event("web_notification", data, Event.Type.REACTION_EVENT);
        AppData.eventManager.newEvent(reaction);
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

    public static Condition getConditionByParameterAndState(List<Condition> conditions, int parameterId, MODE mode) {
        Condition result = null;
        for (Condition c : conditions) {
            if (c.parameterId == parameterId && c.boxMode == mode) {
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

    public void parameterBATT_TEMPActions(Event e) { //Door Sensor
        //Заряд аккумуляторов меньше 30%
        String alias = "BAT_CHARGE_LESS_30";
        Parameter p = (Parameter) e.data.get("parameter");
        Measurement m = (Measurement) e.data.get("measurement");
        Parameter chargeEnabledP = AppData.parametersStorage.getParameter(AppData.parametersStorage.resolveAlias("CHARGE_ENABLED"));
        boolean chargeEnabled = measurementsCache.getLastMeasurement(chargeEnabledP).getBooleanValue();

        if (m.getIntegerValue() < BoxSettingsAPI.getDouble("VBatAlertCritical") && !chargeEnabled) {
            Condition c = getConditionByAlias(alias + (BoxMode.isArmed() ? "_ARMED" : "_IDLE"));
            c.setState(1);
            runStandardActions(c, m, p);
            c = getConditionByAlias("BAT_CHARGE_NORMAL_IDLE");
            c.setState(0);
            c = getConditionByAlias("BAT_CHARGE_NORMAL_ARMED");
            c.setState(0);

            /*action TODO	
            Записать полный бэкап на сервер
            Выключить одну камеру
            Выключить экран
            отправить историю всех событий на email хозяина 
             */
        }

        //Заряд аккумуляторов < 50% и > 30%
        alias = "BAT_CHARGE_BETWEEN_30_50";
        if (m.getIntegerValue() >= BoxSettingsAPI.getDouble("VBatAlertCritical") && m.getIntegerValue() < BoxSettingsAPI.getDouble("VBatAlertMinor") && !chargeEnabled) {
            Condition c = getConditionByAlias(alias + (BoxMode.isArmed() ? "_ARMED" : "_IDLE"));
            c.setState(1);
            runStandardActions(c, m, p);
            c = getConditionByAlias("BAT_CHARGE_NORMAL_IDLE");
            c.setState(0);
            c = getConditionByAlias("BAT_CHARGE_NORMAL_ARMED");
            c.setState(0);
        }

        if (chargeEnabled) {
            Condition c = getConditionByAlias("BAT_CHARGE_LESS_30_ARMED");
            c.setState(0);
            c = getConditionByAlias("BAT_CHARGE_LESS_30_IDLE");
            c.setState(0);
            c = getConditionByAlias("BAT_CHARGE_BETWEEN_30_50_ARMED");
            c.setState(0);
            c = getConditionByAlias("BAT_CHARGE_BETWEEN_30_50_IDLE");
            c.setState(0);
        }

    }

    public void parameterBATT_CHARGEActions(Event e) { //Door Sensor
        //Заряд аккумуляторов меньше 30%
        String alias = "BAT_CHARGE_LESS_30";
        Parameter p = (Parameter) e.data.get("parameter");
        Measurement m = (Measurement) e.data.get("measurement");
        Parameter chargeEnabledP = AppData.parametersStorage.getParameter(AppData.parametersStorage.resolveAlias("CHARGE_ENABLED"));
        boolean chargeEnabled = measurementsCache.getLastMeasurement(chargeEnabledP).getBooleanValue();

        if (m.getIntegerValue() < BoxSettingsAPI.getDouble("VBatAlertCritical") && !chargeEnabled) {
            Condition c = getConditionByAlias(alias + (BoxMode.isArmed() ? "_ARMED" : "_IDLE"));
            c.setState(1);
            runStandardActions(c, m, p);
            c = getConditionByAlias("BAT_CHARGE_NORMAL_IDLE");
            c.setState(0);
            c = getConditionByAlias("BAT_CHARGE_NORMAL_ARMED");
            c.setState(0);
        }

        //Заряд аккумуляторов < 50% и > 30%
        alias = "BAT_CHARGE_BETWEEN_30_50";
        if (m.getIntegerValue() >= BoxSettingsAPI.getDouble("VBatAlertCritical") && m.getIntegerValue() < BoxSettingsAPI.getDouble("VBatAlertMinor") && !chargeEnabled) {
            Condition c = getConditionByAlias(alias + (BoxMode.isArmed() ? "_ARMED" : "_IDLE"));
            c.setState(1);
            runStandardActions(c, m, p);
            c = getConditionByAlias("BAT_CHARGE_NORMAL_IDLE");
            c.setState(0);
            c = getConditionByAlias("BAT_CHARGE_NORMAL_ARMED");
            c.setState(0);
        }

        if (chargeEnabled) {
            Condition c = getConditionByAlias("BAT_CHARGE_LESS_30_ARMED");
            c.setState(0);
            c = getConditionByAlias("BAT_CHARGE_LESS_30_IDLE");
            c.setState(0);
            c = getConditionByAlias("BAT_CHARGE_BETWEEN_30_50_ARMED");
            c.setState(0);
            c = getConditionByAlias("BAT_CHARGE_BETWEEN_30_50_IDLE");
            c.setState(0);
        }

    }

    public void parameterDOOR_SENSORActions(Event e) { //Door Sensor
        //Сработал датчик открывания двери
        String alias = "DOOR_SENSOR_OPEN";
        Parameter p = (Parameter) e.data.get("parameter");
        Measurement m = (Measurement) e.data.get("measurement");

        if (m.getBooleanValue()) {
            Condition c = getConditionByAlias(alias + (BoxMode.isArmed() ? "_ARMED" : "_IDLE"));
            c.setState(1);
            runStandardActions(c, m, p);
        } else {
            Condition c = getConditionByAlias(alias + (BoxMode.isArmed() ? "_ARMED" : "_IDLE"));
            c.setState(0);
        }
    }

    public void parameterVAC_SENSORActions(Event e) { //AC Voltage
        int VACTimeout = 1; //минут
        Parameter p = (Parameter) e.data.get("parameter");

        //последние 5 минут нет электричества
        String alias = "VAC_SENSOR_POWER_LOSS";
        Measurement mMax = measurementsCache.getMaxMeasurementFrom(p, System.currentTimeMillis() - 1000 * 60 * VACTimeout);
        Measurement mMin = measurementsCache.getMinMeasurementFrom(p, System.currentTimeMillis() - 1000 * 60 * VACTimeout);
        if (mMax.getDoubleValue() < BoxSettingsAPI.getDouble("VACAlertMin")) {
            Condition c = getConditionByAlias(alias + (BoxMode.isArmed() ? "_ARMED" : "_IDLE"));
            c.setState(1);
            runStandardActions(c, mMax, p);
            c = getConditionByAlias("VAC_SENSOR_POWER_RECOVERED_ARMED");
            c.setState(0);
            c = getConditionByAlias("VAC_SENSOR_POWER_RECOVERED_IDLE");
            c.setState(0);
        }

        //напряжение более 5 минут в норме после отказа
        alias = "VAC_SENSOR_POWER_RECOVERED";
        if (mMax.getDoubleValue() < BoxSettingsAPI.getDouble("VACAlertMax") && mMin.getDoubleValue() > BoxSettingsAPI.getDouble("VACAlertMin")) {
            Condition c = getConditionByAlias(alias + (BoxMode.isArmed() ? "_ARMED" : "_IDLE"));
            c.setState(1);
            runStandardActions(c, mMax, p);
            c = getConditionByAlias("VAC_SENSOR_POWER_LOSS_ARMED");
            c.setState(0);
            c = getConditionByAlias("VAC_SENSOR_POWER_LOSS_IDLE");
            c.setState(0);
        }

        //Опасный для электроприборов скачок напряжения электросети
        Measurement m = measurementsCache.getLastMeasurement(p);
        alias = "VAC_SENSOR_UNSTABLE";
        if (m.getDoubleValue() > BoxSettingsAPI.getDouble("VACAlertMax") || m.getDoubleValue() < BoxSettingsAPI.getDouble("VACAlertMin")) {
            Condition c = getConditionByAlias(alias + (BoxMode.isArmed() ? "_ARMED" : "_IDLE"));
            c.setState(1);
            runStandardActions(c, m, p);
        }
    }

    public void runStandardActions(Condition c, Measurement m, Parameter p) {
        if (c.state <= 0) {
            for (Action a : c.actions) {
                String message = a.data.replaceAll("ДД ВР", Tools.getFormatedDate(new Date(m.getTime()), "dd.MM.yyyy HH:mm"));
                message = message.replaceAll("<%=VALUE%>", m.toStringValue());
                switch (a.module) {
                    case "DisplayModule":
                        actionDisplay(a.severity, message, p, c, a);
                        break;
                    case "Logger":
                        actionLog(a.severity, message);
                        break;
                    case "ModemModule":
                        actionSMS(a.severity, message);
                        break;
                    case "TunnelClientModule":
                        actionEmail(a.severity, message);
                        break;
                    case "WebNotificationsModule":
                        actionWebNotify(a.severity, message, p.getAlias());
                        break;
                }
            }
        }
    }

}
