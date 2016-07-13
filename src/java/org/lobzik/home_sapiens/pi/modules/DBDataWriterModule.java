/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi.modules;

import java.sql.Connection;
import java.util.Date;
import java.util.HashMap;
import org.lobzik.home_sapiens.entity.Measurement;
import org.lobzik.home_sapiens.pi.BoxCommonData;
import org.lobzik.home_sapiens.pi.event.Event;
import org.lobzik.home_sapiens.pi.event.EventManager;
import org.lobzik.tools.db.mysql.DBTools;

/**
 *
 * @author lobzik
 */
public class DBDataWriterModule implements Module {

    public final String MODULE_NAME = this.getClass().getSimpleName();
    private static DBDataWriterModule instance = null;
    private static Connection conn = null;

    private DBDataWriterModule() { //singleton
    }

    public static DBDataWriterModule getInstance() {
        if (instance == null) {
            instance = new DBDataWriterModule(); //lazy init
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
            conn = DBTools.openConnection(BoxCommonData.dataSourceName);
            //ready to accept events, subscribing
            EventManager.subscribeForEventType(this, Event.Type.PARAMETER_UPDATED);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void handleEvent(Event e) {
        if (e.type != Event.Type.PARAMETER_UPDATED) {
            return; // JIC
        }
        HashMap dataMap = new HashMap();
        dataMap.put("parameter_id", e.data.get("parameter_id"));
        Measurement m = (Measurement) e.data.get("measurement_new");
        dataMap.put("value_d", m.getDoubleValue());
        dataMap.put("date", new Date(m.getTime()));
        System.out.println("About to insert " + dataMap.toString());
        try {
            DBTools.insertRow("sensors_data", dataMap, conn);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void finish() {
        DBTools.closeConnection(conn);
    }
}
