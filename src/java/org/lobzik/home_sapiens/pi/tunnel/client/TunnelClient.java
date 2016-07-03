/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi.tunnel.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import org.apache.log4j.Logger;
import org.lobzik.home_sapiens.pi.BoxCommonData;
import org.json.JSONObject;
import org.lobzik.home_sapiens.pi.AppData;
import org.lobzik.tools.Tools;

/**
 *
 * @author lobzik
 */
public class TunnelClient extends Thread {

    private static TunnelClient instance = null;

    public static final int NOT_CONNECTED_STATE = -1;
    public static final int CONNECTING_STATE = 0;
    public static final int CONNECTED_STATE = 1;

    private int state = NOT_CONNECTED_STATE;
    private static long connectTries = 0;
    private static Logger log = null;
    private static boolean run = true;
    private static String authToken = null;
    private static final JSONObject boxJson = new JSONObject();

    static {
        boxJson.put("id", BoxCommonData.BOX_ID);
        boxJson.put("public_key", BoxCommonData.PUBLIC_KEY);
        boxJson.put("version", BoxCommonData.BOX_VERSION);
        boxJson.put("ssid", BoxCommonData.SSID);
        boxJson.put("wpa_psk", BoxCommonData.WPA_PSK);
        if (AppData.settings.get("phone_num") != null) {
            boxJson.put("phone_num", (String) (AppData.settings.get("phone_num")));
        }

    }

    private TunnelClient() { //singleton
    }

    public static TunnelClient getInstance() {
        if (instance == null) {
            instance = new TunnelClient(); //lazy init
        }
        return instance;
    }

    public static void connect() {
        //TODO authenticate on server
        run = true;
        instance.start();
    }

    public synchronized void run() {
        while (run) {
            try {
                connectTries++;
                state = CONNECTING_STATE;
                if (authToken != null && authToken.length() > 0) {//token already present, try to use it

                } else {
                    authenticateWithRSA();
                }
            } catch (Exception e) {
                e.printStackTrace();
                //break;
            }
            if (connectTries > 5) {
                run = false;
                break;
            }
                
        }
    }

    public synchronized void disconnect() {
        log.info("Disconnecting...");
        run = false;
        state = NOT_CONNECTED_STATE;

        synchronized (this) {
            notifyAll();

        }
        log.info("Disconnected.");
    }

    public int getTunnelState() {
        return state;
    }

    public void setLogger(Logger logger) {
        log = logger;
    }

    private boolean authenticateWithRSA() throws Exception {
        JSONObject reqJson = new JSONObject();
        reqJson.put("action", "auth_request");
        reqJson.put("box_data", boxJson);

        URL url = new URL(BoxCommonData.TUNNEL_SERVER_URL);
        URLConnection conn = url.openConnection();
        conn.setDoInput(true);
        conn.setDoOutput(true);
        OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
        out.write(reqJson.toString());
        out.close();

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String decodedString;
        StringBuffer sb = new StringBuffer();
        while ((decodedString = in.readLine()) != null) {
            sb.append(decodedString);
        }
        in.close();

        JSONObject response = new JSONObject(sb.toString());
        if (!response.has("challenge")) {
            return false;
        }
        String challenge = response.getString("challenge");
        String challengeResponse = challenge + "RSA"; //TODO calculate on private key
        reqJson = new JSONObject();
        reqJson.put("action", "auth_challenge");
        reqJson.put("challenge_response", challengeResponse);
        reqJson.put("box_data", boxJson);
        
        url = new URL(BoxCommonData.TUNNEL_SERVER_URL);
        conn = url.openConnection();
        conn.setDoInput(true);
        conn.setDoOutput(true);
        out = new OutputStreamWriter(conn.getOutputStream());
        out.write(reqJson.toString());
        out.close();

        in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
         sb = new StringBuffer();
        while ((decodedString = in.readLine()) != null) {
            sb.append(decodedString);
        }
        in.close();

        response = new JSONObject(sb.toString());
        
        if (response.has("device_auth_token")) {
            authToken = response.getString("device_auth_token");
            return true;
        }
        else {
            return false;
        }
    }
}
