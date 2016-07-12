/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi.modules;

import java.sql.Connection;
import org.lobzik.home_sapiens.pi.BoxCommonData;
import org.lobzik.home_sapiens.pi.event.Event;
import org.lobzik.tools.db.mysql.DBTools;

/**
 *
 * @author lobzik
 */
public class VideoModule implements Module {

    public final String MODULE_NAME = this.getClass().getSimpleName();
    private static VideoModule instance = null;
    private static Connection conn = null;

    private VideoModule() { //singleton
    }

    public static VideoModule getInstance() {
        if (instance == null) {
            instance = new VideoModule(); //lazy init
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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleEvent(Event e) {
    }

    public static void finish() {
        DBTools.closeConnection(conn);
    }
}