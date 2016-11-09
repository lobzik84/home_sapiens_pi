/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi.event;

import java.util.Date;
import java.util.Map;

/**
 *
 * @author lobzik
 */
public class Event {
    //public Module origin;
    public Date date;
    public String name;
    public Map data;
    public Type type;
    
    public enum Type
    {   
        PARAMETER_CHANGED,
        PARAMETER_UPDATED,
        USER_ACTION,
        ACTION_COMPLETED,
        SYSTEM_SETTING_CHANGED,
        SYSTEM_MODE_CHANGED,
        TIMER_EVENT,
        SYSTEM_EVENT,
        REACTION_EVENT
    };
    
    public Event(String name, Map data, Event.Type type ) {
        this.date = new Date();
        this.name = name;
        this.data = data;
        this.type = type;
    }
    
    public Event.Type getType() {
        return this.type;
    }
}
