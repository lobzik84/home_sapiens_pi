/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi.modules;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import org.lobzik.home_sapiens.pi.AppData;
import org.lobzik.home_sapiens.pi.BoxCommonData;
import org.lobzik.home_sapiens.pi.event.Event;
import org.lobzik.home_sapiens.pi.event.EventManager;
import org.lobzik.tools.Tools;
import org.lobzik.tools.db.mysql.DBSelect;
import org.lobzik.tools.db.mysql.DBTools;

public class TestModule implements Module {

    public final String MODULE_NAME = this.getClass().getSimpleName();
    private static TestModule instance = null;

    private TestModule() { //singleton
    }

    public static TestModule getInstance() {
        if (instance == null) {
            instance = new TestModule(); //lazy init
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
            try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {
                List<HashMap> resList = DBSelect.getRows("select stage from test_stages order by id desc limit 1;", conn);
                if (resList.isEmpty()) {
                    AppData.testStage = 1;
                } else {
                    AppData.testStage = Tools.parseInt(resList.get(0).get("stage"), 1);
                }
            }

            EventManager.subscribeForEventType(this, Event.Type.USER_ACTION);

            Event e = new Event("internal_sensors_poll", new HashMap(), Event.Type.TIMER_EVENT);
            Thread.sleep(3000);//чтобы дождалось
            AppData.eventManager.newEvent(e); //чтобы загрузило
            e = new Event("get_capture", null, Event.Type.USER_ACTION);
            AppData.eventManager.newEvent(e); //чтобы забрало картинку
            
            
            //DisplayModule.getInstance().start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleEvent(Event e) {
        if (e.type == Event.Type.USER_ACTION && e.name.equals("build-graph")) {

        }
    }

    public static void finish() {

    }

}
