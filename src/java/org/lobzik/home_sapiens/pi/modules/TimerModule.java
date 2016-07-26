/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi.modules;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.*;
import org.lobzik.home_sapiens.pi.AppData;
import org.lobzik.home_sapiens.pi.BoxCommonData;
import org.lobzik.home_sapiens.pi.event.Event;
import org.lobzik.tools.Tools;
import org.lobzik.tools.db.mysql.DBSelect;
import org.lobzik.tools.db.mysql.DBTools;

/**
 *
 * @author lobzik
 */
public class TimerModule implements Module {

    public final String MODULE_NAME = this.getClass().getSimpleName();
    private static TimerModule instance = null;
    private static HashMap<Integer, Timer> timers = new HashMap<Integer, Timer>();
    private final static String datePattern = "yyyy.MM.dd HH:mm:ss";

    private TimerModule() { //singleton
    }

    public static TimerModule getInstance() {
        if (instance == null) {
            instance = new TimerModule(); //lazy init
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
            Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName);
            List<HashMap> timerDBList = getDBTimers(conn);
            conn.close();
            for (HashMap timerMap : timerDBList) {
                startTimer(timerMap);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleEvent(Event e) {
    }

    public static void finish() {
        for (int id : timers.keySet()) {
            Timer timer = timers.get(id);
            timer.cancel();
        }
    }


    private void startTimer(HashMap timerMap) throws Exception {
        Date startDate = (Date) timerMap.get("START_DATE");
        int period = Tools.parseInt(timerMap.get("PERIOD"), 0);
        int id = Tools.parseInt(timerMap.get("ID"), 0);
        int enabled = Tools.parseInt(timerMap.get("ENABLED"), 0);
        if (enabled == 0) {
            return;
        }
        if (startDate == null && period == 0) {
            return;
        }
        if (startDate == null) {
            startDate = new Date();
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        if (cal.getTimeInMillis() <= System.currentTimeMillis() && period > 0) {
            while (cal.getTimeInMillis() <= System.currentTimeMillis()) {
                cal.add(Calendar.SECOND, period);
            }
        }

        String name = (String) timerMap.get("NAME");
        Timer timer = new Timer(name + "-Timer", true);
        SignalTask signalTask = new SignalTask(name, null);
        if (period > 0) {
            timer.scheduleAtFixedRate(signalTask, cal.getTime(), period * 1000);
        } else {
            timer.schedule(signalTask, cal.getTime());
        }
        timers.put(id, timer);
    }

    private List<HashMap> getDBTimers(Connection conn) throws Exception {
        String sSQL = " SELECT * FROM TIMER ";
        SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
        List<HashMap> result = DBSelect.getRows(sSQL, conn);
        for (HashMap timer : result) {
            if (timer.get("START_DATE") != null) {
                timer.put("START_DATE_STR", sdf.format(timer.get("START_DATE")));
            } else {
                timer.put("START_DATE_STR", "Сразу");
            }
            int period = Tools.parseInt(timer.get("PERIOD"), -1);
            int periodUnits = 1;
            if (period % 60 == 0) {
                periodUnits = 60;
                period = period / 60;
                if (period % 60 == 0) {
                    periodUnits = 3600;
                    period = period / 60;

                }
            }
            timer.put("PERIOD_U", period);
            timer.put("PERIOD_UNITS", periodUnits);
        }
        return result;
    }
}

class SignalTask extends TimerTask {

    private String name = null;
    private Map data = null;

    public SignalTask(String nameP, Map dataP) {
        super();
        name = nameP;
        data = dataP;
    }

    @Override
    public void run() {
        //System.out.println("Timer signal: " + name + "=" + value);

        Event e = new Event(name, data, Event.Type.TIMER_EVENT);

        AppData.eventManager.newEvent(e);
    }
}
