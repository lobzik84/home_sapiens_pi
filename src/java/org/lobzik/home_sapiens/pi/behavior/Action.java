/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi.behavior;

import org.lobzik.home_sapiens.pi.BoxMode;

/**
 *
 * @author Nikolas
 */
    public class Action {
        public int id;
        public String alias;
        public String module;
        public String eventName;
        public String notificationText;
        public Notification.Severity severity;
        public BoxMode.MODE boxMode;
        public Integer conditionState;
        
        public Action(int id, String alias, String module, String eventName, String notificationText, Notification.Severity severity, BoxMode.MODE boxMode, Integer conditionState){
            this.id = id;
            this.alias = alias;
            this.module = module;
            this.eventName = eventName;
            this.notificationText = notificationText;
            this.severity = severity;
            this.boxMode = boxMode;
            this.conditionState = conditionState;
        }
    }
