/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
public class BoxRegistrationAPI {

    public static String doRegister() throws Exception {
        if (BoxCommonData.BOX_ID > 0) {
            return "Box registered already, " + BoxCommonData.BOX_ID_FILE + " exists. Exiting.";
        }
        Properties props = new Properties();
        props.load(new FileInputStream(BoxCommonData.BOX_PROPERTIES_FILE));
        String hostapdConfFileName = props.getProperty("hostapd_conf_file");
        StringBuilder output = new StringBuilder();
        Properties HAPDprops = new Properties();
        HAPDprops.load(new FileInputStream(hostapdConfFileName));


        JSONObject boxJson = new JSONObject();
        boxJson.put("ssid", HAPDprops.getProperty("ssid"));
        boxJson.put("public_key", new String(Files.readAllBytes(Paths.get(BoxCommonData.PUBLIC_KEY_FILE)), "UTF-8"));
        boxJson.put("version", BoxCommonData.BOX_VERSION);
        boxJson.put("wpa_psk", HAPDprops.getProperty("wpa_passphrase"));

        JSONObject reqJson = new JSONObject();
        reqJson.put("action", "register_request");
        reqJson.put("box_data", boxJson);

        URL url = new URL(BoxCommonData.REGISTER_SERVER_URL);

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

            int id = response.getInt("box_id");
            File boxIdFile = new File(BoxCommonData.BOX_ID_FILE);
            
            FileOutputStream fos = new FileOutputStream(boxIdFile);
            OutputStreamWriter idFileOs = new OutputStreamWriter(fos);
            idFileOs.write("box_id=" + id + "\n");
            idFileOs.flush();
            idFileOs.close();
            fos.flush();
            fos.close();
            output.append("Box registered successfully\n");
            output.append("Box ID: " + id +"\n");
            output.append("Box SSID: " + HAPDprops.getProperty("ssid") +"\n");
            output.append("Box WPA_PSK: " + HAPDprops.getProperty("wpa_passphrase") +"\n"); //print on a sticker
            output.append("Box RSA public key: " + new String(Files.readAllBytes(Paths.get(BoxCommonData.PUBLIC_KEY_FILE)), "UTF-8") +"\n");
            output.append("Box registration done\n");
        } else {
            output.append("Error while registering device \n");
            output.append(sb.toString());
        }
        return output.toString();
    }
}
