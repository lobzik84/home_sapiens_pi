/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi.behavior;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import org.lobzik.home_sapiens.pi.BoxCommonData;

import org.lobzik.tools.db.mysql.DBSelect;
import org.lobzik.tools.db.mysql.DBTools;

/**
 *
 * @author Nikolas
 */
    public class Condition {
    
        public int id;
        public int parameterId;
        public String alias;
        public String name;
        public int state;
        public List<Action> actions;
        
        public Condition(int id, int parameterId, String alias, String name, int state) {
            this.id = id;
            this.parameterId = parameterId;
            this.alias = alias;
            this.name = name;
            this.state = state;
            actions = new ArrayList<Action>();
        }
        
        public void addAction(Action action){
            actions.add(action);
        }            
        
        public void setState(int state){
            int oldState = this.state;
            this.state = state;
            try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {
                String sSQL = "update conditions set state=" + this.state + " where id = " + this.id + ";";
                DBSelect.executeStatement(sSQL, null, conn);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        public List<Action> getActions(String alias){
            List<Action> result = new ArrayList<Action>();
            for (Action a:actions){
                if (a.alias.equalsIgnoreCase(alias))
                    result.add(a);
            }
            return result;
        }
        
        
        public String getName() {
            return name;
        }

        public String getAlias() {
            return alias;
        }

        public int getId() {
            return id;
        }
    
        
    }