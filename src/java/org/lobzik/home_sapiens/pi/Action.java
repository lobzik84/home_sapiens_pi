/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi;

import org.lobzik.home_sapiens.pi.modules.BehaviorModule;

/**
 *
 * @author Nikolas
 */
    public class Action {
        public int id;
        public String alias;
        public String module;
        public String data;
        public WebNotification.Severity severity;
        
        public Action(int id, String alias, String module, String data, WebNotification.Severity severity){
            this.id = id;
            this.alias = alias;
            this.module = module;
            this.data = data;
            this.severity = severity;
        }
    }
