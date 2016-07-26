/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.lobzik.home_sapiens.entity.Measurement;
import org.lobzik.home_sapiens.entity.Parameter;
import org.lobzik.tools.Tools;
import org.lobzik.tools.db.mysql.DBSelect;
import org.lobzik.tools.db.mysql.DBTools;

/**
 *
 * @author lobzik
 */
public class ParametersStorage {

    private final static HashMap<Integer, Parameter> storage = new HashMap();

    private static ParametersStorage instance = null;

    private ParametersStorage() {
    }

    public static ParametersStorage getInstance() throws Exception {
        if (instance == null) {
            instance = new ParametersStorage();
            Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName);
            instance.configure(DBSelect.getRows("select * from parameters", conn));
            conn.close();
                    
           
        }
        return instance;
    }

    private void configure(List<HashMap> dbParamsList) {
        for (HashMap h : dbParamsList) {
            int id = Tools.parseInt(h.get("id"), 0);
            Parameter p = new Parameter(id, (String) h.get("name"), (String) h.get("alias"), null, (String) h.get("description"), (String) h.get("format_pattern"), (String) h.get("unit"));
            storage.put(id, p);
        }
    }

    public int resolve(String name) {
        for (Integer id : storage.keySet()) {
            if (storage.get(id).getName().equals(name)) {
                return id;
            }
        }
        return -1;
    }

    public int resolveAlias(String alias) {
        for (Integer id : storage.keySet()) {
            if (alias.equals(storage.get(id).getAlias())) {
                return id;
            }
        }
        return -1;
    }

    public String resolve(int id) {
        Parameter p = storage.get(id);
        if (p == null) {
            return null;
        } else {
            return p.getName();
        }
    }

    public void set(int id, Measurement measurementP) {
        Parameter p = storage.get(id);
        if (p == null) {
            return;
        }
        p.set(measurementP);
    }

    public void setData(int id, Object data) {
        Parameter p = storage.get(id);
        if (p == null) {
            return;
        }
        p.setData(data);

    }

    public void set(String name,  Measurement measurementP) {
        set(resolve(name), measurementP);
    }

    public Measurement get(int id) {
        Parameter p = storage.get(id);
        if (p == null) {
            return null;
        } else {
            return p.get();
        }
    }

    public Measurement get(String name) {
        return get(resolve(name));
    }
/*
    public String getF(int id) {
        Parameter p = storage.get(id);
        if (p == null) {
            return null;
        } else {
            return p.getF();
        }
    }
    public String getF(String name) {
        return getF(resolve(name));
    }

    public String getFU(int id) {
        Parameter p = storage.get(id);
        if (p == null) {
            return null;
        } else {
            return p.getFU();
        }
    }

    public String getFU(String name) {
        return getFU(resolve(name));
    }

    public Date getUpdateTime(int id) {
        Parameter p = storage.get(id);
        if (p == null) {
            return null;
        } else {
            return p.getUpdateTime();
        }
    }

    public Date getUpdateTime(String name) {
        return getUpdateTime(resolve(name));
    }
*/
    public String getDescription(int id) {
        Parameter p = storage.get(id);
        if (p == null) {
            return null;
        } else {
            return p.getDescription();
        }
    }

    public String getDescription(String name) {
        return getDescription(resolve(name));
    }

    public Parameter getParameter(int id) {
        return storage.get(id);
    }

    public Parameter getParameter(String name) {
        return getParameter(resolve(name));
    }
    
    public Set<Integer> getParameterIds() {
        return storage.keySet();
    }
            
}
