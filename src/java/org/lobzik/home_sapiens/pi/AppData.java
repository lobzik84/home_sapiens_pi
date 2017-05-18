/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.Query;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletContextEvent;
import org.apache.commons.dbcp2.BasicDataSource;
import org.lobzik.home_sapiens.pi.behavior.Notification;
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
    public static final HashMap<Integer, Notification> emailNotification = new HashMap();

    private static boolean runsAfterFailure = false;
    private static boolean runsAfterReboot = false;

    private static File soundWorkDir = null;
    private static File graphicsWorkDir = null;
    private static File backupWorkDir = null;
    private static File captureWorkDir = null;

    private static String localUrlContPath = null;

    public static final String RUN_FILE = "/usr/local/share/hs_app.run";
    public static final String REBOOT_FILE = "/usr/local/share/hs_app.reboot";

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

    public static void init(ServletContextEvent sce) {
        try {
            List<String> eps = getHTTPEndPoints();
            if (!eps.isEmpty()) {
                localUrlContPath = eps.get(0) + sce.getServletContext().getContextPath();
            }
            System.out.println("checking for run file");
            runsAfterFailure = new File(RUN_FILE).exists();
            if (runsAfterFailure) {
                System.err.println("!!!! Runs after failure!!!");
            }
            System.out.println("checking for reboot file");
            runsAfterReboot = new File(REBOOT_FILE).exists();
            if (runsAfterReboot) {
                System.out.println("Runs after reboot");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (localUrlContPath == null) {
            localUrlContPath = "http://localhost/" + sce.getServletContext().getContextPath();
        }
    }
    
    
    public static final boolean runsAfterFailure() {
        return runsAfterFailure;
    }

    public static final boolean runsAfterReboot() {
        return runsAfterReboot;
    }

    public static String getLocalUrlContPath() {
        return localUrlContPath;
    }

    /**
     * @return the backupWorkDir
     */
    public static File getBackupWorkDir() {
        return backupWorkDir;
    }

    /**
     * @param aBackupWorkDir the backupWorkDir to set
     */
    public static void setBackupWorkDir(File aBackupWorkDir) {
        backupWorkDir = aBackupWorkDir;
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

    public static List<String> getHTTPEndPoints() throws Exception { //tomcat-specific
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        Set<ObjectName> objs = mbs.queryNames(new ObjectName("*:type=Connector,*"),
                Query.match(Query.attr("protocol"), Query.value("HTTP/1.1")));
        String hostname = InetAddress.getLocalHost().getHostName();
        InetAddress[] addresses = InetAddress.getAllByName(hostname);
        ArrayList<String> endPoints = new ArrayList<String>();
        for (Iterator<ObjectName> i = objs.iterator(); i.hasNext();) {
            ObjectName obj = i.next();
            String scheme = mbs.getAttribute(obj, "scheme").toString();
            if (!scheme.toLowerCase().equals("http")) {
                continue;
            }
            String port = obj.getKeyProperty("port");
            for (InetAddress addr : addresses) {
                String host = addr.getHostAddress();
                String ep = scheme + "://" + host + ":" + port;
                endPoints.add(ep);
            }
        }
        return endPoints;
    }
}
