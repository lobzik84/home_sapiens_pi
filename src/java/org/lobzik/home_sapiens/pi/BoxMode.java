/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import org.lobzik.tools.db.mysql.DBSelect;
import org.lobzik.tools.db.mysql.DBTools;

/**
 *
 * @author lobzik
 */
public class BoxMode {

    public static enum MODE {
        IDLE,
        ARMED
    }

    private static MODE Mode = MODE.IDLE;

    public static MODE initFromDB() {
        try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {
            List<HashMap> resList = DBSelect.getRows("select * from mode;", conn);
            if (resList.size() == 1) {
                String dbVal = (String) resList.get(0).get("box_mode");
                Mode = MODE.valueOf(dbVal);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return MODE.IDLE;
    }

    public static boolean isIdle() {
        return Mode == MODE.IDLE;
    }

    public static boolean isArmed() {
        return Mode == MODE.ARMED;
    }

    public static void setIdle() {
        if (Mode != MODE.IDLE) {
            Mode = MODE.IDLE;
            saveMode();
        }
    }

    public static void setArmed() {
        if (Mode != MODE.ARMED) {
            Mode = MODE.ARMED;
            saveMode();
        }
    }
    
    public static String string() {
        return Mode.toString();
    }

    private static void saveMode() {
        try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {
            String sSQL = "update mode set box_mode=\'" + Mode.toString() + "\';";
            DBSelect.executeStatement(sSQL, null, conn);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
