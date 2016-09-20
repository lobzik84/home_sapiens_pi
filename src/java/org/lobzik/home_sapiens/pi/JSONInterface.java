/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi;

import java.security.SecureRandom;
import java.security.Signature;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import org.json.JSONObject;
import org.lobzik.home_sapiens.entity.Measurement;
import org.lobzik.home_sapiens.entity.Parameter;
import org.lobzik.home_sapiens.pi.event.Event;

/**
 * class for generating and parsing JSON to be used in TunnelClient and
 * JSONServlet
 *
 * @author lobzik
 */
public class JSONInterface {

    public static void doUserCommand(JSONObject json) throws Exception {
        String commandName = json.getString("command_name");
        HashMap commandData = new HashMap();
        JSONObject jsonData = json.getJSONObject("command_data");
        for (String key : jsonData.keySet()) {
            commandData.put(key, jsonData.get(key));
        }
        Event event = new Event(commandName, commandData, Event.Type.USER_ACTION);
        AppData.eventManager.newEvent(event);
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
            paramsJson.put(p.getAlias() + "", parJson);
        }
        paramsJson.put("test", "test ok");
        
        String plain = paramsJson.toString();
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
        byte[] encrypted = cipher.doFinal(plain.getBytes("UTF-8"));
        String data = DatatypeConverter.printHexBinary(encrypted);

        Signature digest = Signature.getInstance("SHA256withRSA");
        digest.initSign(BoxCommonData.PRIVATE_KEY);
        digest.update(data.getBytes());
        byte[] digestRaw = digest.sign();
        String digestHex = DatatypeConverter.printHexBinary(digestRaw);

        Cipher cipherRSA = Cipher.getInstance("RSA");
        // encrypt the plain text using the public key
        cipherRSA.init(Cipher.ENCRYPT_MODE, publicKey);
        String keyString = DatatypeConverter.printHexBinary(rawKey);
        byte[] keyCipherRaw = cipherRSA.doFinal(keyString.getBytes());

        String keyCipher = DatatypeConverter.printHexBinary(keyCipherRaw);
        JSONObject reply = new JSONObject();
        reply.put("parameters", data);
        reply.put("key_cipher", keyCipher);
        reply.put("digest", digestHex);
        return reply;
        //TODO encrypt on users key and sign
    }

    public static JSONObject getCaptureJSON() throws Exception {
        JSONObject reply = new JSONObject();
        //reply.put("captur", paramsJson);
        return reply;

    }
}
