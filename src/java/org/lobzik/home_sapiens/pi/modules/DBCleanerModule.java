/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi.modules;

import java.sql.Connection;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.lobzik.home_sapiens.entity.Parameter;
import org.lobzik.home_sapiens.pi.AppData;
import org.lobzik.home_sapiens.pi.BoxCommonData;
import org.lobzik.home_sapiens.pi.ConnJDBCAppender;
import org.lobzik.home_sapiens.pi.event.Event;
import org.lobzik.home_sapiens.pi.event.EventManager;
import org.lobzik.tools.db.mysql.DBSelect;
import org.lobzik.tools.db.mysql.DBTools;

/**
 *
 * @author lobzik
 */
public class DBCleanerModule extends Thread implements Module {

    public final String MODULE_NAME = this.getClass().getSimpleName();
    private static DBCleanerModule instance = null;
    private static Connection conn = null;
    private static Logger log = null;
    private static boolean run = true;
    private static boolean clearingInProgress = false;
    private static final int DAYS_TO_STORE_RAW_SENSORS_DATA = 5;
    private static final int DAYS_TO_STORE_DEBUG_MSG = 3;

    private DBCleanerModule() { //singleton
    }

    public static DBCleanerModule getInstance() {
        if (instance == null) {
            instance = new DBCleanerModule(); //lazy init
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
    public void run() {
        try {
            EventManager.subscribeForEventType(this, Event.Type.TIMER_EVENT);
            while (run) {
                synchronized (this) {
                    wait();
                }
                if (run && !clearingInProgress) {
                    clearingInProgress = true;
                    doClearing();
                    clearingInProgress = false;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void finish() {
        log.info("Stopping " + instance.MODULE_NAME);
        run = false;
        synchronized (instance) {
            instance.notify();
        }
    }

    private void doClearing() {
        try {
            conn = DBTools.openConnection(BoxCommonData.dataSourceName);
            DBSelect.executeStatement("delete from logs where level = 'DEBUG' and datediff(curdate(), dated) > " + DAYS_TO_STORE_DEBUG_MSG, null, conn);
            log.info("Log table cleared");

            for (Integer paramId : AppData.parametersStorage.getParameterIds()) {
                Parameter p = AppData.parametersStorage.getParameter(paramId);
                try {
                    log.debug("Clearing parameter " + p.getName());
                    switch (p.getType()) {
                        //todo группировать значения с разрешением, скажем, час
                        case DOUBLE:
                            String sSQL = "insert into sensors_data \n"
                                    + "(select \n"
                                    + "null,\n"
                                    + "gsd.parameter_id, \n"
                                    + "gsd.date,\n"
                                    + "gsd.value_d,\n"
                                    + "null as value_s, \n"
                                    + "null as value_b, \n"
                                    + "null as value_i,\n"
                                    + "gsd.value_min,\n"
                                    + "max(sd1.date_min) as date_min,\n"
                                    + "gsd.value_max,\n"
                                    + "max(sd2.date_max) as date_max,\n"
                                    + "null as transfer_count, \n"
                                    + "1 as grouped\n"
                                    + "from (SELECT \n"
                                    + "sd.parameter_id, concat(year(sd.date), \"-\", month(sd.date), \"-\", day(sd.date), \" \", hour(sd.date), \":00:00\") as date, \n"
                                    + "avg(sd.value_d) as value_d, \n"
                                    + "min(sd.value_min) as value_min, \n"
                                    + "max(sd.value_max) as value_max\n"
                                    + " FROM sensors_data sd\n"
                                    + "where sd.grouped = 0 and sd.parameter_id = " + p.getId() + " \n"
                                    + "and datediff(curdate(), sd.date) > " + DAYS_TO_STORE_RAW_SENSORS_DATA + " \n"
                                    + "group by  concat(year(sd.date), \"-\", month(sd.date), \"-\", day(sd.date), \" \", hour(sd.date), \":00:00\")) gsd\n"
                                    + "inner join sensors_data sd1 on  concat(year(sd1.date), \"-\", month(sd1.date), \"-\", day(sd1.date), \" \", hour(sd1.date), \":00:00\") = gsd.date and sd1.parameter_id=gsd.parameter_id and sd1.value_min=gsd.value_min\n"
                                    + "inner join sensors_data sd2 on  concat(year(sd2.date), \"-\", month(sd2.date), \"-\", day(sd2.date), \" \", hour(sd2.date), \":00:00\") = gsd.date and sd2.parameter_id=gsd.parameter_id and sd2.value_max=gsd.value_max\n"
                                    + "group by gsd.date)";
                            DBSelect.executeStatement(sSQL, null, conn);
                            DBSelect.executeStatement("delete from sensors_data where parameter_id = " + p.getId() + " and datediff(curdate(), date) > " + DAYS_TO_STORE_RAW_SENSORS_DATA, null, conn);
                            break;

                        case BOOLEAN:
                            //todo transfer counts sum
                            sSQL = "insert into sensors_data \n"
                                    + "(select \n"
                                    + "null,\n"
                                    + "sd.parameter_id, \n"
                                    + "concat(year(sd.date), \"-\", month(sd.date), \"-\", day(sd.date), \" \", hour(sd.date), \":00:00\") as date,\n"
                                    + "null as value_d,\n"
                                    + "null as value_s, \n"
                                    + "null as value_b, \n"
                                    + "null as value_i,\n"
                                    + "null as value_min,\n"
                                    + "null as date_min,\n"
                                    + "null as value_max,\n"
                                    + "null as date_max,\n"
                                    + "sum(sd.transfer_count) as transfer_count, \n"
                                    + "1 as grouped\n"
                                    + "from sensors_data sd\n"
                                    + "where sd.grouped = 0 and sd.parameter_id = " + p.getId() + " \n"
                                    + "and datediff(curdate(), sd.date) > " + DAYS_TO_STORE_RAW_SENSORS_DATA + " \n"
                                    + "group by  concat(year(sd.date), \"-\", month(sd.date), \"-\", day(sd.date), \" \", hour(sd.date), \":00:00\")\n"
                                    + ")";
                            DBSelect.executeStatement(sSQL, null, conn);
                            DBSelect.executeStatement("delete from sensors_data where parameter_id = " + p.getId() + " and datediff(curdate(), date) > " + DAYS_TO_STORE_RAW_SENSORS_DATA, null, conn);
                            break;

                        case INTEGER:

                            DBSelect.executeStatement("delete from sensors_data where parameter_id = " + p.getId() + " and datediff(curdate(), date) > " + DAYS_TO_STORE_RAW_SENSORS_DATA, null, conn);
                            //todo sum? think this over
                            break;

                        default:
                            DBSelect.executeStatement("delete from sensors_data where parameter_id = " + p.getId() + " and datediff(curdate(), date) > " + DAYS_TO_STORE_RAW_SENSORS_DATA, null, conn);
                            break;
                    }
                    log.debug("Done.");
                } catch (Exception ex) {
                    log.error("Error while grouping parameter " + p.getName() + " : " + ex.getMessage());
                }
            }

        } catch (Exception e) {
            log.error("Error while DB Clearing: " + e.getMessage());

        } finally {
            DBTools.closeConnection(conn);
        }
    }

    @Override
    public synchronized void handleEvent(Event e) {
        if (e.type == Event.Type.TIMER_EVENT && e.name.equals("db_clearing")) {
            if (!clearingInProgress) {
                synchronized (this) {
                    notify();
                }
            }
        }
    }

}
