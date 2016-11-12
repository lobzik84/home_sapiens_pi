/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.sql.Connection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.lobzik.home_sapiens.entity.Measurement;
import org.lobzik.home_sapiens.entity.Parameter;
import org.lobzik.home_sapiens.entity.UsersSession;
import org.lobzik.home_sapiens.pi.event.Event;
import org.lobzik.home_sapiens.pi.modules.VideoModule;
import org.lobzik.home_sapiens.pi.modules.WebNotificationsModule;
import org.lobzik.tools.Tools;
import org.lobzik.tools.db.mysql.DBSelect;
import org.lobzik.tools.db.mysql.DBTools;

/**
 * class for generating and parsing JSON to be used in TunnelClient and
 * JSONServlet
 *
 * @author lobzik
 */
public class JSONAPI {

    public static void doEncryptedUserCommand(JSONObject json, RSAPrivateKey boxKey, RSAPublicKey userPublicKey) throws Exception {
        String commandName = json.getString("command_name");
        HashMap commandData = new HashMap();
        //JSONObject jsonData = json.getJSONObject("command_data");
        String digest = json.getString("digest");
        String keyCipher = json.getString("key_cipher");
        String encrypted = json.getString("command_data");

        Cipher cipherRSA = Cipher.getInstance("RSA");
        cipherRSA.init(Cipher.DECRYPT_MODE, boxKey);
        String aesKeyStr = new String(cipherRSA.doFinal(Tools.toByteArray(keyCipher)));
        byte[] rawKey = Tools.toByteArray(aesKeyStr);
        IvParameterSpec ivSpec = new IvParameterSpec(rawKey);

        SecretKeySpec skeySpec = new SecretKeySpec(rawKey, "AES");
        Cipher cipher = Cipher.getInstance("AES/CFB/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivSpec);

        byte[] decrypted = cipher.doFinal(Tools.toByteArray(encrypted));

        Signature verifier = Signature.getInstance("SHA256withRSA");
        verifier.initVerify(userPublicKey);
        verifier.update(decrypted);
        boolean valid = verifier.verify(Tools.toByteArray(digest));
        if (!valid) {
            throw new Exception("Invalid digest for command");
        }
        String data = new String(decrypted);
        JSONObject jsonData = new JSONObject(data);

        switch (commandName) {
            case "save_settings":
                JSONObject settingsJson = jsonData.getJSONObject("settings");
                Map<String, String> smap = new HashMap();
                for (String key : settingsJson.keySet()) {
                    smap.put(key, settingsJson.getString(key));
                }
                BoxSettingsAPI.set(smap);
                break;

            case "switch_mode":
                String mode = jsonData.getString("mode");
                switch (mode) {
                    case "ARMED":
                        BoxMode.setArmed();
                        Event event = new Event(commandName, commandData, Event.Type.SYSTEM_MODE_CHANGED);
                        AppData.eventManager.newEvent(event);
                        break;

                    case "IDLE":
                        BoxMode.setIdle();
                        event = new Event(commandName, commandData, Event.Type.SYSTEM_MODE_CHANGED);
                        AppData.eventManager.newEvent(event);
                        break;
                }
                break;

            default:
                for (String key : jsonData.keySet()) {
                    commandData.put(key, jsonData.get(key));
                }
                Event event = new Event(commandName, commandData, Event.Type.USER_ACTION);
                AppData.eventManager.newEvent(event);
                break;

        }
    }

    public static JSONObject getSettingsJSON(RSAPublicKey publicKey, String login) throws Exception {
        JSONObject reply = new JSONObject();
        JSONObject settingsJSON = new JSONObject();
        Map<String, String> settings = BoxSettingsAPI.getSettingsMap();
        for (String name : settings.keySet()) {
            settingsJSON.put(name, settings.get(name));
        }
        settingsJSON.put("UserLogin", login);
        reply.put("settings", settingsJSON);
        return reply;
    }

    public static JSONObject getEncryptedParametersJSON(RSAPublicKey publicKey) throws Exception {

        JSONObject paramsJson = new JSONObject();
        ParametersStorage ps = AppData.parametersStorage;
        MeasurementsCache mc = AppData.measurementsCache;
        for (Integer pId : ps.getParameterIds()) {
            Parameter p = ps.getParameter(pId);
            if (mc.getLastMeasurement(p) == null) {
                continue;
            }
            JSONObject parJson = new JSONObject();
            parJson.put("par_type", p.getType().toString());
            Measurement m = mc.getLastMeasurement(p);
            parJson.put("last_value", m.toStringValue());
            parJson.put("last_date", m.getTime());
            if (p.getState() != null && p.getState() != Parameter.State.OK) {
                parJson.put("state", p.getState().toString());
            }

            paramsJson.put(p.getAlias() + "", parJson);
        }

        paramsJson.put("mode", BoxMode.string());
        paramsJson.put("box_time", System.currentTimeMillis());

        List<WebNotification> notifications = WebNotificationsModule.getNotifications();
        JSONObject[] notifStrings = new JSONObject[notifications.size()];
        for (int i = 0; i < notifications.size(); i++) {
            notifStrings[i] = notifications.get(i).toJSON();
        }
        JSONArray webNotificationsJson = new JSONArray(notifStrings);

        String notifPlain = webNotificationsJson.toString();

        String paramsPlain = paramsJson.toString();
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        sr.nextBytes(new byte[8]);
        sr.setSeed(1232);//add entropy
        kgen.init(128, sr);
        SecretKey skey = kgen.generateKey();
        byte[] rawKey = skey.getEncoded();
        //byte[] iv = new byte[] { 0x0, 0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7, 0x8, 0x9, 0xA, 0xB, 0xC, 0xD, 0xE, 0xF }; 
        IvParameterSpec ivSpec = new IvParameterSpec(rawKey);

        SecretKeySpec skeySpec = new SecretKeySpec(rawKey, "AES");
        Cipher cipher = Cipher.getInstance("AES/CFB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivSpec);
        byte[] parEncrypted = cipher.doFinal(paramsPlain.getBytes("UTF-8"));
        String paramData = DatatypeConverter.printHexBinary(parEncrypted);

        cipher = Cipher.getInstance("AES/CFB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivSpec);
        byte[] notifEncrypted = cipher.doFinal(notifPlain.getBytes("UTF-8"));
        String notifData = DatatypeConverter.printHexBinary(notifEncrypted);

        Signature digest = Signature.getInstance("SHA256withRSA");
        digest.initSign(BoxCommonData.PRIVATE_KEY);
        digest.update(paramData.getBytes());
        byte[] digestRaw = digest.sign();
        String digestHex = DatatypeConverter.printHexBinary(digestRaw);

        Cipher cipherRSA = Cipher.getInstance("RSA");
        // encrypt the plain text using the public key
        cipherRSA.init(Cipher.ENCRYPT_MODE, publicKey);
        String keyString = DatatypeConverter.printHexBinary(rawKey);
        byte[] keyCipherRaw = cipherRSA.doFinal(keyString.getBytes());

        String keyCipher = DatatypeConverter.printHexBinary(keyCipherRaw);
        JSONObject reply = new JSONObject();
        reply.put("notifications", notifData);
        reply.put("parameters", paramData);
        reply.put("key_cipher", keyCipher);
        reply.put("digest", digestHex);
        return reply;

    }

    public static JSONObject getEncryptedCaptureJSON(RSAPublicKey publicKey) throws Exception {
        //TODO get scaled image for mobile version
        File workdir = AppData.getCaptureWorkDir();
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        sr.nextBytes(new byte[8]);
        sr.setSeed(1232);//add entropy
        kgen.init(128, sr);
        SecretKey skey = kgen.generateKey();
        byte[] rawKey = skey.getEncoded();
        IvParameterSpec ivSpec = new IvParameterSpec(rawKey);

        SecretKeySpec skeySpec = new SecretKeySpec(rawKey, "AES");
        Cipher cipher = Cipher.getInstance("AES/CFB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivSpec);

        Cipher cipherRSA = Cipher.getInstance("RSA");
        // encrypt the plain text using the public key
        cipherRSA.init(Cipher.ENCRYPT_MODE, publicKey);
        String keyString = DatatypeConverter.printHexBinary(rawKey);
        byte[] keyCipherRaw = cipherRSA.doFinal(keyString.getBytes());
        String keyCipher = DatatypeConverter.printHexBinary(keyCipherRaw);

        Signature digest = Signature.getInstance("SHA256withRSA");
        digest.initSign(BoxCommonData.PRIVATE_KEY);

        JSONObject capture = new JSONObject();

        for (String fileName : VideoModule.IMAGE_FILES) {
            JSONObject cam = new JSONObject();
            Path path = Paths.get(workdir.getAbsolutePath(), fileName);
            byte[] image = Files.readAllBytes(path);
            byte[] encrypted = cipher.doFinal(image);
            String data = DatatypeConverter.printHexBinary(encrypted);
            digest.update(data.getBytes());
            byte[] digestRaw = digest.sign();
            String digestHex = DatatypeConverter.printHexBinary(digestRaw);

            cam.put("img_date", Files.getLastModifiedTime(path).toMillis());
            cam.put("img_cipher", data);
            cam.put("img_digest", digestHex);
            cam.put("key_cipher", keyCipher);
            capture.put(fileName, cam);
        }

        return capture;

    }

    public static JSONObject getEncryptedHistoryJSON(JSONObject json, RSAPublicKey publicKey) throws Exception {
        long from = json.getLong("from");
        long to = json.getLong("to");
        long quant = 30 * 60 * 1000;//30 mins by default
        if (json.has("quant")) {
            quant = json.getLong("quant");
        }
        JSONObject historyJson = new JSONObject();
        historyJson.put("test", "test");
        historyJson.put("from", from);
        historyJson.put("to", to);
        historyJson.put("quant", quant);

        try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {

            List<JSONObject> historyList = new LinkedList();
            for (Integer pId : AppData.parametersStorage.getParameterIds()) {
                Parameter p = AppData.parametersStorage.getParameter(pId);
                if (p.getType() == Parameter.Type.DOUBLE) {
                    //String alias = p.getAlias();
                    String sSQL = "select  unix_timestamp(sd.date) as x,floor(unix_timestamp(sd.date)/" + quant / 1000 + ") as udate, \n"
                            + " avg(sd.value_d) as value_d\n"
                            + " from sensors_data sd \n"
                            + " where sd.parameter_id=" + pId
                            + " and unix_timestamp(sd.date) > " + from / 1000 + " \n"
                            + " and unix_timestamp(sd.date) < " + to / 1000 + " \n"
                            + "  and sd.value_d is not null\n"
                            + " group by udate;";
                    List<HashMap> history = DBSelect.getRows(sSQL, conn);

                    if (history.isEmpty()) {
                        continue;
                    }
                    double calibration = Tools.parseDouble(p.getCalibration(), 1);
                    JSONObject[] points = new JSONObject[history.size()];
                    for (int i = 0; i < history.size(); i++) {
                        HashMap h = history.get(i);

                        JSONObject point = new JSONObject();
                        point.put("x", (long) Tools.parseInt(h.get("x"), 0) * 1000l);
                        point.put("y", (Double) h.get("value_d") * calibration);
                        points[i] = point;
                    }
                    JSONArray data = new JSONArray(points);

                    JSONObject parameterHistory = new JSONObject();
                    parameterHistory.put("alias", p.getAlias());
                    parameterHistory.put("description", p.getDescription());
                    parameterHistory.put("data", data);

                    historyList.add(parameterHistory);
                }
            }

            JSONObject[] histories = new JSONObject[historyList.size()];
            for (int i = 0; i < historyList.size(); i++) {
                histories[i] = historyList.get(i);
            }
            JSONArray historyListJson = new JSONArray(histories);

            historyJson.put("list", historyListJson);

        } catch (Exception e) {
            throw e;
        }
        String historyPlain = historyJson.toString();
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        sr.nextBytes(new byte[8]);
        sr.setSeed(1232);//add entropy
        kgen.init(128, sr);
        SecretKey skey = kgen.generateKey();
        byte[] rawKey = skey.getEncoded();
        //byte[] iv = new byte[] { 0x0, 0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7, 0x8, 0x9, 0xA, 0xB, 0xC, 0xD, 0xE, 0xF }; 
        IvParameterSpec ivSpec = new IvParameterSpec(rawKey);

        SecretKeySpec skeySpec = new SecretKeySpec(rawKey, "AES");
        Cipher cipher = Cipher.getInstance("AES/CFB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivSpec);

        byte[] historyEncrypted = cipher.doFinal(historyPlain.getBytes("UTF-8"));
        String historyData = DatatypeConverter.printHexBinary(historyEncrypted);

        Signature digest = Signature.getInstance("SHA256withRSA");
        digest.initSign(BoxCommonData.PRIVATE_KEY);
        digest.update(historyData.getBytes());
        byte[] digestRaw = digest.sign();
        String digestHex = DatatypeConverter.printHexBinary(digestRaw);

        Cipher cipherRSA = Cipher.getInstance("RSA");
        // encrypt the plain text using the public key
        cipherRSA.init(Cipher.ENCRYPT_MODE, publicKey);
        String keyString = DatatypeConverter.printHexBinary(rawKey);
        byte[] keyCipherRaw = cipherRSA.doFinal(keyString.getBytes());

        String keyCipher = DatatypeConverter.printHexBinary(keyCipherRaw);
        JSONObject reply = new JSONObject();

        reply.put("history", historyData);
        reply.put("key_cipher", keyCipher);
        reply.put("digest", digestHex);
        return reply;
    }

    public static JSONObject getEncryptedLogJSON(JSONObject json, RSAPublicKey publicKey) throws Exception {
        long from = json.getLong("from");
        long to = json.getLong("to");
        String moduleName = "";
        if (json.has("module_name")) {
            moduleName = json.getString("module_name");
        }
        String severity = "";
        if (json.has("severity")) {
            severity = json.getString("severity");
        }

        JSONObject logJson = new JSONObject();
        logJson.put("from", from);
        logJson.put("to", to);

        try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {
            String sSQL = "select * from logs l where 1=1 \n"
                    + " and unix_timestamp(l.dated) > " + from / 1000 + " \n"
                    + " and unix_timestamp(l.dated) < " + to / 1000 + " \n";
            if (moduleName != null && moduleName.length() > 0 && !moduleName.equals("*")) {
                sSQL += " and l.module_name = '" + moduleName + "' \n"; //;)
            }
            switch (severity) {
                case "ALARM":
                    sSQL += " and l.level in ('FATAL') \n";
                    break;
                case "ALERT":
                    sSQL += " and l.level in ('FATAL', 'ERROR') \n";
                    break;
                case "OK":
                    sSQL += " and l.level in ('FATAL', 'ERROR', 'WARNING') \n";
                    break;
                case "INFO":
                    sSQL += " and l.level in ('FATAL', 'ERROR', 'WARNING', 'INFO') \n";
                    break;
                default:
                    break;
            }
            sSQL += " order by l.dated limit 100;";
            List<HashMap> logs = DBSelect.getRows(sSQL, conn);
            JSONObject[] logRecords = new JSONObject[logs.size()];
            for (int i = 0; i < logs.size(); i++) {
                JSONObject logRecord = new JSONObject();
                HashMap h = logs.get(i);
                switch ((String) h.get("level")) {
                    case "FATAL":
                        logRecord.put("severity", "ALARM");
                        break;
                    case "ERROR":
                        logRecord.put("severity", "ALERT");
                        break;
                    case "WARNING":
                        logRecord.put("severity", "OK");
                        break;
                    default:
                        logRecord.put("severity", "INFO");
                        break;
                        
                }
                logRecord.put("id", (int) h.get("id"));

                logRecord.put("date", ((Date) h.get("dated")).getTime());
                logRecord.put("text", (String) h.get("message"));
                logRecords[i] = logRecord;
            }
            JSONArray recs = new JSONArray(logRecords);
            logJson.put("recs", recs);

        } catch (Exception e) {
            throw e;
        }
        String logPlain = logJson.toString();
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        sr.nextBytes(new byte[8]);
        sr.setSeed(1232);//add entropy
        kgen.init(128, sr);
        SecretKey skey = kgen.generateKey();
        byte[] rawKey = skey.getEncoded();
        //byte[] iv = new byte[] { 0x0, 0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7, 0x8, 0x9, 0xA, 0xB, 0xC, 0xD, 0xE, 0xF }; 
        IvParameterSpec ivSpec = new IvParameterSpec(rawKey);

        SecretKeySpec skeySpec = new SecretKeySpec(rawKey, "AES");
        Cipher cipher = Cipher.getInstance("AES/CFB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivSpec);

        byte[] logEncrypted = cipher.doFinal(logPlain.getBytes("UTF-8"));
        String logData = DatatypeConverter.printHexBinary(logEncrypted);

        Signature digest = Signature.getInstance("SHA256withRSA");
        digest.initSign(BoxCommonData.PRIVATE_KEY);
        digest.update(logData.getBytes());
        byte[] digestRaw = digest.sign();
        String digestHex = DatatypeConverter.printHexBinary(digestRaw);

        Cipher cipherRSA = Cipher.getInstance("RSA");
        // encrypt the plain text using the public key
        cipherRSA.init(Cipher.ENCRYPT_MODE, publicKey);
        String keyString = DatatypeConverter.printHexBinary(rawKey);
        byte[] keyCipherRaw = cipherRSA.doFinal(keyString.getBytes());

        String keyCipher = DatatypeConverter.printHexBinary(keyCipherRaw);
        JSONObject reply = new JSONObject();

        reply.put("log", logData);
        reply.put("key_cipher", keyCipher);
        reply.put("digest", digestHex);
        return reply;
    }
}
