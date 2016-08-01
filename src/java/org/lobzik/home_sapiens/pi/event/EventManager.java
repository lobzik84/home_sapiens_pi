/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi.event;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.lobzik.home_sapiens.pi.modules.Module;

/**
 *
 * @author lobzik
 */
public class EventManager extends Thread {

    private static final Map<Event.Type, List> subscribers = new HashMap();
    private static final List<Event> eventList = new LinkedList();
    private static boolean run = true;
    private static EventManager instance = null;

    private EventManager() {

    }

//TODO subscription by event name?
    public static EventManager getInstance() {
        if (instance == null) {
            instance = new EventManager(); //lazy init

        }
        return instance;
    }

    @Override
    public void run() {
        setName(this.getClass().getSimpleName() + "-Thread");
        while (run) {
            try {

                while (eventList.size() > 0) {
                    Event e = eventList.remove(0);
                    if (subscribers.get(e.type) != null) {
                        List<Module> subscribersList = subscribers.get(e.type);
                        //System.out.println("Event type " + e.type + ", notifying " + subscribersList.size() + " subscribers");
                        for (Module m : subscribersList) {
                            m.handleEvent(e);
                            //System.out.println("Notifying " + m.getModuleName());

                        }
                    }               
                }
                
                try {
                    synchronized (this) {
                        wait();
                    }
                } catch (InterruptedException ie) {
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void newEvent(Event event) {
        //System.out.println("New event! " + event.data);
        if (event == null) return;
        eventList.add(event);
        synchronized (this) {
            notify();
        }
    }

    public void finish() {
        run = false;
        synchronized (this) {
            notify();
        }
    }

    public static void subscribeForEventType(Module module, Event.Type type) {

        if (subscribers.get(type) == null) {
            List<Module> modulesList = new LinkedList();
            subscribers.put(type, modulesList);
        }
        if (!subscribers.get(type).contains(module))
            subscribers.get(type).add(module); //subscribe only once
    }

}
