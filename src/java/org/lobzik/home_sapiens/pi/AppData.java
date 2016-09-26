/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi;

import java.io.File;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.sql.Connection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import javax.naming.Context;
import javax.naming.InitialContext;
import org.apache.commons.dbcp2.BasicDataSource;
import org.lobzik.home_sapiens.pi.event.EventManager;
import org.lobzik.tools.Tools;
import org.lobzik.tools.db.mysql.DBSelect;
import org.lobzik.tools.db.mysql.DBTools;

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
    public static final UsersSessionsStorage sessions = new UsersSessionsStorage();//  storage class with time limits
    public static final HashMap<Integer, RSAPublicKey> usersKeys = new HashMap();
    private static File soundWorkDir = null;
    private static File captureWorkDir = null;

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

    public static void initUserPublicKey(int userId) {
        String sSQL = "select id, salt, verifier, public_key from users where status = 1 and id=" + userId;

        try (Connection conn = AppData.dataSource.getConnection()) {
            List<HashMap> resList = DBSelect.getRows(sSQL, conn);
            if (resList.size() > 0) {
                String publicKey = (String) resList.get(0).get("public_key");
                BigInteger modulus = new BigInteger(publicKey, 16);
                RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, BoxCommonData.RSA_E);
                KeyFactory factory = KeyFactory.getInstance("RSA");
                RSAPublicKey usersPublicKey = (RSAPublicKey) factory.generatePublic(spec);
                usersKeys.put(userId, usersPublicKey);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
