/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi;

import java.io.File;
import java.util.HashMap;
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
    public static final UsersPublicKeysCache usersPublicKeysCache = UsersPublicKeysCache.getInstance();
    public static final UsersSessionsStorage sessions = new UsersSessionsStorage();//  storage class with time limits
    private static File soundWorkDir = null;
    private static File graphicsWorkDir = null;
    
    private static File captureWorkDir = null;
    
    public static int testStage = 0;

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

    /**
     * @return the soundWorkDir
     */
    public static File getSoundWorkDir() {
        return soundWorkDir;
    }

    /**
     * @param aSoundWorkDir the soundWorkDir to set
     */
    public static void setSoundWorkDir(File aSoundWorkDir) {
        if (soundWorkDir == null) {
            soundWorkDir = aSoundWorkDir;
        }
    }

        /**
     * @return the soundWorkDir
     */
    public static File getGraphicsWorkDir() {
        return graphicsWorkDir;
    }

    /**
     * @param aSoundWorkDir the soundWorkDir to set
     */
    public static void setGraphicsWorkDir(File aGraphicsWorkDir) {
        if (graphicsWorkDir == null) {
            graphicsWorkDir = aGraphicsWorkDir;
        }
    }
    
    /**
     * @return the captureWorkDir
     */
    public static File getCaptureWorkDir() {
        return captureWorkDir;
    }

    
    /**
     * @param aCaptureWorkDir the captureWorkDir to set
     */
    public static void setCaptureWorkDir(File aCaptureWorkDir) {
        if (captureWorkDir == null) {
            captureWorkDir = aCaptureWorkDir;
        }
    }
}
