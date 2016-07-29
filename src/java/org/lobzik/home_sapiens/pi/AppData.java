/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.naming.Context;
import javax.naming.InitialContext;
import org.apache.commons.dbcp2.BasicDataSource;
import org.lobzik.home_sapiens.pi.event.EventManager;

/**
 *
 * @author lobzik
 */
public class AppData {

    public static final HashMap<String, Object> settings = new HashMap();
    public static final BasicDataSource dataSource;
    public static final EventManager eventManager = EventManager.getInstance(); //launches BEFORE AppListener called
    public static final ParametersStorage parametersStorage; //launches BEFORE AppListener called
    public static final MeasurementsCache measurementsCache;

    static {
        BasicDataSource ds = null;
        ParametersStorage ps = null;
        try {
            Context initCtx = new InitialContext();
            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            ds = (BasicDataSource) envCtx.lookup(BoxCommonData.dataSourceName);
            
            ps = ParametersStorage.getInstance();    
            
        } catch (Exception e) {
            System.err.println("Fatal error during initialization!");
            e.printStackTrace();
            System.exit(-1);
        }
        dataSource = ds;
        parametersStorage = ps;
        
        measurementsCache = MeasurementsCache.getInstance();;
        
        eventManager.start();
    }
    
    public static void init() {
        
    }
}
