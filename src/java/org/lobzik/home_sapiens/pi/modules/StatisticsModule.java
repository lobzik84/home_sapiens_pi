/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi.modules;

import java.util.HashMap;
import org.lobzik.home_sapiens.pi.BoxSettingsAPI;
import org.lobzik.home_sapiens.pi.event.Event;
import org.lobzik.home_sapiens.pi.event.EventManager;
public class StatisticsModule implements Module {

    public final String MODULE_NAME = this.getClass().getSimpleName();
    private static StatisticsModule instance = null;


    private StatisticsModule() { //singleton
    }

    public static StatisticsModule getInstance() {
        if (instance == null) {
            instance = new StatisticsModule(); //lazy init
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
            EventManager.subscribeForEventType(this, Event.Type.TIMER_EVENT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleEvent(Event e) {
        if (e.type == Event.Type.TIMER_EVENT && e.name.equals("email_statistics")) {
            String html = "<html><head></head><body>statictics!</body></html>";//todo get from jsp
            String email = BoxSettingsAPI.get("NotificationsEmail");
            HashMap data = new HashMap();
            data.put("mail_to", email);
            data.put("mail_text", html);
            data.put("mail_subject", "Управдом " + BoxSettingsAPI.get("BoxName") + ": статистика");
            Event mail = new Event("send_email", data, Event.Type.BEHAVIOR_EVENT);
        }
    }
  

    public static void finish() {

    }
    
    

}