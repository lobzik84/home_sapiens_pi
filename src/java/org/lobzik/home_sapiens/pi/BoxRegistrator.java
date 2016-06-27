/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import org.json.JSONObject;

/**
 *
 * @author lobzik
 */
public class BoxRegistrator {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        try {
            File boxIdFile = new File(CommonData.BOX_ID_FILE);
            if (boxIdFile.exists()) {
                System.err.println("Box registered already, " + CommonData.BOX_ID_FILE + " exists. Exiting.");
                return;
            }
            //TODO option for forced registration with rewrite

            //String hostapdConf = new String(Files.readAllBytes(Paths.get(CommonData.HOSTAPD_CONFIG_FILE)), "UTF-8");
            Properties hostapdConf = new Properties();
            hostapdConf.load(new FileInputStream(CommonData.HOSTAPD_CONFIG_FILE));
            String ssid = hostapdConf.getProperty("ssid");
            String wpa_key = hostapdConf.getProperty("wpa_passphrase");
            String publicKey = new String(Files.readAllBytes(Paths.get(CommonData.PUBLIC_KEY_FILE)), "UTF-8");
            int id = 0;

            JSONObject boxJson = new JSONObject();
            boxJson.put("ssid", ssid);
            boxJson.put("public_key", publicKey);
            boxJson.put("version", CommonData.BOX_VERSION);
            boxJson.put("wpa_psk", wpa_key);

            JSONObject reqJson = new JSONObject();
            reqJson.put("action", "register_request");
            reqJson.put("box_data", boxJson);

            URL url = new URL(CommonData.TUNNEL_SERVER_URL);

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
            if (response.has("register_result") && response.getString("register_result").equals("success")) {
                //TODO write box.id
                id = response.getInt("box_id");
                
                System.out.println("Box registered successfully");
                System.out.println("Box ID: " + id);
                System.out.println("Box SSID: " + ssid);
                System.out.println("Box WPA_PSK: " + wpa_key); //print on a sticker
                System.out.println("Box RSA public key: " + publicKey);
                System.out.println("Box registration done");
            } else {
                System.err.println("Error while registering device ");
                System.err.println(sb.toString());
            }
        } catch (Throwable e) {
            System.err.println("Error while registering device ");
            e.printStackTrace();
        }
    }

    /*private static int readIdFromFile() throws Exception {
     int box
     return 0;
     }*/
}
