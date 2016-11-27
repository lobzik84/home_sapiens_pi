/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi.modules;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.lobzik.home_sapiens.entity.Measurement;
import org.lobzik.home_sapiens.entity.Parameter;
import org.lobzik.home_sapiens.pi.AppData;
import org.lobzik.home_sapiens.pi.ConnJDBCAppender;
import org.lobzik.home_sapiens.pi.event.Event;
import org.lobzik.home_sapiens.pi.event.EventManager;
import static org.lobzik.home_sapiens.pi.modules.ModemModule.test;
import org.lobzik.home_sapiens.pi.weather.WeatherGetter;
import org.lobzik.home_sapiens.pi.weather.entity.Forecast;
import org.lobzik.tools.Tools;

/**
 *
 * @author lobzik
 */
public class WeatherModule extends Thread implements Module {

    public final String MODULE_NAME = this.getClass().getSimpleName();
    private static WeatherModule instance = null;
    private static boolean run = true;
    private static Logger log = null;
    private static final long MAXTIMEDIFF = 24 * 3600 * 1000l; //forecast older then 1 day is not valid
    private static double latitude = 55.784884;
    private static double longitude = 37.613116; //default city is the most default
    private static Calendar sunrise = null;
    private static Calendar sunset = null;

    private WeatherModule() { //singleton
    }

    public static WeatherModule getInstance() {
        if (instance == null) {
            instance = new WeatherModule(); //lazy init
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
        setName(this.getClass().getSimpleName() + "-Thread");
        log.info("Starting " + getName());
        //defaults
        sunrise = new GregorianCalendar();
        sunrise.set(Calendar.HOUR, 8);
        sunrise.set(Calendar.MINUTE, 0);

        sunset = new GregorianCalendar();
        sunset.set(Calendar.HOUR, 21);
        sunset.set(Calendar.MINUTE, 0);

        EventManager.subscribeForEventType(this, Event.Type.TIMER_EVENT);
        EventManager.subscribeForEventType(this, Event.Type.SYSTEM_EVENT);
        while (run) {
            try {
                synchronized (this) {
                    wait();
                }
            } catch (InterruptedException e) {
            }
            if (run) {
                try {
                    Location location = new Location(latitude + "", longitude + "");
                    SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(location, Locale.getDefault().getDisplayName());

                    sunrise = calculator.getOfficialSunriseCalendarForDate(Calendar.getInstance());
                    sunset = calculator.getOfficialSunsetCalendarForDate(Calendar.getInstance());
                    log.debug("Daytime is " + Tools.getFormatedDate(sunrise.getTime(), "HH:mm") + " - " + Tools.getFormatedDate(sunset.getTime(), "HH:mm"));

                    List<Forecast> forecast = WeatherGetter.getWeatherInfo(latitude, longitude).getList();
                    if (!forecast.isEmpty()) {
                        HashMap eventData = new HashMap();
                        eventData.put("forecast", forecast);
                        Event ev = new Event("forecast_loaded", eventData, Event.Type.SYSTEM_EVENT);
                        AppData.eventManager.newEvent(ev);
                        Forecast mostActual = null;
                        long diff = System.currentTimeMillis();
                        for (Forecast f : forecast) {
                            //searching for closest forecast

                            long timeDiff = f.getTime().getTime() - System.currentTimeMillis();
                            if (timeDiff < 0) {
                                timeDiff = timeDiff * -1;
                            }
                            if (timeDiff < MAXTIMEDIFF && timeDiff < diff) {
                                diff = timeDiff;
                                mostActual = f;
                            }
                        }
                        if (mostActual != null) {
                            double outTemp = mostActual.getTemperature();
                            log.debug("Forecast loaded, out temp " + outTemp + " for " + mostActual.getTime());
                            //TODO осадки, облачность
                            int paramId = AppData.parametersStorage.resolveAlias("OUTSIDE_TEMP");
                            if (paramId > 0) {
                                Parameter p = AppData.parametersStorage.getParameter(paramId);
                                Measurement m = new Measurement(p, outTemp, mostActual.getTime().getTime());
                                if (!test) {
                                    eventData = new HashMap();
                                    eventData.put("parameter", p);
                                    eventData.put("measurement", m);
                                    Event event = new Event("forecast updated", eventData, Event.Type.PARAMETER_UPDATED);

                                    AppData.eventManager.newEvent(event);
                                }
                            }
                            paramId = AppData.parametersStorage.resolveAlias("RAIN");
                            if (paramId > 0) {
                                Parameter p = AppData.parametersStorage.getParameter(paramId);
                                Measurement m = new Measurement(p, mostActual.getPrecipitation(), mostActual.getTime().getTime());
                                if (!test) {
                                    eventData = new HashMap();
                                    eventData.put("parameter", p);
                                    eventData.put("measurement", m);
                                    Event event = new Event("forecast updated", eventData, Event.Type.PARAMETER_UPDATED);

                                    AppData.eventManager.newEvent(event);
                                }
                            }
                            paramId = AppData.parametersStorage.resolveAlias("CLOUDS");
                            if (paramId > 0) {
                                Parameter p = AppData.parametersStorage.getParameter(paramId);
                                Measurement m = new Measurement(p, (double) mostActual.getClouds(), mostActual.getTime().getTime());
                                if (!test) {
                                    eventData = new HashMap();
                                    eventData.put("parameter", p);
                                    eventData.put("measurement", m);
                                    Event event = new Event("forecast updated", eventData, Event.Type.PARAMETER_UPDATED);

                                    AppData.eventManager.newEvent(event);
                                }
                            }
                        } else {
                            log.error("Failed to load actual forecast");
                        }

                    }

                } catch (Throwable e) {
                    log.error("Error loading forecast: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public void handleEvent(Event e) {
        if (e.type == Event.Type.SYSTEM_EVENT && e.name.equals("location_detected")) {
            latitude = Tools.parseDouble(e.data.get("latitude"), latitude);
            longitude = Tools.parseDouble(e.data.get("longitude"), longitude);
            log.info("Coordinates updated: " + e.data);
            notifyThread();
        }
        if (e.type == Event.Type.TIMER_EVENT && e.name.equals("get_forecast")) {
            log.debug("Getting forecast");
            notifyThread();
        }
        if (e.type == Event.Type.TIMER_EVENT && e.name.equals("update_display")) {//раз в минуту
            Calendar current = new GregorianCalendar();
            current.setTimeZone(sunrise.getTimeZone());
            int dayMinutes = current.get(Calendar.HOUR_OF_DAY) * 60 + current.get(Calendar.MINUTE);
            int riseMins = sunrise.get(Calendar.HOUR_OF_DAY) * 60 + sunrise.get(Calendar.MINUTE);
            int setMins = sunset.get(Calendar.HOUR_OF_DAY) * 60 + sunset.get(Calendar.MINUTE);
            
            if (dayMinutes > riseMins && dayMinutes < setMins) {
                //DAYTIME
                Parameter pNightTime = AppData.parametersStorage.getParameter(AppData.parametersStorage.resolveAlias("NIGHTTIME"));
                Measurement isDay = new Measurement(pNightTime, false);
                HashMap eventData = new HashMap();
                eventData.put("parameter", pNightTime);
                eventData.put("measurement", isDay);
                Event newE = new Event("daytime", eventData, Event.Type.PARAMETER_UPDATED);
                AppData.eventManager.newEvent(newE);
            } else {
                //NIGHTTIME
                Parameter pNightTime = AppData.parametersStorage.getParameter(AppData.parametersStorage.resolveAlias("NIGHTTIME"));
                Measurement isNight = new Measurement(pNightTime, true);
                HashMap eventData = new HashMap();
                eventData.put("parameter", pNightTime);
                eventData.put("measurement", isNight);
                Event newE = new Event("nighttime", eventData, Event.Type.PARAMETER_UPDATED);
                AppData.eventManager.newEvent(newE);
            }
        }

    }

    private void notifyThread() {
        synchronized (this) {
            notify();
        }
    }

    public static void finish() {
        run = false;
        instance.notifyThread();
    }
}
