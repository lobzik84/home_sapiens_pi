/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi.modules;

import java.math.BigInteger;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.lobzik.home_sapiens.entity.Measurement;
import org.lobzik.home_sapiens.pi.AppData;
import org.lobzik.home_sapiens.pi.BoxCommonData;
import org.lobzik.home_sapiens.pi.BoxMode;
import org.lobzik.home_sapiens.pi.ConnJDBCAppender;
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
public class BehaviorModule implements Module {

    public final String MODULE_NAME = this.getClass().getSimpleName();
    private static Logger log = null;
    private static BehaviorModule instance = null;

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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleEvent(Event e) {
        switch (e.type) {
            case SYSTEM_EVENT:
                searchForLocationByCellId(e.data);
                break;

            case SYSTEM_MODE_CHANGED:
                if (BoxMode.isArmed()) log.warn("Включен режим Охрана") ;
                else if (BoxMode.isIdle()) log.warn("Включен режим Хозяин Дома") ;
                
                break;

            case PARAMETER_CHANGED:
                switch (e.name) {
                    case "mic_noise":
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
                    throw new Exception("CellId " + cid + " with lac " + lac + " not found in DB");
                }
                double latitude = Tools.parseDouble(resList.get(0).get("lat"), 0);
                double longitude = Tools.parseDouble(resList.get(0).get("lon"), 0);
                log.info("Detected location " + latitude + ", " + longitude);
                HashMap eventData = new HashMap();
                eventData.put("latitude", latitude);
                eventData.put("longitude", longitude);
                Event event = new Event("location_detected", eventData, Event.Type.SYSTEM_EVENT);
                AppData.eventManager.newEvent(event);
            }

        } catch (Exception e) {
            log.error("Error getting locatuion: " + e.getMessage());
        }
    }

}
